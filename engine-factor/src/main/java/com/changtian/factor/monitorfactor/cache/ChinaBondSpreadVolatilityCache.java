package com.changtian.factor.monitorfactor.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 中债价差波动率缓存对象
 */
@Getter@Setter@ToString
public class ChinaBondSpreadVolatilityCache {
    private String volatilityDay;
    private BigDecimal historyVolatility = BigDecimal.ZERO;//历史窗口波动率
    private BigDecimal historyMean = BigDecimal.ZERO;//历史窗口平均数

}
