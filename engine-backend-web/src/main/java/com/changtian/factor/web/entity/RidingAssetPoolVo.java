package com.changtian.factor.web.entity;

import com.changtian.factor.entity.riding.RidingStrategyInstance;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class RidingAssetPoolVo {
    private String strategyId;
    private String strategyName;
    private String assetPoolId;
    private String assetPoolName;
    private List<RidingStrategyInstance> strategyInstanceVoList;
}
