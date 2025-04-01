package com.changtian.factor.enums;

import lombok.Getter;

/**
 * 系统模块枚举
 */
@Getter
public enum SystemEnum {
    FACTOR("FACTOR","因子分析系统"),
    STC("STC","债券做市系统")
    ;

    private final String key;
    private final  String name;

    SystemEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }
}