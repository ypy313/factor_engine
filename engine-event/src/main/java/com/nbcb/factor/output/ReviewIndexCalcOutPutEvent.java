package com.nbcb.factor.output;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nbcb.factor.enums.ResultTypeEnum;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.output.forex.ForexOhlcOutputEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 所以有指标计算结果
 */
@Getter
@Setter
@ToString
public class ReviewIndexCalcOutPutEvent implements OutputEvent, Serializable, SymbolOutputEvent {
    private static final long serialVersionUID = 1L;
    private String eventId;//事件id
    private String eventName;//事件名称
    private String srcTimestamp;//来源时间，路透行情里的原始时间
    private String resTimestamp;//flink接收行情时间
    private String createTime;//结果产生时间
    private String updateTime;//更新最后时间
    private String resultType;//结果类型
    private IndexInfoResult eventData;

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public String getPkKey() {
        return null;
    }

    @Override
    public String getCompressFlag() {
        return "0";
    }

    @Override
    public String eventType() {
        return null;
    }

    @JsonIgnore
    @Override
    public String getInstrumentId() {
        return eventData.getInstanceId();
    }

    public ReviewIndexCalcOutPutEvent convert(FxOhlcResultOutputEvent ohlcInputEvent){
        this.setEventId(ohlcInputEvent.getPkKey());
        this.setEventName("OHLC_"+ohlcInputEvent.getEventData().getPeriod());
        this.setSrcTimestamp(ohlcInputEvent.getSrcTimestamp());
        this.setResTimestamp(ohlcInputEvent.getSrcTimestamp());
        this.setCreateTime(ohlcInputEvent.getCreateTime());
        this.setUpdateTime(ohlcInputEvent.getUpdateTime());
        this.setResultType(ResultTypeEnum.VALUE.getCode());
        this.setEventData(new ReviewIndexCalcOutPutEvent.IndexInfoResult());
        return this;
    }

    public ReviewIndexCalcOutPutEvent convert(ForexOhlcOutputEvent forexOhlcOutputEvent){
        this.setEventId(forexOhlcOutputEvent.getEventId());
        this.setEventName("OHLC_"+forexOhlcOutputEvent.getEventData().getPeriod());
        this.setSrcTimestamp(forexOhlcOutputEvent.getSrcTimestamp());
        this.setResTimestamp(forexOhlcOutputEvent.getResTimestamp());
        this.setCreateTime(forexOhlcOutputEvent.getCreateTime());
        this.setUpdateTime(forexOhlcOutputEvent.getUpdateTime());
        this.setResultType(ResultTypeEnum.VALUE.getCode());
        this.setEventData(new ReviewIndexCalcOutPutEvent.IndexInfoResult());
        return this;
    }


    @Getter@Setter@ToString
    public static class IndexInfoResult implements Serializable{
        private static final long serialVersionUID = 1L;

        private String source;//行情来源
        private String strategyName;//策略TA
        private String instanceId;//因子实例id
        private String instanceName;//因子名称
        private String factorName;//因子名称
        private String symbol;//货币对
        private String ric;
        private String period;//1D 1H
        private String indexCategory;//RSI
        private String summaryType;//ResultTypeEnum(DETAIL|HIS)
        private String beginTime;//bar开始时间（左闭）
        private String endTime;//bar结束时间（右开）
        private String ohlc;//ohlc计算结果
        private String calResult;//rsi值
        private String[] relationId;
    }

}

