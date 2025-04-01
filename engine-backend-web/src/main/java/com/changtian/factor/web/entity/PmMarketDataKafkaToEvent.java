package com.changtian.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class PmMarketDataKafkaToEvent {
    private String eventId;
    private String ric;//ric
    private String symbol;//货币对
    private String tenor;//期限
    private String source;//来源
    private String securityDesc;//类型
    private String side;//报价方向
    private String vaild;//有效时间
    private BigDecimal bid;//银行成本价（买）
    private BigDecimal ask;//银行成本价（卖）
    private BigDecimal mid;//中间价
    private String biddate;//银行成本价（买）计算日期
    private String bidTime;//银行成本价（买）计算时间
    private String askdate;//银行成本价（卖）计算日期
    private String askTime;//银行成本价（卖）计算时间
    private String eventTime;//bid/ask发生时间
    private String created;//connector接收后开始解析时间
    private String ctbtr_1;//流动性
    private String drawLineType;//中间价
    private BigDecimal bid1;
    private BigDecimal ask1;
    private String bidDate1;
    private String askDate1;
    private String timestamp;
}
