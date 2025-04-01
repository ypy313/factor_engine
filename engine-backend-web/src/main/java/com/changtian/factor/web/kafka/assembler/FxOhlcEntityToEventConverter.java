package com.changtian.factor.web.kafka.assembler;

import com.changtian.factor.web.entity.OhlcMessgeEventDataEntity;
import com.changtian.factor.web.entity.OhlcValueEvent;
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
