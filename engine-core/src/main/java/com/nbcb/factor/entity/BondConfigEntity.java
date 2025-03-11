package com.nbcb.factor.entity;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 债券配置
 */
@Data
@ToString
public class BondConfigEntity {
    private BigDecimal id;//主键
    private String securityId;//债券代码
    private String symbol;//债券简称
    private String issueSize;//代偿期
    private String termToMaturityString;//代偿期/剩余期限
    private String valuation;//中债估值
    private String valuationDate;//中债估值日期
    private String securityType;//债券类型
    private String securityDec;//债券描述
    private String securityTypeId;//债券描述ID
    private String securityDecMapper;//债券描述映射
    private String securityKey;//分组编号
    private String createTime;//创建时间
}
