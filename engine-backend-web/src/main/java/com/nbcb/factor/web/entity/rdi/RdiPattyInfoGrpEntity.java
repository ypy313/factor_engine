package com.nbcb.factor.web.entity.rdi;

import lombok.Data;

import java.math.BigDecimal;

/**
 * rdi机构信息实体
 */
@Data
public class RdiPattyInfoGrpEntity {
    private BigDecimal id;
    private String cnFullName;//机构信息
    private String cnShortName;//机构简称
    private String enFullName;
    private String enShortName;
    private String partyCode21;//21位机构编码
    private String partyCode6;//6位机构编码
    private String updateTime;//更新时间
}
