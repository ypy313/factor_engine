package com.nbcb.factor.event;

import com.nbcb.factor.enums.MarketEnum;
import com.nbcb.factor.event.broker.BrokerBondDeal;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * broker成交报文
 */
@Slf4j
public class BrokerBondDealBondInputEvent implements BondInputEvent<BrokerBondDeal>,BondDealInputEvent {
    private BrokerBondDeal bondDeal = null;
    private String instrumentId = null;
    @Setter
    private String eventId;
    @Setter
    private String createTimestamp;

    @Override
    public BigDecimal dealYield() {
        if (bondDeal == null) {
            log.info("BondDealEvent calcBestOfr is not init");
            return null;
        }
        return bondDeal.getDealPrice();//当前成交
    }

    @Override
    public void setEventData(BrokerBondDeal eventData) {
        this.bondDeal = eventData;
    }

    @Override
    public BigDecimal calcBestBidYield() {
        return null;
    }

    @Override
    public BigDecimal calcBestOfrYield() {
        return null;
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

    @Override
    public String getCreateTimestamp() {
        return createTimestamp;
    }

    @Override
    public String getScQuoteTime() {
        return bondDeal.getPubTime();
    }

    @Override
    public String getSource() {
        return bondDeal.getBroker();
    }

    @Override
    public BrokerBondDeal getEventData() {
        return this.bondDeal;
    }

    @Override
    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    @Override
    public String getEventId() {
        return "BrokerBondDealBondEvent_"+eventId;
    }

    @Override
    public String getEventName() {
        return "BrokerBondDealBondEvent";
    }

    @Override
    public String getInstrumentId() {
        //交易市场： CFETS-银行间 XSHG-上交所 XSHE-深交所
        if(StringUtils.equals(MarketEnum.CFETS.getMarket(),bondDeal.getMarket())){
            return instrumentId +".IB";//交易中心报文默认不带IB,需要补加
        }else if(StringUtils.equals(MarketEnum.XSHG.getMarket(),bondDeal.getMarket())){
            return instrumentId +".SH";//上交所报文默认不带IB,需要补加
        }else if(StringUtils.equals(MarketEnum.XSHE.getMarket(),bondDeal.getMarket())){
            return instrumentId +".SZ";//深交所报文默认不带IB，需要补加
        }
        return instrumentId;
    }
}
