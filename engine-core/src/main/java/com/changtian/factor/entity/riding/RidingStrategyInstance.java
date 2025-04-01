package com.changtian.factor.entity.riding;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class RidingStrategyInstance {
    private String instanceId;//实例id
    private String instanceName;//实例名称
    private String status;//状态
    private String displayName;//显示名称
    private List<RidingInputConfig> inputConfigList;//配置参数
}
