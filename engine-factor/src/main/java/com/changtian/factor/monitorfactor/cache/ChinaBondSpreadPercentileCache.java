package com.changtian.factor.monitorfactor.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 中债估值价差百分位redis缓存格式
 */
@Getter@Setter@ToString
public class ChinaBondSpreadPercentileCache {
    private String percentileMapDay;
    private Map<BigDecimal,BigDecimal> percentileMap;
}
