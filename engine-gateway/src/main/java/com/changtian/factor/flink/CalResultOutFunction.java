package com.changtian.factor.flink;

import com.changtian.factor.event.OutputEvent;
import com.changtian.factor.event.SymbolOutputEvent;
import com.changtian.factor.flink.aviatorfun.entity.OhlcParam;
import com.changtian.factor.output.IndexCalOutResult;
import com.changtian.factor.output.ReviewIndexCalcOutPutEvent;
import com.changtian.factor.output.forex.ForexOhlcOutputEvent;
import lombok.NonNull;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CalResultOutFunction extends ProcessFunction<List<SymbolOutputEvent>, OutputEvent> {
    private OutputTag<OutputEvent> indexCalOutputTag;
    public CalResultOutFunction(OutputTag<OutputEvent> indexCalOutputTag) {
        this.indexCalOutputTag = indexCalOutputTag;
    }
    @Override
    public void processElement(List<SymbolOutputEvent> value,
                               ProcessFunction<List<SymbolOutputEvent>, OutputEvent>.Context ctx,
                               Collector<OutputEvent> out) throws Exception {
        //按实例id去重
        List<ForexOhlcOutputEvent<IndexCalOutResult>> calResult = value.stream()
                .map(e -> (ForexOhlcOutputEvent<IndexCalOutResult>) e).collect(Collectors
                .collectingAndThen(Collectors.toMap(ForexOhlcOutputEvent::getInstrumentId, Function.identity()
                        , (existing, replacement) -> existing), map -> new ArrayList(map.values())));

        for (ForexOhlcOutputEvent<IndexCalOutResult> outputEvent : calResult) {
            List<ReviewIndexCalcOutPutEvent> list = buildIndexInfoOutput(outputEvent);
            for (ReviewIndexCalcOutPutEvent reviewIndexCalcOutPutEvent : list) {
                //组装行情计算结果对象，测分流值kafka
                if ("HIS".equals(outputEvent.getEventData().getSummaryType())) {
                    ctx.output(indexCalOutputTag,reviewIndexCalcOutPutEvent);
                }
                //历史的行情结果输出
                out.collect(reviewIndexCalcOutPutEvent);
            }
        }
    }

    @NonNull
    protected List<ReviewIndexCalcOutPutEvent> buildIndexInfoOutput(ForexOhlcOutputEvent<IndexCalOutResult> forexOhlcOutputEvent){
        List<ReviewIndexCalcOutPutEvent> reviewIndexCalcOutPutEvents = new ArrayList<>();
        Map<String, Object> indexResult = forexOhlcOutputEvent.getCalculationResultMap()
                .entrySet().stream().filter(
                        k -> !k.getKey().startsWith("list_") && !k.getKey().startsWith("TICK")
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        indexResult.forEach((indexName,indexValue)->{
            ReviewIndexCalcOutPutEvent event = new ReviewIndexCalcOutPutEvent().convert(forexOhlcOutputEvent);
            ReviewIndexCalcOutPutEvent.IndexInfoResult data = event.getEventData();
            //set from ohlc
            IndexCalOutResult outResult = forexOhlcOutputEvent.getOhlcDetailResult();
            data.setStrategyName(outResult.getStrategyName());
            data.setSymbol(outResult.getSymbol());
            data.setRic(outResult.getRic());
            data.setPeriod(outResult.getPeriod());
            data.setSource(outResult.getSource());
            data.setSummaryType(outResult.getSummaryType());
            data.setRelationId(outResult.getRelationId());
            OhlcParam ohlcParam = OhlcParam.convert(outResult);
            data.setOhlc(ohlcParam.toOhlcInfo());

            data.setInstanceId(outResult.getInstanceId());
            data.setInstanceName(outResult.getInstanceName());
            data.setFactorName(indexName);
            data.setIndexCategory(outResult.getIndexCategory());

            data.setBeginTime(outResult.getBeginTime());
            data.setEndTime(outResult.getEndTime());
            data.setCalResult(indexValue.toString());
            reviewIndexCalcOutPutEvents.add(event);
        });
        return reviewIndexCalcOutPutEvents;
    }
}
