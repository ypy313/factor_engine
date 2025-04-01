package com.changtian.factor.event;

import com.changtian.factor.enums.MarketEnum;
import com.changtian.factor.event.broker.BrokerBondBestOffer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * broker最优报价事件
 */
@Slf4j
public class BrokerBondBestOfferBondInputEvent implements BondInputEvent <BrokerBondBestOffer>{
    private BrokerBondBestOffer brokerBondBestOffer = null;
    private String instrumentId = null;
    @Setter
    private String eventId;
    @Setter
    private String createTimestamp;

    @Override
    public void setEventData(BrokerBondBestOffer eventData) {
        this.brokerBondBestOffer = eventData;
    }

    @Override
    public BigDecimal calcBestBidYield() {
        if (brokerBondBestOffer == null) {
            log.info("brokerBondBestOffer calcBestBid is not init");
            return null;//返回空
        }
        return brokerBondBestOffer.getBid();
    }

    @Override
    public BigDecimal calcBestOfrYield() {
        if (brokerBondBestOffer == null) {
            log.info("brokerBondBestOffer calcBestOfrYield is not init");
            return null;
        }
        return brokerBondBestOffer.getAsk();
    }

    @Override
    public BigDecimal calcBestBidVolume() {
        if (brokerBondBestOffer == null) {
            log.info("brokerBondBestOffer calcBestBidVolume is not init");
            return null;
        }
        return brokerBondBestOffer.getBidQty();
    }

    @Override
    public BigDecimal calcBestOfrVolume() {
        if(brokerBondBestOffer ==null){
            log.info("brokerBondBestOffer calcBestOfrVolume is not init");
            return null;
        }
        return brokerBondBestOffer.getAskQty();
    }

    @Override
    public String getStrategyName() {
        return null;
    }

    @Override
    public String getCreateTimestamp() {
        return createTimestamp;
    }

    @Override
    public String getScQuoteTime() {
        return brokerBondBestOffer.getPubTime();
    }

    @Override
    public String getSource() {
        return brokerBondBestOffer.getBroker();
    }
    @Override
    public String getInstrumentId(){
        //交易市场： CFETS-银行间 XSHG-上交所 XSHE-深交所
        if(StringUtils.equals(MarketEnum.CFETS.getMarket(),brokerBondBestOffer.getMarket())){
            return instrumentId +".IB";//交易中心报文默认不带IB,需要补加
        }else if(StringUtils.equals(MarketEnum.XSHG.getMarket(),brokerBondBestOffer.getMarket())){
            return instrumentId +".SH";//上交所报文默认不带IB,需要补加
        }else if(StringUtils.equals(MarketEnum.XSHE.getMarket(),brokerBondBestOffer.getMarket())){
            return instrumentId +".SZ";//深交所报文默认不带IB，需要补加
        }
        return instrumentId;
    }

    @Override
    public BrokerBondBestOffer getEventData() {
        return this.brokerBondBestOffer;
    }

    @Override
    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    @Override
    public String getEventId() {
        return "BrokerBondBestOfferEvent"+eventId;
    }

    @Override
    public String getEventName() {
        return "BrokerBondBestOfferEvent";
    }

}
