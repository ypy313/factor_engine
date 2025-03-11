package com.nbcb.factor.event;

import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.output.OhlcDetailResult;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ForexOhlcInputEvent implements SymbolInputEvent<OhlcDetailResult> {
    private OhlcDetailResult ohlcDetailResult;//数据
    @Getter
    private String srcTimestamp;//行情来源时间
    private final String createTimestamp;//创建时间
    @Setter
    private String eventId;//事件id
    @Getter
    private String symbol;//货币对
    private String instrumentId = null;//资产id
    private final List<String> relationId = new ArrayList<>();

    /**
     * 初始化 output 转input
     */
    public ForexOhlcInputEvent(OutputEvent outputEvent){
        this.ohlcDetailResult = outputEvent.getEventData();
        this.srcTimestamp = outputEvent.getSrcTimestamp();
        this.symbol = ohlcDetailResult.getSymbol();
        this.createTimestamp = DateUtil.getNowstr();
    }

    @Override
    public void setEventData(OhlcDetailResult eventData) {
        this.ohlcDetailResult = eventData;
    }

    @Override
    public String getStrategyName() {
        return this.ohlcDetailResult.getStrategyName();
    }

    @Override
    public List<String> getRelationId() {
        return this.relationId;
    }

    @Override
    public boolean addRelationId(String relationId) {
        this.relationId.add(relationId);
        return true;
    }

    @Override
    public String getCreateTimestamp() {
        return this.createTimestamp;
    }

    @Override
    public String getScQuoteTime() {
        return this.srcTimestamp;
    }

    @Override
    public String getSource() {
        return this.ohlcDetailResult.getSource();
    }

    @Override
    public Object getEventData() {
        return this.ohlcDetailResult;
    }

    @Override
    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    @Override
    public String getEventId() {
        return "FOREX_OHLC_INPUT_EVENT_"+this.eventId;
    }

    @Override
    public String getEventName() {
        return "FOREX_OHLC_INPUT_EVENT_OUT_TO_INPUT";
    }

    @Override
    public String getInstrumentId() {
        return this.instrumentId;
    }
}
