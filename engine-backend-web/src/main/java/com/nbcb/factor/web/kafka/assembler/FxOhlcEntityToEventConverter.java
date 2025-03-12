package com.nbcb.factor.web.kafka.assembler;

import com.nbcb.factor.web.entity.OhlcMessgeEventDataEntity;
import com.nbcb.factor.web.entity.OhlcValueEvent;
import org.springframework.stereotype.Component;

/**
 * ohlc信号实体转换类
 */
@Component
public class FxOhlcEntityToEventConverter extends  EventDataToPublishDataConvert<OhlcMessgeEventDataEntity, OhlcValueEvent>{
    public FxOhlcEntityToEventConverter(){
        super(OhlcMessgeEventDataEntity.class, OhlcValueEvent.class);
    }
    @Override
    protected  OhlcValueEvent createTarget(){
        return new OhlcValueEvent();
    }
}
