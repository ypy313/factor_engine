package com.nbcb.factor.web.enums;

import lombok.Getter;

@Getter
public enum BullAndSellEnum {
    BULL("P","买"),
    SELL("S","卖"),
    ;
    private String key;
    private String name;

    BullAndSellEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }
}
