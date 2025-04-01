package com.changtian.factor.event;

import com.changtian.factor.enums.MsgTypeEnum;
import com.changtian.factor.event.cmds.TheCashMarketTBTEntity;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * cmds成交报文
 */
@Slf4j
public class CfetsCmdsBondDealInputEvent implements BondInputEvent<TheCashMarketTBTEntity>,BondDealInputEvent{
    private TheCashMarketTBTEntity marketTBT = null;
    private String instrumentId = null;
    @Setter
    private String eventId;
    @Setter
    private String createTimestamp;

    @Override
    public BigDecimal dealYield() {
        if (marketTBT == null) {
            log.info("CfetsCmdsBondDealInputEvent dealYield is not init");
            return null;
        }
        return marketTBT.getStipulationValue();//当前成交
    }

    @Override
    public void setEventData(TheCashMarketTBTEntity eventData) {
        this.marketTBT = eventData;
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
        return marketTBT.getTransactTime();
    }

    @Override
    public String getSource() {
        return marketTBT.getSource();
    }

    @Override
    public TheCashMarketTBTEntity getEventData() {
        return this.marketTBT;
    }

    @Override
    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    @Override
    public String getEventId() {
        return "CfetsCmdsBondDealEvent_"+eventId;
    }

    @Override
    public String getEventName() {
        return "CfetsCmdsBondDealEvent";
    }

    @Override
    public String getInstrumentId() {
        if (StringUtils.equals(MsgTypeEnum.CMDS.getMsgType(),marketTBT.getSource())) {
            return instrumentId +".IB";//交易中心报文默认不带IB,需要补加
        }else{
            return instrumentId;
        }
    }
}
