package com.changtian.factor.web.job.basic.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.changtian.factor.common.AllRedisConstants;
import com.changtian.factor.common.RedisUtil;
import com.changtian.factor.web.entity.FactorInstanceConfigData;
import com.changtian.factor.web.entity.FactorInstanceDefinitionSymbol;
import com.changtian.factor.web.entity.FactorPmRedisSymbolValue;
import com.changtian.factor.web.job.basic.FactorBasicJob;
import com.changtian.factor.web.service.FactorPmSymbolService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PmSymbolSynJob implements FactorBasicJob {
    private final RedisUtil redisUtil = RedisUtil.getInstance();
    @Autowired
    private FactorPmSymbolService factorPmSymbolService;


    /**
     * 查询数据库，将数据同步到redis中
     */
    private List<String> selectSynRedis(Long definitionId){
        //查询数据库中贵金属中的所有指标与货币对
        List<FactorInstanceDefinitionSymbol> allIndexCategorySymbol =
                factorPmSymbolService.getAllIndexCategorySymbol(definitionId);
        //过滤获取数据库中贵金属对应的指标
        List<String> indexCategoryList = allIndexCategorySymbol.stream()
                .map(FactorInstanceDefinitionSymbol::getIndexCategory)
                .filter(e->!"qsx".equals(e) && !"QSX".equals(e)).distinct().collect(Collectors.toList());
        //存储贵金属对应的指标
        redisUtil.setRiding(AllRedisConstants.FACTOR_PM_INDEX_CATEGORY, JSONUtil.toJsonStr(indexCategoryList));
        //拼接格式definition_indexCategory_symbol
        List<String> allDefinitionIndexSymbol = allIndexCategorySymbol.stream()
                .map(e -> e.getDefinition() + "_" + e.getIndexCategory() + "_" + e.getSymbol())
                .collect(Collectors.toList());
        if (allDefinitionIndexSymbol.isEmpty()) {
            XxlJobLogger.log("查询数据库为null");
        }
        //将查到的数据库数据存入reids中，将其改为Y
        allDefinitionIndexSymbol.forEach(symbol->{
            redisUtil.setString(AllRedisConstants.FACTOR_PM_SYMBOL,symbol,JSONUtil.toJsonStr(new FactorPmRedisSymbolValue()));
        });
        return allDefinitionIndexSymbol;

    }

    @Override
    public Object executeJob(Object param) {
        Long definitionId = 100006L;
        List<String> allSymbolList = selectSynRedis(definitionId);
        if (CollUtil.isEmpty(allSymbolList)) {
            XxlJobLogger.log("第一次数据库同步redis失败");
            return ReturnT.FAIL;
        }
        //获取redis中数据
        Map<String, String> symbolMap = redisUtil.getString(AllRedisConstants.FACTOR_PM_SYMBOL);
        //过滤获取未添加的资产definitionId indexCategory symbol
        List<String> symbolKeySet = new ArrayList<>();
        Set<Map.Entry<String, String>> entries = symbolMap.entrySet();
        entries.forEach(k->{
            String value = k.toString();
            String[] split = value.split("=");
            if (split[1].contains("N")) {
                symbolKeySet.add(split[0]);
            }
        });
        if (CollUtil.isEmpty(symbolKeySet)) {
            XxlJobLogger.log("没有需要更新的指标资产symbol");
            return ReturnT.FAIL;
        }
        //获取贵金属需要插入的configInfoId的指标id
        List<FactorInstanceDefinitionSymbol> getConfigInfoId = factorPmSymbolService.selectConfigInfoId(definitionId);
        if (CollUtil.isEmpty(symbolKeySet)) {
            XxlJobLogger.log("获取贵金属需要插入的configInfoId的指标id失败");
            return ReturnT.FAIL;
        }
        //找寻最大的configDataId
        Long maxConfigDataId = factorPmSymbolService.getMaxConfigDataId();
        List<FactorInstanceConfigData> addSymbolList = new ArrayList<>();
        for (int j = 0; j < symbolKeySet.size(); j++) {
            String[] definitionIndexSymbols = symbolKeySet.get(j).split("_");
            FactorInstanceDefinitionSymbol configInfoId = getConfigInfoId.stream()
                    .filter(e -> e.getIndexCategory().equals(definitionIndexSymbols[1]))
                    .collect(Collectors.toList()).get(0);
            //获取该指标中最大的展示顺序值
            Integer displayOrder = factorPmSymbolService.selectMaxDisplayOrder(Long.valueOf(configInfoId.getId()));
            FactorInstanceConfigData factorInstanceConfigData = new FactorInstanceConfigData.FactorInstanceConfigDataBuilder().build();
            factorInstanceConfigData.setConfigInfoId(Long.valueOf(configInfoId.getId()));
            factorInstanceConfigData.setDataKey(definitionIndexSymbols[2]);
            factorInstanceConfigData.setDataName(definitionIndexSymbols[2]);
            factorInstanceConfigData.setDataType("String");
            factorInstanceConfigData.setDefauleValue(definitionIndexSymbols[2]);
            factorInstanceConfigData.setDisplayOrder(displayOrder+1);
            factorInstanceConfigData.setId(++maxConfigDataId);
            addSymbolList.add(factorInstanceConfigData);
        }
        factorPmSymbolService.insertSymbolList(addSymbolList);
        //第二次查询数据库，将数据同步到redis中，保证数据一致性
        List<String> data = selectSynRedis(definitionId);
        if (CollUtil.isEmpty(data)) {
            XxlJobLogger.log("第二次数据库同步redis失败");
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }
}
