package com.nbcb.factor.web.service;

import com.nbcb.factor.entity.StrategyConfigVo;
import com.nbcb.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.nbcb.factor.web.entity.StrategyConfigEntity;
import com.nbcb.factor.web.factory.StrategyConfigFactory;
import com.nbcb.factor.web.factory.StrategyConfigHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class StrategyInstanceService {

    @Autowired
    StrategyConfigFactory strategyConfigFactory;

    /**
     * 根据因子策略名称，获取所有本因子策略的实例配置列表，供系统初始化使用。
     * 参考json格式：
     * String configjson = "{" +
     * "\"strategyName\":\"SpreadOnePoolStrategy\"," +
     * "\"strategyInstanceName\":\"SpreadOnePoolStrategy.111\"," +
     * "\"marketDataSetting\":{\"leg1\":\"220203.IB\",\"leg2\":\"101800431.IB\",\"dataSource\":" +
     * "\"All\",\"dataDirect\":\"bid\",\"preLoad\":10}," +
     * "\"monitorSetting\":[" +
     * "{\"name\":\"realSpread\",\"chiName\":\"实时价差\",\"dataWindow\":-1,\"yellowUpper\":0,\"yellowDown\":0,\"orangeUpper\":0,\"orangeDown\":0,\"redUpper\":0,\"redDown\":0}," +
     * "{\"name\":\"ChinaBondSpreadPercentile\",\"chiName\":\"中债历史价差\",\"dataWindow\":0,\"yellowUpper\":0,\"yellowDown\":0,\"orangeUpper\":0,\"orangeDown\":0,\"redUpper\":0,\"redDown\":0}," +
     * "{\"name\":\"XX\",\"chiName\":\"自定义\",\"dataWindow\":0,\"yellowUpper\":0,\"yellowDown\":-1,\"orangeUpper\":0,\"orangeDown\":-1,\"redUpper\":0,\"redDown\":-1}" +
     * "]" +
     * "};";
     *
     */
    public List<LocalCurrencyStrategyInstanceResult> getConfigJson(StrategyConfigVo strategyConfigVo) {
        List<String> assetPools = new ArrayList<>();
        if (StringUtils.isNotEmpty(strategyConfigVo.getAssetPoolIds())) {
            //当assetPoolIds不为空时，则只处理
            assetPools = Arrays.asList(strategyConfigVo.getAssetPoolIds().split(","));
        }

        //所有参数赋值
        List<LocalCurrencyStrategyInstanceResult> localCurrencyStrategyInstanceResponses = new ArrayList<>();
        StrategyConfigHandler strategyConfigHandler = strategyConfigFactory
                .getStrategyConfigHandlerMap().get(strategyConfigVo.getStrategyName());
        if (strategyConfigHandler != null) {
            StrategyConfigEntity strategyConfigEntity = new StrategyConfigEntity();
            strategyConfigEntity.setAssetPools(assetPools);
            strategyConfigEntity.setStrategyName(strategyConfigVo.getStrategyName());
            strategyConfigEntity.setStrategyInstanceId(strategyConfigVo.getStrategyInstanceIds());
            strategyConfigEntity.setSymbol(strategyConfigVo.getSymbol());
            strategyConfigEntity.setSymbolType(strategyConfigVo.getSymbolType());
            localCurrencyStrategyInstanceResponses = strategyConfigHandler.strategyConfigHandler(strategyConfigEntity);
        }
        return localCurrencyStrategyInstanceResponses;
    }
}
