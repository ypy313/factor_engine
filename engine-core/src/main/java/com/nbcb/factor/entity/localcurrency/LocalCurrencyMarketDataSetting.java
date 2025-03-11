package com.nbcb.factor.entity.localcurrency;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 本币数据源设置
 */
@Getter
@Setter
public class LocalCurrencyMarketDataSetting {
    private String dataSource;//数据源
    private String preLoad;//预加载个数
    private String leg1;//债券1
    private String leg2;//债券2

    //外汇添加
    private String leg;//货币对
    private String accountMethod;//计算方式 mid ofr bid
    private BigDecimal  latticeValue;//格值
    private BigDecimal frame;//格数
    //信号优化添加
    private String pushStartTime;//推送开始时间
    private String pushEndTime;//推送结束时间
}
