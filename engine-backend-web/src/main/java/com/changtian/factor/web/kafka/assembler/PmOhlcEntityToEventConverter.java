package com.changtian.factor.web.kafka.assembler;

import com.changtian.factor.web.entity.PmOhlcMessgeEventDataEntity;
import com.changtian.factor.web.entity.PmOhlcValueEvent;
import org.springframework.stereotype.Component;

@Component
public class PmOhlcEntityToEventConverter extends EventDataToPublishDataConvert<PmOhlcMessgeEventDataEntity, PmOhlcValueEvent>{
    public PmOhlcEntityToEventConverter() {
        super(PmOhlcMessgeEventDataEntity.class, PmOhlcValueEvent.class);
    }
    @Override
    protected PmOhlcValueEvent createTarget() {
        return null;
    }
}
