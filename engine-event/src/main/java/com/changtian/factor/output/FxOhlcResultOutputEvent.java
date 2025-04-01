package com.changtian.factor.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.changtian.factor.common.DateUtil;
import com.changtian.factor.common.JsonUtil;
import com.changtian.factor.enums.EventTypeEnum;
import com.changtian.factor.enums.PeriodEnum;
import com.changtian.factor.enums.ResultTypeEnum;
import com.changtian.factor.event.FactorFxReuterMarketDataEvent;
import com.changtian.factor.event.OutputEvent;
import com.changtian.factor.event.SymbolInputEvent;
import com.changtian.factor.event.SymbolOutputEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import static com.changtian.factor.common.Constants.TA_PATTERN;

@NoArgsConstructor
@Getter@Setter@ToString
public class FxOhlcResultOutputEvent implements SymbolOutputEvent<OhlcDetailResult>, OutputEvent, Serializable {
    private String eventId;//事件ID
    private String pkKey;
    private String eventName;//事件名称
    private String eventType;//事件类型
    private String srcTimestamp;//来源时间，路透行情里的原始时间
    private String resTimestamp;//flink接收行情时间
    private String createTime;//结果产生时间
    private String updateTime;//更新最后时间
    private String resultType;//结果类型
    private OhlcDetailResult eventData;

    public FxOhlcResultOutputEvent(SymbolInputEvent<?> event, OhlcDetailResult dataDetailResult){
        this.eventData = dataDetailResult;
        String source = ((FactorFxReuterMarketDataEvent) event).getEventData().getSource();
        this.eventId = source +'_'+event.getEventId();
        this.eventName = source;
        this.srcTimestamp = event.getScQuoteTime();
        this.resTimestamp = event.getCreateTimestamp();
        this.eventType = EventTypeEnum.OHLC.getCode();
        this.createTime = this.updateTime = DateUtil.format(LocalDateTime.now(),TA_PATTERN);
        this.resultType=EventTypeEnum.NONE.getCode();
        //RMS_75565465412_OHLC_5Min
        this.pkKey = String.format("%s_OHLC_%s",eventId,dataDetailResult.getPeriod());
    }

    public FxOhlcResultOutputEvent(FxPmMarketDataOutput event,OhlcDetailResult detailResult){
        this.eventData = detailResult;
        String source = event.getEventData().getSource();
        this.eventId = source +'_'+event.getEventId();
        this.eventName = source;
        this.eventType = EventTypeEnum.OHLC.getCode();
        this.createTime = this.updateTime = DateUtil.format(LocalDateTime.now(),TA_PATTERN);
        this.resultType= ResultTypeEnum.VALUE.getCode();
        this.pkKey = String.format("%s_OHLC_%s",eventId,detailResult.getPeriod());
    }


    @Override
    public long getCount() {
        return 1;
    }

    @Override
    public String getCompressFlag() {
        return "0";
    }

    @Override
    public String eventType() {
        return this.eventType;
    }

    @Override
    public String getInstrumentId() {
        return this.eventData.getSymbol();
    }

    @Override
    public FxOhlcResultOutputEvent clone() {
        try {
            FxOhlcResultOutputEvent clone = (FxOhlcResultOutputEvent) super.clone();
            clone.eventData = eventData.clone();
            return clone;
        }catch (CloneNotSupportedException e){
            throw new RuntimeException(e);
        }
    }
    public void updatePeriod(PeriodEnum period, String strategyName){
        eventData.updatePeriod(period,strategyName);
        pkKey += period.getCode();
    }

    public static FxOhlcResultOutputEvent fromRedis(String json) throws JsonProcessingException {
        return JsonUtil.toObject(json, FxOhlcResultOutputEvent.class);
    }

    public String toRedisString(){
        try {
            return JsonUtil.toJson(this);
        }catch (JsonProcessingException e){
            throw new IllegalArgumentException(e);
        }
    }
}
