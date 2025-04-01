package com.changtian.factor.event;

import java.math.BigDecimal;

/**
 * 功能描述
 */
public interface BondDealInputEvent {
    BigDecimal dealYield();//计算成交收益率
}
