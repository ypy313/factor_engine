package com.nbcb.factor.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 因子实例请求
 */
@Getter@Setter@ToString
public class StrategyConfigVo {
    private String strategyName;
    private String strategyInstanceIds;
    private String assetPoolIds;
    private String symbol;
    private String symbolType;
}
