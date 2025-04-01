package com.changtian.factor.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 现金流实体对象
 */
@Getter
@Setter
@ToString
public class BondCFVo {
    private String id;
    private String infoWindCode;//债券代码
    private String infoCarryDate;//起息日
    private String infoEndDate;//结束日
    private double infoCouponRate;//利率
    private String infoPaymentDate;//付息日
    private double infoPaymentInterest;//应付利息
    private double infoPaymentParValue;//应付本金
    private double infoPaymentSum;//应付总额
}
