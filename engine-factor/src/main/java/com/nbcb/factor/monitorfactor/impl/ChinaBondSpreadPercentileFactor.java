package com.nbcb.factor.monitorfactor.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nbcb.factor.common.*;
import com.nbcb.factor.monitorfactor.TradeSignal;
import com.nbcb.factor.monitorfactor.cache.ChinaBondSpreadPercentileCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 中债收益率价差因子
 * 根据实时价差计算所在周期内的中债收益率价差的百分位数
 */
@Slf4j
public class ChinaBondSpreadPercentileFactor extends FactorTemplate{
    private static final String FACTORNAME = "ChinaBondSpreadPercentile";
    //百分位表，按分位数从小到大排序
    public Map<BigDecimal,BigDecimal> percentileMap = new TreeMap<>();
    //leg1
    public String leg1;
    //leg2
    public String leg2;
    //取数窗口
    public Long dataWindow;
    //百分位数的日期，记录百分位数的取值日期
    private String percentileMapDay;
    /**
     * monitor setting 监控设置
     * 例子格式：{"name":"chinaBondSpreadPercentile","chiName":"中债历史价差"
     * ，”dataWindow“:"0","yellowUpper":"0","yellowDown":"0"，”orangeUpper“:"0"
     * ,"orangeDown":"0","red":"0","upper":"0","redDown":0}
     */
    public Map<String,Objects> monitorMap = new HashMap<>();

    @Override
    public void init(Map paraMap) {
        Map marketDataSetting = (Map) paraMap.get(MARKETDATASETTING);
        leg1 = (String) marketDataSetting.get("leg1");
        leg2 = (String) marketDataSetting.get("leg2");

        List monitorSetting = (List) paraMap.get(MONITORSETTING);
        if (null == monitorSetting) {
            log.error("ChinaBondSpreadPercentileFactor  monitorSetting ：{} is null",MONITORSETTING);
            return;
        }
        Map factorMap = getFactorMonitorSetting(monitorSetting,FACTORNAME);
        if (factorMap == null) {
            log.error("can`t find factorName paraMap is {},monitorSetting is {} factorName is {}"
            ,paraMap,monitorSetting,FACTORNAME);
            return;
        }
        monitorMap.putAll(factorMap);
        dataWindow = (Long) factorMap.get(DATAWINDOW);

        boolean result = checkParameter();
        if (!result) {
            log.error("factor init occur error leg1{} leg2{}",leg1,leg2);
        }else {
            log.info("ChinaBondSpreadFactor factor has been" +
                    " initialized successfully leg1{} leg2{}",leg1,leg2);
        }
        //初始化中债收益率百分位表
        initPercentileMap(leg1,leg2,dataWindow);
    }

    /**
     * 根据实时价差计算出所在周期内中债收益率价差的百分位数，循环都计算一遍
     * @param context 计算输入上下文
     * @return 返回计算结果 百分位
     */
    @Override
    public Object calc(Map context) {
        Map map = new HashMap<>();
        Set<Map.Entry<String,Object>> entries = context.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            if (entry.getValue() == null) {
                log.info("inputPrice is null,factor can`t calc");
                continue;
            }
            if (entry.getValue() instanceof BigDecimal) {
                log.info("entry get key:{},entry value:{} percentile:{}",
                        entry.getKey(),entry.getValue(),getPercentile((BigDecimal)entry.getValue()));
                map.put(entry.getKey(),getPercentile((BigDecimal)entry.getValue()));
            }
        }
        return map;
    }

    /**
     * 返回信号
     * @return 根据factorOut 在monitorMap的位置，判断是那一档的信号
     * reddown 为最小值，如果小于他，则信号为red，如果处于yellowdown 与 yellowupper 则无信号
     */
    @Override
    public Map<String, Objects> calcSignal(Map factorOutputContext) {
        //取最优的最为信号触发的依据，可能出现图上已经到达信号触发，实际没有触发
        BigDecimal factorOutput = (BigDecimal)factorOutputContext.get("factor");
        String direct = (String) factorOutputContext.get("direct");
        Map ret = new HashMap<>();
        ret.put("factorNameCn","百分位");
        String triggerRuleFormat = "百分位当前值%s,%s档位值%s,触发%s信号！";
        //百分位数计算不出的情况，信号也不产生，不同向的情况下，不能触发信号
        if (factorOutput== null|| StringUtils.equals(direct,"false")) {
            ret.put("signal", TradeSignal.NOTEXISTS);
            ret.put("triggerRule","");
            return ret;
        }
        if (factorOutput.compareTo(new BigDecimal(monitorMap.get("redUpper").toString()))>=0) {
            String triggerRule = String
                    .format(triggerRuleFormat, factorOutput, "大于强烈", monitorMap.get("redUpper").toString(), "强烈");
            ret.put("signal", TradeSignal.STRONG);
            ret.put("triggerRule",triggerRule);
            return ret;
        }
        if (factorOutput.compareTo(new BigDecimal(monitorMap.get("redDown").toString()))<0) {
            String triggerRule = String
                    .format(triggerRuleFormat, factorOutput, "小于强烈", monitorMap.get("redDown").toString(), "强烈");
            ret.put("signal", TradeSignal.STRONG);
            ret.put("triggerRule",triggerRule);
            return ret;
        }
        if (factorOutput.compareTo(new BigDecimal(monitorMap.get("orangeUpper").toString()))>=0) {
            String triggerRule = String
                    .format(triggerRuleFormat, factorOutput, "大于推荐", monitorMap.get("orangeUpper").toString(), "推荐");
            ret.put("signal", TradeSignal.STRONG);
            ret.put("triggerRule",triggerRule);
            return ret;
        }
        if (factorOutput.compareTo(new BigDecimal(monitorMap.get("orangeDown").toString()))<0) {
            String triggerRule = String
                    .format(triggerRuleFormat, factorOutput, "小于推荐", monitorMap.get("orangeDown").toString(), "推荐");
            ret.put("signal", TradeSignal.STRONG);
            ret.put("triggerRule",triggerRule);
            return ret;
        }
        if (factorOutput.compareTo(new BigDecimal(monitorMap.get("yellowUp").toString()))>=0) {
            String triggerRule = String
                    .format(triggerRuleFormat, factorOutput, "大于一般", monitorMap.get("yellowDown").toString(), "一般");
            ret.put("signal", TradeSignal.STRONG);
            ret.put("triggerRule",triggerRule);
            return ret;
        }
        if (factorOutput.compareTo(new BigDecimal(monitorMap.get("yellowDown").toString()))<0) {
            String triggerRule = String
                    .format(triggerRuleFormat, factorOutput, "小于一般", monitorMap.get("yellowDown").toString(), "一般");
            ret.put("signal", TradeSignal.STRONG);
            ret.put("triggerRule",triggerRule);
            return ret;
        }
        ret.put("signal", TradeSignal.NOTEXISTS);
        ret.put("triggerRule","");
        return ret;
    }

    @Override
    public String getFactorName() {
        return FACTORNAME;
    }

    /**
     * 参数检查
     * @return 参数检查结果 false 为检查不通过 true 为检查通过
     */
    private boolean checkParameter() {
        if (StringUtils.isBlank(leg1)) {
            return false;
        }
        if (StringUtils.isBlank(leg2)) {
            return false;
        }
        if (dataWindow == null) {
            return false;
        }
        return true;
    }

    /**
     * 根据两条腿及周期，初始化中债估值百分位表
     * @param leg1  leg1
     * @param leg2  leg2
     * @param period 久期
     *               首先你判断是否已经初始化，如果没有
     *               则从redis 内存中查询是否已经存在，如果内存中没有，则调用smds web 链接获取
     *               获取后的结果，存入在本地map和redis
     *               百分位数表格式为：分位数（百分制） ：对应值
     *
     */
    public void initPercentileMap(String leg1, String leg2, Long period) {
        String today = DateUtil.getDaystr();
        RedisUtil instance = RedisUtil.getInstance();
        log.info("______RedisUtil is :{}",instance);
        ChinaBondSpreadPercentileCache chinaBondSpreadPercentileCache;
        String depth = LoadConfig.getProp().getProperty("depth");
        String chinaBondPercentile = LoadConfig.getProp().getProperty("ChinaBondPercentile");

        if (StringUtils.isBlank(percentileMapDay)
                ||!StringUtils.equals(today,percentileMapDay)
                ||percentileMap == null
                || percentileMap.isEmpty()) {
            try {
                //从redis取出缓存对象
                chinaBondSpreadPercentileCache = JsonUtil
                        .toObject(instance.getChinaBondSpreadCache(leg1,leg2,period),ChinaBondSpreadPercentileCache.class);
                //直接使用
                if (null != chinaBondSpreadPercentileCache
                        && StringUtils.equals(today,chinaBondSpreadPercentileCache.getPercentileMapDay())) {
                    percentileMap = chinaBondSpreadPercentileCache.getPercentileMap();
                    percentileMapDay = chinaBondSpreadPercentileCache.getPercentileMapDay();
                    if (percentileMap != null && percentileMapDay != null) {
                        log.info("The percentileMap has been initialized from cache leg1:{}  leg2:{}",leg1,leg2);
                        return;
                    }
                }
            } catch (JsonProcessingException e) {
                //没有取到 直接进入下一步
                log.error("exception",e);
            }
            //从web端取
            try {
                //获取失败次数
                Integer count = getWebFailure(leg1,leg2);
                String webFailure = LoadConfig.getProp().getProperty("web.failure");
                Integer webFailureCn = new Integer(webFailure);
                //已失败次数大于设定次数，则不再反问
                if (count.compareTo(webFailureCn)>0) {
                    log.error("webFailure count {} bigger than {} ",count,webFailureCn);
                    return;
                }
                String response = HttpUtils.httpGet(chinaBondPercentile+"?leg1="+leg1+"&leg2="+leg2
                        +"&days="+period+"&depth="+"depth");
                Map<String,Objects> responseData = JsonUtil.toObject(response, new TypeReference<Map<String, Objects>>() {});
                String status =responseData.get("status").toString();
                if (!StringUtils.equals(status, Constants.HTTP_SUCC)) {
                    log.error("The web server return fail:{} initializing percentileMap failed.leg1={},leg2={},days={},depth={}",
                            response,leg1,leg2,period,depth);
                    setWebFailure(leg1,leg2);
                    return;
                }
                chinaBondSpreadPercentileCache = new ChinaBondSpreadPercentileCache();
                chinaBondSpreadPercentileCache.setPercentileMapDay(today);
                chinaBondSpreadPercentileCache.setPercentileMap(JsonUtil
                        .toObject(JsonUtil.toJson(responseData.get("data")), new TypeReference<Map<BigDecimal, BigDecimal>>() {}));
                //保存到redis,初始化本地
                percentileMap = chinaBondSpreadPercentileCache.getPercentileMap();
                percentileMapDay = chinaBondSpreadPercentileCache.getPercentileMapDay();
                log.info("The percentileMap has been initialized from web.leg1 is {},leg2 is {}",leg1,leg2);
                instance.setChinaBondSpreadCache(leg1,leg2,period,JsonUtil.toJson(chinaBondSpreadPercentileCache));
            } catch (IOException e) {
                log.error("exception",e);
            }
        }
    }

    public BigDecimal getPercentile(BigDecimal calcValue){
        if (percentileMap == null || percentileMap.size() == 0) {
            log.error("the percentileMap is null ,please initialize the it firstly");
            return null;
        }
        log.info("percentileMap :{}",percentileMap);
        BigDecimal lowPercentile,lowValue,highPercentile,highValue,maxValue,minValue;
        lowPercentile = lowValue= highValue = highPercentile = new BigDecimal(0);
        maxValue = minValue = percentileMap.values().iterator().next();
        for (Map.Entry<BigDecimal, BigDecimal> entry : percentileMap.entrySet()) {
            //计算值与分位数值相等时，直接返回分位数
            if (entry.getValue().compareTo(calcValue) == 0) {
                return entry.getKey();
            }
            if (minValue.compareTo(entry.getValue())>0) {
                minValue = entry.getValue();//更新最小值
            }
            if (maxValue.compareTo(entry.getValue())<0) {
                maxValue = entry.getValue();//更新最大值
            }
            //更新区间下限
            if (entry.getValue().compareTo(calcValue)<0 && entry.getKey().compareTo(lowPercentile)>=0) {
                lowPercentile = entry.getKey();
                lowValue = entry.getValue();
            }
            //更新区间上限
            if (entry.getValue().compareTo(calcValue)>0
                    && (entry.getKey().compareTo(highPercentile)<0
                    ||highPercentile.compareTo(new BigDecimal(0))==0)) {
                highPercentile = entry.getKey();
                highValue = entry.getValue();
            }
        }
        //不在任何区间
        if (calcValue.compareTo(minValue)<0) {
            return new BigDecimal("0");
        }
        if(calcValue.compareTo(maxValue)>0){
            return new BigDecimal("100");
        }
        //返回分位数
        return lowPercentile.add(calcValue.subtract(lowValue)
                .divide(highValue.subtract(lowValue),8, RoundingMode.HALF_UP)
                .multiply(highPercentile.subtract(lowPercentile)))
                .setScale(2,RoundingMode.HALF_UP);
    }
}
