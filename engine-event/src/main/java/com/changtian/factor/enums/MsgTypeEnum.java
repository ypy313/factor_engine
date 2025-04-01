package com.changtian.factor.enums;

import lombok.Getter;

@Getter
public enum MsgTypeEnum {
    ESP("ESP"),
    XBOND("XBOND"),
    XSWAP("XSWAP"),
    BROKER("BROKER"),
    CMDM("CMDM"),
    CMDS("CMDS")
    ;
    private String msgType;
    MsgTypeEnum(String msgType) {
        this.msgType = msgType;
    }
}
