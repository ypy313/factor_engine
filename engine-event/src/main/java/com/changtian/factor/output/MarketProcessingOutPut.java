package com.changtian.factor.output;

import com.changtian.factor.common.DateUtil;
import com.changtian.factor.event.BondInputEvent;
import com.changtian.factor.event.OutputEvent;
import com.changtian.factor.output.bondprice.BondPriceOutputResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Getter
@ToString
public class MarketProcessingOutPut implements OutputEvent, Serializable {
    private String eventId;//事件id
    private String eventName;//事件名称
    private String srcTimestamp;//来源时间
    private String resTimestamp;//结果时间
    private String instrumentId;//资立
    private String mapperName;// 映射名称
    private String createTime;//结果产生时间
    private String updateTime;//结果修改时间。压缩的时候，只更新修改时间
    private MarketProcessingDetailResult xBondBestYield = new MarketProcessingDetailResult(); // XB0ND 最优价
    private MarketProcessingDetailResult xSwapBestYield = new MarketProcessingDetailResult(); // xswap最优价
    private MarketProcessingDetailResult espBestYield = new MarketProcessingDetailResult();// Esp 最优价
    private MarketProcessingDetailResult cfetsBestYield = new MarketProcessingDetailResult(); // cfets 最优价
    /*
     *CNEX-国际 PATR-平安 BGC-中诚 TP-国科 MQM-信唐 UEDA-上田
     */
    private Map<String, MarketProcessingDetailResult> brokerBestYieldMap = new ConcurrentHashMap <> ();
    private MarketProcessingDetailResult brokerBestYield = new MarketProcessingDetailResult(); // broker 鼎优价
    private MarketProcessingDetailResult marketBestYield = new MarketProcessingDetailResult(); // 全市场 最优价
    private MarketProcessingDealDetailResult cmdsDealYield = new MarketProcessingDealDetailResult(); // cmds 成交价
    private MarketProcessingDealDetailResult brokerDealYield = new MarketProcessingDealDetailResult(); // broket 成交价
    private MarketProcessingDealDetailResult marketDealYield = new MarketProcessingDealDetailResult(); // 全市场 成交价

    public MarketProcessingOutPut() {

    }
    public MarketProcessingOutPut(BondInputEvent event){
        this.eventId = event.getEventId();
        this.eventName = event.getEventName();
        this.srcTimestamp = event.getScQuoteTime();//原始时间
        this.resTimestamp = event.getCreateTimestamp();//接收行情的时间
        this.instrumentId = event.getInstrumentId();
        this.updateTime = DateUtil.getSendingTime();//处理完成的时间
        this.createTime = DateUtil.getSendingTime();//处理完成的时间
    }

    public BondPriceOutputResult convertBondPriceResult(BondPriceOutputResult bondPriceOutputResult){
        bondPriceOutputResult.setWholeBestBidYield(this.getMarketBestYield().getBestBidYield());
        bondPriceOutputResult.setWholeBestOfrYield(this.getMarketBestYield().getBestOfrYield());
        bondPriceOutputResult.setLatestTrans(this.getMarketDealYield().getDealYield());
        bondPriceOutputResult.setTransTime(this.getMarketDealYield().getTradeTime());
        bondPriceOutputResult.setBidDataSource(this.getMarketBestYield().getBidDatasource());
        bondPriceOutputResult.setOfrDataSource(this.getMarketBestYield().getOfrDataSource());
        bondPriceOutputResult.setBidVolume(this.getMarketBestYield().getBidVolume());
        bondPriceOutputResult.setOfrVolume(this.getMarketBestYield().getOfrVolume());
        return bondPriceOutputResult;
    }

    @Override
    public String getEventId() {
        return this.eventId;
    }

    @Override
    public String getEventName() {
        return this.eventName;
    }

    @Override
    public String getInstrumentId() {
        return this.instrumentId;
    }

    @Override
    public String getSrcTimestamp() {
        return this.srcTimestamp;
    }

    @Override
    public String getResTimestamp() {
        return this.resTimestamp;
    }

    @Override
    public String getResultType() {
        return MARKET_PROCESSING;
    }

    @Override
    public String getCreateTime() {
        return this.createTime;
    }

    @Override
    public String getUpdateTime() {
        return this.updateTime;
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public String getPkKey() {
        return this.eventId;
    }

    @Override
    public String getCompressFlag() {
        return "0";
    }

    @Override
    public <T> T getEventData() {
        return null;
    }
}
