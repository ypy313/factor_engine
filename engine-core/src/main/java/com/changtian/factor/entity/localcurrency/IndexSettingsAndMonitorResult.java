package com.changtian.factor.entity.localcurrency;

import lombok.Getter;
import lombok.Setter;

/**
 * 本币原返回格式-指标+监控
 */
@Getter
@Setter
public class IndexSettingsAndMonitorResult {
    private String chiName;//参数解释
    private String name;//参数名称
    private int dataWindow;//参数值

    private String redUpper;//强烈上限
    private String redDown;//强烈下限
    private String orangeUpper;//推荐上限
    private String orangeDown;//推荐下限
    private String yellowUpper;//一般上限
    private String yellowDown;//一般下限
}
