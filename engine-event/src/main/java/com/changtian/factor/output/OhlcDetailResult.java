package com.changtian.factor.output;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.changtian.factor.enums.PeriodEnum;
import com.changtian.factor.enums.ResultTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * OHLC eventData详情
 */
@Getter@Setter@ToString
public class OhlcDetailResult implements Serializable ,Cloneable{
    private String strategyName;//策略TA
    private String instanceId;//symbol+period
    private String instanceName;//instanceId
    private String factorName;//ohlc
    private String symbol;//货币对
    private String ric;
    private String period;//1D 1H..
    private String source;//来源
    private String indexCategory;//指标类型
    private String summaryType;//ResultTypeEnum(DETAIL|HIS)
    private String beginTime;//bar开始时间（左闭）
    private String endTime;//bar结束时间（右开）
    private double openPrice;//开盘价
    private double highPrice;//最高价
    private double lowPrice;//最低价
    private double closePrice;//收盘价
    private String[] relationId;

    public static OhlcDetailResult createDetail(String symbol,String period,String strategyName){
        OhlcDetailResult e = create(symbol,period,strategyName);
        e.summaryType = ResultTypeEnum.DETAIL.getCode();
        return e;
    }
    private static OhlcDetailResult create(String symbol,String period,String strategyName){
        OhlcDetailResult e = new OhlcDetailResult();
        e.symbol = symbol;
        e.period = period == null?"":period;
        e.setDefault(strategyName);
        return e;
    }

    private void setDefault(String strategyNameInfo){
        strategyName = strategyNameInfo;
        factorName = indexCategory = "OHLC";
        instanceId = instanceName = symbol+'_'+period;
    }

    public void setOhlc(double o,double h,double l,double c){
        openPrice = o;
        highPrice = h;
        lowPrice = l;
        closePrice = c;
    }

    public void setOhlcId(String openEventId,String highEventId,String lowEventId,String closeEventId){
        relationId = new String[]{openEventId,highEventId,lowEventId,closeEventId};
    }

    /**
     * high
     */
    @JsonIgnore
    public String getHighEventId(){
        return relationId[1];
    }
    public void setHighEventId(String highEventId){
        relationId[1] = highEventId;
    }

    /**
     * low
     */
    @JsonIgnore
    public String getLowEventId(){
        return relationId[2];
    }
    public void setLowEventId(String lowEventId){
        relationId[2] = lowEventId;
    }

    /**
     * close
     */
    public String getCloseEventId(){
        return relationId[3];
    }

    public void setCloseEventId(String closeEventId){
        relationId[3] = closeEventId;
    }


    @Override
    public OhlcDetailResult clone() {
        try {
            return (OhlcDetailResult) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void updatePeriod(PeriodEnum period, String strategyName){
        this.period = period.getCode();
        setDefault(strategyName);
    }
}
