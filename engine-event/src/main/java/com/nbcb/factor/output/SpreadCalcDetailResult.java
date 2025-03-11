package com.nbcb.factor.output;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
@Getter@Setter@ToString
public class SpreadCalcDetailResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private String   factorName;
    private String   instanceId;
    private BigDecimal calResult;//用mid
    private BigDecimal bestCalResult;//用最优
    private String leg1;
    private String leg2;
    private String strategyName;//因子策略名称
    private String instanceName;//因子名称
    private Map<String, Map<String, Object>> allLegYieldMap;//所有腿原始数据

}
