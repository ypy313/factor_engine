package com.changtian.factor.event.cdh;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter@Setter@ToString
public class BondDealDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * "NET_PRICE" 净价
     * “NEXTERFUND” 行权收益率
     * “YTM” 到期收益率
     * “FULL_PRICE” 全价
     * “SPREAD” 利差
     * 未提供一般表示到期收益率
     */
    private String price_type;
    /**
     * 清算速度（“T+0"或者”T+1“）
     */
    private String clear_speed;
    /**
     * 全价
     */
    private BigDecimal full_price;
    /**
     * 原始价格
     */
    private BigDecimal original_price;
    /**
     * 债券代码
     */
    private String bond_code;
    /**
     * 债券发行市场，CIB:银行间/
     * SSE:上交所 / SZE：深交所
     * 注：bond_code +listed_market提供的信息等
     * 价于SecurityID(48号域)
     */
    private String listed_market;
    /**
     * 森浦实时数据检测状态
     * 1：表示正常数据 2：表示森浦
     */
    private String ss_detect;
    /**
     * 新增状态 0：表示行权收益率 1或空：到期收益率
     */
    private String exercise_flag;
}
