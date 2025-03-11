package com.nbcb.factor.event;

import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.event.forex.FactorFxPmAllMarketData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.nbcb.factor.common.Constants.TA_PATTERN;

/**
 * 外汇行情输入事件
 */
public class FactorFxReuterMarketDataEvent implements SymbolInputEvent<FactorFxPmAllMarketData>{
    public final static String TOPIC = "FACTOR_FX_REUTER_MARKET_DATA";
    public final static String TOPIC_BLOOMBERG = "FACTOR_FX_BLOOMBERG_MARKET_DATA";

    @Getter@Setter
    private FactorFxPmAllMarketData factorFxPmAllMarketData;
    @Setter@Getter
    private String createTimestamp;//创建时间
    @Setter
    private String eventId;//事件id
    @Getter
    private String symbol;//货币对
    @Getter
    private List<String> relationId = new ArrayList<>();//关联

    public long getEventTime(){
        return this.factorFxPmAllMarketData.getEventTime();
    }
    public void setSymbol(String  symbol){
        this.symbol = this.factorFxPmAllMarketData.getSymbol();
    }

    @Override
    public void setEventData(FactorFxPmAllMarketData eventData) {
        this.factorFxPmAllMarketData = eventData;
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
        return DateUtil.format(factorFxPmAllMarketData.getEventTime(),TA_PATTERN);
    }

    @Override
    public String getSource() {
        return this.factorFxPmAllMarketData.getSource();
    }

    @Override
    public FactorFxPmAllMarketData getEventData() {
        return factorFxPmAllMarketData;
    }

    @Override
    public void setInstrumentId(String instrumentId) {

    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getEventName() {
        return "FactorFxReuterMarketDataEvent";
    }

    @Override
    public String getInstrumentId() {
        return null;
    }

}
