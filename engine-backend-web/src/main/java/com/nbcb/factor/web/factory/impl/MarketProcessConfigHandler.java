package com.nbcb.factor.web.factory.impl;

import com.nbcb.factor.common.Constants;
import com.nbcb.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.nbcb.factor.web.entity.StrategyConfigEntity;
import com.nbcb.factor.web.entity.SubscriptionAssetDataVo;
import com.nbcb.factor.web.factory.StrategyConfigHandler;
import com.nbcb.factor.web.mapper.StrategyInstanceMapper;
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
