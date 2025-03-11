package com.nbcb.factor.enums;

import lombok.Getter;

@Getter
public enum PriceTypeEnum {
    NEXTREFUND("STRIKEYIELD","行权收益率"),
    MATURITY("MATURITY","到期收益率"),
    YIELD("9","收益率"),
    CHANGE("6","利差"),
    NETPRICE("100","净价"),
    NOPRICE("104","意向"),
    FULLPRICE("103","全价"),
    UNKNOWN("8","经济商未指定")
    ;
    private String code;
    private String desc;

    PriceTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
