package com.changtian.factor.monitor.signal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 信号数据记录数据
 */
@Getter@Setter@ToString
public class SignalInfoEntity {
    //信号产生必须满足信号（srcTime>signalStartTime +5min）
    private String signalStartTime;//信号产生的开始时间
    //信号在该时间端是否发生过
    private boolean signalSendFlag;
}
