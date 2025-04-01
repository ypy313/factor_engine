package com.changtian.factor.entity.localcurrency;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 本币监控表达式
 */
@Getter@Setter@ToString
public class LocalCurrencyMonitorSetting {
    private String monitorName;//监控名称
    private String upper;//一般
    private String down;//推荐
    private String signal;//强烈
}
