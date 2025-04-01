package com.changtian.factor.strategy;

import lombok.Getter;

/**
 * 曲线变化
 */
@Getter
public enum CurveChangeEnum {
    DOWN("down", "向下"),
    NOCHANGE("noChange", "不变"),
    UPPER("upper", "向上"),
    CUSTOM("custom", "自定义");

    private String value;
    private String name;

    CurveChangeEnum(String value, String name) {
        this.name = name;
        this.value = value;
    }
}
