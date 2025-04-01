package com.changtian.factor.flink.aviatorfun.entity;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.math.BigDecimal;
@Getter@Setter
public class IndexCalcResult implements Serializable ,Cloneable{
    private String beginTime;
    private String endTime;
    private BigDecimal value;

    public static IndexCalcResult create(final OhlcParam time, double value) {
        return convert(time,BigDecimal.valueOf(value));
    }

    public static IndexCalcResult convert(final OhlcParam time,BigDecimal value) {
        IndexCalcResult result = new IndexCalcResult();
        result.beginTime = time.getBeginTime();
        result.endTime = time.getEndTime();
        result.value = value;
        return result;
    }
    @Override
    public String toString(){
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("beginTime",beginTime)
                .append("endTime",endTime)
                .append("value",value == null ?null:value.doubleValue())
                .toString();
    }

    @Override
    public IndexCalcResult clone(){
        try {
            return (IndexCalcResult) super.clone();
        }catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
