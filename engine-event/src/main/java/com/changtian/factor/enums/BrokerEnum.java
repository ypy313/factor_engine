package com.changtian.factor.enums;

import lombok.Getter;

@Getter
public enum BrokerEnum {
    CNEX("CNEX"),
    PATR("PATR"),
    BGC("BGC"),
    TP("TP"),
    MQM("MQM"),
    UEDA("UEDA")
    ;
    private String broker;

    BrokerEnum(String broker) {
        this.broker = broker;
    }
}
