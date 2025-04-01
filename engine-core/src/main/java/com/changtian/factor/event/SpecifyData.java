package com.changtian.factor.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 指定执行时Data
 */
@Getter@Setter@ToString
public class SpecifyData {
    private String definitionId;//模型id
    private String definitionName;//模型名称
    private String assetPoolId;//资产池id
    private String assetPoolName;//资产池名称
    private String instanceId;//实例id
    private String instanceName;//实例名称
}
