package com.changtian.factor.flink.aviatorfun.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.changtian.factor.output.FxOhlcResultOutputEvent;
import com.changtian.factor.output.IndexCalOutResult;
import com.changtian.factor.output.OhlcDetailResult;
import lombok.Data;

/**
 * ohlc信息
 */
@Data
public class OhlcParam implements Cloneable{
    private String symbol;//货币对
    private String period;//1D 1H...
    @JsonAlias("begin")
    private String beginTime;
    @JsonAlias("end")
    private String endTime;
    @JsonAlias("o")
    private double openPrice;//开盘价
    @JsonAlias("h")
    private double highPrice;//最高价
    @JsonAlias("l")
    private double lowPrice;//最低价
    @JsonAlias("c")
    private double closePrice;//收盘价
    @JsonAlias("srcTimestamp")
    private String srcTimestamp;//srcTimestamp

    public static OhlcParam convert(OhlcDetailResult ohlcDetailResult, String srcTimestamp){
        OhlcParam ohlcParam = new OhlcParam();
        ohlcParam.setSymbol(ohlcDetailResult.getSymbol());
        ohlcParam.setPeriod(ohlcDetailResult.getPeriod());
        ohlcParam.setBeginTime(ohlcDetailResult.getBeginTime());
        ohlcParam.setEndTime(ohlcDetailResult.getEndTime());
        ohlcParam.setOpenPrice(ohlcDetailResult.getOpenPrice());
        ohlcParam.setHighPrice(ohlcDetailResult.getHighPrice());
        ohlcParam.setLowPrice(ohlcDetailResult.getLowPrice());
        ohlcParam.setClosePrice(ohlcDetailResult.getClosePrice());
        ohlcParam.setSrcTimestamp(srcTimestamp);
        return ohlcParam;
    }
    public static OhlcParam convert(IndexCalcResult indexCalcResult){
        OhlcParam ohlcParam = new OhlcParam();
        ohlcParam.setBeginTime(indexCalcResult.getBeginTime());
        ohlcParam.setEndTime(indexCalcResult.getEndTime());
        return ohlcParam;
    }

    public static OhlcParam convert(FxOhlcResultOutputEvent fxOhlcResultOutputEvent){
        OhlcParam ohlcParam = new OhlcParam();
        OhlcDetailResult eventData = fxOhlcResultOutputEvent.getEventData();
        ohlcParam.setOpenPrice(eventData.getOpenPrice());
        ohlcParam.setHighPrice(eventData.getHighPrice());
        ohlcParam.setLowPrice(eventData.getLowPrice());
        ohlcParam.setClosePrice(eventData.getClosePrice());
        return ohlcParam;
    }

    public static OhlcParam convert(IndexCalOutResult indexCalcResult){
        OhlcParam ohlcParam = new OhlcParam();
        ohlcParam.setOpenPrice(indexCalcResult.getOpenPrice());
        ohlcParam.setHighPrice(indexCalcResult.getHighPrice());
        ohlcParam.setLowPrice(indexCalcResult.getLowPrice());
        ohlcParam.setClosePrice(indexCalcResult.getClosePrice());
        return ohlcParam;
    }


    @Override
    public String toString() {
        return "{" +
                "symbol='" + symbol + '\'' +
                ", period='" + period + '\'' +
                ", beginTime='" + beginTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", o=" + openPrice +
                ", h=" + highPrice +
                ", l=" + lowPrice +
                ", c=" + closePrice +
                '}';
    }

    /**
     * 加快序列化的手段
     * @return
     */
    public String toJsonString(){
        return "{" +
                "\"symbol\":\""+symbol+'"'+
                "\"period\":\""+period+'"'+
                "\"begin\":\""+beginTime+'"'+
                "\"end\":\""+endTime+'"'+
                "\"o\":\""+openPrice+'"'+
                "\"h\":\""+highPrice+'"'+
                "\"l\":\""+lowPrice+'"'+
                "\"c\":\""+closePrice+'"'+
                '}';
    }

    /**
     * ohlc数据信息打印
     * @return
     */
    public String toOhlcInfo(){
        return "{" +
                "\"o\":"+openPrice+
                ",\"h\":"+highPrice+
                ",\"l\":"+lowPrice+
                ",\"c\":"+closePrice+
                '}';
    }

    @Override
    public OhlcParam clone(){
        try {
            return (OhlcParam) super.clone();
        }catch (CloneNotSupportedException e){
            throw new UnsupportedOperationException(e);
        }
    }
}
