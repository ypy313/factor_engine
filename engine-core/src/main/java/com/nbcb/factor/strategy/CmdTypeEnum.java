package com.nbcb.factor.strategy;

import lombok.Getter;

/**
 * 命令类型枚举
 */
@Getter
public enum CmdTypeEnum {
    ALL("ALL"),
    SPECIFY("SPECIFY");
    private final String type;//类型

    CmdTypeEnum(String type) {
        this.type = type;
    }
}
