package com.changtian.factor.web.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 因子实例配置工厂
 */
@Component
public class StrategyConfigFactory {
    private Map<String,StrategyConfigHandler> strategyConfigHandlerMap = new ConcurrentHashMap<>();
    public Map<String,StrategyConfigHandler> getStrategyConfigHandlerMap() {
        return strategyConfigHandlerMap;
    }
    @Autowired
    public void setStrategyConfigHandlerMap(Map<String,StrategyConfigHandler> strategyConfigHandlerMap) {
        this.strategyConfigHandlerMap = strategyConfigHandlerMap;
    }
}
