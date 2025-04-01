package com.changtian.factor.flink;

import com.changtian.factor.common.JsonUtil;
import com.changtian.factor.event.SymbolOutputEvent;
import com.changtian.factor.event.forex.FactorFxPmAllMarketData;
import com.changtian.factor.output.FxPmMarketDataOutput;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

/**
 * tick数据分发
 */
public class TICKDistributeProcessFunction implements MapFunction<Tuple2<String, SymbolOutputEvent>,Tuple2<String,String>> {

    @Override
    public Tuple2<String, String> map(Tuple2<String, SymbolOutputEvent> value) throws Exception {
        String topic = value.f0;
        FactorFxPmAllMarketData factorFxPmAllMarketData = ((FxPmMarketDataOutput) value.f1).getEventData();
        return Tuple2.of(topic, JsonUtil.toJson(factorFxPmAllMarketData));
    }
}
