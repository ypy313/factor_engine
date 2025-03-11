package com.nbcb.factor.web.enums;

import lombok.Getter;

/**
 * 付息（重定价）频率
 */
@Getter
public enum InterestBasisEnum {
    A360("A360","A360"),
    A365F("A365F","A365F"),
    ACT("ACT","ACT"),
    ACT29("ACT29","ACT29"),
    END29("END29","END29"),
    EXC29("EXC29","EXC29"),
    M30_365("30/365","30/365"),
    COUP("COUP","COUP")
    ;
    private String key;
    private String name;
    InterestBasisEnum(String key,String name) {
        this.key = key;
        this.name = key;
    }
}
