package com.changtian.factor.event.cmds;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
@Setter@Getter@ToString
public class NoMDType implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 行情类型
     * R-本币市场行情
     */
    private String mdType;
    /**
     * 4-现券买卖
     */
    private String marketIndicator;
    /**
     * 4-现券方式
     * 0-汇总
     */
    private String tradeMethod;
    /**
     * 交易品种类型
     */
    private String mdSubType;
    /**
     * 重复组次数
     */
    private String noRelatedSym;
    /**
     * NoRelatedSym集合
     */
}

