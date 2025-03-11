package com.nbcb.factor.entity.riding;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
@Getter
@Setter
@ToString
public class RidingAssetPool {
    private String strategyId;//策略id
    private String strategyName;//策略名称
    private String assetPoolId;//资产池id
    private String assetPoolName;//资产池名称
    private List<RidingStrategyInstance> strategyInstanceVoList;

    public String getAssetPoolIdAndName(){
        return getAssetPoolId()+"__"+getAssetPoolName();
    }
}
