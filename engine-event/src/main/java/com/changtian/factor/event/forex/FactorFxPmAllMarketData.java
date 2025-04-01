package com.changtian.factor.event.forex;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 行情源实体
 */
@Getter@Setter@ToString
public class FactorFxPmAllMarketData implements Serializable {
    private String eventId;//事件id
    private String ric;//ric
    private String symbol;//货币对
    private String tenor;//期限
    private String source;//来源
    private String securityDesc;//类型
    private String side;//报价方向1-bid 2-ask 3-both
    private String valid;//
    private String bid;//银行成本价（买）
    private String ask;//银行成本价（卖）
    private String biddate;//银行成本价（买）计算日期
    private String bidtime;//银行成本价（买）计算时间
    private String askdate;//银行成本价（卖）计算日期
    private String asktime;//银行成本价（卖）计算时间
    private String ctbtr_1;//流动性
    private long eventTime;//bid/ask发生时间
    private String created;//connector接收后开始解析时间
    private String mid;//中间价
}
