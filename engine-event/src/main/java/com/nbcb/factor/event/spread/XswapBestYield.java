package com.nbcb.factor.event.spread;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
@Setter@Getter@ToString
public class XswapBestYield implements Serializable {
    //交易买方最优值
    private BigDecimal bestBidYield;
    //交易卖方最优值
    private BigDecimal bestOfrYield;
    //交易时间
    private String tradeTime;
    //买方数据源
    private String bidDataSource;
    //卖方数据源
    private String ofrDataSource;
    //买方交易量
    private BigDecimal bidVolume;
    //卖方交易量
    private BigDecimal ofrVolume;
}
