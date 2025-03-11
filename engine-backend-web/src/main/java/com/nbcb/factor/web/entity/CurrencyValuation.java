package com.nbcb.factor.web.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 中债估值，利率互换曲线收盘曲线
 */
@Getter@Setter
public class CurrencyValuation implements Serializable {
    private static final long serialVersionUID = 1L;
    private String ID;//对象id
    private String securitySuperType;//债券类型
    private String issueSize;//代偿期
    private String termToMaturityString;//代偿期/剩余期数
    private String securityId;//证券代码
    private String symbol;//债券名称
    private String securityDec;//债券描述
    private String securityTypeId;//债券类型id
    private String tradeDt;//交易日期
    private BigDecimal rateValue;//估价收益率（%）
}
