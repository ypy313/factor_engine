package com.nbcb.factor.web.entity;

import lombok.Data;

@Data
public class FactorInstanceDefinitionSymbol {
    private String id;
    //策略id
    private String definition;
    //指标
    private String indexCategory;
    //货币对
    private String symbol;
}
