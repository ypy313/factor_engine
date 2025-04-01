package com.changtian.factor.web.entity.rdi;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RdiBondSecurityMasterEntity {
    private BigDecimal id;
    private String securityId;
    private String marketSuffix;
    private String securitySuperType;
    private String securityDec;
    private String securityType;
    private String securityTypeId;
    private String symbol;
    private String issueSize;
    private String par;
    private String maturityDate;
    private String termToMaturityString;
    private String issuer;
    private String creditRating;
    private String couponRate;
    private String text;
    private String cdcValuePx;
    private String accruedInterestAmt;
    private String dayCount;
    private String circulationSize;
    private String latest;
    private String updateTime;
}
