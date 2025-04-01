package com.changtian.factor.web.mapper;

import com.changtian.factor.web.entity.FactorInstanceConfigData;
import com.changtian.factor.web.entity.FactorInstanceDefinitionSymbol;

import java.util.List;

public interface FactorPmSymbolMapper {
    int insertSymbolList(FactorInstanceConfigData factorInstanceConfigData);

    List<String> getAllSymbolList(Long definitionId);

    List<FactorInstanceDefinitionSymbol> selectConfigInfoId(Long definitionId);

    Integer selectMaxDisplayOrder(Long configInfoId);

    Long getMaxConfigDataId();

    List<FactorInstanceDefinitionSymbol> getAllIndexCategorySymbol(Long definitionId);
}
