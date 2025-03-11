package com.nbcb.factor.event.cdh;


import com.nbcb.factor.event.Streamable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;

/**
 * 经纪商bond双边最优报价
 */
@Getter@Setter@ToString
public class BondBestOffer implements Streamable {
    private static final long serialVersionUID = 1L;
    private static final String KEY = MethodHandles.lookup().lookupClass().getSimpleName();
    private static final String TOPIC ="SMDS_IN_CDH_BOND_BEST_OFFER";
    private final long createTimeStamp = System.currentTimeMillis();//接收行情的时间
    private String msgType;//消息头部
    private String symbol;//债券简称（UTF-8编码）
    private String securityID;//债券代码加后缀
    private String scQuoteTime;//报价时间
    private BigDecimal bidPx;//bid报价
    private BigDecimal offerPx;//offer报价
    private String bidSize;//bid量
    private String offerSize;//offer量
    private BondBestOfferDetail text;//58域 详细信息
    private String securityType;//经纪商数据类型

    @Override
    public long getCreated() {
        return createTimeStamp;
    }

    @Override
    public long getEventTime() {
        return createTimeStamp;
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
