package com.changtian.factor.cache;

import com.changtian.factor.utils.TaRedisUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class QsxBarManager implements Serializable {
    //计算方案相同的指标只需计算一次
    private final Map<String,Integer> explainIndexMap = new HashMap<String,Integer>();

    public void clear(){explainIndexMap.clear();}

    public void addOrUpdateQsxBarManager(String key,Integer x3BarNum){
        //判断是否存在这个key,没有查redis
        if (explainIndexMap.containsKey(key)) {
            explainIndexMap.put(key,x3BarNum);
            //存储reids
            TaRedisUtils.setQsxNum(key,x3BarNum);
        }else {
            String qsxNum = TaRedisUtils.getQsxNum(key);
            //初始化时，当只有redis中没有x3数据，
            // 或者redis中数据小于数据库中的数据则使用数据库中的数据
            // ，如果当部分更新初始化时使用redis的数据
            Integer x3Num = qsxNum ==null||x3BarNum>Integer.valueOf(qsxNum)?x3BarNum:Integer.valueOf(qsxNum);
            explainIndexMap.put(key,x3Num);
            TaRedisUtils.setQsxNum(key,x3Num);
        }
    }
}
