package com.changtian.factor.enums;

import lombok.Getter;

import java.time.temporal.ChronoUnit;

/**
 * 周期类型枚举
 */
@Getter
public enum PeriodEnum {
    NONE("NONE","无",0,null),
    ONE_MOUTH("1Month","1月",1,ChronoUnit.MONTHS),
    ONE_WEEK("1Week","1周",1,ChronoUnit.WEEKS),
    ONE_DAY("1D","1天",1,ChronoUnit.DAYS),
    ONE_HOUR("1H","1小时",1,ChronoUnit.HOURS),
    FOUR_HOUR("4H","4小时",4,ChronoUnit.HOURS),
    THIRTY_MINUTES("30Min","30分钟",30,ChronoUnit.MINUTES),
    FIFTEEN_MINUTES("15Min","15分钟",15,ChronoUnit.MINUTES),
    FIVE_MINUTES("5Min","5分钟",5,ChronoUnit.MINUTES),
    ONE_MINUTES("1Min","1分钟",5,ChronoUnit.MINUTES),
    FIVE_SECONDS("TICK","5秒",5,ChronoUnit.SECONDS),
    ;

    private final String code;//值
    private final String dec;//名称
    private final int value;//值
    private final ChronoUnit unit;//单位

    PeriodEnum(String code, String dec, int value, ChronoUnit unit) {
        this.code = code;
        this.dec = dec;
        this.value = value;
        this.unit = unit;
    }

    public static PeriodEnum parse(String code){
        for (PeriodEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return NONE;
    }
}
