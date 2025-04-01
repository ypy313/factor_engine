package com.changtian.factor.event;

import com.changtian.factor.enums.MsgTypeEnum;
import com.changtian.factor.event.cfets.MarketDataSnapshotFullRefresh;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * cfets 行情
 */
@Slf4j
public class MarketDataSnapshotFullRefreshBondInputEvent implements BondInputEvent<MarketDataSnapshotFullRefresh> {
    private MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh = null;
    private String instrumentId = null;
    private String eventId;
    private String createTimestamp;

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public String getEventId() {
        return "MarketDataSnapshotFullRefreshEvent_" + eventId;
    }

    @Override
    public String getEventName() {
        return "MarketDataSnapshotFullRefreshEvent";
    }

    @Override
    public String getCreateTimestamp() {
        return createTimestamp;
    }

    @Override
    public String getScQuoteTime() {
        return marketDataSnapshotFullRefresh.getSendingTime();
    }

    @Override
    public String getSource() {
        if (StringUtils.equals("2", marketDataSnapshotFullRefresh.getMarketIndicator())) {

            if (StringUtils.equals("1", marketDataSnapshotFullRefresh.getRealTimeUndertakeFlag())) {
                //利率互换
                return "XSWAP_RealTime";
            } else {
                return "XSWAP_NotRealTime";
            }
        } else {
            return marketDataSnapshotFullRefresh.getMsgType();
        }
    }

    @Override
    public String getInstrumentId() {
        if (StringUtils.equals("XSWAP", marketDataSnapshotFullRefresh.getMsgType())) {
            return instrumentId;
        } else {
            return instrumentId + ".IB";//交易中心报文默认不带IB，需要补加

        }
    }

    @Override
    public MarketDataSnapshotFullRefresh getEventData() {
        return this.marketDataSnapshotFullRefresh;
    }

    @Override
    public void setEventData(MarketDataSnapshotFullRefresh eventData) {
        this.marketDataSnapshotFullRefresh = eventData;
    }

    @Override
    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    @Override
    // 计算价格
    public BigDecimal calcBestBidYield() {
        if (marketDataSnapshotFullRefresh == null) {
            log.info("MarketDataSnapshotFullRefreshEvent  calcBestBid  is not init");
            return null;//返回空
        }
        String msgType = marketDataSnapshotFullRefresh.getMsgType();
        String mdBookType = marketDataSnapshotFullRefresh.getMdBookType();
        String mdSubBookType = marketDataSnapshotFullRefresh.getMdSubBookType();
        String marketIndicator = marketDataSnapshotFullRefresh.getMarketIndicator();
        log.info("marketDataSnapshotFullRefresh event msgType is {} mdBookType is {} mdSubBookType is {}  ",
                msgType, mdBookType, mdSubBookType);
        if (StringUtils.equals("W", msgType)
                && StringUtils.equals("2", mdBookType)
                && StringUtils.equals("123", mdSubBookType)) {
            BigDecimal ret = calcESPBestBid(marketDataSnapshotFullRefresh);
            log.info("marketDataSnapshotFullRefresh esp event calcBestBid ret is {}", ret);
            return ret;
        }
        if (StringUtils.equals("XBOND", msgType)
                && StringUtils.equals("2", mdBookType)) {
            log.info("marketDataSnapshotFullRefresh xbond event calcBestBid");
            return calcXBONDBestBid(marketDataSnapshotFullRefresh);
        }
        if (StringUtils.equals("XSWAP", msgType)
                && StringUtils.equals("2", mdBookType)
                && StringUtils.equals("2", marketIndicator)) {
            log.info("marketDataSnapshotFullRefresh XWSAP event calcBestBid");
            return calcXSWAPBestBid(marketDataSnapshotFullRefresh);
        }
        log.error("marketDataSnapshotFullRefresh can't find yield in calcBestBid");
        return null;//返回空

    }

    @Override
    public BigDecimal calcBestOfrYield() {
        if (marketDataSnapshotFullRefresh == null) {
            log.info("MarketDataSnapshotFullRefreshEvent  calcBestOfr  is not init");
            return null;//返回空
        }
        String msgType = marketDataSnapshotFullRefresh.getMsgType();
        String mdBookType = marketDataSnapshotFullRefresh.getMdBookType();
        String mdSubBookType = marketDataSnapshotFullRefresh.getMdSubBookType();
        String marketIndicator = marketDataSnapshotFullRefresh.getMarketIndicator();
        log.info("marketDataSnapshotFullRefresh calcBestOfr event msgType is {} mdBookType is {} mdSubBookType is {}  ",
                msgType, mdBookType, mdSubBookType);
        if (StringUtils.equals("W", msgType)
                && StringUtils.equals("2", mdBookType)
                && StringUtils.equals("123", mdSubBookType)) {
            log.info("marketDataSnapshotFullRefresh esp event calcBestOfr");
            return calcESPBestOfr(marketDataSnapshotFullRefresh);
        }
        if (StringUtils.equals("XBOND", msgType)
                && StringUtils.equals("2", mdBookType)) {
            log.info("marketDataSnapshotFullRefresh xbond event calcBestOfr");
            return calcXBONDBestOfr(marketDataSnapshotFullRefresh);
        }
        if (StringUtils.equals("XSWAP", msgType)
                && StringUtils.equals("2", mdBookType)
                && StringUtils.equals("2", marketIndicator)) {
            log.info("marketDataSnapshotFullRefresh XWSAP event calcBestOfr");
            return calcXSWAPBestOfr(marketDataSnapshotFullRefresh);
        }
        log.error("marketDataSnapshotFullRefresh can't find yield in calcBestOfr");
        return null;//返回空

    }

    @Override
    public BigDecimal calcBestOfrVolume() {
        if (marketDataSnapshotFullRefresh == null) {
            log.info("MarketDataSnapshotFullRefreshEvent  calcBestOfrVolume  is not init");
            return null;//返回空
        }
        String msgType = marketDataSnapshotFullRefresh.getMsgType();
        String mdBookType = marketDataSnapshotFullRefresh.getMdBookType();
        String mdSubBookType = marketDataSnapshotFullRefresh.getMdSubBookType();
        String marketIndicator = marketDataSnapshotFullRefresh.getMarketIndicator();
        log.info("marketDataSnapshotFullRefresh calcBestOfrVolume event msgType is{} mdBookType is {} mdSubBookType is {}",
                msgType, mdBookType, mdSubBookType);
        if (StringUtils.equals("W", msgType)
                && StringUtils.equals("2", mdBookType)
                && StringUtils.equals("123", mdSubBookType)) {
            log.info("marketDataSnapshotFullRefresh esp event calcESPBestVolumeOfr");
            return calcESPBestVolumeOfr(marketDataSnapshotFullRefresh);
        }
        if (StringUtils.equals("XBOND", msgType)
                && StringUtils.equals("2", mdBookType)) {
            log.info("marketDataSnapshotFullRefresh xbond event calcXBONDBestVolumeOfr");
            return calcXBONDBestVolumeOfr(marketDataSnapshotFullRefresh);
        }
        if (StringUtils.equals("XSWAP", msgType)
                && StringUtils.equals("2", mdBookType)
                && StringUtils.equals("2", marketIndicator)) {
            log.info("marketDataSnapshotFullRefresh XWSAP event calcXSWAPBestVolumeOfr");
            return calcXSWAPBestVolumeOfr(marketDataSnapshotFullRefresh);
        }
        log.error("marketDataSnapshotFullRefresh can't find yield in calcBestOfrVolume");
        return null;//返回空
    }
    @Override
    public BigDecimal calcBestBidVolume(){
        if (marketDataSnapshotFullRefresh == null) {
            log.info("MarketDataSnapshotFullRefreshEvent  calcBestBidVolume is not init");
            return null;//返回空
        }
        String msgType = marketDataSnapshotFullRefresh.getMsgType();
        String mdBookType = marketDataSnapshotFullRefresh.getMdBookType();
        String mdSubBookType = marketDataSnapshotFullRefresh.getMdSubBookType();
        String marketIndicator = marketDataSnapshotFullRefresh.getMarketIndicator();
        log.info("marketDataSnapshotFullRefresh calcBestBidVolume event msgType is {} mdBookType is {} mdSubBookType is {}",
                msgType, mdBookType, mdSubBookType);
        if (StringUtils.equals("W", msgType)
                && StringUtils.equals("2", mdBookType)
                && StringUtils.equals("123", mdSubBookType)) {
            log.info("marketDataSnapshotFullRefresh esp event calcESPBestVolumeOfr");
            return calcESPBestVolumeBid(marketDataSnapshotFullRefresh);
        }
        if (StringUtils.equals("XBOND", msgType)
                && StringUtils.equals("2", mdBookType)) {
            log.info("marketDataSnapshotFullRefresh xbond event calcXBONDBestVolumeOfr");
            return calcXBONDBestVolumeBid(marketDataSnapshotFullRefresh);
        }
        if (StringUtils.equals("XSWAP", msgType)
                && StringUtils.equals("2", mdBookType)
                && StringUtils.equals("2", marketIndicator)) {
            log.info("marketDataSnapshotFullRefresh XWSAP event calcXSWAPBestVolumeOfr");
            return calcXSWAPBestVolumeBid(marketDataSnapshotFullRefresh);
        }
        log.error("marketDataSnapshotFullRefresh can't find yield in calcBestBidVolume");
        return null;//返回空
    }

    private static BigDecimal calcESPBestVolumeBid(MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh){
        List<MarketDataSnapshotFullRefresh.NoMDEntries> noMDEntriesList = marketDataSnapshotFullRefresh.getNoMDEntriesList();
        //如果行情个数为0，则返回空
        if (noMDEntriesList == null || noMDEntriesList.isEmpty()) {
            return null;
        }
        for (MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries : noMDEntriesList) {
            String mdEntryType = noMDEntries.getMdEntryType();
            String mdPriceLevel = noMDEntries.getMdPriceLevel();
            String quoteEntryId = noMDEntries.getQuoteEntryID();//报价编号
            //bid边，且为第一档
            if (StringUtils.equals("0",mdEntryType) && StringUtils.equals("1",mdPriceLevel)) {
                log.info("calcESPBestVolumeBid quoteEntryId is {} , yield is {}", quoteEntryId,noMDEntries.getYield());
                return noMDEntries.getMdEntrySize();//获得最优bid的报量
            }
        }
        return null;
    }

    private static BigDecimal calcXBONDBestVolumeBid(MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh){
        List<MarketDataSnapshotFullRefresh.NoMDEntries> noMDEntriesList = marketDataSnapshotFullRefresh.getNoMDEntriesList();
        //如果行情个数为0，则返回空
        if (noMDEntriesList.isEmpty()) {
            return null;
        }
        for (MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries : noMDEntriesList) {
            String mdEntryType = noMDEntries.getMdEntryType();
            String mdPriceLevel = noMDEntries.getMdPriceLevel();
            String quoteEntryId = noMDEntries.getQuoteEntryID();//报价编号
            //bid边，且为第一档
            if (StringUtils.equals("0",mdEntryType) && StringUtils.equals("1",mdPriceLevel)) {
                log.info("calcXBONDBestVolumeBid quoteEntryId is {} , yield is {}", quoteEntryId,noMDEntries.getYield());
                return noMDEntries.getMdEntrySize();//获得最优
            }
        }
        return null;
    }

    private static BigDecimal calcXSWAPBestVolumeBid(MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh){
        List<MarketDataSnapshotFullRefresh.NoMDEntries> noMDEntriesList = marketDataSnapshotFullRefresh.getNoMDEntriesList();
        //如果行情个数为0，则返回空
        if (noMDEntriesList.isEmpty()) {
            return null;
        }
        for (MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries : noMDEntriesList) {
            String mdEntryType = noMDEntries.getMdEntryType();
            String mdPriceLevel = noMDEntries.getMdPriceLevel();
            String quoteEntryId = noMDEntries.getQuoteEntryID();//报价编号
            //bid边，且为第一档
            if (StringUtils.equals("0",mdEntryType) && StringUtils.equals("1",mdPriceLevel)) {
                log.info("calcXSWAPBestVolumeBid quoteEntryId is {} , yield is {}", quoteEntryId,noMDEntries.getYield());
                return noMDEntries.getMdEntrySize();//获得最优
            }
        }
        return null;
    }

    private static BigDecimal calcESPBestVolumeOfr(MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh){
        List<MarketDataSnapshotFullRefresh.NoMDEntries> noMDEntriesList = marketDataSnapshotFullRefresh.getNoMDEntriesList();
        //如果行情个数为0，则返回空
        if (noMDEntriesList.isEmpty()) {
            return null;
        }
        for (MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries : noMDEntriesList) {
            String mdEntryType = noMDEntries.getMdEntryType();
            String mdPriceLevel = noMDEntries.getMdPriceLevel();
            String quoteEntryId = noMDEntries.getQuoteEntryID();//报价编号
            //ofr边，且为第一档
            if (StringUtils.equals("1",mdEntryType) && StringUtils.equals("1",mdPriceLevel)) {
                log.info("calcESPBestVolumeOfr quoteEntryId is {} , yield is {}", quoteEntryId,noMDEntries.getYield());
                return noMDEntries.getMdEntrySize();//获得最优
            }
        }
        return null;
    }

    private static BigDecimal calcXBONDBestVolumeOfr(MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh){
        List<MarketDataSnapshotFullRefresh.NoMDEntries> noMDEntriesList = marketDataSnapshotFullRefresh.getNoMDEntriesList();
        //如果行情个数为0，则返回空
        if (noMDEntriesList.isEmpty()) {
            return null;
        }
        for (MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries : noMDEntriesList) {
            String mdEntryType = noMDEntries.getMdEntryType();
            String mdPriceLevel = noMDEntries.getMdPriceLevel();
            String quoteEntryId = noMDEntries.getQuoteEntryID();//报价编号
            //ofr边，且为第一档
            if (StringUtils.equals("1",mdEntryType) && StringUtils.equals("1",mdPriceLevel)) {
                log.info("calcXBONDBestVolumeOfr quoteEntryId is {} , yield is {}", quoteEntryId,noMDEntries.getYield());
                return noMDEntries.getMdEntrySize();//获得最优
            }
        }
        return null;
    }

    private static BigDecimal  calcXSWAPBestVolumeOfr(MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh){
        List<MarketDataSnapshotFullRefresh.NoMDEntries> noMDEntriesList = marketDataSnapshotFullRefresh.getNoMDEntriesList();
        //如果行情个数为0，则返回空
        if (noMDEntriesList.isEmpty()) {
            return null;
        }
        for (MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries : noMDEntriesList) {
            String mdEntryType = noMDEntries.getMdEntryType();
            String mdPriceLevel = noMDEntries.getMdPriceLevel();
            String quoteEntryId = noMDEntries.getQuoteEntryID();//报价编号
            //ofr边，且为第一档
            if (StringUtils.equals("1",mdEntryType) && StringUtils.equals("1",mdPriceLevel)) {
                log.info("calcXSWAPBestVolumeOfr quoteEntryId is {} , yield is {}", quoteEntryId,noMDEntries.getYield());
                return noMDEntries.getMdEntrySize();//获得最优
            }
        }
        return null;
    }


    private static BigDecimal calcESPBestBid(MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh) {
        List<MarketDataSnapshotFullRefresh.NoMDEntries> noMDEntriesList = marketDataSnapshotFullRefresh.getNoMDEntriesList();
        //如果行情个数为0，则返回空
        if (noMDEntriesList == null || noMDEntriesList.isEmpty()) {
            return null;
        }
        for (MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries : noMDEntriesList) {
            String mdEntryType = noMDEntries.getMdEntryType();
            String mdPriceLevel = noMDEntries.getMdPriceLevel();
            String quoteEntryId = noMDEntries.getQuoteEntryID();//报价编号
            //bid 边，且为第一档
            if (StringUtils.equals("0", mdEntryType) && StringUtils.equals("1", mdPriceLevel)) {
                log.info("calcESPBestBid quoteEntryId is {} , yield is {} ", quoteEntryId, noMDEntries.getYield());
                return noMDEntries.getYield();//获得最优
            }
        }

        return null;
    }

    private static BigDecimal calcXBONDBestBid(MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh) {
        List<MarketDataSnapshotFullRefresh.NoMDEntries> noMDEntriesList = marketDataSnapshotFullRefresh.getNoMDEntriesList();
        //如果行情个数为0，则返回空
        if (noMDEntriesList.isEmpty()) {
            return null;
        }
        for (MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries : noMDEntriesList) {
            String mdEntryType = noMDEntries.getMdEntryType();
            String mdPriceLevel = noMDEntries.getMdPriceLevel();
            String quoteEntryId = noMDEntries.getQuoteEntryID();//报价编号
            //bid 边，且为第一档
            if (StringUtils.equals("0", mdEntryType) && StringUtils.equals("1", mdPriceLevel)) {
                log.info("calcXBONDBestBid quoteEntryId is {} , yield is {} ", quoteEntryId, noMDEntries.getYield());
                return noMDEntries.getYield();//获得最优
            }
        }

        return null;
    }

    private static BigDecimal calcXSWAPBestBid(MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh) {
        List<MarketDataSnapshotFullRefresh.NoMDEntries> noMDEntriesList = marketDataSnapshotFullRefresh.getNoMDEntriesList();
        //如果行情个数为0，则返回空
        if (noMDEntriesList.isEmpty()) {
            return null;
        }
        for (MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries : noMDEntriesList) {
            String mdEntryType = noMDEntries.getMdEntryType();
            String mdPriceLevel = noMDEntries.getMdPriceLevel();
            String quoteEntryId = noMDEntries.getQuoteEntryID();//报价编号
            //bid 边，且为第一档
            if (StringUtils.equals("1", mdEntryType) && StringUtils.equals("1", mdPriceLevel)) {
                log.info("calcXSWAPBestBid quoteEntryId is {} , yield is {} ", quoteEntryId, noMDEntries.getYield());
                return noMDEntries.getMdEntryPx();
            }
        }

        return null;
    }


    private static BigDecimal calcESPBestOfr(MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh) {
        List<MarketDataSnapshotFullRefresh.NoMDEntries> noMDEntriesList = marketDataSnapshotFullRefresh.getNoMDEntriesList();
        //如果行情个数为0，则返回空
        if (noMDEntriesList.isEmpty()) {
            return null;
        }
        for (MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries : noMDEntriesList) {
            String mdEntryType = noMDEntries.getMdEntryType();
            String mdPriceLevel = noMDEntries.getMdPriceLevel();
            String quoteEntryId = noMDEntries.getQuoteEntryID();//报价编号
            //ofr 边，且为第一档
            if (StringUtils.equals("1", mdEntryType) && StringUtils.equals("1", mdPriceLevel)) {
                log.info("calcESPBestOfr quoteEntryId is {} , yield is {} ", quoteEntryId, noMDEntries.getYield());
                return noMDEntries.getYield();//获得最优
            }
        }
        return null;
    }

    private static BigDecimal calcXBONDBestOfr(MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh) {
        List<MarketDataSnapshotFullRefresh.NoMDEntries> noMDEntriesList = marketDataSnapshotFullRefresh.getNoMDEntriesList();
        //如果行情个数为0，则返回空
        if (noMDEntriesList.isEmpty()) {
            return null;
        }
        for (MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries : noMDEntriesList) {
            String mdEntryType = noMDEntries.getMdEntryType();
            String mdPriceLevel = noMDEntries.getMdPriceLevel();
            String quoteEntryId = noMDEntries.getQuoteEntryID();//报价编号
            //ofr 边，且为第一档
            if (StringUtils.equals("1", mdEntryType) && StringUtils.equals("1", mdPriceLevel)) {
                log.info("calcXBONDBestOfr quoteEntryId is {} , yield is {} ", quoteEntryId, noMDEntries.getYield());
                return noMDEntries.getYield();//获得最优
            }
        }

        return null;
    }

    private static BigDecimal calcXSWAPBestOfr(MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh) {
        List<MarketDataSnapshotFullRefresh.NoMDEntries> noMDEntriesList = marketDataSnapshotFullRefresh.getNoMDEntriesList();
        //如果行情个数为0，则返回空
        if (noMDEntriesList.isEmpty()) {
            return null;
        }
        for (MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries : noMDEntriesList) {
            String mdEntryType = noMDEntries.getMdEntryType();
            String mdPriceLevel = noMDEntries.getMdPriceLevel();
            String quoteEntryId = noMDEntries.getQuoteEntryID();//报价编号
            //ofr 边，且为第一档
            if (StringUtils.equals("0", mdEntryType) && StringUtils.equals("1", mdPriceLevel)) {
                log.info("calcXSWAPBestOfr quoteEntryId is {} , yield is {} ", quoteEntryId, noMDEntries.getYield());
                return noMDEntries.getMdEntryPx();
            }
        }

        return null;
    }

    public void setCreateTimestamp(String createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    /**
     * 获取行情类型
     * @return
     */
    public String getMsgType(){
        String msgType = marketDataSnapshotFullRefresh.getMsgType();
        String mdBookType = marketDataSnapshotFullRefresh.getMdBookType();
        String mdSubBookType = marketDataSnapshotFullRefresh.getMdSubBookType();
        String marketIndicator = marketDataSnapshotFullRefresh.getMarketIndicator();
        if (StringUtils.equals("W", msgType)
                && StringUtils.equals("2", mdBookType)
                && StringUtils.equals("123", mdSubBookType)) {
            return MsgTypeEnum.ESP.getMsgType();
        }
        if (StringUtils.equals("XBOND", msgType)
                && StringUtils.equals("2", mdBookType)) {
            return MsgTypeEnum.XBOND.getMsgType();
        }
        if (StringUtils.equals("XSWAP", msgType)
                && StringUtils.equals("2", mdBookType)
                && StringUtils.equals("2", marketIndicator)) {
            return MsgTypeEnum.XSWAP.getMsgType();
        }
        return "";
    }
    @Override
    public String getStrategyName(){
        return null;
    }
}
