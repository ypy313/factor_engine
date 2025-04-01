package com.changtian.factor.web.job.calc.impl;

import com.changtian.factor.common.AllRedisConstants;
import com.changtian.factor.common.DateUtil;
import com.changtian.factor.common.LoadConfig;
import com.changtian.factor.common.RedisUtil;
import com.changtian.factor.entity.StrategyConfigVo;
import com.changtian.factor.entity.localcurrency.LocalCurrencyMarketDataSetting;
import com.changtian.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.changtian.factor.web.entity.CurrencyValuation;
import com.changtian.factor.web.job.calc.FactorCalcJob;
import com.changtian.factor.web.mapper.CurrencyValuationMapper;
import com.changtian.factor.web.mapper.StrategyInstanceMapper;
import com.changtian.factor.web.service.StrategyInstanceService;
import com.changtian.factor.web.util.DateUtils;
import com.changtian.factor.web.util.ExceptionUtils;
import com.changtian.factor.web.util.StringUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class CalLstdayValueJob implements FactorCalcJob {
    @Autowired
    StrategyInstanceMapper strategyInstanceMapper;
    @Autowired
    public CurrencyValuationMapper currencyValuationMapper;
    @Autowired
    private StrategyInstanceService strategyInstanceService;
    private final RedisUtil redisUtil = RedisUtil.getInstance();

    @Override
    public ReturnT executeJob(String param) {
        try {
            XxlJobLogger.log("CalLstdayValueJob start");

            String allSpreadStrategy = (String) LoadConfig.getProp().get("SpreadStrategy");
            String[] spreadStrategyArr = allSpreadStrategy.split(",");
            if (spreadStrategyArr.length < 1) {
                XxlJobLogger.log("configuration error not found spreadStrategy");
                return ReturnT.FAIL;
            }
            //清楚redis中价差昨日值
            redisUtil.delString(AllRedisConstants.FACTOR_LASTDAY_VALUE);
            for (int i = 0; i < spreadStrategyArr.length; i++) {
                getLastdayValue(spreadStrategyArr[i]);
            }
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("跑批失败，原因为：{}", ExceptionUtils.toString(e));
            return ReturnT.FAIL;
        }
    }

    private void getLastdayValue(String strategyName) {
        StrategyConfigVo strategyConfigVo = new StrategyConfigVo();
        strategyConfigVo.setStrategyName(strategyName);
        List<LocalCurrencyStrategyInstanceResult> configjson = strategyInstanceService.getConfigJson(strategyConfigVo);
        for (LocalCurrencyStrategyInstanceResult localCurrencyStrategyInstanceResponse : configjson) {
            LocalCurrencyMarketDataSetting marketDataSetting = localCurrencyStrategyInstanceResponse.getMarketDataSetting();
            String leg1 = marketDataSetting.getLeg1();
            String leg2 = marketDataSetting.getLeg2();
            String keyInstanceId = localCurrencyStrategyInstanceResponse.getInstanceId();
            String spread = calSpread(leg1, leg2);
            if (spread != null) {
                redisUtil.setLastdayValue(keyInstanceId, spread);
                XxlJobLogger.log("昨日值存入redis。keyInstanceId：" + keyInstanceId + "leg1:" + leg1 + "leg2:" + leg2 + "LastDaySpread:" + spread);
            }
        }
    }

    private String calSpread(String leg1, String leg2) {
        XxlJobLogger.log("从数据库中查询,因子id获取昨收价差开始：" + DateUtils.getSendingTime());

        int days = 10;
        Map<String, List<CurrencyValuation>> cnbdMapList = getCnbdMapList(leg1, leg2, days);
        //从上一个有数据的收盘价
        String close = null;

        if (cnbdMapList != null && !cnbdMapList.isEmpty()) {
            //查询leg1的价格
            List<CurrencyValuation> leg1List = cnbdMapList.get("leg1List");
            //查询leg2的价格
            List<CurrencyValuation> leg2List = cnbdMapList.get("leg2List");

            //leg1-leg2 key 日期 value 每日价差
            Map<String, Object> minusMapList = processData(leg1List, leg2List, days);
            //计算leg1-leg2
            TreeMap<String, BigDecimal> spreadTMap = (TreeMap<String, BigDecimal>) minusMapList.get("minusTMap");
            if (spreadTMap.isEmpty()) {
                XxlJobLogger.log("因子组合价差数组为空，leg1:{},leg2:{},datawindow:{}", leg1, leg2, days);
                return null;
            }
            close = getYesterdayValue(spreadTMap);//昨日值
            XxlJobLogger.log("从数据库中查询,因子id获取昨收价差结束：" + DateUtils.getSendingTime());
        }
        return close;
    }

    /**
     * 查询leg1 和 leg2的价格
     *
     * @param leg1
     * @param leg2
     * @param days
     * @return
     */
    private Map<String, List<CurrencyValuation>> getCnbdMapList(String leg1, String leg2, int days) {
        //开始日期
        String startDate = DateUtil.parseDateToStr(DateUtil.DAYSTR, DateUtil.calcDaysBefore(days));
        //结束日期
        String endDate = DateUtil.parseDateToStr(DateUtil.DAYSTR, DateUtil.calcDaysBefore(1));

        //sql查询,返回的数据
        List<CurrencyValuation> list = currencyValuationMapper.selectCurrencyValuationList(startDate, endDate, leg1, leg2);
        if (list.isEmpty()) {
            XxlJobLogger.log("中债登估值查询数据为空,leg1:{},leg2:{}", leg1, leg2);
            return null;
        }
        List<CurrencyValuation> leg1List = new ArrayList<>(100);
        List<CurrencyValuation> leg2List = new ArrayList<>(100);
        list.forEach(item -> {
            if (leg1.equals(item.getSecurityId())) {
                leg1List.add(item);
            } else {
                leg2List.add(item);
            }
        });

        Map<String, List<CurrencyValuation>> map = new HashMap<>();
        map.put("leg1List", leg1List);
        map.put("leg2List", leg2List);

        return map;
    }

    /**
     * 过滤值为0和日期不匹配的项
     */
    private Map<String, Object> processData(List<CurrencyValuation> legList1, List<CurrencyValuation> legList2, int days) {
        //key日期  value收益率  按日期从小到大排序
        TreeMap<String, List<BigDecimal>> dayMap = new TreeMap<>();
        //把需要查询的所有日期设为key
        for (int i = 1; i < days; i++) {
            String date = DateUtil.format(DateUtil.calcDaysBefore(i), DateUtil.DAYSTR);
            dayMap.put(date, new ArrayList<>(2));
        }
        //leg1数据 根据日期把收益率放入list
        for (CurrencyValuation currencyValuation : legList1) {
            String date = currencyValuation.getTradeDt();
            List list = dayMap.get(date);
            dayMap.get(date).add(currencyValuation.getRateValue());
        }
        //leg2数据 根据日期把收益率放入list
        for (CurrencyValuation currencyValuation : legList2) {
            String date = currencyValuation.getTradeDt();
            dayMap.get(date).add(currencyValuation.getRateValue());
        }

        //计算leg1-leg2
        final TreeMap<String, BigDecimal> minusTMap = new TreeMap<>();
        //计算leg1-leg2
        List<BigDecimal> minusList = new ArrayList<>(200);
        //x轴日期list
        List<String> daysList = new ArrayList<>(200);

        BigDecimal zero = BigDecimal.valueOf(0);
        // 循环判断对应日期是否有2个对应的值，并且过滤值为0的项
        dayMap.forEach((key, value) -> {
            if (value.size() == 2) {
                if (value.get(0).compareTo(zero) > 0 && value.get(1).compareTo(zero) > 0) {
                    BigDecimal minus = value.get(0).subtract(value.get(1));
                    minusTMap.put(key, minus);
                    minusList.add(minus);
                    daysList.add(key);
                }
            }
        });

        Map<String, Object> map = new HashMap<>();
        map.put("minusTMap", minusTMap);
        map.put("minusList", minusList);
        map.put("daysList", daysList);

        return map;
    }

    private String getYesterdayValue(TreeMap<String, BigDecimal> minusTMap) {
        ListIterator<Map.Entry<String, BigDecimal>> li = new ArrayList<>(minusTMap.entrySet()).listIterator(minusTMap.size());
        //倒叙取数
        String yesterdayValue = null;
        while (li.hasPrevious()) {
            Map.Entry<String, BigDecimal> entry = li.previous();
            if (StringUtils.equals(entry.getKey(), DateUtil.getDaystr())) {
                continue;
            } else {
                //昨日值保留思维，四舍五入
                yesterdayValue = entry.getValue() == null ? null : entry.getValue().setScale(4, RoundingMode.HALF_UP).toString();
                break;
            }
        }
        return yesterdayValue;
    }
}
