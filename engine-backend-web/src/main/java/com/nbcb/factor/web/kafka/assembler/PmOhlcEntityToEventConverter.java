package com.nbcb.factor.web.kafka.assembler;

import com.nbcb.factor.web.entity.PmOhlcMessgeEventDataEntity;
import com.nbcb.factor.web.entity.PmOhlcValueEvent;
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
