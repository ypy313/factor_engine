package com.changtian.factor.output;

import com.changtian.factor.SpreadTradeSignalDetailResult;
import com.changtian.factor.common.DateUtil;
import com.changtian.factor.event.BondInputEvent;
import com.changtian.factor.event.OutputEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 价差交易信息结果
 */
@Getter@Setter@ToString
public class SpreadTradeSignalResultOutput implements OutputEvent, Serializable {
    private static final long serialVersionUID = 1L;
    private String  eventId;//时间id
    private String  eventName;//时间名称
    private String srcTimestamp;//来源时间
    private String resTimestamp;//结果时间
    private String  instrumentId;//资产
    private String  pkKey;// 压缩防重唯一key  交易信号需要压缩。按leg1 leg2 信号级别 做压缩
    private String  createTime;//结果产生时间
    private String  updateTime;//结果修改时间。压缩的时候，只更新修改时间
    private long    count;//日内产生个数
    private String  compressFlag;//压缩标记 0 未压缩 1 已压缩


    private SpreadTradeSignalDetailResult eventData;

    /**
     * 类如果要转json 如果存在有参构造 要添加无参构造
     */
    public SpreadTradeSignalResultOutput(){

    }

    public SpreadTradeSignalResultOutput(BondInputEvent event, SpreadTradeSignalDetailResult spreadTradeSignalDetailResult){
        this.eventId = event.getEventId();
        this.eventName = event.getEventName();
        this.srcTimestamp = event.getScQuoteTime();
        this.resTimestamp = event.getCreateTimestamp();
        this.instrumentId = event.getInstrumentId();
        this.eventData = spreadTradeSignalDetailResult;
        String pkKeyFormat = "%s_%s_%s_%s";
        this.pkKey = String.format(pkKeyFormat,
                spreadTradeSignalDetailResult.getFactorName(),
                spreadTradeSignalDetailResult.getLeg1(),
                spreadTradeSignalDetailResult.getLeg2(),
                spreadTradeSignalDetailResult.getSignal());
        this.updateTime = DateUtil.getSendingTime();
        this.createTime = DateUtil.getSendingTime();
        this.compressFlag = "0";//未压缩
        this.count = 1l;

    }

    @Override
    public String getResultType() {
        return TRADE_SIGNAL;
    }
}
