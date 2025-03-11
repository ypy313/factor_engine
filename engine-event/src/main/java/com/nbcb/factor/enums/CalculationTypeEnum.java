package com.nbcb.factor.enums;

import lombok.Getter;

/**
 * 计算类型 用来识别常规指标计算。自定义计算（点数图。高斯模型等）
 */
@Getter
public enum CalculationTypeEnum {
    GENERAL("GENERAL","通用指标计算"),
    GMM("GMM","高斯混合模型计算"),
    POINT_FIGURE("QSX","点数图计算"),
    TREND_LINE("qsx","趋势线计算")
    ;
    private final String name;
    private final String description;

    CalculationTypeEnum(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

}
