package com.nbcb.factor.common;

import com.nbcb.factor.enums.DataTypeEnum;

public class AllRedisConstants {
    public static final String FACTOR_STRA_INS_CALC = StringUtil
            .redisKeySpreadJoint(DataTypeEnum.CALC.getKey());
    //中债估值百分位数 FACTOR:SPREAD:CBOND_PERCENT
    public static final String FACTOR_SPREAD_CBOND_PERCENT = StringUtil
            .redisKeySpreadJoint(DataTypeEnum.CBOND_PERCENT.getKey());
    //历史波动率和平均值 FACTOR:SPREAD:CBOND_PERCENT
    public static final String FACTOR_SPREAD_CBOND_VOLATILITY = StringUtil
            .redisKeySpreadJoint(DataTypeEnum.CBOND_VOLATILITY.getKey());
    //最优行情key FACTOR:MARKET_PROCESSING:BEST_YIELD
    public static final String FACTOR_MARKET_PROCESSING_BEST_YIELD = StringUtil
            .redisKeySpreadJoint(DataTypeEnum.BEST_YIELD.getKey());
    //行情执行配置资产数据集合 FACTOR:MARKET_PROCESSING:CONFIG_ASSET_DATA_LIST
    public static final String FACTOR_MARKET_PROCESSING_CONFIG_ASSET_DATA_LIST =
            StringUtil.redisKeySpreadJoint(DataTypeEnum.CONFIG_ASSET_DATA_LIST.getKey());
    //因子腿价格数据
    public static final String FACTOR_LEG_VALUE =
            StringUtil.redisKeyMarketJoint(DataTypeEnum.LEG_VALUE.getKey());
    //贵金属因子实例货币
    public static final String FACTOR_PM_SYMBOL =
            StringUtil.redisKeyPMJoint(DataTypeEnum.HOLIDAY.getKey());
    //因子策略计算结果
    public static final String FACTOR_SPREAD_CALC =
            StringUtil.redisKeySpreadJoint(DataTypeEnum.CALC.getKey());
    //价差配置信息
    public static final String FACTOR_CONFIG_FLINK =
            StringUtil.redisKeySpreadJoint(DataTypeEnum.SPREAD_CONFIG.getKey());
    //交易信号输出
    public static final String FACTOR_SPREAD_CBOND_TRADESIGNAL =
            StringUtil.redisKeySpreadJoint(DataTypeEnum.CBOND_TRADESIGNAL.getKey());
    //因子实例价差昨日值
    public static final String FACTOR_LASTDAY_VALUE =
            StringUtil.redisKeySpreadJoint(DataTypeEnum.LASTDAY_VALUE.getKey());

    public static final String FACTOR_SPREAD_CBOND_VALUESINGAL =
            StringUtil.redisKeySpreadJoint(DataTypeEnum.CBOND_VALUESINGAL.getKey());
    //贵金属节假日开始时间
    public static final String FACTOR_PM_HOLIDAY = StringUtil
            .redisKeyPMJoint(DataTypeEnum.HOLIDAY.getKey());

    public static final String FACTOR_PM_INDEX_CATEGORY =
            StringUtil.redisKeyPMJoint(DataTypeEnum.INDEX_CATEGORY.getKey());

    //FACTOR:RIDING:CONFIG:* 单个骑乘配置
    public static final String FACTOR_CONFIG_RIDING_SINGLE =
            StringUtil.redisKeyRidingJoint(DataTypeEnum.RIDING_CONFIG.getKey());
    //FACTOR:RIDING:CONFIG_LIST骑乘配置集合
    public static final String FACTOR_CONFIG_RIDING_LIST =
            StringUtil.redisKeyRidingJoint(DataTypeEnum.RIDING_CONFIG_LIST.getKey());

    //FACTOR:COMMON:CBOND_CURVE_CNBD 收益率曲线
    public static final String FACTOR_COMMON_CBOND_CURVE_CNBD =
            StringUtil.redisKeyCommonJoint(DataTypeEnum.CBOND_CURVE_CNBD.getKey());

    //FACTOR:COMMON:REPO_7D REPO_7D利率表
    public static final String FACTOR_COMMON_REPO_7D =
            StringUtil.redisKeyCommonJoint(DataTypeEnum.REPO_7D.getKey());
    //FACTOR:COMMON:SHIBOR_7D
    public static final String FACTOR_COMMON_SHIBOR_7D = StringUtil
            .redisKeyCommonJoint(DataTypeEnum.SHIBOR_7D.getKey());
    //FACTOR:COMMON:CBONDCF
    public static final String FACTOR_COMMON_CBONDCF = StringUtil
            .redisKeyCommonJoint(DataTypeEnum.CBONDCF.getKey());

    //FACTOR:COMMON:HOLIDAY HOLIDAY假日数据
    public static final String FACTOR_COMMON_HOLIDAY = StringUtil
            .redisKeyCommonJoint(DataTypeEnum.HOLIDAY.getKey());
//FACTOR:RIDING:FUR_RIDING_YIELD_CURVE:* FUR_RIDING_YIELD_CURVE收益率曲线chart
    public static final String FACTOR_RIDING_FUR_RIDING_YIELD_CURVE = StringUtil
            .redisKeyCommonJoint(DataTypeEnum.FUR_RIDING_YIELD_CURVE.getKey());

    //FACTOR:RIDING:RIDING_CAL_DATA_TABLE:* RIDING_CAL_DATA_TABLE 收益率表table
    public static final String FACTOR_RIDING_RIDING_CAL_DATA_TABLE = StringUtil
            .redisKeyCommonJoint(DataTypeEnum.RIDING_CAL_DATA_TABLE.getKey());

    //FACTOR:COMMON:SHIBOR_3M SHIBOR_3M利率表
    public static final String FACTOR_COMMON_SHIBOR_3M = StringUtil
            .redisKeyCommonJoint(DataTypeEnum.SHIBOR_3M.getKey());

    //FACTOR:COMMON:CBONDANALYSISCNBD 中债估值
    public static final String FACTOR_COMMON_CBONDANALYSISCNBD = StringUtil
            .redisKeyCommonJoint(DataTypeEnum.CBONDANALYSISCNBD.getKey());


}
