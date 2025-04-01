package com.changtian.factor.web.kafka.common;

public class KafkaTopicConstants {
    private KafkaTopicConstants() {

    }
    //命令类
    public static final String FACTOR_ANALYZE_STRATEGY_CMD = "FACTOR_ANALYZE_STRATEGY_CMD";
    //贵金属行情
    public static final String FACTOR_PRECIOUS_METAL_MARKET_DATA = "FACTOR_PRECIOUS_METAL_MARKET_DATA";
    //短信号topic
    public static final String FACTOR_TEXT_MESSAGE = "FACTOR_TEXT_MESSAGE";
    //因子系统ohlc等相关信息
    public static final String TOPIC_FACTOR_FX_DETAIL_OHLC= "FACTOR_FX_DETAIL_OHLC";
    //贵金属ohlc等相关信息
    public static final String TOPIC_FACTOR_PRECIOUS_METAL_DETAIL_OHLC = "FACTOR_PRECIOUS_METAL_DETAIL_OHLC";
    //指标计算成交
    public static final String TOPIC_FACTOR_CAL_RESULT= "FACTOR_CAL_RESULT";

}
