package com.nbcb.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter@Getter@ToString
public class ProjectTraConfigKeyGroupVo {
    private String configId;
    private String configKey;
    private String propertyKey;
    private String propertyType;
    private String propertyValue;
    private Integer displayOrder;
}
