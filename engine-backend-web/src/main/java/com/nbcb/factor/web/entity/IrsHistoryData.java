package com.nbcb.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * irs历史数据
 */
@Getter
@Setter
@ToString
public class IrsHistoryData {
    private Long id;
    private String referenceRate;
    private String swapTerm;//互换期限
    private String valueDate;//起息日
    private String dueDate;//到期日
    private String startDate;//区间开始日期
    private String endDate;//区间结束日期
    private String interestFixedDate;//利率确定日（定盘日期）
    private String interestAccrualDays;//计息天数
    private String interestRate;//利率
    private String createdBy;//创建人
    private String createdTime;//创建时间
    private String updatedBy;
    private String updatedTime;
}
