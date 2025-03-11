package com.nbcb.factor.event.pm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
@Getter@Setter@ToString
public class FactorPreciousMetalMarketData implements Serializable {
    private String eventId;//事件id
    private String eventName;//事件名称
    private String eventTime;//bid/ask发生时间

    private String ric;//ric
    private String symbol;//货币对
    private String marketDataType;//行情类型 TRADE交易行情 DEPTH深度行情
    private String dataSource;//来源
    private String side;//报价方向1-bid 2-ask 3-both
    private String timeInLocal;//connector接收行情事件
    private String timestamp;//connector接收行情的时间 long结构

    private String tenor;//期限
    private String source;//来源
    private String securityDesc;//类型
    private String valid;//
    private String bid;//银行成本价（买）
    private String ask;//银行成本价（卖）
    private String biddate;//银行成本价（买）计算日期
    private String bidtime;//银行成本价（买）计算时间
    private String askdate;//银行成本价（卖）计算日期
    private String asktime;//银行成本价（卖）计算时间
    private String ctbtr_1;//流动性
    private String created;//connector接收后开始解析时间
    private String min;//中间价

    /**
     * 五档行情 路透只有最优一档 dimple有五档
     */
    private String bid1;//银行成本价（买）
    private String ask1;//银行成本价（卖）
    private String bidDate1;//银行成本价（买）计算日期
    private String askDate1;//银行成本价（卖）计算日期

    private String bid2;//银行成本价（买）
    private String ask2;//银行成本价（卖）
    private String bidDate2;//银行成本价（买）计算日期
    private String askDate2;//银行成本价（卖）计算日期

    private String bid3;//银行成本价（买）
    private String ask3;//银行成本价（卖）
    private String bidDate3;//银行成本价（买）计算日期
    private String askDate3;//银行成本价（卖）计算日期

    private String bid4;//银行成本价（买）
    private String ask4;//银行成本价（卖）
    private String bidDate4;//银行成本价（买）计算日期
    private String askDate4;//银行成本价（卖）计算日期

    private String bid5;//银行成本价（买）
    private String ask5;//银行成本价（卖）
    private String bidDate5;//银行成本价（买）计算日期
    private String askDate5;//银行成本价（卖）计算日期
}
