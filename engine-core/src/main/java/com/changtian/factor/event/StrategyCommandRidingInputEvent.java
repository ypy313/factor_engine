package com.changtian.factor.event;

import com.changtian.factor.common.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 策略命令，包括启动，暂停，重新加载等
 */
@Slf4j
public class StrategyCommandRidingInputEvent implements BondInputEvent<StrategyCommand>{
    public static final String TOPIC = "FACTOR_ANALYZE_STRATEGY_CMD";
    private StrategyCommand strategyCommand;
    @Override
    public String getEventId() {
        return "StrategyCommandEvent_"+ DateUtil.getNowstr();
    }

    @Override
    public String getEventName() {
        return "StrategyCommandEvent";
    }
    @Override
    public void setEventData(StrategyCommand eventData) {
        this.strategyCommand = eventData;
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

    @Override
    public String getInstrumentId() {
        return strategyCommand.getCmd();
    }
}
