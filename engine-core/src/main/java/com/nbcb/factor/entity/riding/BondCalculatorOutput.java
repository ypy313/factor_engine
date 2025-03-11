package com.nbcb.factor.entity.riding;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 债券计算输出
 */
@Getter@Setter@ToString
public class BondCalculatorOutput {
    private int f;//年付息频率
    private int n;//单利还是复利 n=1 单利 n>1复利 付息次数
    private BigDecimal remainYear ;//剩余年限
    private BigDecimal ty;//当前计息年度的实际天数，算头不算尾
    private BigDecimal d;//券结算日至下一最近付息日之间的实际天数
    private BigDecimal t;//券结算日至上一最近付息日之间的实际天数
    private BigDecimal ts;//当前付息周期的实际天数
    private BigDecimal ai;//应记利息
    private BigDecimal w;//d/ts
    private String settlementDate;//结算日
}
