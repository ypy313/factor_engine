package com.nbcb.factor.web.entity;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class BondConfigEntity {
    private BigDecimal id;
    private String securityId;
    private String symbol;
    private String issueSize;
    private String termToMaturityString;
    private String valuation;
    private String valuationDate;
    private String securityType;
    private String securityDec;
    private String securityTypeId;
    private String securityDecMapper;
    private String securityKey;
    private String createTime;

    public String getGroupKey() {
        return this.securityDecMapper + "." + this.securityKey;

    }
}
