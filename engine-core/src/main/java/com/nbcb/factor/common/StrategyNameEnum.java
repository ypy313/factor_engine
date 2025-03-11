package com.nbcb.factor.common;

public enum StrategyNameEnum {
    SPREAD_ONE_POOL_STRATEGY("SpreadOnePoolStrategy","价差债券池策略"),
    TA("TA","TA策略"),
    PM("PM","贵金属策略")
    ;
    private final String name;//名称
    private final String desc;//描述

    StrategyNameEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
