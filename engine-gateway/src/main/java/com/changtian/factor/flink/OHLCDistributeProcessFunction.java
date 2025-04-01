package com.changtian.factor.flink;

import com.changtian.factor.common.FxLoadConfig;
import com.changtian.factor.event.OutputEvent;
import com.changtian.factor.output.FxOhlcResultOutputEvent;
import com.changtian.factor.output.OhlcDetailResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import static com.changtian.factor.common.LoadConfig.getProp;
@Slf4j
public class OHLCDistributeProcessFunction extends ProcessFunction<Tuple2<String, FxOhlcResultOutputEvent>, OutputEvent> {
    final String DETAIL_TOPIC = getProp().getProperty(FxLoadConfig.PRODUCER_DETAIL_TOPIC);
    final String HIS_TOPIC = getProp().getProperty(FxLoadConfig.PRODUCER_HIS_TOPIC);
    private static final OutputTag<OutputEvent> detailOutputTag = new OutputTag<OutputEvent>("detail-output"){
        private static final long serialVersionUID = 1L;
    };
    private static final OutputTag<OutputEvent> historyOutputTag = new OutputTag<OutputEvent>("his-output"){
        private static final long serialVersionUID = 1L;
    };

    @Override
    public void processElement(Tuple2<String, FxOhlcResultOutputEvent> value
            , ProcessFunction<Tuple2<String, FxOhlcResultOutputEvent>, OutputEvent>.Context ctx
            , Collector<OutputEvent> out) throws Exception {
        if (value.f0.equals(DETAIL_TOPIC)) {
            // 过滤每5s的tick数据
            if (!"TICK".equals(value.f1.getEventData().getPeriod())) {
                ctx.output(detailOutputTag, value.f1);
            }
            if (log.isDebugEnabled()) {
                OhlcDetailResult data = value.f1.getEventData();
                log.debug("send detail to kafka: {} {}-{} [{} ~ {}]", value.f1.getPkKey()
                        , data.getSymbol(), data.getPeriod(), data.getBeginTime(), data.getEndTime());
            }
        } else if (value.f0.equals(HIS_TOPIC) && !"TICK".equals(value.f1.getEventData().getPeriod())) {
            ctx.output(historyOutputTag, value.f1);
            if (log.isInfoEnabled()) {
                OhlcDetailResult data = value.f1.getEventData();
                log.info("send history to kafka: {}-{}-{} [{} ~ {}]", value.f1.getPkKey(),
                        data.getSymbol(), data.getPeriod(), data.getBeginTime(), data.getEndTime());
            }
        }
        out.collect(value.f1);
    }
}
