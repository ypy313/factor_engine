package com.changtian.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 本币实例计算参数信息
 */
@Setter
@Getter
@ToString
public class ProjectTraPropertyStrategyInputVo {
    private String instanceId;
    private String displayName;//实例名称
    private String indexCategory;//指标类别
    private String configName;
    private String configKey;
    private String configType;
    private Integer rowNum;
    private String indicatorExec;//执行指标公式
    private List<ProjectTraConfigKeyGroupVo> projectTraConfigKeyGroupVoList;//配置值分组
}
