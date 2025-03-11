package com.nbcb.factor.web.enums;

import lombok.Getter;

/**
 * 是否调整
 */
@Getter
public enum IntRuleEnum {
    Adjusted("Y","Adjusted"),
    Unadjusted("N","Unadjusted"),
    ;
    private String key;
    private String name;

    IntRuleEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }
}
