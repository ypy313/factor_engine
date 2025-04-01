package com.changtian.factor.output;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 成交处理详情
 */
@Getter@Setter@ToString
public class MarketProcessingDealDetailResult implements Serializable {
    private BigDecimal dealYield = null;//成交收益率
    private String tradeTime;//业务发生时间
}
