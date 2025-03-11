package com.nbcb.factor.event.cdh;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
@Getter@Setter@ToString
public class BondBestOfferDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    private String bondCode;//债券代码
    private String listedMarket;//债券发行市场 CIB：银行间/SSE：上清所 /SZE:深交所
    private String bidID;//报价bid方的id值，对于国利/平安/信唐可用于标记某笔报价 当无值时表示bid缺失
    private BigDecimal bidYield;//报价的收益率
    private BigDecimal bidNetPrice;//报价的净价
    private int bigBargainFlag;//1、需请示 2:必须请示，无值表示不可议价
    private int bidRelationFlag;//1 表示OCO 2 表示打包，无值表示无oco无打包
    private int bidExerciseFlag;//0 表示行权收益率 1或空:到期收益率
    private String bidComment;//报价的其他注释（UTF-8编码）
    private String bidSize;//当134号域（BidSize）为多个量合并时，这里显示每个分量（20181015）
    private String bidPriceType;//0-意向；1-净价；2-全价；3-收益率；4-利差；8-经纪商未指定
    private String bid_ss_detect;//森浦实时数据检测状态1：表示正常数据 2：表示森浦
    private String ofrID;//报价offer方的id值，对于国利/平安/信唐可用于标记某笔报价 当无值时表示offer缺失
    private BigDecimal ofrYield;//报价的收益率
    private BigDecimal ofrNetPrice;//报价的净价
    private int ofrBargainFlag;//1、需请示 2、必须请示，无值表示不可议价
    private int ofrRelationFlag;//1、表示OCO 2、表示打包，无值表示无OCO无打包
    private int ofrExerciseFlag;//0 表示行权收益率 1 或空：到期收益率
    private String ofrComment;//报价的其他注释（UTF-8编码
    private String ofrSize;//当134号域（BidSize）为多个量合并时，这里显示每个分量
    private String ofrPriceType;//0-意向 1-净价 2-全价 3-收益率 4-利差 8-经纪商未指定
    private String ofr_ss_detect;//森浦实时数据检测状态 1：表示正常数据 2：表示森浦



}
