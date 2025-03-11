package com.nbcb.factor.event;

import com.nbcb.factor.common.DateUtil;

import java.util.List;

public class StrategyCommandSymbolInputEvent implements SymbolInputEvent<StrategyCommand>{
    public static final String TOPIC = "FACTOR_ANALYZE_STRATEGY_CMD";
    private StrategyCommand strategyCommand;

    @Override
    public void setEventData(StrategyCommand eventData) {
        this.strategyCommand = eventData;
    }

    @Override
    public String getStrategyName() {
        return strategyCommand.getStrategyName();
    }

    @Override
    public List<String> getRelationId() {
        return null;
    }

    @Override
    public boolean addRelationId(String relationId) {
        return false;
    }

    @Override
    public String getCreateTimestamp() {
        return DateUtil.getNowstr();
    }

    @Override
    public String getScQuoteTime() {
        return DateUtil.getSendingTime();
    }

    @Override
    public String getSource() {
        return "CMD";
    }

    @Override
    public Object getEventData() {
        return this.strategyCommand;
    }

    @Override
    public void setInstrumentId(String instrumentId) {

    }

    @Override
    public String getEventId() {
        return "StrategyCommandEvent_"+ DateUtil.getNowstr();
    }

    @Override
    public String getEventName() {
        return "StrategyCommandEvent";
    }

    @Override
    public String getInstrumentId() {
        return strategyCommand.getCmd();
    }
}
