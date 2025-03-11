package com.nbcb.factor.monitorfactor.impl;

import com.nbcb.factor.monitorfactor.TradeSignal;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  中债spread 实时收益率价差
 *  根据实时报价计算实时收益率价差
 */
@Slf4j
public class RealTimeSpreadFactor extends FactorTemplate {
    private static final String FACTORNAME= "RealTimeSpread";

    //leg1
    public String leg1;
    //leg2
    public String leg2;



    /**
     * monitor setting 监控设置
     * 例子格式：{"name":"RealTimeSpreadFactor","chiName":"实时价差","dataWindow":0,"yellowUpper":0,"yellowDown":0,"orangeUpper":0,"orangeDown":0,"red
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
            log.error("can't find FACTORNAME paraMap is {} ,monitorSetting is {} FACTORNAME is {} "
                    ,paraMap,monitorSetting,FACTORNAME);
            return;
        }
        monitorMap.putAll(factorMap);

    }

    /**
     *  根据实时价差进行计算，取中间价作为实时价差
     * @param context 实时价差上下文
     * @return 返回计算结果 实时价差
     */
    @Override
    public Map calc(Map context) {
        BigDecimal inputPrice =(BigDecimal) context.get("mid");
        if (inputPrice==null){
            log.info("inputPrice is null ,factor can't calc");
            return null;
        }
        return context;

    }

    /**
     * 返回信号
     * @param factorOutput
     * @return
     *   根据 factorOutput 在 monitorMap 的位置，判断是哪一档的信号。
     *                  factorOutput
     *  (>=)redDown   (>=)orangeDown    (>=)yellowDown            yellowUpper(<=)  orangeUpper(<=) redUpper(<=）
     *  reddown 为最小值，如果小于他，则信号为red，如果处于 yellowdown  与 yellowupper ，则无信号。
     */
//    @Override
//    public Map<String, Object> calcSignal(BigDecimal factorOutput) {
//        Map ret = new HashMap();
//        String triggerRuleFormat = "当前值%s,%s 档位 %s 值,触发%s信号";
//        if (factorOutput == null ){     //百分位数计算不出的情况，信号也不产生
//            ret.put("signal",TradeSignal.NOTEXISTS);
//            ret.put("triggerRule","");
//            return ret;
//        }
//        if (factorOutput.compareTo(new BigDecimal(monitorMap.get("redUpper").toString()))>=0){
//            String triggerRule= String.format(triggerRuleFormat,factorOutput,"大于",monitorMap.get("redUpper").toString(),"强烈");
//            ret.put("signal",TradeSignal.STRONG);
//            ret.put("triggerRule",triggerRule);
//            return  ret;
//        }
//
//        if (factorOutput.compareTo(new BigDecimal(monitorMap.get("redDown").toString()))<0){
//            String triggerRule= String.format(triggerRuleFormat,factorOutput,"小于",monitorMap.get("redDown").toString(),"强烈");
//            ret.put("signal",TradeSignal.STRONG);
//            ret.put("triggerRule",triggerRule);
//            return  ret;
//        }
//
//        if (factorOutput.compareTo(new BigDecimal(monitorMap.get("orangeUpper").toString()))>=0){
//            String triggerRule= String.format(triggerRuleFormat,factorOutput,"大于",monitorMap.get("orangeUpper").toString(),"推荐");
//            ret.put("signal",TradeSignal.RECOMMEND);
//            ret.put("triggerRule",triggerRule);
//            return  ret;
//        }
//
//        if (factorOutput.compareTo(new BigDecimal(monitorMap.get("orangeDown").toString()))<0){
//            String triggerRule= String.format(triggerRuleFormat,factorOutput,"小于",monitorMap.get("orangeDown").toString(),"推荐");
//            ret.put("signal",TradeSignal.RECOMMEND);
//            ret.put("triggerRule",triggerRule);
//            return  ret;
//        }
//
//        if (factorOutput.compareTo(new BigDecimal(monitorMap.get("yellowUpper").toString()))>=0){
//            String triggerRule= String.format(triggerRuleFormat,factorOutput,"大于",monitorMap.get("yellowUpper").toString(),"一般");
//            ret.put("signal",TradeSignal.NORMAL);
//            ret.put("triggerRule",triggerRule);
//            return  ret;
//        }
//
//        if (factorOutput.compareTo(new BigDecimal(monitorMap.get("yellowDown").toString()))<0){
//            String triggerRule= String.format(triggerRuleFormat,factorOutput,"小于",monitorMap.get("yellowDown").toString(),"一般");
//            ret.put("signal",TradeSignal.NORMAL);
//            ret.put("triggerRule",triggerRule);
//            return  ret;
//        }
//
//        ret.put("signal",TradeSignal.NOTEXISTS);
//        ret.put("triggerRule","");
//        return ret;
//    }

    /**
     * 债券池实时价差不产生信号
     * @param factorOutputContext
     * @return
     */
    @Override
    public Map<String, Object> calcSignal(Map factorOutputContext) {
        Map ret = new HashMap();

        ret.put("signal",TradeSignal.NOTEXISTS);
        ret.put("triggerRule","");
        return ret;

    }

    @Override
    public String getFactorName() {
        return FACTORNAME;
    }



}
