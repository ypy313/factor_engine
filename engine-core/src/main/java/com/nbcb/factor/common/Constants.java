package com.nbcb.factor.common;

import java.time.format.DateTimeFormatter;

public class Constants {
    public static final String CURVE_CHANGE_KEY ="curveChange";//曲线变化
    public static final String CURVE_DEVIATION_VALUE_KEY="curveDeviationValue";//向上向下偏离值
    public static final String BOND_CATEGORY_KEY="bondCategory";//债券类型标识
    public final static String HTTP_SUCC = "success";//http请求成功代码

    public static final DateTimeFormatter TA_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss SSS");
    public static String HTTP_RESPONSE_STATUS = "status";
    public static String HTTP_RESPONSE_DATA = "data";
    public static final String FACTOR_MARKET_PROCESSING_CONFIG_BOND_KEY ="bond";

    public static final String BOND_PRICE_RESULT_EVENT = "BondPriceResultEvent";
    public static final String MARKET_PROCESSING_BOND_PRICE_RESULT = "Market-processing-BondPriceResult";

    public static final String OUTPUT_TO_KAFKA = "Output to kafka[";

    public static final String TA_LIST_OPEN = "list_OPEN";
    public static final String TA_LIST_HIGH = "list_HIGH";
    public static final String TA_LIST_LOW = "list_LOW";
    public static final String TA_LIST_CLOSE = "list_CLOSE";
    public static final String OPEN = "OPEN";
    public static final String HIGH = "HIGH";
    public static final String LOW = "LOW";
    public static final String CLOSE = "CLOSE";
    public static final String TICK = "TICK";

    public static final Integer MODEL_DETAIL = 0;
    public static final Integer MODEL_HISTORY = 1;
    public static final Integer MODEL_DELAY_DETAIL = 2;

    public static final String MONITOR_NORMAl_REMINDER = "normalReminder";
    public static final String MONITOR_SEND = "send";

    public static final String FACTOR_SPREAD_CBOND_VOLATILITY = "ChinaBondSpreadVolatility";
}

