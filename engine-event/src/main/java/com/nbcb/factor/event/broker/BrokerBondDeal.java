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
public class BrokerBondDeal implements Streamable {
    private static final long serialVersionUID = 1L;
    private static final String KEY = MethodHandles.lookup().lookupClass().getSimpleName();
    /**
     * TOPIC = "SMDS_IN_BROKER_BROKER"
     */
    public static final String TOPIC = "FACTOR_BROKER_BOND_DEAL";
    /**
     * createdTimestamp = System.currentTimeMillis();
     */
    private final long createdTimestamp = System.currentTimeMillis();
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
    private BigDecimal dealPrice;//成交价格（成交行情）
    private String dealPriceType;//成交价格类别 6-利差 9-收益率 100-净价 103-全价
    private String dealQuantity;//成家量（成交行情）
    private String dealYieldType;//成交收益率类型（成家行情） MATURITY-到期 STRIKEYIELD-行权
    private String valuationYield;//CFETS估值（成交行情）
    private String tradeDate;//成交日期（成交行情）
    private String tradeTime;//成交时间（成交行情）
    private String transactTime;//更新日期（成交行情）
    private String transactionMethod;//成交方向（成交行情）TKN,GVN,TRD
    private String execType;//成交状态（成交行情）F-正常 4-撤销 k-NothingDone
    private String execID;//成交编号（成交行情）
    private String settlType;//清算速度（成交行情）1-T+0  2-T+1
    private String spread;//涨跌BP(成交行情)
    private String dealRemark;//备注（成交行情）
    private String recTime;//行情接收时间
    private String pubTime;//行情发布时间
    private Integer mdEntries;//行情发布时间

    @Override
    public long getCreated() {
        return createdTimestamp;
    }

    @Override
    public long getEventTime() {
        if (tradeDate!=null && tradeTime!=null) {
            try{
                String time = tradeDate+"_"+tradeTime+".000";
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
