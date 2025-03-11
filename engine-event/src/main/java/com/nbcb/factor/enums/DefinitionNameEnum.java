package com.nbcb.factor.enums;

import lombok.Getter;

/**
 * 模型名称枚举
 */
@Getter
public enum DefinitionNameEnum {
    SPREAD_ONE_POOL_STRATEGY("100001","SpreadOnePoolStrategy","债券池价差策略"),
    TA("100002","TA","TA策略"),
    RIDING_YIELD_CURVE("100003","RidngYieldCurve","骑乘策略")
    ;
    private final String id;//id
    private final String name;//模型名称
    private final String displayName;//显示名称
    DefinitionNameEnum(String id, String name, String displayName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
    }
}
