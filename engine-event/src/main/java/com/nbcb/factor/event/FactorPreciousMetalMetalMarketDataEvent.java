package com.nbcb.factor.event;

import com.nbcb.factor.event.pm.FactorPreciousMetalMarketData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactorPreciousMetalMetalMarketDataEvent implements SymbolInputEvent<FactorPreciousMetalMarketData>{
    public static final String TOPIC = "FACTOR_PRECIOUS_METAIL_MARKET_DATA";
    @Getter
    private FactorPreciousMetalMarketData factorPreciousMetalMarketData;//数据
    @Setter
    private String createTimestamp;//创建时间
    @Setter
    private String eventId;//事件id
    @Getter
    private String symbol;//货币对
    private final List<String> relationId = new ArrayList<>();//关联id

    @Override
    public void setEventData(FactorPreciousMetalMarketData eventData) {
        this.factorPreciousMetalMarketData = eventData;
    }
    public void setSymbol(){
        this.symbol = factorPreciousMetalMarketData.getSymbol();
    }

    @Override
    public String getStrategyName() {
        return null;
    }

    @Override
    public List<String> getRelationId() {
        return Collections.emptyList();
    }

    @Override
    public boolean addRelationId(String relationId) {
        return this.relationId.add(relationId);
    }

    @Override
    public String getCreateTimestamp() {
        return "";
    }

    @Override
    public String getScQuoteTime() {
        return factorPreciousMetalMarketData.getEventTime();
    }

    @Override
    public String getSource() {
        return this.factorPreciousMetalMarketData.getDataSource();
    }

    @Override
    public Object getEventData() {
        return factorPreciousMetalMarketData;
    }

    @Override
    public void setInstrumentId(String instrumentId) {
        this.symbol = instrumentId;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getEventName() {
        return "FactorPreciousMetalMetalMarketData";
    }

    @Override
    public String getInstrumentId() {
        return this.symbol;
    }

    public String getEventTime(){
        return this.factorPreciousMetalMarketData.getEventTime();
    }
}
