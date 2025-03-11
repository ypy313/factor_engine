package com.nbcb.factor.event.cdh;

import com.nbcb.factor.enums.PriceTypeEnum;
import com.nbcb.factor.event.Streamable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;

@Getter@Setter@ToString
public class BondDeal implements Streamable {
    private static final long serialVersionUID = 1L;
    private static final String KEY = MethodHandles.lookup().lookupClass().getSimpleName();
    public static final String TOPIC = "SMDS_IN_CDH_BOND_DEAL";
    private final long createdTimeStamp = System.currentTimeMillis();
    private String msgType;//消息头部
    private String symbol;//债券简称（UTF-8编码）
    private String orderQty;//成交量（注：下发为0，成交量不再下发 ）
    private String side;//X-taken y-given Z-trade
    private String quoteID;//交易行情ID
    private String quoteReqID;//交易行情id
    private String settlDate;//结算日期，该信息仅能作为参考
    private String strikePrice;//净价
    private String instrmtAssignmentMethod;//交易操作类型
    private String securityID;//债券代码加后缀加170215.IB(IB/SH/SZ)
    private BigDecimal yield;//收益率
    private String transactTime;//交易时间（UTC时间）
    private String dealStatus;//成交状态
    private String securityType;//经纪商数据类型
    private String quoteType;//行情类别，始终为1-成交
    private BondDealDetail text;//58域详细信息

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
