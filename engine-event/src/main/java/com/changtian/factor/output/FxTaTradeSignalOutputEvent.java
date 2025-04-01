package com.changtian.factor.output;

import com.changtian.factor.enums.ResultTypeEnum;
import com.changtian.factor.event.OutputEvent;
import com.changtian.factor.event.SymbolOutputEvent;
import com.changtian.factor.monitorfactor.TradeSignal;
import com.changtian.factor.output.forex.ForexOhlcOutputEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * 监控rsi计算的信号实体
 */
@Getter@Setter@ToString
public class FxTaTradeSignalOutputEvent implements OutputEvent , Serializable, SymbolOutputEvent {
    private static final long serialVersionUID = 1L;

    private String eventId;//事件ID
    private String eventName;//事件名称
    private String srcTimestamp;//来源时间，路透行情里的原始时间
    private String resTimestamp;//flink接收行情时间
    private String createTime;//结果产生时间
    private String updateTime;//更新最后时间
    private String resultType;//结果类型
    private SignalDetailResult eventData;

    private long count;//日内产生个数

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
        return "";
    }

    @Override
    public String getInstrumentId() {
        return eventData.getInstanceId();
    }
    @Override
    public long getCount(){return count;}

    public FxTaTradeSignalOutputEvent convert(FxOhlcResultOutputEvent ohlcInputEvent) {
        this.setEventId(ohlcInputEvent.getPkKey());
        this.setEventName("OHLC_"+ohlcInputEvent.getEventData().getPeriod());
        this.setSrcTimestamp(ohlcInputEvent.getSrcTimestamp());
        this.setResTimestamp(ohlcInputEvent.getResTimestamp());
        this.setCreateTime(ohlcInputEvent.getCreateTime());
        this.setUpdateTime(ohlcInputEvent.getUpdateTime());
        this.setResultType(ResultTypeEnum.TRADE_SIGNAL.getCode());
        this.setEventData(new SignalDetailResult());
        return this;
    }

    public FxTaTradeSignalOutputEvent convert(ForexOhlcOutputEvent forexOhlcOutputEvent) {
        this.setEventId(forexOhlcOutputEvent.getEventId());
        this.setEventName("OHLC_"+forexOhlcOutputEvent.getEventData().getPeriod());
        this.setSrcTimestamp(forexOhlcOutputEvent.getSrcTimestamp());
        this.setResTimestamp(forexOhlcOutputEvent.getResTimestamp());
        this.setCreateTime(forexOhlcOutputEvent.getCreateTime());
        this.setUpdateTime(forexOhlcOutputEvent.getUpdateTime());
        this.setResultType(ResultTypeEnum.TRADE_SIGNAL.getCode());
        this.setEventData(new SignalDetailResult());
        return this;
    }
    @Getter@Setter@ToString
    public static class SignalDetailResult implements Serializable{
        private static final long serialVersionUID = 1L;

        private String source;//行情来源
        private String strategyName;//策略TA
        private String instanceId;//因子实例
        private String instanceName;//因子名称
        private String monitorId;//监控表达式id
        private String monitorName;//监控名称
        private String assetPoolName;//指标组名称
        private String symbol;//货币对
        private String ric;
        private String period;//1D 1h..
        private String indexCategory;//RSI
        private String summaryType;//ResultTypeEnum(DETAIL|HIS)
        private String beginTime;//bar开始时间（左闭 ）
        private String endTime;//bar结束时间（右开）
        private TradeSignal signal;//STRONG
        private Map<String,Object> calResult;
        private String triggerRule;//监控表达式
        private String action;//买入
        private String signalPopup;//是否弹窗
        private String reminderCycle;//提醒周期
        private Long reminderInterval;//提醒间隔，毫秒值
        private String sendingText;//是否推送短信
        private String pushStartTime;//信号推送开始时间
        private String pushEndTime;//信号推送结束时间
        private String[] relationId;
    }
}
