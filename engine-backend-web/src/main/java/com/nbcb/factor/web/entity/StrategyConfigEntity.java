package com.nbcb.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 策略配置查询实体
 */
@Getter@Setter@ToString
public class StrategyConfigEntity {
    private List<String> assetPools;
    private String strategyName;
    private String strategyInstanceId;
    private String symbol;
    private String symbolType;
}
