package com.nbcb.factor.event.cmds;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
@Getter@Setter@ToString
public class MDEntryType implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 类型
     */
    private String mdRntryType;
    /**
     * f-前收盘净价
     * h-前加权平均净价
     * e-最新净价
     * 4-开盘净价
     * 7-最高净价
     * 8-最低净价
     * 5-收盘净价
     * g-加权平均净价
     * B-成交量
     * a-涨跌幅
     * i-前收盘收益率
     * j-前加权平均收益率
     * k-开盘收益率
     * m-最高收益率
     * p-加权平均收益率
     * n-最低收益率
     * o-收盘收益率
     * 6-other
     * j-净价涨跌
     * N-收益率涨跌
     */
    private String mdEntryRx;
    /**
     * 成交笔数
     */
    private String transactionNum;
}
