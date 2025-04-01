package com.changtian.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 指标计算ohlc数据实体
 */
@Getter
@Setter
@ToString
public class CalResultEventDataEntity implements Serializable {
    private String source;
    private String strategyName;
    private String instanceId;
    private String instanceName;
    private String factorName;
    private String symbol;
    private String ric;
    private String period;
    private String indexCategory;
    private String summaryType;
    private String beginTime;
    private String endTime;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private String calResult;
    private List<String> relationId;
}
