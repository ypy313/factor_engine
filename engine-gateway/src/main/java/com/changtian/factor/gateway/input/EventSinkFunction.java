package com.changtian.factor.gateway.input;

import com.changtian.factor.event.BondInputEvent;
import com.changtian.factor.job.engine.AsyncEventEngine;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * 事件sink
 */
public class EventSinkFunction implements SinkFunction<Tuple1<BondInputEvent>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static AsyncEventEngine engine;
    public EventSinkFunction(AsyncEventEngine engine){
       this.engine = engine;
   }

   public EventSinkFunction(){
        LOGGER.info("EventSinkFunction init");
   }

    @Override
    public void invoke(Tuple1<BondInputEvent> value, Context context) throws Exception {
       LOGGER.info("EventSinkFunction invoke");
       BondInputEvent event1 = value.f0;
       engine.asyncProcEvent(event1);
    }
}
