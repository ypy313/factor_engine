package com.nbcb.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 计算结果
 */
@Getter
@Setter
@ToString
public class CalResultData extends ApplicationEvent {
    private String strategyName;
    private String instanceId;
    private  String instanceName;
    private String factorName;
    private  String symbol;
    private  String ric;
    private  String period;
    private String indexCategory;
    private String summaryType;
    private String beginTime;
    private String endTime;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private String calResult;
    private String eventId;
    private String srcTimestamp;
    public CalResultData(){
        super(CalResultData.class);
    }
    public CalResultData(Map<String ,Object> map){
        super(CalResultData.class);
        Field[] declaredFields = this.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            if (map.get(field.getName())!=null) {
                try{
                    field.set(this,map.get(field.getName()));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
