package com.nbcb.factor.web.factory.impl;

import com.nbcb.factor.common.Constants;
import com.nbcb.factor.entity.localcurrency.*;
import com.nbcb.factor.web.entity.ProjectTraConfigKeyGroupVo;
import com.nbcb.factor.web.entity.ProjectTraPropertyStrategyInputVo;
import com.nbcb.factor.web.entity.ProjectTraStrategyInstanceVo;
import com.nbcb.factor.web.entity.StrategyConfigEntity;
import com.nbcb.factor.web.factory.StrategyConfigHandler;
import com.nbcb.factor.web.mapper.StrategyInstanceMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Component(Constants.SPREAD_ONE_POOL_STRATEGY)
public class SpreadOnePoolConfigHandler implements StrategyConfigHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    StrategyInstanceMapper strategyInstanceMapper;

    @Override
    public String getStrategyName() {
        return Constants.SPREAD_ONE_POOL_STRATEGY;
    }

    @Override
    public List<LocalCurrencyStrategyInstanceResult> strategyConfigHandler(StrategyConfigEntity strategyConfigEntity) {
        Long longStrategyInstanceId = null;
        if (StringUtils.isNumeric(strategyConfigEntity.getStrategyInstanceId())) {
            try {
                longStrategyInstanceId = Long.parseLong(strategyConfigEntity.getStrategyInstanceId());
            } catch (NumberFormatException e) {
                LOGGER.error(String.format("%s转Long失败", strategyConfigEntity.getStrategyInstanceId()));
            }
        }
        List<ProjectTraStrategyInstanceVo> disStraInsListTest = strategyInstanceMapper
                .getProjectTraStrategyInputVoList(strategyConfigEntity.getStrategyName(),
                        longStrategyInstanceId, strategyConfigEntity.getAssetPools());
        List<LocalCurrencyStrategyInstanceResponse> localCurrencyStrategyInstanceResponseList = new ArrayList<>();
        List<LocalCurrencyStrategyInstanceResult> localCurrencyStrategyInstanceList = new ArrayList<>();
        for (ProjectTraStrategyInstanceVo projectTraSpreadOnePoolStrategyInstanceVo : disStraInsListTest) {
            LocalCurrencyStrategyInstanceResponse localCurrencyStrategyInstanceResponse = new LocalCurrencyStrategyInstanceResponse();
            localCurrencyStrategyInstanceResponse.setStrategyInstanceId(projectTraSpreadOnePoolStrategyInstanceVo.getInstanceId());
            localCurrencyStrategyInstanceResponse.setStrategyInstanceName(projectTraSpreadOnePoolStrategyInstanceVo.getInstanceName());
            localCurrencyStrategyInstanceResponse.setStrategyName(projectTraSpreadOnePoolStrategyInstanceVo.getStrategyName());

            LocalCurrencyMarketDataSetting marketDataSettingList = new LocalCurrencyMarketDataSetting();
            List<LocalCurrencyIndexSetting> indexSettingList = new ArrayList<>();
            List<LocalCurrencyMonitorSetting> monitorSettingList = new ArrayList<>();

            for (ProjectTraPropertyStrategyInputVo projectTraSpreadOnePoolStrategyInputVo : projectTraSpreadOnePoolStrategyInstanceVo.getProjectTraPropertyStrategyInputVoList()) {
                if (Constants.PRO_TRA_DATA_SOURCE_CONFIG.equals(projectTraSpreadOnePoolStrategyInputVo.getConfigKey())) {
                    setSpreadOnePoolStrategyDataSourceSetting(marketDataSettingList, projectTraSpreadOnePoolStrategyInputVo.getProjectTraConfigKeyGroupVoList());
                    localCurrencyStrategyInstanceResponse.setMarketDataSetting(marketDataSettingList);
                }
                if (Constants.PRO_TRA_INDEX_SETTING.equals(projectTraSpreadOnePoolStrategyInputVo.getConfigKey())) {
                    indexSettingList.add(setSpreadOnePoolStrategyIndexSetting(projectTraSpreadOnePoolStrategyInputVo.getProjectTraConfigKeyGroupVoList()));
                    localCurrencyStrategyInstanceResponse.setIndexSettings(indexSettingList);
                }
                if (Constants.PRO_TRA_MONITOR_EXPRESSION.equals(projectTraSpreadOnePoolStrategyInputVo.getConfigKey())) {
                    monitorSettingList.add(setSpreadOnePoolStrategyMoniterSetting(projectTraSpreadOnePoolStrategyInputVo.getProjectTraConfigKeyGroupVoList()));
                    localCurrencyStrategyInstanceResponse.setMonitorSettings(monitorSettingList);
                }
            }
            LocalCurrencyStrategyInstanceResult localCurrencyStrategyInstanceResult = setOldStructure(localCurrencyStrategyInstanceResponse);
            localCurrencyStrategyInstanceList.add(localCurrencyStrategyInstanceResult);
            localCurrencyStrategyInstanceResponseList.add(localCurrencyStrategyInstanceResponse);
        }
        return localCurrencyStrategyInstanceList;
    }

    private LocalCurrencyStrategyInstanceResult setOldStructure(LocalCurrencyStrategyInstanceResponse localCurrencyStrategyInstanceResponse) {
        LocalCurrencyStrategyInstanceResult localCurrencyStrategyInstanceResult = new LocalCurrencyStrategyInstanceResult();
        localCurrencyStrategyInstanceResult.setInstanceId(localCurrencyStrategyInstanceResponse.getStrategyInstanceId());
        localCurrencyStrategyInstanceResult.setInstanceName(localCurrencyStrategyInstanceResponse.getStrategyInstanceName());
        localCurrencyStrategyInstanceResult.setStrategyName(localCurrencyStrategyInstanceResponse.getStrategyName());
        localCurrencyStrategyInstanceResult.setMarketDataSetting(localCurrencyStrategyInstanceResponse.getMarketDataSetting());
        List<IndexSettingsAndMonitorResult> localCurrencyIndexSettings = new ArrayList<>();
        if (localCurrencyStrategyInstanceResponse.getIndexSettings() == null) {
            return localCurrencyStrategyInstanceResult;
        }
        for (LocalCurrencyIndexSetting indexSetting : localCurrencyStrategyInstanceResponse.getIndexSettings()) {
            IndexSettingsAndMonitorResult indexSettingsAndMonitorResult = new IndexSettingsAndMonitorResult();
            indexSettingsAndMonitorResult.setChiName(indexSetting.getChiName());
            indexSettingsAndMonitorResult.setName(indexSetting.getIndexName());
            indexSettingsAndMonitorResult.setDataWindow(indexSetting.getIndexPreferences());
            for (LocalCurrencyMonitorSetting localCurrencyMonitorSetting : localCurrencyStrategyInstanceResponse.getMonitorSettings()) {
                if (indexSetting.getIndexName().equals(localCurrencyMonitorSetting.getMonitorName())) {
                    if (Constants.MONITOR_NORMAL.equals(localCurrencyMonitorSetting.getSignal())) {
                        indexSettingsAndMonitorResult.setYellowUpper(localCurrencyMonitorSetting.getUpper());
                        indexSettingsAndMonitorResult.setYellowDown(localCurrencyMonitorSetting.getDown());
                    } else if (Constants.MONITOR_RECOMMEND.equals(localCurrencyMonitorSetting.getSignal())) {
                        indexSettingsAndMonitorResult.setOrangeUpper(localCurrencyMonitorSetting.getUpper());
                        indexSettingsAndMonitorResult.setOrangeDown(localCurrencyMonitorSetting.getDown());
                    } else if (Constants.MONITOR_STRONG.equals(localCurrencyMonitorSetting.getSignal())) {
                        indexSettingsAndMonitorResult.setRedUpper(localCurrencyMonitorSetting.getUpper());
                        indexSettingsAndMonitorResult.setRedDown(localCurrencyMonitorSetting.getDown());
                    } else {
                        LOGGER.info("当前监控信号不明！因子实例id:{},信号值为:{}", localCurrencyStrategyInstanceResult.getInstanceId(), localCurrencyMonitorSetting.getSignal());
                    }
                }
            }
            localCurrencyIndexSettings.add(indexSettingsAndMonitorResult);
        }
        localCurrencyStrategyInstanceResult.setSpreadOnePoolMonitorSetting(localCurrencyIndexSettings);
        return localCurrencyStrategyInstanceResult;
    }

    private LocalCurrencyMonitorSetting setSpreadOnePoolStrategyMoniterSetting(List<ProjectTraConfigKeyGroupVo> projectTraConfigKeyGroupVoList) {
        LocalCurrencyMonitorSetting spreadOnePoolStrategyMonitorSetting = new LocalCurrencyMonitorSetting();
        for (ProjectTraConfigKeyGroupVo projectTraConfigKeyGroupVo : projectTraConfigKeyGroupVoList) {
            switch (projectTraConfigKeyGroupVo.getPropertyKey()) {
                case Constants.MONITOR_NAME:
                    spreadOnePoolStrategyMonitorSetting.setMonitorName(projectTraConfigKeyGroupVo.getPropertyValue());
                    break;
                case Constants.MONITOR_UPPER:
                    spreadOnePoolStrategyMonitorSetting.setUpper(projectTraConfigKeyGroupVo.getPropertyValue());
                    break;
                case Constants.MONITOR_DOWN:
                    spreadOnePoolStrategyMonitorSetting.setDown(projectTraConfigKeyGroupVo.getPropertyValue());
                    break;
                case Constants.MONITOR_SIGNAL:
                    spreadOnePoolStrategyMonitorSetting.setSignal(projectTraConfigKeyGroupVo.getPropertyValue());
                    break;
            }
        }
        return spreadOnePoolStrategyMonitorSetting;
    }

    private LocalCurrencyIndexSetting setSpreadOnePoolStrategyIndexSetting(List<ProjectTraConfigKeyGroupVo> projectTraConfigKeyGroupVoList) {
        LocalCurrencyIndexSetting parameterSetting = new LocalCurrencyIndexSetting();
        for (ProjectTraConfigKeyGroupVo projectTraConfigKeyGroupVo : projectTraConfigKeyGroupVoList) {
            if (Constants.PARAMETER_INDEX_NAME.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                parameterSetting.setIndexName(projectTraConfigKeyGroupVo.getPropertyValue());
                parameterSetting.chiNameBuild(parameterSetting);
            } else if (Constants.PARAMETER_INDEX_PREFERENCES.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                parameterSetting.setIndexPreferences(Integer.parseInt(projectTraConfigKeyGroupVo.getPropertyValue()));
            } else {
                LOGGER.error("当前指标设置参数不明！改参数为：{}", projectTraConfigKeyGroupVo.getPropertyKey());
            }
        }
        return parameterSetting;
    }

    private void setSpreadOnePoolStrategyDataSourceSetting(LocalCurrencyMarketDataSetting marketDataSettingList, List<ProjectTraConfigKeyGroupVo> projectTraConfigKeyGroupVoList) {
        for (ProjectTraConfigKeyGroupVo projectTraStrategyInstanceInputVo : projectTraConfigKeyGroupVoList) {
            switch (projectTraStrategyInstanceInputVo.getPropertyKey()) {
                case Constants.PRO_TRA_LEG1:
                    marketDataSettingList.setLeg1(projectTraStrategyInstanceInputVo.getPropertyValue());
                    break;
                case Constants.PRO_TRA_LEG2:
                    marketDataSettingList.setLeg2(projectTraStrategyInstanceInputVo.getPropertyValue());
                    break;
                case Constants.PRO_TRA_DATA_SOURCE:
                    marketDataSettingList.setDataSource(projectTraStrategyInstanceInputVo.getPropertyValue());
                    break;
                case Constants.PRO_TRA_PRELOAD:
                    marketDataSettingList.setPreLoad(projectTraStrategyInstanceInputVo.getPropertyValue());
                    break;
            }
        }
    }
}
