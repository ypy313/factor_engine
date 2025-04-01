package com.changtian.factor.web.kafka;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.changtian.factor.common.AllRedisConstants;
import com.changtian.factor.common.JacksonUtils;
import com.changtian.factor.common.RedisUtil;
import com.changtian.factor.event.OutputEvent;
import com.changtian.factor.web.entity.*;
import com.changtian.factor.web.job.basic.impl.RidingYieldCurveSynJob;
import com.changtian.factor.web.kafka.assembler.CalResultOhlcEntityToEventConverter;
import com.changtian.factor.web.kafka.assembler.FxOhlcEntityToEventConverter;
import com.changtian.factor.web.kafka.assembler.PmOhlcEntityToEventConverter;
import com.changtian.factor.web.mapper.CalResultMapper;
import com.changtian.factor.web.mapper.FactorFxOhlcDetailMapper;
import com.changtian.factor.web.mapper.FactorPmOhlcDetailMapper;
import com.changtian.factor.web.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import static com.changtian.factor.web.kafka.common.KafkaTopicConstants.*;

@Slf4j
@Component
public class KafkaConsumer {
    @Resource
    private FactorFxOhlcDetailMapper factorFxOhlcDetailMapper;
    @Resource
    private FactorPmOhlcDetailMapper factorPmohlcDetailMapper;
    @Resource
    private CalResultMapper calResultMapper;
    @Autowired
    private FxOhlcEntityToEventConverter fxOhlcEntityToEventConverter;
    @Autowired
    private PmOhlcEntityToEventConverter pmOhlcEntityToEventConverter;
    @Autowired
    private CalResultOhlcEntityToEventConverter calResultOhlcEntityToEventConverter;


    @Getter
    private Map<String,BondSpreadService> bondSpreadserviceMap;
    @Getter
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final RedisUtil redisutil = RedisUtil.getInstance();
    @Autowired
    RidingYieldCurveSynJob ridingYieldCurvesynJob;


    public KafkaConsumer(){
        new Thread(()->{
            while (true){
                try{
                    processMessage();
                }catch (InterruptedException e){
                    log.error("kafka signal thread fail!",e);
                }
            }
        }).start();
    }

    /**
     * 监听信号消息
     */
    @KafkaListener(topics = {"FACTOR_ANALYZE_CHINA_BOND_SPREAD"})
    public void bondSpreadMessage(ConsumerRecord<?,?> record){
        try{
            String jsonStr = record.value().toString();
            queue.add(jsonStr);
        }catch (Exception e){
            log.error("error when processing kafka message!",e);
        }
    }

    /**
     * 根据信号分类处理数据存redis
     */
    public void processMessage()throws InterruptedException{
        String jsonStr = queue.take();
        JSONObject jsonMap = JSONUtil.parseObj(jsonStr);
        //信号类型
        String resultType = (String) jsonMap.get("resultType");
        //分类处理
        if (bondSpreadserviceMap.containsKey(resultType)) {
            bondSpreadserviceMap.get(resultType).process(jsonStr);
        }else {
            log.info("kafka unknown resultType");
        }
    }

    /**
     * 注入所有信号处理实现类bean 名称为对应信号resultType
     */
    @Autowired
    public void setBondSpreadserviceMap(Map<String, BondSpreadService> bondSpreadserviceMap) {
        this.bondSpreadserviceMap = bondSpreadserviceMap;
    }

    /**
     * 监听信号消息
     */
    @KafkaListener(topics = {FACTOR_PRECIOUS_METAL_MARKET_DATA})
    public void pmSymbol(ConsumerRecord<?,?> record){
        String recordJson = record.value().toString();
        if (StringUtils.isEmpty(recordJson)) {
            log.error("pmSymbol kafka record is null");
        }
        try{
            //解析json数据
            PmMarketDataKafkaToEvent pmMarketDataKafkaToEvent = JSONUtil.toBean(recordJson, PmMarketDataKafkaToEvent.class);
            //将贵金属的资产symbol存入redis中
            if (pmMarketDataKafkaToEvent.getSymbol().contains("@")) {
                String dataSymbol = pmMarketDataKafkaToEvent.getSymbol();
                String[] split = dataSymbol.split("@");
                setSymbolToRedis(split[0].contains(".")?split[0].replace(".",""):split[0]);
            }
        }catch (Exception e){
            log.error("监听TOPICS:{},收到消息：{}，解析或推送异常！",record.topic(),recordJson,e);
        }
    }

    public void setSymbolToRedis(String symbol){
        //获取redis中现有配置的所有配置的所有指标，得出indexCategory_symbol
        String indexCategorys = redisutil.getRiding(AllRedisConstants.FACTOR_PM_INDEX_CATEGORY);
        List<String> indexCategoryList = JacksonUtils.toList(indexCategorys, String.class);
        for (String indexCategory : indexCategoryList) {
            String definitionIndexSymbol = "100006"+"_"+indexCategory+"_"+symbol;
            //字母Y用于判断数据库是否存在，如果存在则为Y symbol 不存在则symbolValue为null
            String string = redisutil.getString(AllRedisConstants.FACTOR_PM_SYMBOL, definitionIndexSymbol);
            FactorPmRedisSymbolValue factorPmRedisSymbolValue = new FactorPmRedisSymbolValue();
            if (null == string) {
                factorPmRedisSymbolValue.setValue("N");
            }else {
                factorPmRedisSymbolValue = JSONUtil.toBean(string,FactorPmRedisSymbolValue.class);
            }
            if (!"Y".equals(factorPmRedisSymbolValue.getValue())) {
                //value中N是未同步，Y是同步
                redisutil.setString(AllRedisConstants.FACTOR_PM_SYMBOL,definitionIndexSymbol,JSONUtil.toJsonStr(factorPmRedisSymbolValue));
            }
        }
    }

    /**
     * 骑乘监听信号命令
     */
    @KafkaListener(topics = {FACTOR_ANALYZE_STRATEGY_CMD})
    public void ridingYieldCmd(ConsumerRecord<?,?> record){
        String recordJson = record.value().toString();
        if (StringUtils.isEmpty(recordJson)) {
            log.error("ridingYieldCmd kafka record is null");
        }
        try {
            ridingYieldCurvesynJob.executeJob(recordJson);
        }catch (Exception e){
            log.error("监听topics:{},收到消息：{}，解析或推送异常！",record.topic(),recordJson,e);
        }
    }

    /**
     * 骑乘监听信号命令
     */
    @KafkaListener(topics = {FACTOR_TEXT_MESSAGE})
    public void pushToWorkPlat(ConsumerRecord<?,?> record){
        String recordJson = record.value().toString();
        if (StringUtils.isEmpty(recordJson)) {
            log.error("{} INFORMATION IS NULL",record.topic());
        }
        if (bondSpreadserviceMap.containsKey(OutputEvent.WORK_PLATFORM_SIGNAL)) {
            bondSpreadserviceMap.get(OutputEvent.WORK_PLATFORM_SIGNAL).process(recordJson);
        }else {
            log.info("not have serviceHandler,message is :{}",record);
        }
    }

    private ExecutorService executorService = new ThreadPoolExecutor(
            10,10,1, TimeUnit.MINUTES,new LinkedBlockingQueue<>(4000),
            new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 指标计算结果信号命令
     */
    @KafkaListener(topics = {TOPIC_FACTOR_CAL_RESULT,TOPIC_FACTOR_FX_DETAIL_OHLC,TOPIC_FACTOR_PRECIOUS_METAL_DETAIL_OHLC})
    public void save(ConsumerRecord<?,?> record){
        String recordJson = record.value().toString();
        String topic = record.topic();
        if (StringUtils.isEmpty(recordJson)) {
            log.warn("监听toic is {},收到消息为空！",topic);
            return;
        }
        try {
            executorService.submit(()->{
                if (topic.equals(TOPIC_FACTOR_CAL_RESULT)) {
                    //解析json数据
                    AcceptEventEntity<CalResultEventDataEntity> acceptEventEntity =
                            JacksonUtils.toObject(recordJson,
                                    new TypeReference<AcceptEventEntity<CalResultEventDataEntity>>(){});

                    //转事件类
                    CalResultEventDataEntity eventDataObj = acceptEventEntity.getEventData();
                    CalResultData eventObj = calResultOhlcEntityToEventConverter.convert(eventDataObj);
                    eventObj.setSrcTimestamp(acceptEventEntity.getSrcTimestamp());
                    //保存数据
                    calResultMapper.insertCalResult(eventObj);
                }else if (topic.equals(TOPIC_FACTOR_FX_DETAIL_OHLC)){
                    //解析json数据
                    AcceptEventEntity<OhlcMessgeEventDataEntity> acceptEventEntity =
                            JacksonUtils.toObject(recordJson,
                                    new TypeReference<AcceptEventEntity<OhlcMessgeEventDataEntity>>(){});
                    //转事件类
                    OhlcMessgeEventDataEntity eventDataObj = acceptEventEntity.getEventData();
                    OhlcValueEvent eventObj = fxOhlcEntityToEventConverter.convert(eventDataObj);
                    eventObj.setEventId(acceptEventEntity.getEventId());
                    eventObj.setSrcTimeStamp(acceptEventEntity.getSrcTimestamp());
                    //保存数据
                    factorFxOhlcDetailMapper.insertFactorFxOhlcDetailList(eventObj);
                }else if (topic.equals(TOPIC_FACTOR_PRECIOUS_METAL_DETAIL_OHLC)){
                    //解析json数据
                    AcceptEventEntity<PmOhlcMessgeEventDataEntity> acceptEventEntity =
                            JacksonUtils.toObject(recordJson,
                                    new TypeReference<AcceptEventEntity<PmOhlcMessgeEventDataEntity>>(){});
                    //转事件类
                    PmOhlcMessgeEventDataEntity eventDataObj = acceptEventEntity.getEventData();
                    PmOhlcValueEvent eventObj = pmOhlcEntityToEventConverter.convert(eventDataObj);
                    String[] split = eventObj.getSymbol().split("@");
                    eventObj.setSymbol(split[0]);
                    eventObj.setEventId(acceptEventEntity.getEventId());
                    eventObj.setSrcTimeStamp(acceptEventEntity.getSrcTimestamp());
                    //保存数据
                    factorPmohlcDetailMapper.insertFactorPmOhlcDetailList(eventObj);
                }
            });
        }catch (Exception e){
            log.error("监听topic:{},收到消息：{}，解析或推送异常！",topic,recordJson,e);
        }
    }
}
