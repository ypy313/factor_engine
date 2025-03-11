package com.nbcb.factor.event;

import java.io.Serializable;

/**
 * 事件处理结果
 */
public interface OutputEvent extends Event, Serializable {
    String SPREAD_VALUE = "SpreadValue";
    String TRADE_SIGNAL = "TradeSignal";
    String STRATEGY_NOTICE = "StrategyNotice";
    String MARKET_PROCESSING = "MarketProcess";
    String WORK_PLATFORM_SIGNAL = "WorkPlatformSignal";

    String getSrcTimestamp();//获取事件来源时间戳
    String getResTimestamp();//获取事件处理后的时间戳
    String getResultType();//TradeSignal SpreadValue
    String getCreateTime();//产生时间
    String getUpdateTime();//最近更新时间
    long getCount();//累计产生次数
    String getPkKey();//压缩防重唯一key
    String getCompressFlag();//压缩标记 0未压缩 1已压缩
    <T> T getEventData();//获取事件处理结果

}
