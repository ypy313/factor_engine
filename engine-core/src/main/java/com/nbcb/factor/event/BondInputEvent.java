package com.nbcb.factor.event;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 事件接口定义
 */
public interface BondInputEvent<T> extends InputEvent, Serializable {
    void setEventData(T eventData);//设置数据
    BigDecimal calcBestBidYield();//计算最优bid
    BigDecimal calcBestOfrYield();//计算最优ofr
    BigDecimal calcBestBidVolume();//计算最优报量
    BigDecimal calcBestOfrVolume();//计算最优报量
    String getStrategyName();//获取策略名称
}
