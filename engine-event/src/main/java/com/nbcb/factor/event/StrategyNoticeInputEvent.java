package com.nbcb.factor.event;

import com.nbcb.factor.enums.DataProcessNoticeTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class StrategyNoticeInputEvent implements BondInputEvent<String>{
    private static final long serialVersionUID = 1L;
    public static final String TOPIC = "FACTOR_ANALYZE_STRATEGY_NOTICE";
    private long createTimeStamp;
    private String eventId;//时间id
    private String eventName;//时间名称
    private String notice = "finish";//命令
    private String key;
    private String noticeTime;//通知时间
    private String strategyName;//策略名称
    //通知类型 BASIC_DATA_PROCESS 基础数据处理 RIDING_YIELD_JOB骑乘job通知 默认认为骑乘通知
    private String noticeType = DataProcessNoticeTypeEnum.RIDING_YIELD_JOB.getKey();
    private List<StrategyNoticeDetail> data = new ArrayList<>();
    @Override
    public String getEventId() {
        return eventId;
    }
    @Override
    public String getEventName() {
        return eventName;
    }
    @Override
    public void setEventData(String eventData) {

    }

    @Override
    public BigDecimal calcBestBidYield() {
        return null;
    }

    @Override
    public BigDecimal calcBestOfrYield() {
        return null;
    }

    @Override
    public BigDecimal calcBestBidVolume() {
        return null;
    }

    @Override
    public BigDecimal calcBestOfrVolume() {
        return null;
    }

    @Override
    public String getCreateTimestamp() {
        return null;
    }

    @Override
    public String getScQuoteTime() {
        return null;
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public Object getEventData() {
        return null;
    }

    @Override
    public void setInstrumentId(String instrumentId) {

    }

    @Override
    public String getInstrumentId() {
        return null;
    }
}
