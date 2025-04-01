package com.changtian.factor.cache;

import com.changtian.factor.entity.forex.GlobalParameter;
import com.changtian.factor.entity.forex.MonitorSetting;
import com.changtian.factor.entity.forex.ParameterSetting;
import com.changtian.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

@Slf4j
public class TaCacheManager implements Serializable {
    private static TaCacheManager INSTANCE;
    //趋势线个数管理方
    private final QsxBarManager qsxBarManager = new QsxBarManager();

    public static TaCacheManager getInstance(){
        if(INSTANCE == null){
            synchronized (TaCacheManager.class){
                if(INSTANCE == null){
                    INSTANCE = new TaCacheManager();
                }
            }
        }
        return INSTANCE;
    }
    @Getter@Setter
    public static class IndexDTO{
        private String assetPoolName;//资产池名称
        private String instanceId;//实例id
        private String instanceName;//实例名称
        private String strategyId;//策略id
        private String strategyName;//策略名称
        private String period;//周期
        private String pushStartTime;//推送开始时间
        private String pushEndTime;//推送结束时间
        private List<GlobalParameter> globalParameter;//实例全局参数设置
        private ParameterSetting parameterSetting;//参数设置
        private List<MonitorSetting> monitorSetting;//行情监控设置

        public static IndexDTO create(LocalCurrencyStrategyInstanceResult si,ParameterSetting parameter,List<MonitorSetting> monitor){
            IndexDTO e = new IndexDTO();
            e.assetPoolName =si.getAssetPoolName();
            e.instanceId = si.getInstanceId();
            e.instanceName = si.getInstanceName();
            e.strategyId = si.getStrategyId();
            e.strategyName = si.getStrategyName();
            e.period = si.getPeriodsSetting().getOhlc();
            e.parameterSetting = parameter;
            e.monitorSetting = monitor;
            e.pushStartTime = si.getMarketDataSetting().getPushStartTime();
            e.pushEndTime = si.getMarketDataSetting().getPushEndTime();
            return e;
        }
    }

}
