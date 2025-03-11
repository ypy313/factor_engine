package com.nbcb.factor.flink;

import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.event.forex.FactorFxPmAllMarketData;
import com.nbcb.factor.output.FxPmMarketDataOutput;
import com.nbcb.factor.utils.TaScriptUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.Iterator;

/**
 * 趋势线抽样tick function
 */
public class QsxTickProcessFunction
        extends ProcessWindowFunction<SymbolOutputEvent, Tuple2<String, FxPmMarketDataOutput>, String, TimeWindow> {
    private final String kafkaDefaultTopic;
    private final OutputTag<Tuple2<String,SymbolOutputEvent>> odsSideOutputTag;

    public QsxTickProcessFunction(String kafkaDefaultTopic, OutputTag<Tuple2<String, SymbolOutputEvent>> odsSideOutputTag) {
        this.kafkaDefaultTopic = kafkaDefaultTopic;
        this.odsSideOutputTag = odsSideOutputTag;
    }

    @Override
    public void process(String s, ProcessWindowFunction<SymbolOutputEvent
            , Tuple2<String, FxPmMarketDataOutput>, String, TimeWindow>.Context context
            , Iterable<SymbolOutputEvent> element
            , Collector<Tuple2<String, FxPmMarketDataOutput>> out) throws Exception {
        boolean hasValueBidAndOffer = true;
        Iterator<SymbolOutputEvent> iterator = element.iterator();
        while (hasValueBidAndOffer && iterator.hasNext()) {
            FxPmMarketDataOutput fxPmMarketDataOutput = (FxPmMarketDataOutput) iterator.next();
            //只有行情
            FactorFxPmAllMarketData marketData = fxPmMarketDataOutput.getEventData();
            if (StringUtils.isNotEmpty(marketData.getBid()) && StringUtils.isNotEmpty(marketData.getAsk())) {
                hasValueBidAndOffer = false;
                //计算中间价bid offer判空，抛弃，取最后一条
                double midPrice = TaScriptUtils.clacMidPrice(marketData.getBid(),marketData.getAsk());
                marketData.setMid(String.valueOf(midPrice));
                fxPmMarketDataOutput.setEventData(marketData);
                context.output(odsSideOutputTag,Tuple2.of(kafkaDefaultTopic,fxPmMarketDataOutput));
                out.collect(Tuple2.of(kafkaDefaultTopic,fxPmMarketDataOutput));
            }
        }
    }
}
