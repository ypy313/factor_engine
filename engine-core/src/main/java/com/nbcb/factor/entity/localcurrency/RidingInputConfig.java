package com.nbcb.factor.entity.localcurrency;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据源设置
 */@Getter@Setter@ToString
public class RidingInputConfig {
    private String configKey;//配置key
    private String configName;//配置名称
    private String configType;//配置类型
    private String propertyKey;//属性key
    private String propertyName;//属性名称
    private String propertyType;//属性类型
    private String propertyValue;//属性值
    private int rowNum;//行号
    private int displayOrder;//排序
}
