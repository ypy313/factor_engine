package com.changtian.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 外汇因子OHLC历史表
 */
@Getter
@Setter
@ToString
public class FactorOhlcHis {
    private String id;
    private String eventId;
    private String ric;//ric码
    private String symbol;//资产代码
    private String source;//行情来源
    private String period;//k线窗口
    private String beginTime;
    private String endTime;
    private String tradeDate;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private String volume;//交易量
    private String sourceType;//行情来源类型
    private String createTime;
    private String updateTime;
}
