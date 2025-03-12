package com.nbcb.factor.web.factory.impl;

import com.nbcb.factor.common.Constants;
import com.nbcb.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.nbcb.factor.web.entity.RidingAssetPoolVo;
import com.nbcb.factor.web.entity.StrategyConfigEntity;
import com.nbcb.factor.web.factory.StrategyConfigHandler;
import com.nbcb.factor.web.mapper.StrategyInstanceMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Component(Constants.RIDING_YIELD_CURVE)
public class RidingConfigHandler implements StrategyConfigHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    @Resource
    private StrategyInstanceMapper strategyInstanceMapper;

    @Override
    public List<LocalCurrencyStrategyInstanceResult> strategyConfigHandler(StrategyConfigEntity strategyConfigEntity) {
        List<Long> instanceIdList = strIdsToLongList(strategyConfigEntity.getStrategyInstanceId());
        List<RidingAssetPoolVo> resultList = strategyInstanceMapper
                .getRidingYieldCurveInstanceList(strategyConfigEntity.getStrategyName(), instanceIdList, strategyConfigEntity.getAssetPools());
        List<LocalCurrencyStrategyInstanceResult> localCurrencyStrategyInstanceResults = new ArrayList<>();
        for (RidingAssetPoolVo ridingStrategyInstanceVo : resultList) {
            LocalCurrencyStrategyInstanceResult localCurrencyStrategyInstanceResult = new LocalCurrencyStrategyInstanceResult();
            BeanUtils.copyProperties(ridingStrategyInstanceVo, localCurrencyStrategyInstanceResult);
            localCurrencyStrategyInstanceResults.add(localCurrencyStrategyInstanceResult);
        }
        return localCurrencyStrategyInstanceResults;

    }

    private List<Long> strIdsToLongList(String ids) {
        List<Long> idList = null;
        if (StringUtils.isNotEmpty(ids)) {
            try {
                String[] apIds = ids.split(",");
                idList = new ArrayList<>();
                for (String apId : apIds) {
                    idList.add(Long.parseLong(apId));
                }
            } catch (NumberFormatException e) {
                LOGGER.error(String.format("%s转Long失败", ids));
            }
        }
        return idList;
    }

    @Override
    public String getStrategyName() {
        return Constants.RIDING_YIELD_CURVE;
    }
}
