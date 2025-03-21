package com.nbcb.factor.output;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 收益率曲线
 */
@Getter
@Setter
@ToString
public class RidingYieldCurveDetailResult {
    private String flag;
    private BigDecimal[] xAxis;
    private BigDecimal[] yAxis;
}
