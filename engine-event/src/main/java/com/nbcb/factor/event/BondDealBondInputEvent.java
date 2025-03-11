package com.nbcb.factor.event;

import com.nbcb.factor.event.cdh.BondDeal;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * broker成交报文
 */
@Slf4j
public class BondDealBondInputEvent implements BondInputEvent<BondDeal>{
    private BondDeal bondDeal = null;
    private String instrumentId = null;
    private String eventId;
    private String createTimestamp ;

    @Override
    public String getEventId() {
        return "BondDealEvent_"+ eventId;
    }

    @Override
    public String getEventName() {
        return "BondDealEvent";
    }

    @Override
    public String getCreateTimestamp() {
        return createTimestamp;
    }

    @Override
    public String getScQuoteTime() {
        return bondDeal.getTransactTime();
    }

    @Override
    public String getSource(){return bondDeal.getSecurityType();}

    @Override
    public String getInstrumentId() {
        return this.instrumentId;
    }

    @Override
    public BondDeal getEventData() {
        return this.bondDeal;
    }

    @Override
    public void setEventData(BondDeal eventData) {
        this.bondDeal = eventData;

    }

    @Override
    public void setInstrumentId(String instrumentId) {
        this.instrumentId= instrumentId;

    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public BigDecimal calcBestBidYield() {
        if (bondDeal == null){
            log.info("BondDealEvent  calcBestBid  is not init");
            return null;//返回空
        }
        return bondDeal.getYield();//当前成交
    }

    @Override
    public BigDecimal calcBestOfrYield() {
        if (bondDeal == null){
            log.info("BondDealEvent  calcBestOfr  is not init");
            return null;//返回空
        }
        return bondDeal.getYield();//当前成交
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
        return "";
    }

    public void setCreateTimestamp(String createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

}
