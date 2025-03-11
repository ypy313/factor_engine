package com.nbcb.factor.web.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 中债估值价差百分位redis
 */
@Getter@Setter
public class ChinaBondSpreadPercentileCache {
    private String percentileMapDay;
    private Map<BigDecimal,BigDecimal> percentileMap;
}
