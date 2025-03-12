package com.nbcb.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RateVo {
    private String tradeDt;//日期
    private double rate;//利率
}
