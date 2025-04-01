package com.changtian.factor.utils;

import com.changtian.factor.common.RedisUtil;
import com.changtian.factor.common.StringUtil;
import com.changtian.factor.enums.DataTypeEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaRedisUtils {
    public static final String FACTOR_FX_QSX_COL_NUM = StringUtil
            .redisKeyFxJoint(DataTypeEnum.QSX_COL_NUM.getKey());
    /**
     * 设置外汇趋势线列表数据
     */
    public static void setQsxNum(String key,Integer num){
        RedisUtil redis = RedisUtil.getInstance();
        redis.setString(FACTOR_FX_QSX_COL_NUM,key,num.toString());
    }

    /**
     * 获取外汇趋势线列表数据
     * @param key
     * @return
     */
    public static String getQsxNum(String key){
        RedisUtil redis = RedisUtil.getInstance();
        return redis.getString(FACTOR_FX_QSX_COL_NUM,key);
    }
}
