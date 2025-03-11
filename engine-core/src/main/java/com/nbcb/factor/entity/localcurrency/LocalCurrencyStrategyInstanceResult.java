package com.nbcb.factor.entity.localcurrency;

import com.nbcb.factor.entity.PeriodsSetting;
import com.nbcb.factor.entity.forex.GlobalParameter;
import com.nbcb.factor.entity.forex.MonitorSetting;
import com.nbcb.factor.entity.forex.ParameterSetting;
import com.nbcb.factor.entity.riding.RidingStrategyInstance;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
@Getter@Setter@ToString
public class LocalCurrencyStrategyInstanceResult {
    private String strategyName;//策略名称
    private String instanceId;//实例id
    private String instanceName;//实例名称
    private LocalCurrencyMarketDataSetting marketDataSetting;//数据源设置
    private List<IndexSettingsAndMonitorResult> spreadOnePoolMonitorSetting;//指标设置

    private String assetPoolName;//资产池名称
    private String assetPoolId;//资产池id
    private String indexCategory;//指标类别
    private String strategyId;//策略id
    private PeriodsSetting periodsSetting;//周期设置
    private List<GlobalParameter> globalParameter;//用户配置的因子实例全局参数
    private List<ParameterSetting> parameterSetting;//参数设置
    private List<MonitorSetting> monitorSetting;//监控设置

    private String displayName;//显示名称
    private String status;//状态
    private List<RidingStrategyInstance> strategyInstanceVoList;

    private String symbol;//资产名称
    private String symbolType;//资产类型

    //执行指标计算公式
    private String formula;

}
