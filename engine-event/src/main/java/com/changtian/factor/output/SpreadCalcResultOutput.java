package com.changtian.factor.output;

import com.changtian.factor.common.DateUtil;
import com.changtian.factor.event.BondInputEvent;
import com.changtian.factor.event.OutputEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
@Getter@Setter@ToString
public class SpreadCalcResultOutput implements OutputEvent, Serializable {
    private static final long serialVersionUID = 1L;
    private String  eventId;//时间id
    private String  eventName;//时间名称
    private String srcTimestamp;//来源时间
    private String resTimestamp;//接收行情的时间
    private String  instrumentId;//资产
    private String  pkKey;// 压缩防重唯一key  交易价差，不需要压缩  事件id即可
    private String  createTime;//处理结果的时间
    private String  updateTime;//结果修改时间。
    private long    count;//日内产生个数
    private String  compressFlag;//压缩标记 0 未压缩 1 已压缩

    private SpreadCalcDetailResult eventData;


    /**
     * 类如果要转json 如果存在有参构造 要添加无参构造
     */
    public SpreadCalcResultOutput(){

    }

    public SpreadCalcResultOutput(BondInputEvent event, SpreadCalcDetailResult detailResult){
        this.eventId = event.getEventId();
        this.eventName = event.getEventName();
        this.srcTimestamp = event.getScQuoteTime();//原始时间
        this.resTimestamp = event.getCreateTimestamp();//接收行情的时间
        this.instrumentId = event.getInstrumentId();
        this.eventData = detailResult;

        String pkKeyFormat = "%s_%s";
        this.pkKey = String.format(pkKeyFormat,
                event.getEventId(),detailResult.getInstanceName());
        this.updateTime = DateUtil.getSendingTime();//处理完成的时间
        this.createTime = DateUtil.getSendingTime();//处理完成的时间
        this.compressFlag = "0";//未压缩
        this.count = 1l;
    }

    @Override
    public String getResultType() {
        return SPREAD_VALUE;
    }
}
