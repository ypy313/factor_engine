package com.changtian.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 贵金属节假日holiday实例
 */
@Getter@Setter@ToString
public class PmTpHoliday {
    private String holidayDate;
    private String holidayName;
    private String note;
}
