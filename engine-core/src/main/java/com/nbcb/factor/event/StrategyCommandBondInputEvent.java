package com.nbcb.factor.event;

import com.nbcb.factor.common.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 策略命令 包括启动，暂停，重新加载等
 */
@Slf4j
public class StrategyCommandBondInputEvent implements BondInputEvent<StrategyCommand> {
    public static final String TOPIC = "FACTOR_ANALYZE_STRATEGY_CMD";
    private StrategyCommand strategyCommand;
    @Override
    public String getEventId(){
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

    @Override
    public void setEventData(StrategyCommand eventData) {
        this.strategyCommand = eventData;
    }

    @Override
    public BigDecimal calcBestBidYield() {
        log.error("StrategyCommand not support calcBestBid");
        return null;//返回空
    }

    @Override
    public BigDecimal calcBestOfrYield() {
        log.error("StrategyCommand not support calcBestOfr");
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
        return strategyCommand.getStrategyName();
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
    public StrategyCommand getEventData() {
        return this.strategyCommand;
    }

    @Override
    public void setInstrumentId(String instrumentId) {

    }

}
