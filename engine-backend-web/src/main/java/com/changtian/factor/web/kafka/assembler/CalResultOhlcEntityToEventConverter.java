package com.changtian.factor.web.kafka.assembler;

import com.changtian.factor.web.entity.CalResultData;
import com.changtian.factor.web.entity.CalResultEventDataEntity;
import org.springframework.stereotype.Component;

@Component
public class CalResultOhlcEntityToEventConverter extends EventDataToPublishDataConvert<CalResultEventDataEntity,CalResultData>{
    public CalResultOhlcEntityToEventConverter() {
        super(CalResultEventDataEntity.class, CalResultData.class);
    }
    @Override
    protected CalResultData createTarget(){
        return new CalResultData();
    }
}
