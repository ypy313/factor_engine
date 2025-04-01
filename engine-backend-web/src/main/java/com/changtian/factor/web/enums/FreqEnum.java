package com.changtian.factor.web.enums;

import lombok.Getter;

/**
 * 付息（重定价）频率
 */
@Getter
public enum FreqEnum {
    Annal("A", "Annal"),
    Quarterly("Q", "Quarterly"),
    Monthly("M", "Monthly"),
    Weekly("W", "Weekly"),
    Daily("D", "Daily"),
    Zero_Coupon("Z","Zero Coupon"),
    Semi_Annual("S","Semi Annual");
    ;
    private String key;
    private String name;

    FreqEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }
}
