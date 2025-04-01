package com.changtian.factor.enums;

import lombok.Getter;

/**
 * 指标类别
 */
@Getter
public enum IndexCategoryEnum {
    RSI("RSI"),
    IKH("ichimoku"),
    QSX("qsx"),
    POINT_QSX("QSX"),
    BBANDS("Bbands"),
    PPO("PPO"),
    OSC("OSC"),
    VI("VI"),
    DDI("DDI"),
    WR("WR"),
    REG("REG"),
    CV("CV"),
    ER("ER"),
    DMA("DMA"),
    CCI("CCI"),
    BIAS("BIAS"),
    MACD("MACD"),
    GMM("GMM");
    private String code;
    IndexCategoryEnum(String code) {
        this.code = code;
    }
}
