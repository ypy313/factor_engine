package com.changtian.factor.monitorfactor.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.changtian.factor.common.*;
import com.changtian.factor.monitorfactor.TradeSignal;
import com.changtian.factor.monitorfactor.cache.ChinaBondSpreadVolatilityCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  中债spread收益率波动率因子
 *  根据实时价差计算所在周期内的中债收益率价差的波动率
 */
public class ChinaBondSpreadVolatilityFactor extends FactorTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String FACTORNAME= "ChinaBondSpreadVolatility";
    //历史窗口波动率
    public BigDecimal historyVolatility = BigDecimal.ZERO;
    // 历史窗口平均数
    private BigDecimal historyMean = BigDecimal.ZERO;
    //leg1
    public String leg1;
    //leg2
    public String leg2;
    // 取数窗口
    public Long dataWindow;

    // 百分位数的日期，记录百分位数的取数日期
    private String historyVolatilityDay ;

    /**
     * monitor setting 监控设置
     * 例子格式：{"name":"ChinaBondSpreadVolatility","chiName":"中债历史价差","dataWindow":0,"yellowUpper":0,"yellowDown":0,"orangeUpper":0,"orangeDown":0,"red
     * Upper":0,"redDown":0}
     */
    public Map<String,Object> monitorMap = new HashMap<>();

    @Override
    public void init(Map paraMap) {
        Map marketDataSetting = (Map) paraMap.get(MARKETDATASETTING);
        leg1 =(String) marketDataSetting.get(LEG1);
        leg2 = (String) marketDataSetting.get(LEG2);


        List monitorSetting = (List) paraMap.get(MONITORSETTING);
        Map factorMap = getFactorMonitorSetting(monitorSetting,FACTORNAME);
        if (factorMap == null){
            LOGGER.error("can't find FACTORNAME paraMap is {} ,monitorSetting is {} FACTORNAME is {} "
                    ,paraMap,monitorSetting,FACTORNAME);
            return;
        }
        monitorMap.putAll(factorMap);

        dataWindow =(Long) factorMap.get(DATAWINDOW);


        boolean result =  checkParameter();
        if (!result){
            LOGGER.error("factor init occur error");
        }else {
            LOGGER.info("ChinaBondSpreadFactor factor init succeed");
        }

        //初始化中债波动率
        initHistoryVolatility(leg1,leg2, dataWindow);
    }

    /**
     *  根据实时价差计算出所在周期内中债收益率价差的波动率倍数
     * @param context 实时价差上下文
     * @return 返回计算结果 波动率倍数
     */
    @Override
    public Map calc(Map context) {
        Map map = new HashMap();
        Set<Map.Entry<String, Object>> entries = context.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            if (entry.getValue()==null){
                LOGGER.info("inputPrice is null ,factor can't calc");
                continue;
            }
            if (entry.getValue() instanceof  BigDecimal){
                LOGGER.info("entry get key {}, entry value {} Volatility {} " +
                                "historyMean {} historyVolatility {} leg1 {} leg2 {}  ",
                        entry.getKey(),entry.getValue(),getVolatility((BigDecimal) entry.getValue())
                        ,historyMean,historyVolatility,leg1,leg2);
                map.put(entry.getKey(),getVolatility((BigDecimal) entry.getValue()));
            }

        }
        return map;


    }

    /**
     * 获得波动率
     * @param inputPrice
     * @return
     */
    private BigDecimal getVolatility(BigDecimal inputPrice) {
        boolean result =  checkParameter();
        if (inputPrice==null){
            LOGGER.info("inputPrice is null ,factor can't calc");
            return null;
        }

        if (historyVolatility.compareTo( BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }else
        {
            //当前价差 减去历史平均价差 除以历史波动率的倍数。 2 位小数，四舍五入
            return new BigDecimal(Math.abs(inputPrice.subtract(historyMean).doubleValue()))
                    .divide(historyVolatility,2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 返回信号
     * @param factorOutputContext
     * @return
     *   根据 factorOutput 在 monitorMap 的位置，判断是哪一档的信号。
     *                  factorOutput
     *  (>=)redDown   (>=)orangeDown    (>=)yellowDown            yellowUpper(<=)  orangeUpper(<=) redUpper(<=）
     *  reddown 为最小值，如果小于他，则信号为red，如果处于 yellowdown  与 yellowupper ，则无信号。
     */
    @Override
    public Map<String, Object> calcSignal(Map factorOutputContext) {
        BigDecimal factorOutput =(BigDecimal) factorOutputContext.get("best");
        String direct =(String) factorOutputContext.get("direct");//方向
        Map ret = new HashMap();
        ret.put("factorNameCn","波动率");
        String triggerRuleFormat = "波动率当前值%s,%s档位值%s,触发%s信号";
        if (factorOutput.compareTo(BigDecimal.ZERO) == 0
                || null == factorOutput
                || StringUtils.equals(direct,"false")){
            ret.put("signal",TradeSignal.NOTEXISTS);
            ret.put("triggerRule","");
            return ret;
        }
        if(monitorMap.get("redUpper") != null && factorOutput.compareTo(new BigDecimal(monitorMap.get("redUpper").toString()))>=0 )
        {
            String triggerRule= String.format(triggerRuleFormat,factorOutput,"大于强烈",monitorMap.get("redUpper").toString(),"强烈");
            ret.put("signal",TradeSignal.STRONG);
            ret.put("triggerRule",triggerRule);
            return  ret;
        }

        if(monitorMap.get("orangeUpper") != null && factorOutput.compareTo(new BigDecimal(monitorMap.get("orangeUpper").toString()))>=0 )
        {
            String triggerRule= String.format(triggerRuleFormat,factorOutput,"大于推荐",monitorMap.get("orangeUpper").toString(),"推荐");
            ret.put("signal",TradeSignal.RECOMMEND);
            ret.put("triggerRule",triggerRule);
            return  ret;
        }
        if(monitorMap.get("yellowUpper") != null && factorOutput.compareTo(new BigDecimal(monitorMap.get("yellowUpper").toString()))>=0 )
        {
            String triggerRule= String.format(triggerRuleFormat,factorOutput,"大于一般",monitorMap.get("yellowUpper").toString(),"一般");
            ret.put("signal",TradeSignal.NORMAL);
            ret.put("triggerRule",triggerRule);
            return  ret;
        }

        ret.put("signal",TradeSignal.NOTEXISTS);
        ret.put("triggerRule","");
        return ret;
    }

    @Override
    public String getFactorName() {
        return FACTORNAME;
    }

    /**
     * 参数检查
     * @return 参数检查结果，false 为检查不通过， true 为检查通过
     */
    private boolean checkParameter(){
        if (StringUtils.isBlank(leg1) ) return false;

        if (StringUtils.isBlank(leg2) ) return false;
        if (dataWindow == null){
            return  false ;
        }
        return true;

    }

    /**
     * 根据两条腿及周期，初始化
     * @param leg1 leg1
     * @param leg2 leg2
     *  首先判断是否已经初始化，如果没有，
     *  则从redis 内存中查询是否已经存在，如果内存中没有,取redis，则调用smds web 链接获取，存入redis
     *  获取后的结果，存入在本地map和redis
     *         标准差和平均值
     */
    public void initHistoryVolatility(String leg1,String leg2,Long period){
        // leg1 ，leg2 ，period 为redis的key值
        String today =DateUtil.getDaystr();
        RedisUtil instance=RedisUtil.getInstance();
        ChinaBondSpreadVolatilityCache chinaBondSpreadVolatilityCache;
        String depth= LoadConfig.getProp().getProperty("depth");
        String ChinaBondVolatility= LoadConfig.getProp().getProperty("ChinaBondVolatility");
        if (StringUtils.isBlank(historyVolatilityDay)||!StringUtils.equals(today,historyVolatilityDay)) {
            try {
                //从redis取出缓存对象
                chinaBondSpreadVolatilityCache = JsonUtil.toObject(instance.getChinaBondVolatilityCache(leg1, leg2, period), ChinaBondSpreadVolatilityCache.class);
                //直接使用
                if (null != chinaBondSpreadVolatilityCache && StringUtils.equals(today, chinaBondSpreadVolatilityCache.getVolatilityDay())) {
                    historyVolatility = chinaBondSpreadVolatilityCache.getHistoryVolatility();
                    historyVolatilityDay = chinaBondSpreadVolatilityCache.getVolatilityDay();
                    historyMean=chinaBondSpreadVolatilityCache.getHistoryMean();

                    if (historyVolatility != null
                            && historyVolatilityDay != null
                            &&  historyMean != null ){
                        LOGGER.info("The historyVolatility has been initialized from  cache.");
                        return;
                    }

                }
            } catch (Exception e) {
                //没有取到，直接进入下一步
                LOGGER.error("exception",e);
            }
            //从web端取
            try {

                //获取失败次数
                Integer count = getWebFailure(leg1,leg2);
                String webFailure= LoadConfig.getProp().getProperty("web.failure");
                Integer webFailureCn = new Integer(webFailure);
                // 已失败次数大于设定次数，则不再访问
                if (count.compareTo(webFailureCn) > 0){
                    LOGGER.error("webFailure count {} bigger than {}",count,webFailureCn);
                    return;
                }

                String response = HttpUtils.httpGet(ChinaBondVolatility+"?leg1=" + leg1 + "&leg2=" + leg2
                        + "&days=" + period );

                Map<String,Object> responseData= JsonUtil.toObject(response, new TypeReference<Map<String,Object>>() {});
                //如果web 没有返回成功
                if(!StringUtils.equals((String)responseData.get("status"),Constants.HTTP_SUCC)) {
                    LOGGER.error("The web server return fail:{}, leg1 is {} ,leg2 is {}" , response,leg1,leg2);
                    setWebFailure(leg1,leg2);
                    return;
                }
                chinaBondSpreadVolatilityCache=new ChinaBondSpreadVolatilityCache();
                chinaBondSpreadVolatilityCache.setVolatilityDay(today);
                chinaBondSpreadVolatilityCache.setHistoryVolatility(JsonUtil.toObject(JsonUtil.toJson(responseData.get("data")), new TypeReference<Map<String, BigDecimal>>() {
                }).get("std"));
                chinaBondSpreadVolatilityCache.setHistoryMean(JsonUtil.toObject(JsonUtil.toJson(responseData.get("data")), new TypeReference<Map<String, BigDecimal>>() {
                }).get("average"));
                //保存到redis，初始化本地
                historyVolatility = chinaBondSpreadVolatilityCache.getHistoryVolatility();
                historyVolatilityDay = chinaBondSpreadVolatilityCache.getVolatilityDay();
                historyMean=chinaBondSpreadVolatilityCache.getHistoryMean();
                LOGGER.info("The historyVolatility has been initialized from web. leg1 is {} ,leg2 is {}",leg1,leg2);
                instance.setChinaBondVolatilityCache(leg1,leg2,period,JsonUtil.toJson(chinaBondSpreadVolatilityCache));
            }catch (Exception e)
            {
                LOGGER.error("exception",e);
            }

        }

    }

}
