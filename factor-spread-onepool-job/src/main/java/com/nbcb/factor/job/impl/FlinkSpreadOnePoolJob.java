package com.nbcb.factor.job.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nbcb.factor.common.JsonUtil;
import com.nbcb.factor.common.LoadConfig;
import com.nbcb.factor.event.*;
import com.nbcb.factor.gateway.input.FlinkKafkaEventInputGateway;
import com.nbcb.factor.gateway.output.FlinkOutputGateway;
import com.nbcb.factor.job.Job;
import com.nbcb.factor.job.LoadConfigFactory;
import com.nbcb.factor.job.LoadConfigHandler;
import com.nbcb.factor.job.engine.StrategyInstance;
import com.nbcb.factor.job.engine.SyncEventEngine;
import com.nbcb.factor.strategy.SpreadOnePoolStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 运行在flink环境中
 */
@Slf4j
public class FlinkSpreadOnePoolJob extends Job implements Serializable {
    private static Map<String, LoadConfigHandler> spreadHandlerMap = new HashMap<String, LoadConfigHandler>();

    public static void main(String[] args) {
        try {
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setBufferTimeout(1);
            env.disableOperatorChaining();//禁用算子链，多线程提高效率。一个算子链在一个线程中
            //env.enableCheckpointing(5000);// 失败时自动拉起  事务无法提交暂不开启
            //env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE);

            FlinkKafkaEventInputGateway inputGateway = new FlinkKafkaEventInputGateway();
            Properties properties = LoadConfig.getProp();
            inputGateway.init(properties);

            String brokerList = (String) properties.get("brokerList");

            Properties outputProperties = new Properties();
            outputProperties.put("bootstrap.servers", brokerList);
            //outputProperties.put("transaction.timeout.ms",1000*60*5+"");暂不开启事务
            String topicName = (String) properties.get("producer.topics");


            DataStream<String> kafkaStream = env.addSource(inputGateway.getConsumer()).setParallelism(1);

            SingleOutputStreamOperator<OutputEvent> singleOutputStreamOperator =
                    kafkaStream.flatMap(new FlatMapFunction<String, OutputEvent>() {

                        @Override
                        public void flatMap(String s, Collector<OutputEvent> collector) throws Exception {
                            BondInputEvent e = buildEvent(s);
                            List<OutputEvent> resultOutput = processEvent(e);
                            if (resultOutput != null) {
                                for (OutputEvent rst : resultOutput) {
                                    collector.collect(rst);
                                }
                            }
                        }
                    });

            FlinkKafkaProducer proc = FlinkOutputGateway.createKafkaSinkFunction(topicName, outputProperties);
            singleOutputStreamOperator.addSink(proc).name("FACTOR_ANALYZE_CHINA_BOND_SPREAD");

            env.execute("FactorSpreadOnePool");
        } catch (Exception e) {
            log.error("Exception in FlinkKafkaEventInputGateway", e);
        }
    }

    private static BondInputEvent buildEvent(String eventStr) {
        try {
            BondInputEvent event = EventFactory.create(eventStr);
            return event;
        } catch (Exception e) {
            log.info("can`t create event {} {}", eventStr, e);
            return null;
        }
    }

    private static List<OutputEvent> processEvent(BondInputEvent bondInputEvent) {
        try {
            //加载所有实例
            if (MapUtils.isEmpty(spreadHandlerMap)) {
                LoadConfigFactory loadConfigFactory = new LoadConfigFactory();
                LoadConfigHandler<?> loadConfigHandler = loadConfigFactory.getHandlerMap().get(SpreadOnePoolStrategy.STRATEGY_NAME);
                spreadHandlerMap.put(SpreadOnePoolStrategy.STRATEGY_NAME, loadConfigHandler);
            }
            LoadConfigHandler loadConfigHandler = spreadHandlerMap.get(SpreadOnePoolStrategy.STRATEGY_NAME);
            List<?> configs = loadConfigHandler.loadConfigHandler(null, SpreadOnePoolStrategy.STRATEGY_NAME, SpreadOnePoolStrategy.class, null);
            List<String> configList = JsonUtil.toList(JsonUtil.toJson(configs), String.class);
            if (CollectionUtils.isEmpty(configList)) {
                log.info("configList is empty,bond code is {} eventId is {}", bondInputEvent.getInstrumentId(), bondInputEvent.getEventId());
                return null;
            }
            boolean filterRet = filterByConfigList(configList, bondInputEvent);
            if (filterRet) {
                log.info("bond filtered ,bond code is {} event is {}", bondInputEvent.getInstrumentId(), bondInputEvent.getEventId());
                return null;
            }
            //初始化事件引擎
            if (FlinkSpreadOnePoolJob.eventEngine == null) {
                FlinkSpreadOnePoolJob.eventEngine = new SyncEventEngine();
            }
            FlinkSpreadOnePoolJob.startInFlink();
            for (String configJson : configList) {
                //循环组装策略
                StrategyInstance strategyInstance = new SpreadOnePoolStrategy();
                Map paraMap = JsonUtil.toObject(configJson, Map.class);
                //如果两条腿都与事件无关，则不处理因子实例
                boolean flag = includeInLegs(paraMap, bondInputEvent);
                if (flag) {
                    //如果被剔除
                    strategyInstance.init(paraMap);
                    FlinkSpreadOnePoolJob.addStrategyInstance(strategyInstance);
                }
            }
            return FlinkSpreadOnePoolJob.putEventInFlink(bondInputEvent);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("error in processEvent", ex);
        }
        return null;
    }

    private static boolean filterByConfigList(List<String> configList, BondInputEvent bondInputEvent) {
        if (bondInputEvent == null) {
            //需要剔除
            return true;
        }
        if (bondInputEvent instanceof BondBestOfferBondInputEvent
                || bondInputEvent instanceof BondDealBondInputEvent
                || bondInputEvent instanceof DummyBondInputEvent
                || bondInputEvent instanceof MarketDataSnapshotFullRefreshBondInputEvent
                || bondInputEvent instanceof MarketDataSpreadBondInputEvent) {
            boolean ret = true;
            for (String configJson : configList) {
                try {
                    String instrumentId = bondInputEvent.getInstrumentId();
                    Map paraMap = JsonUtil.toObject(configJson, Map.class);
                    Map marketDataSetting = (Map) paraMap.get("marketDataSetting");
                    String leg1 = (String) marketDataSetting.get("leg1");
                    String leg2 = (String) marketDataSetting.get("leg2");
                    if (StringUtils.equals(leg1, instrumentId)
                            || StringUtils.equals(leg2, instrumentId)) {//找到一次就可以
                        ret = false;
                        break;
                    }
                } catch (JsonProcessingException e) {
                    log.error("JsonProcessingException", e);
                    ret = false;
                }

            }
            return ret;
        } else {
            return false;
        }
    }

    /**
     * 根据因子腿过滤，如果事件的资产id和实例的两条腿无关，则此实例不进行此事件处理
     * @param paraMap
     * @param bondInputEvent
     * @return
     */
    private static boolean includeInLegs(Map paraMap, BondInputEvent bondInputEvent) {
        String instrumentId = bondInputEvent.getInstrumentId();
        Map marketDataSetting = (Map) paraMap.get("marketDataSetting");
        String leg1 = (String) marketDataSetting.get("leg1");
        String leg2 = (String) marketDataSetting.get("leg2");
        if (StringUtils.equals(leg1, instrumentId)
                || StringUtils.equals(leg2, instrumentId)) {
            return true;
        } else {
            return false;
        }
    }
}
