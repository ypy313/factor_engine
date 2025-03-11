package com.nbcb.factor.enums;

import lombok.Getter;

/**
 * 事件类型枚举
 */
@Getter
public enum EventTypeEnum {
    NONE("NONE","无"),
    OHLC("OHLC","OHLC类型"),
    RSI("RSI","RSI类型"),
    MA("MA","MA类型"),
    SIGNAL("SIGNAL","信号类型")
    ;
    private final String code;//值
    private final String dec;//名称
    private EventTypeEnum(String code, String dec) {
        this.code = code;
        this.dec = dec;
    }
}
