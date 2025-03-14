package com.nbcb.factor.common;

import java.time.format.DateTimeFormatter;

public class Constants {
    public static final String CURVE_CHANGE_KEY ="curveChange";//曲线变化
    public static final String CURVE_DEVIATION_VALUE_KEY="curveDeviationValue";//向上向下偏离值
    public static final String BOND_CATEGORY_KEY="bondCategory";//债券类型标识
    public final static String HTTP_SUCC = "success";//http请求成功代码

    public static final DateTimeFormatter TA_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss SSS");
    public static final String TA = "TA";
    public static final String PRO_TRA_INDEX_SETTING = "indexSettings";
    public static final String PARAMETER_INDEX_NAME = "indexName";
    public static final String PARAMETER_MIN_PREFERENCES ="minPreferences" ;
    public static final String PARAMETER_MAX_PREFERENCES ="maxPreferences" ;
    public static final String PARAMETER_INDEX_PREFERENCES ="indexPreferences" ;
    public static final String PARAMETER_MIN_FORMULA ="formula" ;
    public static final String PRO_TRA_MONITOR_SETTING = "monitorExpression";
    public static final String PRO_TRA_GLOBAL_SETTING = "globalParameter";
    public static final String PRO_TRA_DATA_SOURCE_CONFIG = "dataSourceConfig";
    public static final String MONITOR_ID = "monitorId";
    public static final String MONITOR_EXPRESSION = "expression";
    public static final String MONITOR_REMINDER_INTERVAL = "reminderInterval";
    public static final String GLOBAL_PARAMETER_PARAMETER_NAME = "parameterName";
    public static final String GLOBAL_PARAMETER_PARAMETER_VALUE = "parameterValue";
    public static final String GLOBAL_PARAMETER_XPOINT1 = "xpoint1";
    public static final String GLOBAL_PARAMETER_YPOINT1 = "ypoint1";
    public static final String GLOBAL_PARAMETER_XPOINT2 = "xpoint2";
    public static final String GLOBAL_PARAMETER_YPOINT2 = "ypoint2";
    public static final String GLOBAL_PARAMETER_XCELL1 = "xcell1";
    public static final String GLOBAL_PARAMETER_XCELL2 = "xcell2";
    public static final String GLOBAL_PARAMETER_LATEST_XPOINT = "latestXPoint";
    public static final String GLOBAL_PARAMETER_LATEST_XCELL = "latestXCell";
    public static final String PRO_TRA_OHLC = "ohlc";
    public static final String PRO_TRA_MARKET_DATA_SETTING = "marketDataSetting";
    public static final String PRO_TRA_PERIODS_SETTING = "periodsSetting";
    public static final String TA_INDEX_CATEGORY_QSX = "qsx";
    public static final String BOND_PRICE = "BondPrice";
    public static final String PM = "PM";
    public static final String RIDING_YIELD_CURVE = "RidingYieldCurve";
    public static final String SPREAD_ONE_POOL_STRATEGY = "SpreadOnePoolStrategy";
    public static final String PRO_TRA_LEG1 = "leg1";
    public static final String PRO_TRA_LEG2 = "leg2";
    public static final String PRO_TRA_DATA_SOURCE = "dataSource";
    public static final String PRO_TRA_PRELOAD = "preLoad";
    public static final String PRO_TRA_MONITOR_EXPRESSION = "monitorExpression";
    public static final String MONITOR_NAME = "monitorName";
    public static final String MONITOR_UPPER = "upper";
    public static final String MONITOR_DOWN = "down";
    public static final String MONITOR_SIGNAL = "signal";
    public static final String MONITOR_NORMAL = "NORMAL";
    public static final String MONITOR_RECOMMEND = "RECOMMEND";
    public static final String MONITOR_STRONG = "STRONG";
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
    public static final String HTTP_FAIL = "error";
    //利率类型
    public static final String RIDING_INTEREST_RATE_TYPE_LATEST_VALUE ="latestValue";
    public static final String RIDING_INTEREST_RATE_TYPE_PAST_NEW_VALUE ="pastNewValue";
    public static final String RIDING_INTEREST_RATE_TYPE_CUSTOM ="custom";
    //持有期
    public static final String RIDING_SHOW_HOLD_PERIOD_1M="1M";
    public static final String RIDING_SHOW_HOLD_PERIOD_3M="3M";
    public static final String RIDING_SHOW_HOLD_PERIOD_6M="6M";
    public static final String RIDING_SHOW_HOLD_PERIOD_9M="9M";
    public static final String RIDING_SHOW_HOLD_PERIOD_1Y="1Y";
    public static final String RIDING_SHOW_HOLD_PERIOD_CUSTOM="custom";

    public static final String FR007_IR = "FR007.IR";
    public static final String SHIBOR3M_IR = "SHIBOR3M.IR";
    public static final String SHIBOR1W_IR = "SHIBOR1M.IR";


}

