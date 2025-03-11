package com.nbcb.factor.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
@Getter@Setter@ToString
public class StrategyNoticeDetail implements Serializable {
    private String definitionId;
    private String definitionName;
    private String assetPoolId;
    private String assetPoolName;
    private String instanceId;
    private String instanceName;
}
