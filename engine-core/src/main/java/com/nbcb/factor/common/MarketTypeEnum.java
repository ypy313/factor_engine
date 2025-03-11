package com.nbcb.factor.common;

/**
 * 上清所 金交所 行情平台类型
 */
public enum MarketTypeEnum {
    SGE("@SGE","金交所"),
    SHFE("@SHFE","上清所"),
    FXALL("@FXALL","行情平台数据")
    ;
    private final String endName;//结尾名称
    private final String desc;//描述
    MarketTypeEnum(String endName,String desc){
        this.endName = endName;
        this.desc = desc;
    }

    public String getEndName() {
        return endName;
    }

    public String getDesc() {
        return desc;
    }
}
