package com.changtian.factor.enums;

import lombok.Getter;

/**
 * 数据类型枚举
 */
@Getter
public enum DataTypeEnum {
    //债券类型相关
    TREASURY_BONDS("TREASURY_BONDS","100001","国债"),
    CENTRAL_BANK_BILLS("CENTRAL_BANK_BILLS","100002","央行票据"),
    POLICY_FINANCIAL_BONDS("POLICY_BONDS","100003","政策性金融债"),
    CORPORATE_BONDS("CORPORATE_BONDS","100004","企业债"),
    SUBORDINATE_BONDS("SUBORDINATED_BONDS","100005","次级债"),
    SHORT_TERM_COMMERCIAL_PARER("SHORT_TERM_BONDS","100006","短期融资券"),
    BANK_BOND("BANK_BOND","100007","商业银行普通金融债"),
    IFC_BONDS("IFC_BONDS","100008","国际开发机构债"),
    MEDIUM_TERM_NOTES("MIN","100010","中期票据"),
    LOCAL_GOVERNMENT_BONDS("LOCAL_BONDS","100011","地方政府债"),
    SSC_BONDS("SSC_BONDS","100024","证券公司短期融资券"),
    FLC_BONDS("FLC_BONDS","100026","金融租赁公司金融债"),
    GSE_BONDS("GSE_BONDS","100027","政府支持机构债券"),
    AFC_BONDS("AFC_BONDS","100028","汽车金融公司金融债"),
    CP_BONDS("CP_BONDS","100029","超短期融资债"),
    CERTIFICATE_OF_DEPOSIT("CD","100041","同业存单"),
    TARGETED_TOOLS("TARGETED_TOOLS","100050","定向工具"),
    AMC_BONDS("AMC_BONDS","100053","资产管理公司金融债"),
    TIER_TWO_CAPITAL_TOOL("TWO_CAPITAL_TOOL","100054","二级资本工具"),
    PROJECT_REVENUE_BILLS("REVENUE_BILLS","100055","项目收益票据"),
    INSURANCE_BONDS("INSURANCE_BONDS","100056","保险公司资本补充债"),
    PROJECT_REVENUE_BONDS("REVENUE_BONDS","100057","项目收益债券"),
    FOREIGN_SOVEREIGN_RMB_BONDS("FS_RMB_BONDS","100061","外国主权政府人名币债券"),
    GREEN_BONDS("GREEN_BONDS","100073","绿色债券融资工具"),
    OTHER_FINANCIAL_BONDS("OTHER_BONDS","100082","其他金融债"),
    PERPETUAL_BONDS("PERPETUAL_BONDS","100083","无固定期限资本债券"),
    SWAP_NOTES("SWAP_NOTES","100085","置换票据"),
    IRS("IRS","IRS","利率互换"),

    //外汇
    CONFIG("CONFIG","","实例配置"),
    TRADE_SIGNAL("TRADE_SIGNAL","","交易信号"),
    LAST_HIS_OHLC("LAST_HIS_OHLC","","最后一根bar线数据"),
    BADIC_DETAIL_OHLC("BADIC_DETAIL_OHLC","","OHLC实时bar线"),
    HISTORY_OHLC_QUEUE("HISTORY_OHLC_QUEUE","","历史n个ohlc队列"),
    DianShuTu_COL_NUM("DianShuTu_COL_NUM","","点数图历史列号数据"),
    QSX_COL_NUM("QSX_COL_NUM","","趋势线历史列号数据据"),
    HISTORY_BEGIN_TIME("HISTORY_BEGIN_TIME","","最新历史ohlc的开始时间"),
    PUSH_SIGNAL_INFO("PUSH_SIGNAL_INFO","","周期提醒时间记录"),

    FOREX_HISTORY_OHLC_QUEUE("FOREX_HISTORY_OHLC_QUEUE","","历史N个ohlc队列"),
    //骑乘相关数据
    RIDING_CONFIG_LIST("CONFIG_LIST","","RIDING_LIST骑乘配置集合"),
    RIDING_CONFIG("CONFIG","","RIDING骑乘债券池配置前缀"),
    CBOND_CURVE_CNBD("CBOND_CURVE_CNBD","","收益率曲线"),
    SHIBOR_7D("SHIBOR_7D","","SHIBOR_7D利率表"),
    SHIBOR_3M("SHIBOR_3M","","SHIBOR_3M利率表"),
    REPO_7D("REPO_7D","","REPO_7D利率表"),
    CBONDCF("CBONDCF","","CBONDCF现金流"),
    CBONDANALYSISCNBD("CBONDANALYSISCNBD","","CBONDANALYSISCNBD中债估值"),
    HOLIDAY("HOLIDAY","","HOLIDAY假日数据"),
    FUR_RIDING_YIELD_CURVE("FUR_RIDING_YIELD_CURVE:","","收益率曲线chart"),
    RIDING_CAL_DATA_TABLE("RIDING_CAL_DATA_TABLE:","","RIDING_CAL_DATA_TABLE收益率曲线表table"),

    //价差相关数据
    SPREAD_CONFIG("CONFIG","","价差因子配置"),
    CALC("CALC","","因子策略计算结果"),
    CBOND_PERCENT("CBOND_PERCENT","","中债估值百分位"),
    CBOND_VOLATILITY("CBOND_VOLATILITY","","历史波动率和平均值"),
    CBOND_TRADESIGNAL("CBOND_TRADESIGNAL","","价差交易信号输出"),
    CBOND_VALUESINGAL("CBOND_VALUESINGAL","","因子分析的输出值和信号"),

    CONFIG_ASSET_DATA_LIST("CONFIG_ASSET_DATA_LIST","","行情执行配置资产数据集合"),
    BEST_YIELD("BEST_YIELD","","最优行情key"),
    LEG_VALUE("LEG_VALUE","","因子腿价格数据"),
    LASTDAY_VALUE("LASTDAY_VALUE","","因子实例价差昨日值"),
    SYMBOL("SYMBOL","","贵金属资产"),
    INDEX_CATEGORY("INDEX_CATEGORY","","贵金属指标")
    ;
    private final String key;
    private final String id;
    private final String name;

    DataTypeEnum(String key, String id, String name) {
        this.key = key;
        this.id = id;
        this.name = name;
    }
}