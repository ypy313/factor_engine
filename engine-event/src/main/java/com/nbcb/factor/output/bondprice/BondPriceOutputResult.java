package com.nbcb.factor.output.bondprice;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 前端最终输出结果
 */
@Getter@Setter@ToString
public class BondPriceOutputResult {
    private String securityId;//代码
    private String symbol; //名称
    private BigDecimal wholeBestBidYield;// 全市场最优bid-报实价(%)
    private BigDecimal wholeBestOfrYield;//全市场最优ofr-报卖价(%)
    private BigDecimal spread;// 买卖价差BP
    private BigDecimal latestTrans;// 最新成交(%)
    private String transTime;// 成交时间
    private BigDecimal bidTransDiff;//bid-最新成交(BP)
    private BigDecimal ofrTransDiff;//最新成交-0fr(BP)
    private BigDecimal valuation;// 中债估值(%)
    private BigDecimal bidValuationDiff;// bid-中值(BP)
    private BigDecimal ofrValuationDiff;// 中债估值-0fr(BP)
    private String termToMaturitystring;//代偿期/剩余期数
    private String bidDataSource;//bid最优价数据来源-报买机构
    private String ofrDataSource;//ofr最优数据来源-报卖机构
    private BigDecimal bidVolume;//报买量(万)
    private BigDecimal ofrVolume;//报卖量(万)
    private String securityDec;//债券插述
}
