package com.changtian.factor.web.factory.impl;

import com.changtian.factor.common.Constants;
import com.changtian.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.changtian.factor.web.entity.StrategyConfigEntity;
import com.changtian.factor.web.entity.SubscriptionAssetDataVo;
import com.changtian.factor.web.factory.StrategyConfigHandler;
import com.changtian.factor.web.mapper.StrategyInstanceMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component(Constants.BOND_PRICE)
public class MarketProcessConfigHandler implements StrategyConfigHandler {
    @Autowired
    StrategyInstanceMapper strategyInstanceMapper;

    @Override
    public List<LocalCurrencyStrategyInstanceResult> strategyConfigHandler(StrategyConfigEntity strategyConfigEntity) {
        List<SubscriptionAssetDataVo> subscriptionAssetDataVos = strategyInstanceMapper.selectSubscriptionAssetData(strategyConfigEntity.getStrategyName(),
                strategyConfigEntity.getStrategyInstanceId(), strategyConfigEntity.getSymbol(),
                strategyConfigEntity.getSymbolType());
        List<LocalCurrencyStrategyInstanceResult> localCurrencyStrategyInstanceResults = new ArrayList<>();
        for (SubscriptionAssetDataVo subscriptionAssetDataVo : subscriptionAssetDataVos) {
            LocalCurrencyStrategyInstanceResult localCurrencyStrategyInstanceResult = new LocalCurrencyStrategyInstanceResult();
            BeanUtils.copyProperties(subscriptionAssetDataVo, localCurrencyStrategyInstanceResult);
            localCurrencyStrategyInstanceResults.add(localCurrencyStrategyInstanceResult);
        }
        return localCurrencyStrategyInstanceResults;
    }

    @Override
    public String getStrategyName() {
        return Constants.BOND_PRICE;
    }
}
