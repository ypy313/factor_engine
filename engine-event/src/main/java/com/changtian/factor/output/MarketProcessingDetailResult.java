package com.changtian.factor.output;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 行情处理详情
 */
@Getter@Setter@ToString
public class MarketProcessingDetailResult implements Serializable {
    private BigDecimal bestBidYield =null;// bid最优
    private BigDecimal bestOfrYield =null;// 0fr最优
    private BigDecimal bestMidYield = null;//中间价最优
    private String tradeTime;// 业务发生时间
    private String bidDataSource;//bid最优价数据来源
    private String ofrDataSource;//ofr最优数据来源
    //报里单位为万元
    private BigDecimal bidVolume;//报买里
    private BigDecimal ofrVolume;//报卖里
}
