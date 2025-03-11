package com.nbcb.factor.market.process.job;

import cn.hutool.json.JSONUtil;
import com.nbcb.factor.common.AllRedisConstants;
import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.common.LoadConfig;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.enums.BrokerEnum;
import com.nbcb.factor.enums.MsgTypeEnum;
import com.nbcb.factor.event.*;
import com.nbcb.factor.event.broker.BrokerBondBestOffer;
import com.nbcb.factor.gateway.input.FlinkKafkaEventInputGateway;
import com.nbcb.factor.gateway.output.FlinkOutputGateway;
import com.nbcb.factor.job.Job;
import com.nbcb.factor.job.LoadConfigFactory;
import com.nbcb.factor.job.impl.LoadMarketProcessHandler;
import com.nbcb.factor.market.process.job.function.BondPriceFlatMapFunction;
import com.nbcb.factor.output.MarketProcessingDealDetailResult;
import com.nbcb.factor.output.MarketProcessingDetailResult;
import com.nbcb.factor.output.MarketProcessingOutPut;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 行情处理job
 */
@Slf4j
public class FlinkMarketProcessJob extends Job implements Serializable {
    //现价排序点差等计算，redis中分开存储实时、静态、计算数据、输出前端展示数据
    private static final String BOND_SORTING_VALUE_RESULT = "BOND_SORTING_VALUE_RESULT";

    public static void main(String[] args) {
        try {
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setBufferTimeout(1);
            env.disableOperatorChaining();//禁用算子链，多线程提高效率。一个算子链在一个线程中

            FlinkKafkaEventInputGateway inputGateway = new FlinkKafkaEventInputGateway();
            Properties properties = LoadConfig.getProp();
            inputGateway.init(properties);

            String brokerList = (String) properties.get("brokerList");

            Properties outputProperties = new Properties();
            outputProperties.put("bootstrap.servers", brokerList);
            String topicName = (String) properties.get("producer.topics");

            // 获取需要重新加载数据库进行查询的命令，以计算最优价的债券
            String definitionList = (String) properties.get("cmd.strategy.definition.list");
            if (StringUtils.isEmpty(definitionList)) {
                log.info("配置参数为空！");
                return;
            }
            String[] definitionArray = definitionList.split(",");
            // 计算最优价
            DataStream<String> kafkaStream = env.addSource(inputGateway.getConsumer()).setParallelism(1);
            SingleOutputStreamOperator<OutputEvent> singleOutputStreamOperator =
                    kafkaStream.flatMap(new FlatMapFunction<String, OutputEvent>() {
                        @Override
                        public void flatMap(String s, Collector<OutputEvent> collector) throws Exception {
                            BondInputEvent e = buildEvent(s);
                            // 如果过滤发生，则 e 为 null
                            if (e != null) {
                                OutputEvent resultOutput = processEvent(e, definitionArray);
                                if (resultOutput != null) {
                                    collector.collect(resultOutput);
                                }
                            }
                        }
                    });

            FlinkKafkaProducer proc = FlinkOutputGateway.createKafkaSinkFunction(topicName, outputProperties);
            singleOutputStreamOperator.addSink(proc).name("topicName");

            SingleOutputStreamOperator<OutputEvent> bondPriceOutputStreamOperator = singleOutputStreamOperator.process(new BondPriceFlatMapFunction());
            FlinkKafkaProducer<OutputEvent> kafkaSinkFunction = FlinkOutputGateway.createKafkaSinkFunction(BOND_SORTING_VALUE_RESULT, outputProperties);
            bondPriceOutputStreamOperator.addSink(kafkaSinkFunction).name(BOND_SORTING_VALUE_RESULT);

            env.execute("FlinkMarketProcessingJob");
        } catch (Exception e) {
            log.error("Exception in FlinkKafkaEventInputGateway:", e);
        }
    }

    /**
     * 处理行i去那个
     *
     * @param bondInputEvent 输入事件
     * @return 结果
     */
    private static OutputEvent processEvent(BondInputEvent bondInputEvent, String[] definitionArray) {
        //通知消息处理
        if (bondInputEvent instanceof StrategyNoticeInputEvent) {
            LoadConfigFactory loadConfigFactory = new LoadConfigFactory();
            //收到债券配置信息初始化完成重新加载
            loadConfigFactory.getHandlerMap().get("BondPrice").loadBondConfig();
            return null;
        } else if (bondInputEvent instanceof StrategyCommandBondInputEvent) {
            log.info("收到命令相关消息不进行处理！eventId:{}", bondInputEvent.getEventId());
            return null;
        }
        return integrationMarket(bondInputEvent);
    }

    private static MarketProcessingOutPut integrationMarket(BondInputEvent bondInputEvent) {
        //获取资产
        String symbol = bondInputEvent.getInstrumentId();
        //从redis获取债券信息
        RedisUtil redisUtil = RedisUtil.getInstance();
        String bestYieldStr = redisUtil.getString(AllRedisConstants.FACTOR_MARKET_PROCESSING_BEST_YIELD, symbol);
        MarketProcessingOutPut outPut = new MarketProcessingOutPut(bondInputEvent);
        Map<String, String> configMap = LoadMarketProcessHandler.getStaticBondConfig();

        if (MapUtils.isEmpty(configMap)) {
            LoadConfigFactory loadConfigFactory = new LoadConfigFactory();
            loadConfigFactory.getHandlerMap().get("BondPrice").loadBondConfig();
            configMap = loadConfigFactory.getHandlerMap().get("BondPrice").getBondConfig();
        }

        String mapperName = configMap.get(symbol);
        outPut.setMapperName(mapperName);
        //如果不为空解析成对象，把最优价赋值为当前对象
        if (StringUtils.isNotBlank(bestYieldStr)) {
            MarketProcessingOutPut hisOutPut = JSONUtil.toBean(bestYieldStr, MarketProcessingOutPut.class);
            outPut.setCfetsBestYield(hisOutPut.getCfetsBestYield());
            outPut.setXBondBestYield(hisOutPut.getXBondBestYield());
            outPut.setEspBestYield(hisOutPut.getEspBestYield());
            outPut.setXSwapBestYield(hisOutPut.getXSwapBestYield());
            outPut.setBrokerBestYieldMap(hisOutPut.getBrokerBestYieldMap());
            outPut.setBrokerBestYield(hisOutPut.getBrokerBestYield());
            outPut.setMarketBestYield(hisOutPut.getMarketBestYield());
            outPut.setCmdsDealYield(hisOutPut.getCmdsDealYield());
            outPut.setBrokerDealYield(hisOutPut.getBrokerDealYield());
            outPut.setMarketDealYield(hisOutPut.getMarketDealYield());
        }

        if (bondInputEvent instanceof MarketDataSnapshotFullRefreshBondInputEvent) {
            //收到是cfets最优行情
            MarketDataSnapshotFullRefreshBondInputEvent event =
                    (MarketDataSnapshotFullRefreshBondInputEvent) bondInputEvent;
            String msgType = event.getMsgType();
            MarketProcessingDetailResult detailResult = new MarketProcessingDetailResult();
            BigDecimal calcBestBidYield = bondInputEvent.calcBestBidYield();
            detailResult.setBestBidYield(calcBestBidYield);
            BigDecimal calcBestOfrYield = bondInputEvent.calcBestOfrYield();
            detailResult.setBestOfrYield(calcBestOfrYield);
            getBestMidYield(detailResult, calcBestBidYield, calcBestOfrYield);
            detailResult.setTradeTime(DateUtil.longToFormatDateString(event.getEventData().getEventTime(), DateUtil.DT_FORMAT));
            BigDecimal calcBestBidVolume = bondInputEvent.calcBestBidVolume();
            detailResult.setBidVolume(calcBestBidVolume == null ? null :
                    calcBestBidVolume.divide(BigDecimal.valueOf(10000)).setScale(4, BigDecimal.ROUND_HALF_UP));
            BigDecimal calcBestOfrVolume = bondInputEvent.calcBestOfrVolume();
            detailResult.setOfrVolume(calcBestOfrVolume == null ? null :
                    calcBestOfrVolume.divide(BigDecimal.valueOf(10000)).setScale(4, BigDecimal.ROUND_HALF_UP));
            detailResult.setBidDataSource(calcBestBidYield == null ? null : msgType);
            detailResult.setOfrDataSource(calcBestOfrYield == null ? null : msgType);

            if (MsgTypeEnum.ESP.getMsgType().equals(msgType)) {
                //ESP
                outPut.setEspBestYield(detailResult);
                //融合cfets最优价
                integrationBestYield(outPut);
                //融合全市场最优价
                integrationMarketBestYield(outPut);
            } else if (MsgTypeEnum.XBOND.getMsgType().equals(msgType)) {
                //XBOND
                outPut.setXBondBestYield(detailResult);
                //融合cfets最优价
                integrationBestYield(outPut);
                //融合全市场最优价
                integrationMarketBestYield(outPut);
            } else if (MsgTypeEnum.XSWAP.getMsgType().equals(msgType)) {
                //XSWAP
                outPut.setXSwapBestYield(detailResult);
            } else {
                log.info("暂时不支持事件：{}，msgType:{}的处理!", bondInputEvent.getEventId(), msgType);
                return null;
            }
        } else if (bondInputEvent instanceof BrokerBondBestOfferBondInputEvent) {
            /**
             * 六家经商
             */
            Set<String> BROKER_TYPE_LIST = Arrays.stream(BrokerEnum.values())
                    .map(BrokerEnum::getBroker).collect(Collectors.toSet());
            BrokerBondBestOfferBondInputEvent event = (BrokerBondBestOfferBondInputEvent) bondInputEvent;
            BrokerBondBestOffer brokerBondBestOffer = ((BrokerBondBestOfferBondInputEvent) bondInputEvent).getEventData();
            MarketProcessingDetailResult detailResult = new MarketProcessingDetailResult();
            BigDecimal calcBestBidYield = bondInputEvent.calcBestBidYield();
            detailResult.setBestBidYield(calcBestBidYield);
            BigDecimal calcBestOfrYield = bondInputEvent.calcBestOfrYield();
            detailResult.setBestOfrYield(calcBestOfrYield);
            getBestMidYield(detailResult, calcBestBidYield, calcBestOfrYield);
            detailResult.setTradeTime(DateUtil.longToFormatDateString(event.getEventData().getEventTime(), DateUtil.DT_FORMAT));
            //报买量与报卖量赋值
            BigDecimal calcBestBidVolume = bondInputEvent.calcBestBidVolume();
            //行情过来是以万元为单位，存储进入redis也以万元为单位
            detailResult.setBidVolume(calcBestBidVolume);
            BigDecimal calcBestOfrVolume = bondInputEvent.calcBestOfrVolume();
            detailResult.setOfrVolume(calcBestOfrVolume);
            detailResult.setBidDataSource(calcBestBidYield == null ? null : brokerBondBestOffer.getBroker());
            detailResult.setOfrDataSource(calcBestOfrYield == null ? null : brokerBondBestOffer.getBroker());
            String broker = brokerBondBestOffer.getBroker();
            if (StringUtils.isNotBlank(broker) && BROKER_TYPE_LIST.contains(broker)) {
                outPut.getBrokerBestYieldMap().put(broker, detailResult);
            } else {
                log.info("暂时不支持事件：{}，broker：{}的处理！", bondInputEvent.getEventId(), broker);
                return null;
            }
            //融合broker最优价
            integrationBrokerBestYield(outPut);
            //融合全市场最优价
            integrationMarketBestYield(outPut);

        } else if (bondInputEvent instanceof BrokerBondDealBondInputEvent) {
            //收到是broker成交行情
            BrokerBondDealBondInputEvent event = (BrokerBondDealBondInputEvent) bondInputEvent;
            MarketProcessingDealDetailResult detailResult = new MarketProcessingDealDetailResult();
            BigDecimal dealYield = ((BrokerBondDealBondInputEvent) bondInputEvent).dealYield();
            detailResult.setDealYield(dealYield);
            detailResult.setTradeTime(DateUtil.longToFormatDateString(event.getEventData().getEventTime(), DateUtil.DT_FORMAT));
            //Broker成交行情
            outPut.setBrokerDealYield(detailResult);
            //融合全市场成交较
            integrationMarketDealYield(outPut);
        } else if (bondInputEvent instanceof CfetsCmdsBondDealInputEvent) {
            //收到cmds成交行情
            CfetsCmdsBondDealInputEvent event = (CfetsCmdsBondDealInputEvent) bondInputEvent;
            MarketProcessingDealDetailResult detailResult = new MarketProcessingDealDetailResult();
            BigDecimal dealYield = ((CfetsCmdsBondDealInputEvent) bondInputEvent).dealYield();
            detailResult.setDealYield(dealYield);
            detailResult.setTradeTime(DateUtil.longToFormatDateString(event.getEventData().getEventTime(), DateUtil.DT_FORMAT));
            //cmds成交行情
            outPut.setCmdsDealYield(detailResult);
            //融合全市场成交价
            integrationMarketDealYield(outPut);
        } else {
            log.info("暂时不支持事件：{}的处理！", bondInputEvent.getEventId());
        }
        //存储到redis
        redisUtil.setString(AllRedisConstants.FACTOR_MARKET_PROCESSING_BEST_YIELD, symbol, JSONUtil.toJsonStr(outPut));
        return outPut;
    }

    /**
     * 融合成最优价
     *
     * @param outPut
     */
    private static void integrationBestYield(MarketProcessingOutPut outPut) {
        MarketProcessingDetailResult bestEspResult = outPut.getEspBestYield();
        MarketProcessingDetailResult bestXbondResult = outPut.getXBondBestYield();
        List<MarketProcessingDetailResult> marketProcessingDetailResults = new ArrayList<>();
        marketProcessingDetailResults.add(bestEspResult);
        marketProcessingDetailResults.add(bestXbondResult);
        //获取最优bid与最优ofr数据
        MarketProcessingDetailResult cfetsBestBidYieldResult = marketProcessingDetailResults.stream().sorted(Comparator.comparing(MarketProcessingDetailResult::getBestBidYield, Comparator.nullsLast(BigDecimal::compareTo))
                        .thenComparing(MarketProcessingDetailResult::getTradeTime, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList()).get(0);
        MarketProcessingDetailResult cfetsBestOfrYieldResult = marketProcessingDetailResults.stream()
                .sorted(Comparator.comparing(MarketProcessingDetailResult::getBestOfrYield, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(MarketProcessingDetailResult::getTradeTime, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList()).get(0);

        MarketProcessingDetailResult cfetsDetailResult = new MarketProcessingDetailResult();
        cfetsDetailResult.setBestBidYield(cfetsBestBidYieldResult.getBestBidYield());
        cfetsDetailResult.setBestOfrYield(cfetsBestOfrYieldResult.getBestOfrYield());
        getBestMidYield(cfetsDetailResult, cfetsBestBidYieldResult.getBestBidYield(), cfetsBestOfrYieldResult.getBestOfrYield());
        getBestTradeTime(cfetsDetailResult, bestEspResult.getTradeTime(), bestXbondResult.getTradeTime());

        //报量数据赋值
        cfetsDetailResult.setBidVolume(cfetsBestBidYieldResult.getBidVolume());
        cfetsDetailResult.setOfrVolume(cfetsBestOfrYieldResult.getOfrVolume());
        //数据源赋值
        cfetsDetailResult.setBidDataSource(cfetsBestBidYieldResult.getBestBidYield() == null ?
                null : cfetsBestBidYieldResult.getBidDataSource());
        cfetsDetailResult.setOfrDataSource(cfetsBestOfrYieldResult.getBestOfrYield() == null ?
                null : cfetsBestOfrYieldResult.getOfrDataSource());

        outPut.setCfetsBestYield(cfetsDetailResult);
    }

    /**
     * 融合成全市场报价最优价
     *
     * @param outPut
     */
    private static void integrationBrokerBestYield(MarketProcessingOutPut outPut) {
        //broker市场报价行情最优
        if (MapUtils.isNotEmpty(outPut.getBrokerBestYieldMap())) {
            List<MarketProcessingDetailResult> bestBidSort = outPut.getBrokerBestYieldMap().values().stream()
                    .sorted(Comparator.comparing(MarketProcessingDetailResult::getBestBidYield, Comparator.nullsLast(BigDecimal::compareTo))
                            .thenComparing(MarketProcessingDetailResult::getTradeTime, Comparator.nullsLast(String::compareTo)))
                    .collect(Collectors.toList());

            List<MarketProcessingDetailResult> bestOfrSort = outPut.getBrokerBestYieldMap().values().stream()
                    .sorted(Comparator.comparing(MarketProcessingDetailResult::getBestOfrYield, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(MarketProcessingDetailResult::getTradeTime, Comparator.nullsLast(String::compareTo)))
                    .collect(Collectors.toList());

            MarketProcessingDetailResult bidDetailResult = new MarketProcessingDetailResult();
            MarketProcessingDetailResult ofrDetailResult = new MarketProcessingDetailResult();
            if (CollectionUtils.isNotEmpty(bestBidSort)) {
                bidDetailResult = bestBidSort.get(0);
            }
            if (CollectionUtils.isNotEmpty(bestOfrSort)) {
                ofrDetailResult = bestOfrSort.get(0);
            }
            MarketProcessingDetailResult brokerDetailResult = new MarketProcessingDetailResult();
            brokerDetailResult.setBestBidYield(bidDetailResult.getBestBidYield());
            brokerDetailResult.setBestOfrYield(ofrDetailResult.getBestOfrYield());
            //数据源与变量值赋值
            brokerDetailResult.setBidDataSource(bidDetailResult.getBestBidYield() == null ? null : bidDetailResult.getBidDataSource());
            brokerDetailResult.setOfrDataSource(ofrDetailResult.getBestOfrYield() == null ? null : ofrDetailResult.getOfrDataSource());
            brokerDetailResult.setBidVolume(bidDetailResult.getBidVolume());
            brokerDetailResult.setOfrVolume(ofrDetailResult.getOfrVolume());

            getBestMidYield(brokerDetailResult, bidDetailResult.getBestBidYield(), ofrDetailResult.getBestOfrYield());
            getBestTradeTime(brokerDetailResult, bidDetailResult.getTradeTime(), ofrDetailResult.getTradeTime());

            outPut.setBrokerBestYield(brokerDetailResult);
        }
    }

    /**
     * 融合成全市场报价最优价
     *
     * @param outPut
     */
    private static void integrationMarketBestYield(MarketProcessingOutPut outPut) {
        //全市场报价最优行情
        List<MarketProcessingDetailResult> marketProcessingDetailResults = new ArrayList<>();
        MarketProcessingDetailResult cfetsBestYield = outPut.getCfetsBestYield();
        MarketProcessingDetailResult brokerBestYield = outPut.getBrokerBestYield();
        marketProcessingDetailResults.add(cfetsBestYield);
        marketProcessingDetailResults.add(brokerBestYield);
        //获取全市场最优bid
        MarketProcessingDetailResult marketBestBidYieldResult = marketProcessingDetailResults.stream()
                .sorted(Comparator.comparing(MarketProcessingDetailResult::getBestBidYield, Comparator.nullsLast(BigDecimal::compareTo))
                        .thenComparing(MarketProcessingDetailResult::getTradeTime, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList()).get(0);

        //全市场最优ofr
        MarketProcessingDetailResult marketBestOfrYieldResult = marketProcessingDetailResults.stream()
                .sorted(Comparator.comparing(MarketProcessingDetailResult::getBestOfrYield, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(MarketProcessingDetailResult::getTradeTime, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList()).get(0);

        MarketProcessingDetailResult marketDetailResult = new MarketProcessingDetailResult();
        marketDetailResult.setBestBidYield(marketBestBidYieldResult.getBestBidYield());
        marketDetailResult.setBestOfrYield(marketBestOfrYieldResult.getBestOfrYield());
        getBestMidYield(marketDetailResult, marketBestBidYieldResult.getBestBidYield(), marketBestOfrYieldResult.getBestOfrYield());
        getBestTradeTime(marketDetailResult, cfetsBestYield.getTradeTime(), brokerBestYield.getTradeTime());
        //报量赋值
        marketDetailResult.setBidVolume(marketBestBidYieldResult.getBidVolume());
        marketDetailResult.setOfrVolume(marketBestOfrYieldResult.getOfrVolume());
        //数据源赋值
        marketDetailResult.setBidDataSource(marketBestBidYieldResult.getBestBidYield() == null ?
                null : marketBestBidYieldResult.getBidDataSource());
        marketDetailResult.setOfrDataSource(marketBestOfrYieldResult.getBestOfrYield() == null ?
                null : marketBestOfrYieldResult.getOfrDataSource());
        outPut.setMarketBestYield(marketDetailResult);
    }

    /**
     * 融合成全市场成交最优价
     *
     * @param outPut
     */
    private static void integrationMarketDealYield(MarketProcessingOutPut outPut) {
        String brokerTradeTime = outPut.getBrokerDealYield().getTradeTime();
        String cmdsTradeTime = outPut.getCmdsDealYield().getTradeTime();
        //全市场成交行情最优
        MarketProcessingDealDetailResult marketDetailResult;
        if (StringUtils.isEmpty(brokerTradeTime) || StringUtils.isEmpty(cmdsTradeTime)) {
            outPut.setMarketDealYield(StringUtils.isEmpty(brokerTradeTime) ? outPut.getCmdsDealYield()
                    : outPut.getBrokerDealYield());
        } else {
            if (brokerTradeTime.compareTo(cmdsTradeTime) >= 0) {
                marketDetailResult = outPut.getBrokerDealYield();
            } else {
                marketDetailResult = outPut.getCmdsDealYield();
            }
            outPut.setMarketDealYield(marketDetailResult);
        }
    }

    /**
     * 接收报文转换成事件
     * @param eventStr 报文
     * @return 结果
     */
    private static BondInputEvent buildEvent(String eventStr){
        try{
            BondInputEvent event = EventFactory.createMarketProcessing(eventStr);
            return event;
        }catch (Exception e){
            log.info("can`t create event {} {}",eventStr,e);
            return null;
        }
    }

    /**
     * 比较中间价
     * @param detailResult detailResult
     * @param calcBestBidYield calcBestBidYield
     * @param calcBestOfrYield calcBestOfrYield
     */
    public static void getBestMidYield(MarketProcessingDetailResult detailResult,
                                       BigDecimal calcBestBidYield,BigDecimal calcBestOfrYield) {
        //bid 或 ofr 最优一边有
        if(null ==  calcBestBidYield || null == calcBestOfrYield){
            detailResult.setBestMidYield(null != calcBestBidYield?calcBestBidYield:calcBestOfrYield);
        }else {
            detailResult.setBestMidYield(calcBestBidYield.add(calcBestOfrYield)
                    .divide(BigDecimal.valueOf(2),6, RoundingMode.HALF_UP));
        }
    }

    /**
     * 比较时间最新
     * @param brokerDetailResult brokerDetailResult
     * @param bidTradeTime bidTradeTime
     * @param ofrTradeTime ofrTradeTime
     */
    public static void getBestTradeTime(MarketProcessingDetailResult brokerDetailResult,
                                        String bidTradeTime,String ofrTradeTime){
        if (StringUtils.isAnyBlank(bidTradeTime,ofrTradeTime)) {
            brokerDetailResult.setTradeTime(StringUtils.isEmpty(bidTradeTime)?ofrTradeTime:bidTradeTime);
        }else {
            if (bidTradeTime.compareTo(ofrTradeTime) >=0) {
                brokerDetailResult.setTradeTime(bidTradeTime);
            }else {
                brokerDetailResult.setTradeTime(ofrTradeTime);
            }
        }
    }
}
