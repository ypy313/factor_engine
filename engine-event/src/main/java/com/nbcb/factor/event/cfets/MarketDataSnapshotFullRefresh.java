package com.nbcb.factor.event.cfets;

import com.nbcb.factor.event.Streamable;
import com.nbcb.factor.event.cmds.NoMDType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
@Getter@Setter@ToString
public class MarketDataSnapshotFullRefresh implements Streamable {
    private static final long serialVersionUID = 1L;
    private static final String KEY= MethodHandles.lookup().lookupClass().getName();
    private static final String TOPIC= "SMDS_IN_CFETS_MARKET_DATA_SNAPSHOT_FULL_REFRESH";
    private final long createTimeStamp = System.currentTimeMillis();
    public String globalId;
    private String beginString;//imix协议版本
    private String msgType;//报文类型
    private String sendingTime;//报文发送时间 格式：YYYYMMDD-HH:MM:SS.sss
    private String senderCompID;//发送方代码
    private String senderSubID;//发送方子标识符
    private String targetCompID;//接收方代码
    private String targetSubID;//接收方子标识符
    private String mdBookType;//行情类型 2-市场深度行情
    private String mdSubBookType;//行情类别 123-做市报价
    private String marketIndicator;//市场，必传 esp 4-现券买卖 xswap 2-利率呼唤 42-标准利率互换 43-标准债券远期
    private String securityType;//债券类型
    private String securityID;//债券代码 合约品种 交易品种代码
    private String symbol;//债券名称 交易品种名称
    private String marketDepth;//档位信息 esp:固定值为10 xswap:固定取值5
    private String noMDEntries;//重复值个数/重复组次数 该场景取值不固定，以实际传输为准 空行情不传该组件
    private List<NoMDEntries> noMDEntriesList;//noMDEntriesList集合
    private String noMDTypes;//重复组次数
    private List<NoMDType> noMDTypeList;//NoMDTypes集合
    private String realTimeUndertakeFlag;//实时承接标识
    private String transactTime;//业务发生时间
    private String accountSubjectType;//账户主题
    private String execID;//CMDS 成交编号 10250
    private BigDecimal lastQty;//券面总额
    private String preMarketBondIndicator;//上市前债券 Y-yes N-no 10253
    /**成交方向 10254
     * tkn指买方接受了卖盘 中间可能有议价过程，通常指利多
     * gvn是指卖方接受了买价，通常指利空；
     * trd是双方各退一步；买价和卖价直接匹配叫done；
     */
    private String transactionMethod;
    private String tradeDate;//成交日期 10257
    private char execType;//成交类型：F-成交 4-撤销 5-更新
    private char tradeType;//成交类别 1-非做市 4-买方做市 5-卖方做市10259
    private String tradeTime;//成交时间10260
    private String stipulationValue;//收益率 10262
    private String tradeLimitDays;//交易期限 10263
    private String side;//J表示固定对浮动利率
    private String legPrice;//固定利率值10265
    private String legBenchmarkCurveName;//浮动利率曲线名称
    private String legBenchmarkSpread;//利差 10267
    private String source;//数据来源 cmds 10268
    private  String ric;//唯一值：48域的值 +=CASHTBT 10269
    private String tradeMethod;//10270

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


    @Setter@Getter@ToString
    public static class NoMDEntries implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 买入/卖出方向
         * 0-BID
         * 1-OFFER
         */
        private String mdEntryType;
        /**
         * 可成交总量（元）
         */
        private BigDecimal  tradeVolume;
        /**
         * 档位信息
         */
        private String mdPriceLevel;
        /**
         * 报价类别
         */
        private String mdQuoteType;
        /**
         * 报价编号
         */
        private String quoteEntryID;
        /**
         * 业务发生时间
         */
        private String mdEntryDate;
        /**
         * 业务发生时间
         */
        private String mdEntryTime;
        /**
         * 净价
         */
        private BigDecimal lastPx;
        /**
         * 到期收益率类型
         */
        private String yieldType;
        /**
         * 到期收益率
         */
        private BigDecimal yield;
        /**
         * 全价
         */
        private BigDecimal mdEntryPx;
        /**
         * 券面总额
         */
        private BigDecimal mdEntrySize;
        /**
         * 清算类型
         */
        private String clearingMethod;
        /**
         * 清算速度
         */
        private String settlType;
        /**
         * 结算日
         */
        private String settlDate;
        /**
         * 结算方式
         */
        private String deliveryType;
        /**
         * 结算品种/币种
         */
        private String settlCurrency;
        /**
         * 汇率
         */
        private BigDecimal settlCurrFxRate;
        /**
         * 买入未匹配量（元）
         * 卖出未匹配量
         */
        private String unMatchQty;//esp无该字段
        /**
         * 该场景取值固定1
         */
        private String noPartyIDs;
        /**
         * NoPartyID集合
         */
        private List<NoPartyID> noPartyIDList;

    }
}
