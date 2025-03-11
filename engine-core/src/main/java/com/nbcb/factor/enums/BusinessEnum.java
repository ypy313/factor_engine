package com.nbcb.factor.enums;

import lombok.Getter;

@Getter
public enum BusinessEnum {
    CURRENT_PRICE("CURRENT_PRICE","现价排序"),
    COMMON("COMMON","通用"),
    MARKET_PROCESSING_JOB("MARKET_PROCESSING_JOB","行情融合job"),
    FX("FX","外汇"),
    RIDING("RIDING","骑乘"),
    SPREAD("SPREAD","价差"),
    PM("PM","贵金属"),

    ;
    private final String key;
    private final String name;
    BusinessEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }
}
