package com.nbcb.factor.web.entity.po;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 曲线数据PO
 */
@Getter@Setter@ToString
public class RateSwapClosingCurvePO {
    //ID
    private String id;
    //曲线id
    private String curveId;
    //曲线名称
    private String curveName;
    //合约id
    private String securityId;
    //曲线全程
    private String symbol;
    //期限
    private String term;
    //利率
    private BigDecimal rate;
    //日期
    private String tradeDt;
    //创建时间
    private Date createTime;
}
