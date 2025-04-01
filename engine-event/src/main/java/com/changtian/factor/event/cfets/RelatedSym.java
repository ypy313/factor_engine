package com.changtian.factor.event.cfets;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import quickfix.field.MDEntryType;

import java.io.Serializable;
import java.util.List;
@Setter@ToString@Getter
public class RelatedSym implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 组件必传域，取值“—”
     */
    private String symbol;
    /**
     * 市场，必传
     * 2-利率互换
     * 42-标准利率互换
     * 43-标准债券远期
     */
    private String marketIndicator;
    /**
     * 债券代码，行情类型取值“2-
     * 市场深度行情”时必传，取值“
     * 107-集中匹配行情”时不传
     */
    private String securityID;
    /**
     * 报价方式，必传
     * 9-连续匹配
     * 10-集中匹配
     * 行情类型取值“市场深度行情”
     */
    private String matchType;
    /**
     * 清算速度。行情类型取值“2-
     * 市场速度行情”时必传
     */
    private String settlType;
    /**
     * 年化补偿期
     */
    private String termToMaturity;
    /**
     * 重复组个数
     */
    private String noMDEntries;
    /**
     * 重复组集合
     */
    private List<MDEntryType> mdEntryTypeList;

}
