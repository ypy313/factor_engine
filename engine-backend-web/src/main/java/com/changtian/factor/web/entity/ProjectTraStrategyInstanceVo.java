package com.changtian.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 本币项目改造实例
 */
@Getter
@Setter
@ToString
public class ProjectTraStrategyInstanceVo {
    private String assetPoolName;
    private String assetPoolId;
    private String instanceId;
    private String instanceName;
    private String strategyId;//策略id
    private String strategyName;//策略名称
    private List<ProjectTraPropertyStrategyInputVo> projectTraPropertyStrategyInputVoList;//计算参数信息
}
