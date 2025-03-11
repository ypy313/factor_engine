package com.nbcb.factor.event;

import com.nbcb.factor.event.cdh.BondBestOffer;
import com.nbcb.factor.event.cdh.BondBestOfferDetail;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 最优报价事件
 */
@Slf4j
public class BondBestOfferBondInputEvent implements BondInputEvent<BondBestOffer>{
    private BondBestOffer bondBestOffer = null;
    private  String instrumentId = null;
    @Setter
    private  String eventId;
    private String createTimestamp ;

    @Override
    public void setEventData(BondBestOffer eventData) {
        this.bondBestOffer = eventData;
    }

    @Override
    public BigDecimal calcBestBidYield() {
        if (bondBestOffer == null) {
            log.info("bondBestOffer calcBestBid is not init");
            return null;//返回空
        }
        BondBestOfferDetail bondBestOfferDetail= bondBestOffer.getText();
        if (bondBestOfferDetail != null) {
            return bondBestOfferDetail.getBidYield();//Bid yield
        }else {
            return null;//返回空
        }
    }

    @Override
    public BigDecimal calcBestOfrYield() {
        if (bondBestOffer == null) {
            log.info("bondBestOffer calcBestOfr is not init");
            return null;//返回空
        }
        BondBestOfferDetail bondBestOfferDetail = bondBestOffer.getText();
        if (bondBestOfferDetail != null) {
            return bondBestOfferDetail.getOfrYield();
        }else {
            return null;
        }
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

    @Override
    public String getCreateTimestamp() {
        return createTimestamp;
    }

    @Override
    public String getScQuoteTime() {
        return bondBestOffer.getScQuoteTime();
    }

    @Override
    public String getSource() {
        return bondBestOffer.getSecurityType();
    }

    @Override
    public BondBestOffer getEventData() {
        return this.bondBestOffer;
    }

    @Override
    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    @Override
    public String getEventId() {
        return "BondBestOfferEvent"+eventId;
    }

    @Override
    public String getEventName() {
        return "BondBestOfferEvent";
    }

    @Override
    public String getInstrumentId() {
        return this.instrumentId;
    }
}
