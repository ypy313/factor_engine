package com.changtian.factor.enums;

import lombok.Getter;

/**
 * 结果类型枚举
 */
@Getter
public enum ResultTypeEnum {
    NONE("NONE","无"),
    VALUE("VALUE","因子计算结果"),
    HIS("HIS","HIS"),
    DETAIL("DETAIL","DETAIL"),
    TRADE_SIGNAL("TradeSignal","TRADE_SIGNAL")
    ;
    private final String code;//值
    private final String dec;//名称

    ResultTypeEnum(String code, String dec) {
        this.code = code;
        this.dec = dec;
    }
}
