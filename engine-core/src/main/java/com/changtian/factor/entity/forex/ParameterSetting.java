package com.changtian.factor.entity.forex;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 指标参数设置
 */
@Getter@Setter@ToString
public class ParameterSetting {
    private String indexCategory;//指标类别
    private String indexName;//指标名称
    private String type;//参数类型
    private String minPreferences;//最小指标参数设置
    private String maxPreferences;//最大指标参数设置
    private String indexPreferences;//指标参数
    private String formula;//指标计算公式
}
