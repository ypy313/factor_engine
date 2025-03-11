package com.nbcb.factor.common;

import com.nbcb.factor.entity.FxConfigInfo;
import com.sun.org.apache.bcel.internal.generic.PUSH;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class FxLoadConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    public static volatile FxConfigInfo fxConfigInfo;

    //........ohlc........
    public static final String OHLC_UPDATE_FREQUENCY = "ohlc.update.frequency";
    public static final String PRODUCER_DETAIL_TOPIC = "producer.detail.topic";
    public static final String PRODUCER_HIS_TOPIC = "producer.his.topic";
    public static final String PRODUCER_RSI_TOPIC = "producer.rsi.topic";
    public static final String PRODUCER_INDEX_TOPIC = "producer.index.topic";
    public static final String BROKER_LIST = "kafka.brokerList";
    public static final String OHLC_HISTORY_URL = "ohlc-history.url";
    public static final String OHLC_PERIOD_LIST = "ohlc.period.list";

    public static final String RSI_DEFAULT_CALC_EACH_TIME_MS = "0";
    public static final String RSI_DEFAULT_CALC_EACH_TIME_MS_CONFIG = "rsi.rate-limiter.default-calc-each-time-ms";
    public static final String RSI_CALC_EACH_TIME_MS_PERIOD_FORMAT = "rsi.rate-limiter.calc-each-time-ms.%s";

    public static final String QSX_TICK_UPDATE_FREQUENCY = "qsx.tick.update.frequency";
    public static final String PRODUCER_TICK_TOPIC = "producer.tick.topic";
    public static final String PRODUCER_FACTOR_TEXT_MESSAGE = "producer.text.message.topic";
    public static final String PRODUCER_FACTOR_WEB_VIEW_SIGNAL = "producer.web.view.signal.topic";
    public static final String WATER_CONFIG = "water.config";

    private static void initConfig() {
        log.info("====initConfig====");
        Properties prop = LoadConfig.getProp();
        log.info("=====initConfig prop == null?{}", prop == null);
        if (prop == null) {
            fxConfigInfo = null;
        } else {
            fxConfigInfo = new FxConfigInfo();
            //kafka broker
            fxConfigInfo.setKafkaBrokerList(prop.getProperty("kafka.brokerList"));
            //kafka consume config
            fxConfigInfo.setConsumerGroupId(prop.getProperty("consumer.groupId"));
            fxConfigInfo.setConsumeTopics(prop.getProperty("consume.topics"));
            fxConfigInfo.setConsumeEnableAutoCommit(Boolean.parseBoolean(prop.getProperty("consume.enable.auto.commit")));
            fxConfigInfo.setConsumerAutoOffsetReset(prop.getProperty("consume.auto.offset.reset"));
            fxConfigInfo.setConsumeMaxPollIntervalMs(prop.getProperty("consume.max.poll.interval.ms"));
            //kafka producer config
            fxConfigInfo.setProducerGroupId(prop.getProperty("producer.groupId"));
            fxConfigInfo.setProducerDetailTopic(prop.getProperty("producer.detail.topic"));
            fxConfigInfo.setProducerHisTopic(prop.getProperty("producer.his.topic"));
            fxConfigInfo.setProducerRsiTopic(prop.getProperty("producer.rsi.topic"));
            //redis config
            fxConfigInfo.setJedisHost(prop.getProperty("jedis.host"));
            fxConfigInfo.setJedisTimeout(Integer.parseInt(prop.getProperty("jedis.timeout")));
            fxConfigInfo.setJedisSoTimeout(Integer.parseInt(prop.getProperty("jedis.soTimeout")));
            fxConfigInfo.setJedisMaxRedirections(Integer.parseInt(prop.getProperty("jedis.maxRedirections")));
            fxConfigInfo.setJedisAuth(prop.getProperty("jedis.auth"));
            //backend url
            fxConfigInfo.setStrategyConfigUrl(prop.getProperty("strategy_config.url"));
            fxConfigInfo.setOhlcUpdateFrequency(Integer.parseInt(prop.getProperty("ohlc.update.frequency")));
            //ohlc 周期
            String periodStr = prop.getProperty("ohlc.period.list");
            //点数图格数
            String frameStr = prop.getProperty("ohlc.frame.list");
            List<String> frames = Arrays.asList(frameStr.split(","));
            List<String> periodList = Arrays.asList(periodStr.split(","));
            fxConfigInfo.setOhlcPeriodsList(periodList);
            fxConfigInfo.setFrames(frames.stream().map(Integer::valueOf).collect(Collectors.toList()));
        }
    }

    /**
     * 重新加载配置文件
     */
    public void loadConfigInfo() {
        initConfig();
    }
}
