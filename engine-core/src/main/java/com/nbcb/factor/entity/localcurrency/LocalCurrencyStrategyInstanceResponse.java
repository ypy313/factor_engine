package com.nbcb.factor.entity.localcurrency;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter@Getter@ToString
public class LocalCurrencyStrategyInstanceResponse {
    private String strategyName;//策略名称
    private String strategyInstanceId;//实例id
    private String strategyInstanceName;//实例名称
    private LocalCurrencyMarketDataSetting marketDataSetting;//数据源设置
    private List<LocalCurrencyIndexSetting> indexSettings;//指标设置
    private List<LocalCurrencyMonitorSetting> monitorSettings;//监控设置
}
