package com.nbcb.factor.web.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter@Setter
public class ChinaBondSpreadVolatilityCache {
    private String volatilityDay;
    //历史窗口波动率
    private BigDecimal historyVolatility = BigDecimal.ZERO;
    //历史窗口平均值
    private BigDecimal historyMean = BigDecimal.ZERO;
}
