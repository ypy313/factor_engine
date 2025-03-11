package com.nbcb.factor.event.cmds;

import com.nbcb.factor.event.Streamable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@ToString
@Slf4j
public class TheCashMarketTBTEntity implements Streamable {
    private static final long serialVersionUID = 1L;
    private static final String KEY = MethodHandles.lookup().lookupClass().getSimpleName();
    public static final String TOPIC = "SMDS_IN_CFETS_CMDS_BOND_DEAL";
    private final long createdTimestamp = System.currentTimeMillis();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");

    private String globalId;
    private String ric;//唯一值：90001域的值
    private String source;//数据来源 cmdm
    private String execID;//成交编号 对应开发指引字段17
    private String price;//成交价格 单元：元，整数 44域
    private String lastQty;//券面总额 单位：元.整数 32域
    private String preMarketBondIndicator;//上市前债券Y-yes N-no 11889域
    private String tradeMethod;//交易方式 1-协商 3-匿名点击 4-请求报价 5-点击成交 11-匿名拍卖 10317域，过滤后只剩3，5的数据
    private String transactionMethod;//成交方向 取值：TKN,GVN,TRD,DONE 11596域
    private String securityID;//债券代码 48号域
    private String symbol;//债券名称 55域
    private String tradeDate;//成交日期 75域
    private String execType;//成交类型 F-成交 4-撤销 5-更新 150域
    private String tradeType;//成交类别 1-非做市 4-买方做市 5-卖方做市 10319域
    private String tradeTime;//成交时间 10318域
    private String transactTime;//业务发生时间 60域
    private BigDecimal stipulationValue;//收益率 234域
    private String marketIndicator;//市场标识 4-现券买卖
    private String settlType;//清算速度

    @Override
    public long getCreated() {
        return createdTimestamp;
    }

    @Override
    public long getEventTime() {
        if (transactTime!=null) {
            try {
                return LocalDateTime.parse(transactTime,FORMATTER).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
            }catch (Exception e){
                log.info(e.getMessage());
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
