package com.nbcb.factor.event.spread;

import com.nbcb.factor.event.Streamable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.invoke.MethodHandles;

@Getter@Setter@ToString
public class AllMarketData implements Streamable {
    private static final String KEY = MethodHandles.lookup().lookupClass().getName();
    //消费kafka的topic
    public static final String TOPIC = "FACTOR_MARKET_PROCESSING_RESULT";
    //时间戳
    private final long createdTimeStamp = System.currentTimeMillis();
    //事件id 实例：MarketDataSnapshotFullRefreshEvent_W_959445734191919105
    public String globalId;
    private String eventId;
    //债券id 示例 210210.IB
    private String instrumentId;
    //事件名称 示例 marketDataSnapshotFullRefreshEvent
    private String eventName;
    //消息队列创建时间
    private String createTime;
    //esp交易最优类
    private EspBestYield espBestYield;
    //cfets 最优交易类
    private CfetsBestYield cfetsBestYield;
    //Broker 最优交易类
    private BrokerBestYield brokerBestYield;
    //Market 最优交易类
    private MarketBestYield marketBestYield;
    //Xbond 最优交易类
    private XbondBestYield xbondBestYield;
    //Xswap 最优交易类
    private XswapBestYield xswapBestYield;

    @Override
    public long getCreated() {
        return createdTimeStamp;
    }

    @Override
    public long getEventTime() {
        return createdTimeStamp;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
