package com.changtian.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
@Getter
@Setter
@ToString
public class PmOhlcMessgeEventDataEntity implements Serializable {
    private String strategyName;
    private String instanceId;
    private String instanceName;
    private String factorName;
    private String symbol;
    private String ric;
    private String period;
    private String source;
    private String indexCategory;
    private String summaryType;
    private String beginTime;
    private String endTime;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private List<String> relationId;
}
