package com.nbcb.factor.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 每200ms生成OHLC对象
 */
@Getter@Setter@ToString
public class OhlcEntity {
    private String symbol;//货币对
    private double openPrice;//开盘价
    private double highPrice;//最高价
    private double lowPrice;//最低价
}
