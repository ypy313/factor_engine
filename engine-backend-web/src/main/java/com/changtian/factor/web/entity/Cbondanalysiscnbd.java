package com.changtian.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
@Getter
@Setter
@ToString
public class Cbondanalysiscnbd implements Serializable {
    private static final long serialVersionUID = 1L;
    private String objectId;//对象id
    private String sInfoWindcode;//wind代码
    private String tradeDt;//交易日期
    private BigDecimal bAnalMatuCnbd;//代偿期
    private BigDecimal bAnalDirtyCnbd;//估价全价
    private BigDecimal bAnalAccrintCnbd;//应计利息
    private BigDecimal bAnalNetCnbd;//估价净价
    private BigDecimal bAnalYieldCnbd;//估价收益率
    private BigDecimal bAnalModiduraCnbd;//估价修正久期
    private BigDecimal bAnalCnvxtyCnbd;//估价凸性
    private BigDecimal bAnalVobpCnbd;//估价基点价值
    private BigDecimal bAnalSprduraCnbd;//估价利差久期
    private BigDecimal bAnalSprcnxtCnbd;//估价利差凸性
    private BigDecimal bAnalAccrintcloseCnbd;//日终应计利息
    private BigDecimal bAnalPrice;//市场全价
    private BigDecimal bAnalNetprice;//市场净价
    private BigDecimal bAnalYield;//市场收益率（%）
    private BigDecimal bAnalModifieddduration;//市场修正久期
    private BigDecimal bAnalConvexity;//市场凸性
    private BigDecimal bAnalBpvalue;//市场基点价值
    private  BigDecimal bAnalSduration;//市场利差久期
    private BigDecimal bAnalScnvxty;//市场利差凸性
    private BigDecimal bAnalInterestdurationCnbd;//估价利差久期
    private BigDecimal bAnalInterestcnvxtyCnbd;//估价利率凸性
    private BigDecimal bAnalInterestduration;//市场利率久期
    private BigDecimal bAnalInterestcnvxty;//市场利率凸性
    private BigDecimal bAnalPriceCnbd;//日终估价全价
    private BigDecimal bAnalBpyield;//点差收益率（%）
    private BigDecimal bAnalExchange;//流通场所
    private BigDecimal bAnalCredibility;//可信度
    private String bAnalResidualPri;//剩余本金
    private BigDecimal bAnalExerciseRate;//估算的行权后票面利率
    private Integer bAnalPriority;//优先级
}
