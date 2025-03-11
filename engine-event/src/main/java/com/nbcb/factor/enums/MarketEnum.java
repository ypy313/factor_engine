package com.nbcb.factor.enums;

import lombok.Getter;

@Getter
public enum MarketEnum {
    CFETS("CFETS"),
    XSHG("XSHG"),
    XSHE("XSHE");
    private String market;
    MarketEnum(String market) {
        this.market = market;
    }
}
