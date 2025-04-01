package com.changtian.factor.output.bondprice;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 现价排序redis静态数据存储
 */
@Getter@Setter@ToString
public class BondBasisStaticData {
    private String securitySuperType;//债券信用类型
    private String securityDec;//债券描述
    private String securityId;//债券代码
    private String symbol;//债券简称
    private String termToMaturityString;//代偿期/剩余期数
    private BigDecimal valuation;//中债估值
    private String tradeDt;//中债估值更新时间
}
