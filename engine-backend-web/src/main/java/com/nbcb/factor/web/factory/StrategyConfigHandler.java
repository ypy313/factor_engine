package com.nbcb.factor.web.factory;

import com.nbcb.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.nbcb.factor.web.entity.StrategyConfigEntity;

import java.util.List;

/**
 * 策略配置结构处理接口
 */
public interface StrategyConfigHandler {
    List<LocalCurrencyStrategyInstanceResult> strategyConfigHandler(StrategyConfigEntity strategyConfigEntity);
    String getStrategyName();
}
