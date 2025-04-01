package com.changtian.factor.enums;

import lombok.Getter;

@Getter
public enum DataProcessNoticeTypeEnum {
    BASIC_DATA_PROCESS("BASIC_DATA_PROCESS","基础数据处理"),
    RIDING_YIELD_JOB("RIDING_YIELD_JOB","骑乘job通知"),
    HOLIDAY_DATE("HOLIDAY_DATE","节假日日期更新通知"),
    OTHER("OTHER","其他");

    private final String key;
    private final String name;

    DataProcessNoticeTypeEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }
}
