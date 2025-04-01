package com.changtian.factor.event;

import com.changtian.factor.event.spread.AllMarketData;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 接收kafka的FACTOR_MARKET_PROCESSING_RESULT分区转换成事件
 */
@Slf4j
public class MarketDataSpreadBondInputEvent implements BondInputEvent <AllMarketData>{
    private AllMarketData allMarketData = null;
    private String instrumentId = null;
    private static final String XSWAP = "XSWAP";

    @Override
    public void setEventData(AllMarketData eventData) {
        this.allMarketData = eventData;
    }

    /**
     * 计算买方价格，从融合的行情中获取最优值
     */
    @Override
    public BigDecimal calcBestBidYield() {
        if (allMarketData == null) {
            log.info("allMarketData calcBestBid is not init");
            return null;
        }
        log.info("MarketDataSpreadBondInputEvent calcBestBidYield start");
        if (allMarketData.getEventId().toLowerCase().contains(XSWAP.toLowerCase())) {
            //xswap取值反向取值bid取ofr
            return allMarketData.getXswapBestYield() == null ?null:allMarketData
                    .getXswapBestYield().getBestOfrYield();
        }else {
            return allMarketData.getMarketBestYield() == null ?null:allMarketData
                    .getMarketBestYield().getBestBidYield();
        }
    }

    /**
     * 计算卖方价格，从融合的行情中获取最优值
     */
    @Override
    public BigDecimal calcBestOfrYield() {
        if (allMarketData == null) {
            log.info("allMarketData BestOfrYield is not init");
            return null;
        }
        log.info("MarketDataSpreadBondInputEvent BestOfrYield start");
        if(allMarketData.getEventId().toLowerCase().contains(XSWAP.toLowerCase())) {
            //xswap取值反向取值bid取ofr
            return allMarketData.getXswapBestYield() == null ?null:allMarketData
                    .getXswapBestYield().getBestBidYield();
        }else {
            return allMarketData.getMarketBestYield() == null?null:allMarketData
                    .getMarketBestYield().getBestOfrYield();
        }
    }

    /**
     * 获取最优买方的报量
     */
    @Override
    public BigDecimal calcBestBidVolume() {
        if(allMarketData == null) {
            log.info("allMarketData BestBidVolume is not init");
            return null;
        }
        log.info("MarketDataSpreadBondInputEvent calcBestBidVolume start");
        if(allMarketData.getEventId().toLowerCase().contains(XSWAP.toLowerCase())) {
            //xswap取值反向取值bid取ofr
            return allMarketData.getXswapBestYield()==null ?null:allMarketData
                    .getXswapBestYield().getOfrVolume();
        }else {
            return allMarketData.getMarketBestYield().getBidVolume();
        }
    }

    /**
     * 获取最优卖方的报量
     */
    @Override
    public BigDecimal calcBestOfrVolume() {
        if(allMarketData == null) {
            log.info("allMarketData BestOfrVolume is not init");
            return null;
        }
        log.info("MarketDataSpreadBodnInputEvent calcBestBidVolume start");
        if(allMarketData.getEventId().toLowerCase().contains(XSWAP.toLowerCase())) {
            //sxwap 取值反向取值bid取ofr
            return allMarketData.getXswapBestYield() == null?null:allMarketData
                    .getXswapBestYield().getBidVolume();
        }else {
            return allMarketData.getMarketBestYield() == null ?null:allMarketData
                    .getMarketBestYield().getOfrVolume();
        }
    }

    @Override
    public String getStrategyName() {
        return null;
    }

    @Override
    public String getCreateTimestamp() {
        return allMarketData.getCreateTime();
    }

    @Override
    public String getScQuoteTime() {
        return allMarketData.getCreateTime();
    }

    @Override
    public String getSource() {
        return "AllMarket";
    }

    @Override
    public AllMarketData getEventData() {
        return this.allMarketData;
    }

    @Override
    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    @Override
    public String getEventId() {
        return allMarketData.getEventId();
    }

    @Override
    public String getEventName() {
        return "AllMarketDataEvent";
    }

    @Override
    public String getInstrumentId() {
        return instrumentId;
    }
}
