package com.nbcb.factor.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.common.StringUtil;
import com.nbcb.factor.enums.DataTypeEnum;
import com.nbcb.factor.output.FxOhlcResultOutputEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * ohlc生成相关redis存取方法
 */
@Slf4j
public class OhlcRedisUtils {
    public static final String FACTOR_FX_BASIC_DETAIL_OHLC = StringUtil
            .redisKeyFxJoint(DataTypeEnum.BADIC_DETAIL_OHLC.getKey());
    public static final String FACTOR_FX_HISTORY_BEGIN_TIME = StringUtil
            .redisKeyFxJoint(DataTypeEnum.HISTORY_BEGIN_TIME.getKey());

    /**
     * 删除redis中ohlc实时bar线数据
     * @param symbol 货币对
     * @param period 周期
     * @return 是否成功标识
     */
    public static Long delOhlcOutput(String symbol,String period){
        RedisUtil redis = RedisUtil.getInstance();
        return redis.delKeyString(FACTOR_FX_HISTORY_BEGIN_TIME,symbol+"_"+period);
    }

    /**
     * 获取历史开始时间
     * @param key rediskey值
     * @param beginTime 开始时间
     */
    public static void setOhlcHistoryBeginTime(String key,String beginTime){
        RedisUtil redis = RedisUtil.getInstance();
        redis.setString(FACTOR_FX_HISTORY_BEGIN_TIME,key,beginTime);
    }

    /**
     * 获取历史开始时间
     * @param key
     * @return
     */
    public static String getOhlcHistoryBeginTime(String key){
        RedisUtil redis = RedisUtil.getInstance();
        return redis.getString(FACTOR_FX_HISTORY_BEGIN_TIME,key);
    }

    /**
     * 赋值修改ohlc实例bar线，通过该bar线判断新来的行情是否可以创建历史bar线数据与行情所属区间
     * @param symbol 货币对
     * @param period 周期
     * @param data 数据
     */
    public static void setOhlcOutPut(String symbol, String period, FxOhlcResultOutputEvent data){
        RedisUtil redis = RedisUtil.getInstance();
        redis.setString(FACTOR_FX_BASIC_DETAIL_OHLC,"symbol"+"_"+period,data.toRedisString());
    }

    /**
     *获取ohlc实时bar线，通过该bar线判断新来的行情是否可以创建历史bar线数据和行情所属区间
     * @param symbol 货币对
     * @param period 周期
     * @return  查询redisohlc数据结果
     * @throws JsonProcessingException json解析异常
     */
    public static FxOhlcResultOutputEvent getOhlcOutPut(String symbol,String period) throws JsonProcessingException {
        RedisUtil redis = RedisUtil.getInstance();
        if (redis.keyExists(FACTOR_FX_BASIC_DETAIL_OHLC,symbol+"_"+period)) {
            String json = redis.getString(FACTOR_FX_BASIC_DETAIL_OHLC, symbol + "_" + period);
            return FxOhlcResultOutputEvent.fromRedis(json);
        }
        return null;
    }


}
