package com.nbcb.factor.entity.forex;

import com.nbcb.factor.monitorfactor.TradeSignal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sun.security.mscapi.PRNG;

import java.util.Arrays;
import java.util.List;
@Setter@Getter@ToString
public class MonitorSetting {
    /**
     * 盯盘
     */
    public static final int MONITOR_MODEL_DETAIL=0;
    /**
     * 收盘
     */
    public static final int MONITOR_MODEL_HISTORY=1;
    /**
     * 信号持续时间间隔
     */
    public static final int MONITOR_MODEL_DELAY_DETAIL=2;
    private String monitorId;//监控表达式id
    private String monitorName;//监控名称
    private String expression;//监控表达式
    private TradeSignal signal;//信号等级
    private int monitorOrder;//排序
    private String action;//交易建议
    private Integer monitorModel;//0:盯盘 |1:收盘
    private String signalPopup;//pop弹窗，noPop不弹窗
    private String reminderCycle;//周期提醒-PeriodicReminder 普通提醒-normalReminder
    private Long reminderInterval;//提醒间隔，毫秒值
    private String sendingText;//是否推送短信 send表示发送 noSend表示不发送（默认）
    private String signalDuration;//信号持续时间

    public int getMonitorModel() {
        if (monitorModel == null) {
            monitorModel = MONITOR_MODEL_DETAIL;
        }
        return monitorModel;
    }

    /**
     * 获取所有监控模式
     */
    public static List<Integer> getAllModelList(){
        return Arrays.asList(MONITOR_MODEL_DETAIL,MONITOR_MODEL_HISTORY,MONITOR_MODEL_DELAY_DETAIL);
    }
}
