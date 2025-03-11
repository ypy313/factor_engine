package com.nbcb.factor.common;

import com.nbcb.factor.enums.BusinessEnum;
import com.nbcb.factor.enums.SystemEnum;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
    public static String redisKeyFxJoint(String dateTypeKey){
        List<String> redisKey = new ArrayList<>();
        redisKey.add(SystemEnum.FACTOR.getKey());
        redisKey.add(BusinessEnum.FX.getKey());
        redisKey.add(dateTypeKey);
        return String.join(":",redisKey);
    }

    public static String redisKeySpreadJoint(String dateTypeKey){
        List<String> redisKey = new ArrayList<>();
        redisKey.add(SystemEnum.FACTOR.getKey());
        redisKey.add(BusinessEnum.SPREAD.getKey());
        redisKey.add(dateTypeKey);
        return String.join(":",redisKey);
    }

    public static String redisKeyMarketJoint(String dateTypeKey){
        List<String> redisKey = new ArrayList<>();
        redisKey.add(SystemEnum.FACTOR.getKey());
        redisKey.add(BusinessEnum.MARKET_PROCESSING_JOB.getKey());
        redisKey.add(dateTypeKey);
        return String.join(":",redisKey);
    }

    public static String redisKeyPMJoint(String dateTypeKey){
        List<String> redisKey = new ArrayList<>();
        redisKey.add(SystemEnum.FACTOR.getKey());
        redisKey.add(BusinessEnum.PM.getKey());
        redisKey.add(dateTypeKey);
        return String.join(":",redisKey);
    }

    public static String redisKeyFxAndPmJoint(String dateTypeKey){
        List<String> redisKey = new ArrayList<>();
        redisKey.add(SystemEnum.FACTOR.getKey());
        redisKey.add(BusinessEnum.FX.getKey()+"_"+BusinessEnum.PM.getKey());
        redisKey.add(dateTypeKey);
        return String.join(":",redisKey);
    }
}
