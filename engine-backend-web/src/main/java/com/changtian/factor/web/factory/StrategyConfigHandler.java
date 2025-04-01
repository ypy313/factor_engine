package com.changtian.factor.web.factory;

import com.changtian.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.changtian.factor.web.entity.StrategyConfigEntity;

import java.util.List;

/**
 * 策略配置结构处理接口
 */
public interface StrategyConfigHandler {
    List<LocalCurrencyStrategyInstanceResult> strategyConfigHandler(StrategyConfigEntity strategyConfigEntity);
    String getStrategyName();
}
