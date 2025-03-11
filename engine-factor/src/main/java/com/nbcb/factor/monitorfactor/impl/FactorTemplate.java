package com.nbcb.factor.monitorfactor.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nbcb.factor.common.CalculateUtil;
import com.nbcb.factor.common.JsonUtil;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.monitorfactor.Factor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 因子模板类
 */
@Slf4j
public abstract class FactorTemplate implements Factor {
    protected static final String MARKETDATASETTING = "marketDataSetting";
    protected static final String LEG1 = "leg1";
    protected static final String LEG2 = "leg2";
    protected static final String DATAWINDOW = "dataWindow";
    protected static final String MONITORSETTING = "spreadOnePoolMonitorSetting";

    protected static void setWebFailure(String leg1,String leg2){
        String instanceNameFormat = "%svs%s";
        String instanceName = String.format(instanceNameFormat, leg1, leg2);
        Integer count = getWebFailure(leg1,leg2);
        //之前没有访问过
        if (count==null) {
            count = Integer.valueOf("1");
        }else {
            count = count+1;
        }
        Map<String, Integer> map = new HashMap<>();
        map.put(instanceName,count);
        try {
            log.info("setWebFailure leg1 :{},leg :{},map :{}",leg1,leg2,map);
            RedisUtil.getInstance().setWebFailure(leg1,leg2, JsonUtil.toJson(map));
        }catch (JsonProcessingException e) {
            log.error("setWebFailure",e);
        }
    }

    /**
     * 获取web 失败次数
     */
    protected static Integer getWebFailure(String leg1,String leg2){
        try {
            String instanceNameFormat = "%svs%s";
            String instanceName = String.format(instanceNameFormat, leg1, leg2);
            String rest = RedisUtil.getInstance().getWebFailure(leg1,leg2);
            if (StringUtils.isNotBlank(rest) && !StringUtils.equals("-1",rest)) {
                Map failMap = JsonUtil.toObject(rest,Map.class);
                return CalculateUtil.castBigDecimal(failMap.get(instanceName)).intValue();
            }
        } catch (JsonProcessingException e) {
            log.error("getWebFailure",e);
        }
        return 0;
    }

    /**
     * 根据因子名称获取设置
     * @return
     */
    protected Map getFactorMonitorSetting(List factorList,String factorName){
        for (Object factorSettObj : factorList) {
            Map factorSettMap = (Map) factorSettObj;
            String factorNameSett = (String) factorSettMap.get("name");
            if (StringUtils.equals(factorName,factorNameSett)) {
                log.info("find factor {} monitor setting",factorName);
                return factorSettMap;
            }
        }
        log.info("can`t find factor monitor setting factorList is {},factorName is {}",factorList,factorName);
        return null;
    }
}
