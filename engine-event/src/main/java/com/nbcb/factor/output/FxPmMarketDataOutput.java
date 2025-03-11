package com.nbcb.factor.output;

import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.enums.EventTypeEnum;
import com.nbcb.factor.enums.ResultTypeEnum;
import com.nbcb.factor.event.SymbolInputEvent;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.event.forex.FactorFxPmAllMarketData;
import com.nbcb.factor.event.pm.FactorPreciousMetalMarketData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import static com.nbcb.factor.common.Constants.TA_PATTERN;

@Setter
@Getter
@ToString
public class FxPmMarketDataOutput implements SymbolOutputEvent<FactorFxPmAllMarketData>, Serializable {
    private String  eventId;//事件id
    private String  eventName;//事件名称
    private String eventType;//事件类型
    private long  eventTime;//事件时间
    private String  srcTimestamp;// 来源时间
    private String  resTimestamp;// 接收行情时间
    private String  createTime;//结果产生时间
    private String  updateTime;//结果修改时间。压缩的时候，只更新修改时间
    private String    resultType;//结果类型
    private FactorFxPmAllMarketData eventData;

    public FxPmMarketDataOutput(SymbolInputEvent event, FactorFxPmAllMarketData dataDetailResult){
        this.eventData = dataDetailResult;
        this.eventId = event.getEventId();
        this.eventName = event.getEventName();
        this.srcTimestamp = event.getScQuoteTime();
        this.resTimestamp = DateUtil.timestampFormat(event.getCreateTimestamp(),TA_PATTERN);
        this.eventType = EventTypeEnum.NONE.getCode();
        this.createTime = this.updateTime = DateUtil.format(LocalDateTime.now(),TA_PATTERN);
        this.resultType = ResultTypeEnum.NONE.getCode();
        this.eventTime = this.eventData.getEventTime();
    }

    public FxPmMarketDataOutput(SymbolInputEvent event, FactorPreciousMetalMarketData factorPreciousMetalMarketData){
        this.eventData = convert(factorPreciousMetalMarketData);
        this.eventId = event.getEventId();
        this.eventName = event.getEventName();
        this.srcTimestamp = DateUtil.timestampFormat(event.getScQuoteTime(),TA_PATTERN);
        this.resTimestamp = DateUtil.timestampFormat(event.getCreateTimestamp(),TA_PATTERN);
        this.eventType = EventTypeEnum.NONE.getCode();
        this.createTime= this.updateTime = DateUtil.format(LocalDateTime.now(),TA_PATTERN);
        this.resultType = ResultTypeEnum.NONE.getCode();
        this.eventTime = this.eventData.getEventTime();
    }

    public FactorFxPmAllMarketData convert(FactorPreciousMetalMarketData factorPreciousMetalMarketData){
        FactorFxPmAllMarketData factorFxPmAllMarketData = new FactorFxPmAllMarketData();
        BeanUtils.copyProperties(factorPreciousMetalMarketData,factorFxPmAllMarketData);

        factorFxPmAllMarketData.setSource(StringUtils.isNotEmpty(factorFxPmAllMarketData.getSource())
        ?factorPreciousMetalMarketData.getSource(): factorPreciousMetalMarketData.getDataSource());
        factorFxPmAllMarketData.setSecurityDesc(factorPreciousMetalMarketData.getMarketDataType());
        factorFxPmAllMarketData.setBid(StringUtils.isNotEmpty(factorPreciousMetalMarketData.getBid())?
                factorPreciousMetalMarketData.getBid():factorPreciousMetalMarketData.getBid1());
        factorFxPmAllMarketData.setAsk(StringUtils.isNotEmpty(factorPreciousMetalMarketData.getAsk())?
                factorFxPmAllMarketData.getAsk() : factorPreciousMetalMarketData.getAsk1());

        String bidDate;
        String bidTime;
        String askDate;
        String askTime;
        if(StringUtils.isEmpty(factorFxPmAllMarketData.getBiddate()) && StringUtils.isNotEmpty(factorPreciousMetalMarketData.getBidDate1())){
            String bidDate1 = factorPreciousMetalMarketData.getBidDate1();
            String dateBid = DateUtil.convertDateStr(bidDate1,DateUtil.YYYY_MM_DD_HH_MM_SS_SSS,DateUtil.SEND_TIME_DF);
            bidDate = dateBid.substring(0,8);
            bidTime = dateBid.substring(9,dateBid.length() -1);
        }else {
            bidDate = factorPreciousMetalMarketData.getBiddate();
            bidTime = factorPreciousMetalMarketData.getBidtime();
        }
        if(StringUtils.isEmpty(factorFxPmAllMarketData.getAskdate()) && StringUtils.isNotEmpty(factorPreciousMetalMarketData.getAskDate1())){
            String askDate1 = factorPreciousMetalMarketData.getAskDate1();
            String dateAsk = DateUtil.convertDateStr(askDate1,DateUtil.YYYY_MM_DD_HH_MM_SS_SSS, DateUtil.SEND_TIME_DF);
            askDate = dateAsk.substring(0,8);
            askTime = dateAsk.substring(9,dateAsk.length() -1);
        }else {
            askDate = factorPreciousMetalMarketData.getAskdate();
            askTime = factorPreciousMetalMarketData.getAsktime();
        }

        factorFxPmAllMarketData.setBiddate(bidDate);
        factorFxPmAllMarketData.setBidtime(bidTime);
        factorFxPmAllMarketData.setAskdate(askDate);
        factorFxPmAllMarketData.setAsktime(askTime);

        factorFxPmAllMarketData.setEventTime(Long.valueOf(factorPreciousMetalMarketData.getEventTime()));
        String timeInLocal = factorPreciousMetalMarketData.getTimeInLocal();
        if(StringUtils.isNotEmpty(timeInLocal)){
            Date date = DateUtil.stringToDate(timeInLocal, DateUtil.YYYY_MM_DD_HH_MM_SS_SSS);
            factorFxPmAllMarketData.setCreated(String.valueOf(date.getTime()));
        }else {
            factorFxPmAllMarketData.setCreated(factorPreciousMetalMarketData.getCreated());
        }
        return factorFxPmAllMarketData;
    }

    @Override
    public String eventType() {
        return this.eventType;
    }

    @Override
    public String getInstrumentId() {
        return this.eventData.getSymbol();
    }
}
