package com.changtian.factor;

import com.changtian.factor.monitorfactor.TradeSignal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 价差交易信息，详情字段
 */
@Setter@Getter@ToString
public class SpreadTradeSignalDetailResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private String factorName;//因子名称
    private String factorNameCn;//因子中文名称
    private String instanceId;//实例名称
    private TradeSignal signal;//交易信号
    private BigDecimal calResult;//价差结果
    private BigDecimal bestCalResult;//用最优
    private String triggerRule;//触发阀值
    private String action;//交易动作
    private String leg1;//leg1 名称
    private String leg2; //leg2 名称
    private String strategyName;//因子策略名称
    private String instanceName;//因子名称
}
