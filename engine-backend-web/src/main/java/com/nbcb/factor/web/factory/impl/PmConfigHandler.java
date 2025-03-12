package com.nbcb.factor.web.factory.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nbcb.factor.common.Constants;
import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.common.JacksonUtils;
import com.nbcb.factor.entity.PeriodsSetting;
import com.nbcb.factor.entity.forex.GlobalParameter;
import com.nbcb.factor.entity.forex.MonitorSetting;
import com.nbcb.factor.entity.forex.ParameterSetting;
import com.nbcb.factor.entity.localcurrency.LocalCurrencyMarketDataSetting;
import com.nbcb.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.nbcb.factor.web.entity.ProjectTraConfigKeyGroupVo;
import com.nbcb.factor.web.entity.ProjectTraPropertyStrategyInputVo;
import com.nbcb.factor.web.entity.ProjectTraStrategyInstanceVo;
import com.nbcb.factor.web.entity.StrategyConfigEntity;
import com.nbcb.factor.web.factory.StrategyConfigHandler;
import com.nbcb.factor.web.mapper.FactorPmOhlcHisMapper;
import com.nbcb.factor.web.mapper.FactorPmSampleTickDataMapper;
import com.nbcb.factor.web.mapper.StrategyInstanceMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.*;

@Component(Constants.PM)
public class PmConfigHandler implements StrategyConfigHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    @Resource
    private FactorPmOhlcHisMapper factorPmOhlcHisMapper;
    @Resource
    private FactorPmSampleTickDataMapper factorPmSampleTickDataMapper;
    @Autowired
    StrategyInstanceMapper strategyInstanceMapper;

    @Override
    public String getStrategyName() {
        return Constants.PM;
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
        //查询策略因子实例
        List<ProjectTraStrategyInstanceVo> voList = strategyInstanceMapper
                .getPmTaProjectTraStrategyList(strategyConfigEntity.getStrategyName(), longStrategyInstanceId,
                        strategyConfigEntity.getAssetPools());
        List<LocalCurrencyStrategyInstanceResult> resultList = new ArrayList<>();
        voList.forEach(siv -> {
            LocalCurrencyStrategyInstanceResult response = new LocalCurrencyStrategyInstanceResult();
            response.setInstanceId(siv.getInstanceId());
            response.setAssetPoolId(siv.getAssetPoolId());
            response.setInstanceName(siv.getInstanceName());
            response.setStrategyName(siv.getStrategyName());
            response.setStrategyId(siv.getStrategyId());
            response.setAssetPoolName(siv.getAssetPoolName());
            response.setIndexCategory(siv.getProjectTraPropertyStrategyInputVoList().get(0).getIndexCategory());
            response.setFormula(siv.getProjectTraPropertyStrategyInputVoList().get(0).getIndicatorExec());

            //参数配置
            List<ProjectTraPropertyStrategyInputVo> projectTraPropertyStrategyInputVoList = siv.getProjectTraPropertyStrategyInputVoList();
            if (CollectionUtils.isEmpty(projectTraPropertyStrategyInputVoList)) {
                LOGGER.error("因子实例ID:{},参数配置不存在", siv.getInstanceId());
                return;
            }
            //初始化设置集合
            List<ParameterSetting> parameterSettingList = new ArrayList<>();
            List<MonitorSetting> monitorSettingList = new ArrayList<>();
            List<GlobalParameter> globalParameterList = new ArrayList<>();
            Map<String, Object> marketDataSettingAndPeriodsSetting;
            for (ProjectTraPropertyStrategyInputVo projectTraProperty : projectTraPropertyStrategyInputVoList) {
                String configKey = projectTraProperty.getConfigKey();
                if (StringUtils.isNotEmpty(configKey)
                        && Constants.PRO_TRA_INDEX_SETTING.equals(configKey)) {
                    parameterSettingList.add(setParameterSetting(projectTraProperty));
                } else if (StringUtils.isNotEmpty(configKey)
                        && Constants.PRO_TRA_MONITOR_SETTING.equals(configKey)) {
                    monitorSettingList.add(setMonitorSetting(projectTraProperty));
                } else if (StringUtils.isNotEmpty(configKey)
                        && Constants.PRO_TRA_GLOBAL_SETTING.equals(configKey)) {
                    globalParameterList.add(setGlobalParameter(projectTraProperty));
                } else if (StringUtils.isNotEmpty(configKey)
                        && Constants.PRO_TRA_DATA_SOURCE_CONFIG.equals(configKey)) {
                    marketDataSettingAndPeriodsSetting = setDataSourceConfig(projectTraProperty);
                    response.setMarketDataSetting(new ObjectMapper()
                            .convertValue(marketDataSettingAndPeriodsSetting.get("marketDataSetting"), LocalCurrencyMarketDataSetting.class));
                    response.setPeriodsSetting(new ObjectMapper()
                            .convertValue(marketDataSettingAndPeriodsSetting.get("periodsSetting"), PeriodsSetting.class));
                } else {
                    LOGGER.error("暂时不支持此类计算参数!因子名称:{}因子实例ID:{}", configKey, siv.getInstanceId());
                }
            }
            response.setParameterSetting(parameterSettingList);
            response.setMonitorSetting(monitorSettingList);
            response.setGlobalParameter(globalParameterList);
            if (Constants.TA_INDEX_CATEGORY_QSX.equals(response.getIndexCategory())) {
                setQsxBarNum(response);
            }
            resultList.add(response);
        });
        return resultList;
    }

    private void setQsxBarNum(LocalCurrencyStrategyInstanceResult response) {
        for (GlobalParameter globalParameter : response.getGlobalParameter()) {
            if (globalParameter.getXpoint1() != null && globalParameter.getXpoint2() != null) {
                int x2Num;
                int x3Num;
                Date nowTime = new Date();
                long xDiff = globalParameter.getXpoint2().getTime() - globalParameter.getXpoint1().getTime();
                Date startTime = xDiff >= 0 ? globalParameter.getXpoint1() : globalParameter.getXpoint2();
                Date endTime = xDiff >= 0 ? globalParameter.getXpoint2() : globalParameter.getXpoint1();

                if (!response.getPeriodsSetting().getOhlc().equals(Constants.TICK)) {
                    int hasBarStart = factorPmOhlcHisMapper.selectHasBar(response.getMarketDataSetting().getLeg(),
                            response.getPeriodsSetting().getOhlc(), DateUtil.format(startTime, DateUtil.YYYYMMDD_HH_MM_SS));
                    int hasBarend = factorPmOhlcHisMapper.selectHasBar(response.getMarketDataSetting().getLeg(),
                            response.getPeriodsSetting().getOhlc(), DateUtil.format(endTime, DateUtil.YYYYMMDD_HH_MM_SS));
                    if (hasBarStart <= 0 || hasBarend <= 0) {
                        x2Num = 0;
                        x3Num = 0;
                    } else {
                        x2Num = factorPmOhlcHisMapper.selectBarNum(response.getMarketDataSetting().getLeg(), response.getPeriodsSetting().getOhlc(),
                                DateUtil.format(startTime, DateUtil.YYYYMMDD_HH_MM_SS), DateUtil.format(endTime, DateUtil.YYYYMMDD_HH_MM_SS));
                        x3Num = factorPmOhlcHisMapper.selectBarNum(response.getMarketDataSetting().getLeg(), response.getPeriodsSetting().getOhlc(),
                                DateUtil.format(startTime, DateUtil.YYYYMMDD_HH_MM_SS), DateUtil.format(nowTime, DateUtil.YYYYMMDD_HH_MM_SS));
                    }
                } else {
                    int hasBarStart = factorPmSampleTickDataMapper.selectTickInfo(response.getMarketDataSetting().getLeg(),
                            DateUtil.format(startTime, DateUtil.YYYYMMDD_HH_MM_SS));
                    int hasBarend = factorPmSampleTickDataMapper.selectTickInfo(response.getMarketDataSetting().getLeg(),
                            DateUtil.format(endTime, DateUtil.YYYYMMDD_HH_MM_SS));
                    if (hasBarStart <= 0 || hasBarend <= 0) {
                        x2Num = 0;
                        x3Num = 0;
                    } else {
                        x2Num = factorPmSampleTickDataMapper.selectTickBarNum(response.getMarketDataSetting().getLeg(),
                                startTime.getTime(), endTime.getTime());
                        x3Num = factorPmSampleTickDataMapper.selectTickBarNum(response.getMarketDataSetting().getLeg(),
                                startTime.getTime(), nowTime.getTime());
                    }
                }
                globalParameter.setX2Num(x2Num);
                globalParameter.setX3Num(x3Num);
            }
        }
    }

    private Map<String, Object> setDataSourceConfig(ProjectTraPropertyStrategyInputVo calInputVos) {
        Map<String, Object> resultMap = new HashMap<>();
        PeriodsSetting periodsSetting = new PeriodsSetting();

        for (ProjectTraConfigKeyGroupVo projectTraConfigKeyGroupVo : calInputVos.getProjectTraConfigKeyGroupVoList()) {
            if (Constants.PRO_TRA_OHLC.equals(projectTraConfigKeyGroupVo.getConfigKey())) {
                periodsSetting.setOhlc(projectTraConfigKeyGroupVo.getPropertyValue());
            }
            resultMap.put(projectTraConfigKeyGroupVo.getPropertyKey(), projectTraConfigKeyGroupVo.getPropertyValue());
        }
        LocalCurrencyMarketDataSetting marketDataSetting = JacksonUtils.convertValue(resultMap, LocalCurrencyMarketDataSetting.class);
        marketDataSetting.setLeg(calInputVos.getDisplayName());
        resultMap.put(Constants.PRO_TRA_MARKET_DATA_SETTING, marketDataSetting);
        resultMap.put(Constants.PRO_TRA_PERIODS_SETTING, periodsSetting);
        return resultMap;
    }

    private GlobalParameter setGlobalParameter(ProjectTraPropertyStrategyInputVo projectTraPropertyStrategyInputVo) {
        GlobalParameter globalParameter = new GlobalParameter();
        for (ProjectTraConfigKeyGroupVo projectTraConfigKeyGroupVo : projectTraPropertyStrategyInputVo.getProjectTraConfigKeyGroupVoList()) {
            if (Constants.GLOBAL_PARAMETER_PARAMETER_NAME.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                globalParameter.setParameterName(projectTraConfigKeyGroupVo.getPropertyValue());
            } else if (Constants.GLOBAL_PARAMETER_PARAMETER_VALUE.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                globalParameter.setParameterValue(new BigDecimal(projectTraConfigKeyGroupVo.getPropertyValue()));
            } else if (Constants.GLOBAL_PARAMETER_XPOINT1.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                globalParameter.setXpoint1(DateUtil.transferLongToDate(DateUtil.YYYY_MM_DD_HH_MM_SS_SS, Long.valueOf(projectTraConfigKeyGroupVo.getPropertyValue())));
            } else if (Constants.GLOBAL_PARAMETER_YPOINT1.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                globalParameter.setYpoint1(new BigDecimal(projectTraConfigKeyGroupVo.getPropertyValue()));
            } else if (Constants.GLOBAL_PARAMETER_XPOINT2.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                globalParameter.setXpoint2(DateUtil.transferLongToDate(DateUtil.YYYY_MM_DD_HH_MM_SS_SS, Long.valueOf(projectTraConfigKeyGroupVo.getPropertyValue())));
            } else if (Constants.GLOBAL_PARAMETER_YPOINT2.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                globalParameter.setYpoint2(new BigDecimal(projectTraConfigKeyGroupVo.getPropertyValue()));
            } else if (Constants.GLOBAL_PARAMETER_XCELL1.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                globalParameter.setXcell1(new BigDecimal(projectTraConfigKeyGroupVo.getPropertyValue()));
            } else if (Constants.GLOBAL_PARAMETER_XCELL2.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                globalParameter.setXcell2(new BigDecimal(projectTraConfigKeyGroupVo.getPropertyValue()));
            } else if (Constants.GLOBAL_PARAMETER_LATEST_XPOINT.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                globalParameter.setLatestXPoint(DateUtil.transferLongToDate(DateUtil.YYYY_MM_DD_HH_MM_SS_SS, Long.valueOf(projectTraConfigKeyGroupVo.getPropertyValue())));
            } else if (Constants.GLOBAL_PARAMETER_LATEST_XCELL.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                globalParameter.setLatestXCell(new BigDecimal(projectTraConfigKeyGroupVo.getPropertyValue()));
            }
        }
        return globalParameter;
    }

    private MonitorSetting setMonitorSetting(ProjectTraPropertyStrategyInputVo projectTraPropertyStrategyInputVo) {
        Map<String, Object> resultMap = new HashMap<>();
        for (ProjectTraConfigKeyGroupVo projectTraConfigKeyGroupVo : projectTraPropertyStrategyInputVo.getProjectTraConfigKeyGroupVoList()) {
            resultMap.put(projectTraConfigKeyGroupVo.getPropertyKey(), projectTraConfigKeyGroupVo.getPropertyValue());
            if (Constants.MONITOR_EXPRESSION.equals(projectTraConfigKeyGroupVo.getPropertyKey())) {
                resultMap.put(Constants.MONITOR_ID, projectTraConfigKeyGroupVo.getConfigId());
            } else if (Constants.MONITOR_REMINDER_INTERVAL.equals(projectTraConfigKeyGroupVo.getPropertyKey())
                    && StringUtils.isNotEmpty(projectTraConfigKeyGroupVo.getPropertyValue())) {
                resultMap.put(Constants.MONITOR_REMINDER_INTERVAL, projectTraConfigKeyGroupVo.getPropertyValue());
            }
        }
        MonitorSetting monitorSetting = JacksonUtils.convertValue(resultMap, MonitorSetting.class);
        monitorSetting.setMonitorOrder(projectTraPropertyStrategyInputVo.getRowNum());
        return monitorSetting;
    }

    private ParameterSetting setParameterSetting(ProjectTraPropertyStrategyInputVo projectTraPropertyStrategyInputVo) {
        ParameterSetting parameterSetting = new ParameterSetting();
        for (ProjectTraConfigKeyGroupVo calInputVo : projectTraPropertyStrategyInputVo.getProjectTraConfigKeyGroupVoList()) {
            if (Constants.PARAMETER_INDEX_NAME.equals(calInputVo.getPropertyKey())) {
                //指标名称
                parameterSetting.setIndexName(calInputVo.getPropertyValue());
            } else if (Constants.PARAMETER_MIN_PREFERENCES.equals(calInputVo.getPropertyKey())) {
                //最小指标参数设置
                parameterSetting.setMinPreferences(calInputVo.getPropertyValue());
            } else if (Constants.PARAMETER_MAX_PREFERENCES.equals(calInputVo.getPropertyKey())) {
                //最大指标参数设置
                parameterSetting.setMaxPreferences(calInputVo.getPropertyValue());
            } else if (Constants.PARAMETER_INDEX_PREFERENCES.equals(calInputVo.getPropertyKey())) {
                //指标参数设置
                parameterSetting.setIndexPreferences(calInputVo.getPropertyValue());
                //参数类型
                parameterSetting.setType(calInputVo.getPropertyType());
            } else if (Constants.PARAMETER_MIN_FORMULA.equals(calInputVo.getPropertyKey())) {
                //指标公式
                parameterSetting.setFormula(calInputVo.getPropertyValue());
            }
            parameterSetting.setIndexCategory(projectTraPropertyStrategyInputVo.getIndexCategory());
        }
        return parameterSetting;
    }
}
