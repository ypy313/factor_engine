package com.nbcb.factor.event.broker;

import com.nbcb.factor.event.Streamable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Getter@Setter@ToString
public class BrokerBondBestOffer implements Streamable {
    private static final long serialVersionUID = 1L;
    /**
     * KEY
     */
    private static final String KEY = MethodHandles.lookup().lookupClass().getSimpleName();
    /**
     * TOPIC = "FACTOR_BROKER_BOND_BEST_OFFER"
     */
    public static final String TOPIC = "FACTOR_BROKER_BOND_BEST_OFFER";
    /**
     * createdTimestamp = System.currentTimeMillis()
     */
    private final long createTimestamp = System.currentTimeMillis();
    /**
     * FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");

    private String globalId;
    private String msgType;//消息头部
    private String ric;//ric
    private String sendingTime;//CFETS报文发送时间
    private String mdType;//行情类型 1-最优报价 101-逐笔成交报价
    private String mdSubType;//行情子类型 126-经济商报价
    private String securityID;//债券ID
    private String symbol;//债券名称
    private String termToMaturity;//补偿期
    private String broker;//经纪商公司 CNEX-国际 PATR-平安 BGC-中诚 TP-国利 MQM-信唐 UEDA-上田
    private String market;//交易市场 CFETS-银行间 XSHG-上交所 XSHE-深交所
    private String latestSubjectRat;//评级 例如：AA
    private BigDecimal bid;//报买价格（最优报价）
    private BigDecimal ask;//报卖价格（最优报价）
    private String bidPriceType;//报买价格类别（最优报价）6-利差 9-收益率 100-净价 103-全价 104-意向
    private String askPriceType;//报卖价格类别（最优报价）6-利率 9-收益率 100-净价 103-全价 104-意向
    private String bidQuantity;//报买量（最优报价）
    private String askQuantity;//报卖量（最优报价）
    private BigDecimal bidQty;//报买量（备注中计算的量）
    private BigDecimal askQty;//报卖量（备注中计算的量）
    private String bidYieldType;//报买收益率类型（最优报价）：MATURITY-到期 STRIKEYIELD-行权
    private String askYieldType;//报卖收益率类型（最优报价）：MATURITY-到期 STRIKEYIELD-行权
    private String securityDesc;//债券类型（最优报价）例如：国债
    private String side;//报价方向（最优报价）：1-BID 2-ASK 3-双边
    private String bidRemark;//报卖价备注（最优报价）
    private String askRemark;//报买价备注（最优报价）
    private String recTime;//行情接收时间
    private String pubTime;//行情发布时间
    private String settlType;//清算速度（备注中计算的清算速度）
    private Integer mdEntries;//重复组次数
    private String transactTime;//更新日期（报价行情）

    @Override
    public long getCreated() {
        return createTimestamp;
    }

    @Override
    public long getEventTime() {
        if (transactTime !=null) {
            try{
                String time = transactTime+".000";
                return LocalDateTime.parse(time,FORMATTER)
                        .toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
        return System.currentTimeMillis();
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
