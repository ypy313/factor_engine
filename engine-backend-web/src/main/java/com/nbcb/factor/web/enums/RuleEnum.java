package com.nbcb.factor.web.enums;

import lombok.Getter;

/**
 * 计算规则
 */
@Getter
public enum RuleEnum {
    FOLLOWING("F","following"),
    PRECEDING("P","preceding"),
    MODIFIED_FOLLOWING("MF","modified following"),
    MODIFIED_PREVIOUS("MP","modified previous"),
    EURODOLLAR_CONVENTION("E","eurodollar convention"),
    SOUTH_AFRICAN("SA","south african")
    ;
    private String key;
    private String name;

    RuleEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }
}
