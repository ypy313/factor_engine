package com.nbcb.factor.web.mapper;

import com.nbcb.factor.web.entity.FactorInstanceConfigData;
import com.nbcb.factor.web.entity.FactorInstanceDefinitionSymbol;

import java.util.List;

public interface FactorPmSymbolMapper {
    int insertSymbolList(FactorInstanceConfigData factorInstanceConfigData);

    List<String> getAllSymbolList(Long definitionId);

    List<FactorInstanceDefinitionSymbol> selectConfigInfoId(Long definitionId);

    Integer selectMaxDisplayOrder(Long configInfoId);

    Long getMaxConfigDataId();

    List<FactorInstanceDefinitionSymbol> getAllIndexCategorySymbol(Long definitionId);
}
