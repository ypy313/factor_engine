package com.nbcb.factor.event;

import com.nbcb.factor.common.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Date;

@Slf4j
public class DummyBondInputEvent implements BondInputEvent<String>{
    @Override
    public String getEventId() {
        return "TestEvent"+new Date().getTime();
    }

    @Override
    public String getEventName() {
        return "TestEvent";
    }

    @Override
    public String getCreateTimestamp() {
        return DateUtil.getNowstr();
    }

    @Override
    public String getScQuoteTime() {
        return DateUtil.getNowstr();
    }

    @Override
    public String getSource(){return "Test";}

    @Override
    public String getInstrumentId() {
        return "120110";
    }

    @Override
    public String getEventData() {
        return "Json object";
    }

    @Override
    public void setEventData(String eventData) {

    }


    @Override
    public void setInstrumentId(String instrumentId) {

    }

    @Override
    public BigDecimal calcBestBidYield() {
        log.error("DummyEvent calcBestBid");
        return null;//返回空
    }

    @Override
    public BigDecimal calcBestOfrYield() {
        log.error("DummyEvent calcBestOfr");
        return null;//返回空
    }

    @Override
    public BigDecimal calcBestBidVolume() {
        return null;
    }

    @Override
    public BigDecimal calcBestOfrVolume() {
        return null;
    }

    @Override
    public String getStrategyName() {
        return null;
    }

}
