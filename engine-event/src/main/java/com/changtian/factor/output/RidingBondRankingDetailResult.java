package com.changtian.factor.output;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 排名
 */
@Getter
@Setter
@ToString
public class RidingBondRankingDetailResult {
    private String instanceId;//实例id
    private String securityId;//债券代码
    private String remainingMaturity;//剩余期限
    private String futureRemainingMaturity;//未来剩余期限
    private String currentValuationYield;//当前估值收益率
    private String currentCurveValuation;//当前曲线估值
    private String futureValuationYield;//未来估值收益率
    private String futureCurveValuation;//未来曲线估值
    private String currentValuationPrice;//当前估价价格
    private String futureValuationPrice;//未来估价价格
    private String currentDeviation;//当前偏离值
    private String futureDeviation;//未来偏离
    private String carryProfitAndLoss;//carry损益
    private BigDecimal carryProfitAndLossNum = null;//carry损益数值
    private String carryProfitAndLossPrice;//carry损益价格
    private int carryProfitAndLossSort;//carry损益排序
    private  String rollDownProfitAndLoss;//rollDown损益
    private BigDecimal rollDownProfitAndLossNum = null;//rollDown损益数值
    private String rollDownProfitAndLossPrice;//rollDown损益价格
    private int rollDownProfitAndLossSort;//rolldown损益排序
    private String profitAndLossYield;//综合损益收益率
    private BigDecimal profitAndLossYieldNum = null;//综合损益收益率数值
    private String profitAndLossPrice;//综合损益价格
    private int profitAndLossSort;//综合损益收益率排序
    private String surplusBalancePoint;//盈余平衡点
}
