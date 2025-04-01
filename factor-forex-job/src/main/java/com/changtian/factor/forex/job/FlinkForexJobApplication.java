package com.changtian.factor.forex.job;

import com.changtian.factor.cache.GeneQueueManager;
import com.changtian.factor.common.Constants;
import com.changtian.factor.common.FxLoadConfig;
import com.changtian.factor.common.LoadConfig;
import com.changtian.factor.common.key.MyKey;
import com.changtian.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.changtian.factor.event.*;
import com.changtian.factor.flink.*;
import com.changtian.factor.gateway.input.FlinkFxKafkaEventInputGateway;
import com.changtian.factor.gateway.output.FlinkOutputGateway;
import com.changtian.factor.gateway.output.JsonSchemaTuple2;
import com.changtian.factor.job.ForexJob;
import com.changtian.factor.job.engine.ForexSyncEventEngine;
import com.changtian.factor.monitor.api.SignalModelHandlerFunction;
import com.changtian.factor.output.FxOhlcResultOutputEvent;
import com.changtian.factor.output.FxPmMarketDataOutput;
import com.changtian.factor.output.OhlcDetailResult;
import com.changtian.factor.strategy.ForexStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * 运行在flink环境
 */
@Slf4j
@SuppressWarnings({"rawtypes","unchecked"})
public class FlinkForexJobApplication extends ForexJob implements Serializable {
    /**
     * 趋势线tick输出
     * @param args
     */
    private static final OutputTag<Tuple2<String,SymbolOutputEvent>> qsxTickOutputTag = new OutputTag<Tuple2<String,SymbolOutputEvent>>("qsx-tick-output"){
      private static final long serialVersionUID = 1L;
    };

    /**
     * 获取detail侧输出流
     * @param args
     */
    private static final OutputTag<OutputEvent> detailOutputTag = new OutputTag<OutputEvent>("detail-output"){
        private static final long serialVersionUID = 1L;
    };

    /**
     * 获取history 测输出流
     * @param args
     */
    private static final OutputTag<OutputEvent> historyOutputTag = new OutputTag<OutputEvent>("his-output"){
        private static final long serialVersionUID = 1L;
    };
    private static final OutputTag<OutputEvent> indexCalOutputTag = new OutputTag<OutputEvent>("index-cal-output"){
        private static final long serialVersionUID = 1L;
    };
    private static final OutputTag<OutputEvent> textMessageOutputTag = new OutputTag<OutputEvent>("text-message-output"){
        private static final long serialVersionUID = 1L;
    };

    public static void main(String[] args) {
        try{
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            //设置窗口以时间时间为准
            env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
            env.setBufferTimeout(1);
            env.setParallelism(1);
            env.disableOperatorChaining();//禁用算子连，多线程提高效率。一个算子链在一个线程中
            env.getConfig().setAutoWatermarkInterval(50);//设置水位线更新频率，多少ms/次

            Properties properties = LoadConfig.getProp();
            FlinkFxKafkaEventInputGateway inputGateway = new FlinkFxKafkaEventInputGateway();
            inputGateway.init(properties);
            Properties outputProperties = new Properties();
            String brokerList = properties.getProperty(FxLoadConfig.BROKER_LIST);
            outputProperties.put("bootstrap.servers",brokerList);

            SingleOutputStreamOperator<SymbolOutputEvent> outputEventDataStream = env.addSource(inputGateway.getConsumer()).flatMap(new FlatMapFunction<Tuple2<String, String>, SymbolOutputEvent>() {
                @Override
                public void flatMap(Tuple2<String, String> topicOrMessage, Collector<SymbolOutputEvent> out) throws Exception {
                    //获取事件
                    SymbolInputEvent inputEvent = buildEvent(topicOrMessage.f0, topicOrMessage.f1);
                    if (null != inputEvent) {
                        if (log.isDebugEnabled()) {
                            log.debug("收到行情：{}", inputEvent.getEventData());
                        }
                        List<SymbolOutputEvent> resultOutput = processEvent(inputEvent);
                        if (!CollectionUtils.isEmpty(resultOutput)) {
                            for (SymbolOutputEvent symbolOutputEvent : resultOutput) {
                                out.collect(symbolOutputEvent);
                            }
                        }
                    }
                }
            }).assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<SymbolOutputEvent>(Time.milliseconds(100)) {
                @Override
                public long extractTimestamp(SymbolOutputEvent symbolOutputEvent) {
                    if (!(symbolOutputEvent instanceof FxPmMarketDataOutput)) {
                        throw new RuntimeException("非行情数据事件，不进行处理！");
                    }
                    return ((FxPmMarketDataOutput) symbolOutputEvent).getEventTime();
                }
            }).process(new ProcessFunction<SymbolOutputEvent, SymbolOutputEvent>() {
                @Override
                public void processElement(SymbolOutputEvent value
                        , ProcessFunction<SymbolOutputEvent, SymbolOutputEvent>.Context ctx
                        , Collector<SymbolOutputEvent> out) throws Exception {
                    long currentWatermark = ctx.timerService().currentWatermark();
                    if (ctx.timestamp()<currentWatermark) {
                        //打印会过滤数据日志
                        log.warn("该数据水位线会过滤，currentWatermark:{} 数据内容为：{}",currentWatermark,value);
                    }
                    out.collect(value);
                }
            });

            //tick数据抽样发送至kafka(默认5s一次)
            SingleOutputStreamOperator<Tuple2<String,FxPmMarketDataOutput>> process = outputEventDataStream.keyBy(SymbolOutputEvent::getInstrumentId)
                    .timeWindow(Time.milliseconds(Long.parseLong(properties.getProperty(FxLoadConfig.QSX_TICK_UPDATE_FREQUENCY))))
                    .process(new QsxTickProcessFunction(properties.getProperty(FxLoadConfig.PRODUCER_TICK_TOPIC), qsxTickOutputTag));

            //tick发送至kafka
            final String QSX_TICK_TOPIC = properties.getProperty(FxLoadConfig.PRODUCER_TICK_TOPIC);
            process.getSideOutput(qsxTickOutputTag).map(new TICKDistributeProcessFunction())
                    .addSink(new FlinkKafkaProducer<>(QSX_TICK_TOPIC,new JsonSchemaTuple2(QSX_TICK_TOPIC),outputProperties,
                            FlinkKafkaProducer.Semantic.AT_LEAST_ONCE)).name(Constants.OUTPUT_TO_KAFKA+brokerList+"]");

            //根据货币对配置时间（默认 200ms）计算ohlc
            SingleOutputStreamOperator<FxOhlcResultOutputEvent> ohlcStream = outputEventDataStream.keyBy(SymbolOutputEvent::getInstrumentId)
                    .timeWindow(Time.milliseconds(Long.parseLong(properties.getProperty(FxLoadConfig.OHLC_UPDATE_FREQUENCY))))
                    .process(new OHLCProcessFunction());

            //融合各周期ohlc线
            SingleOutputStreamOperator<OutputEvent> periodKlineProcessStream = ohlcStream.flatMap(new ProduceOHLCFlatMapFunction("gene"))
                    .process(new OHLCDistributeProcessFunction());

            //融合ohlc结果发送至kafka
            final String DETAIL_TOPIC = properties.getProperty(FxLoadConfig.PRODUCER_DETAIL_TOPIC);
            final String HIS_TOPIC = properties.getProperty(FxLoadConfig.PRODUCER_HIS_TOPIC);
            periodKlineProcessStream.getSideOutput(detailOutputTag)
                    .addSink(FlinkOutputGateway.createKafkaSinkFunction(DETAIL_TOPIC,outputProperties))
                    .name(Constants.OUTPUT_TO_KAFKA+brokerList+"]");

            periodKlineProcessStream.getSideOutput(historyOutputTag)
                    .addSink(FlinkOutputGateway.createKafkaSinkFunction(HIS_TOPIC,outputProperties))
                    .name(Constants.OUTPUT_TO_KAFKA+brokerList+"]");

            SingleOutputStreamOperator<List<SymbolOutputEvent>> outputStreamOperator = periodKlineProcessStream
                    .flatMap(new FlatMapFunction<OutputEvent, List<SymbolOutputEvent>>() {
                        @Override
                        public void flatMap(OutputEvent outputEvent, Collector<List<SymbolOutputEvent>> collector) throws Exception {
                            List<SymbolOutputEvent> symbolOutputEventList = processKlineData(outputEvent);

                            if (!CollectionUtils.isEmpty(symbolOutputEventList)) {
                                for (SymbolOutputEvent event : symbolOutputEventList) {
                                    BeanUtils.copyProperties(outputEvent, event);
                                }
                                collector.collect(symbolOutputEventList);
                            }
                        }
                    });

            //指标计算结果转化输出
            SingleOutputStreamOperator<OutputEvent> calResult = outputStreamOperator
                    .process(new CalResultOutFunction(indexCalOutputTag));
            //发送所有指标的计算结果
            final String INDEX_TOPIC = properties.getProperty(FxLoadConfig.PRODUCER_INDEX_TOPIC);
            calResult.addSink(FlinkOutputGateway.createKafkaSinkFunction(INDEX_TOPIC,outputProperties));
            //out_put中his计算结果
            final String FACTOR_RSI_TOPIC = properties.getProperty(FxLoadConfig.PRODUCER_RSI_TOPIC);
            calResult.getSideOutput(indexCalOutputTag).addSink(FlinkOutputGateway.createKafkaSinkFunction(FACTOR_RSI_TOPIC,outputProperties))
                    .name(Constants.OUTPUT_TO_KAFKA+brokerList+"]");
            //监控处理
            SingleOutputStreamOperator<SymbolOutputEvent> outputIndexStreamOperator = outputStreamOperator.flatMap(new FlatMapFunction<List<SymbolOutputEvent>, SymbolOutputEvent>() {

                @Override
                public void flatMap(List<SymbolOutputEvent> value, Collector<SymbolOutputEvent> out) throws Exception {
                    for (SymbolOutputEvent symbolOutputEvent : value) {
                        out.collect(symbolOutputEvent);
                    }
                }
            });
            //对计算结果进行不同的指标监控
            SingleOutputStreamOperator<OutputEvent> signalAndValueStream = outputIndexStreamOperator
                    .process(new SignalModelHandlerFunction());

            final String FACTOR_TEXT_MESSAGE = properties.getProperty(FxLoadConfig.PRODUCER_FACTOR_TEXT_MESSAGE);
            final String FACTOR_WEB_VIEW_SIGNAL = properties.getProperty(FxLoadConfig.PRODUCER_FACTOR_WEB_VIEW_SIGNAL);
            //发送计算结果与所有交易信号与kafka
            signalAndValueStream.addSink(FlinkOutputGateway.createKafkaSinkFunction(FACTOR_RSI_TOPIC,outputProperties));
            //过滤信号分别图iOS那个至前端展示信号与推送至交易平台信号
            SingleOutputStreamOperator<OutputEvent> signalProcess = signalAndValueStream.process(new OHlCSignalFilterFunction());
            //推送至前端的交易信息
            signalProcess.addSink(FlinkOutputGateway.createKafkaSinkFunction(FACTOR_WEB_VIEW_SIGNAL,outputProperties));
            signalProcess.getSideOutput(textMessageOutputTag)
                    .addSink(FlinkOutputGateway.createKafkaSinkFunction(FACTOR_TEXT_MESSAGE,outputProperties))
                    .name(Constants.OUTPUT_TO_KAFKA+brokerList+"]");
            env.execute("FACTOR_JOB");
        }catch (Exception e){
            log.error("start flink forex job fail!",e);
        }
    }

    /**
     * 对因子事件的处理
     * @param inputEvent 输入事件
     * @return 结果
     */
    private static List<SymbolOutputEvent> processEvent(SymbolInputEvent inputEvent){
        //如果是行情事件
        if (inputEvent instanceof FactorFxReuterMarketDataEvent) {
            List<SymbolOutputEvent> symbolOutputEventList = new ArrayList<>();
            FactorFxReuterMarketDataEvent factorFxReuterMarketDataEvent = (FactorFxReuterMarketDataEvent) inputEvent;
            FxPmMarketDataOutput dataOutput = new FxPmMarketDataOutput(
                    inputEvent,
                    factorFxReuterMarketDataEvent.getFactorFxPmAllMarketData());
            symbolOutputEventList.add(dataOutput);
            return symbolOutputEventList;
        }else if (inputEvent instanceof StrategyCommandSymbolInputEvent){
            StrategyCommand eventData = (StrategyCommand) inputEvent.getEventData();
            String strateyName = eventData.getStrategyName();
            if (!strateyName.equals("TA")) {
                log.info("外汇接收cmd命令，strategyName:{} 过滤",strateyName);
                return Collections.emptyList();
            }
        }
        return putForexEventInFlink(inputEvent);
    }

    /**
     * 消息转event
     * @param topic topic
     * @param message 消息
     * @return 结果
     */
    private static SymbolInputEvent buildEvent(String topic, String message) {
        try {
            return EventFactory.createFx(topic,message);
        }catch (Exception ex){
            log.info("can`t create event :{} :{}",message,ex);
            return null;
        }
    }

    /**
     * 处理K线数据
     * @param outputEvent  各周期k线数据
     * @return  结果
     */
    private static List<SymbolOutputEvent> processKlineData(OutputEvent outputEvent){
        //初始化事件引擎
        if (ForexJob.forexAbstractEventEngine == null) {
            ForexJob.forexAbstractEventEngine = new ForexSyncEventEngine();
            //注入计算handler
            ForexJob.injectCalculationHandler();
        }
        //验证是否刷新配置
        boolean isRefreshConfigFlag = ForexJob.checkIsRefreshConfig();
        if (isRefreshConfigFlag) {
            //返回的是LocalCurrencyStrategyInstanceResult结果集
            List<LocalCurrencyStrategyInstanceResult> forexConfigList =
                    ForexJob.loadForexStrategyConfig(ForexStrategy.STRATEGY_NAME);
            //是否更新加载实例配置状态更新
            ForexSyncEventEngine.setForexRefreshFlag(false);
            if (CollectionUtils.isEmpty(forexConfigList)) {
                log.warn("query config list is empty!");
                return Collections.emptyList();
            }
            //校验当前bar线是否在因子实例的计算范围，如果没有忽略
            boolean filterRet = filterByConfigList(forexConfigList,outputEvent.getEventData());
            if (!filterRet) {
                log.info("symbol filtered ,symbol code is {} eventId is {}",
                        outputEvent.getInstrumentId(),outputEvent.getEventId());
                return Collections.emptyList();
            }
            //每个因子实例一个计算策略
            for (LocalCurrencyStrategyInstanceResult config : forexConfigList) {
                String formula = config.getFormula();
                if (StringUtils.isEmpty(formula)) {
                    log.warn("formula is null,no calculation ,instanceId is :{}",config.getInstanceId());
                    continue;
                }
                ForexStrategy<LocalCurrencyStrategyInstanceResult, OhlcDetailResult> forexStrategy = new ForexStrategy();
                try{
                    forexStrategy.init(config);
                }catch (Exception e){
                    log.error("init forex strategy fail!",e);
                    return Collections.emptyList();
                }
                String instanceIdSymbolPeriod = config.getInstanceId() +"_"+
                        config.getMarketDataSetting().getLeg()+"_"+config.getPeriodsSetting().getOhlc();
                ForexJob.addStrategyInstance(instanceIdSymbolPeriod,forexStrategy);
                //公共ohlc队列
                GeneQueueManager instance = GeneQueueManager.getInstance();
                instance.getOrCreateQueue(new MyKey(config.getMarketDataSetting().getLeg()
                ,config.getPeriodsSetting().getOhlc(),""));
            }
        }
        return ForexJob.putForexEventInFlink(new ForexOhlcInputEvent(outputEvent));
    }
}
