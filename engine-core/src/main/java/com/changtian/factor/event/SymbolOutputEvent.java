package com.changtian.factor.event;

import java.io.Serializable;

/**
 * 货币对事件父类
 */
public interface SymbolOutputEvent<T> extends Event, Serializable {
    /**
     * 事件类型 OHLC RSI MA SIGNAL
     * @return 结果
     */
    String eventType();

    /**
     * 获取事件来源时间戳
     * @return 结果
     */
    String getSrcTimestamp();

    /**
     * 获取事件处理后的时间戳
     * @return 结果
     */
    String getResTimestamp();

    /**
     * 结果类型 HIS DETAIL SIGNAL
     * @return 结果
     */
    String getResultType();

    /**
     * 产生时间
     * @return 结果
     */
    String getCreateTime();

    /**
     * 最新更新时间
     * @return 结果
     */
    String getUpdateTime();

    /**
     * 获取事件处理结果
     * @return 结果
     */
    T getEventData();
}
