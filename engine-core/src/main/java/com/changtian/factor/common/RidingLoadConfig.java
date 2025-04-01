package com.changtian.factor.common;

import com.changtian.factor.entity.riding.RidingConfigInfo;
import java.io.Serializable;
import java.util.Properties;

/**
 * 加载配置信息
 */
public class RidingLoadConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    public static volatile RidingConfigInfo ridingConfigInfo;
    public static final String BROKER_LIST ="kafka.brokerList";
    
    private static void initConfig(){
        Properties prop = LoadConfig.getProp();
        if (prop==null) {
            ridingConfigInfo = null;
        }else {
            ridingConfigInfo = new RidingConfigInfo();
            //kafka broker
            ridingConfigInfo.setKafkaBrokerList(prop.getProperty("kafka.brokerList"));
            //kafka consume config
            ridingConfigInfo.setConsumeGroupId(prop.getProperty("consumer.groupId"));
            ridingConfigInfo.setConsumeTopics(prop.getProperty("consume.topics"));
            ridingConfigInfo.setConsumeEnableAutoCommit(Boolean.parseBoolean(prop.getProperty("consume.enable.auto.commit")));
            ridingConfigInfo.setConsumeAutoOffsetReset(prop.getProperty("consume.auto.offset.reset"));
            ridingConfigInfo.setConsumeMaxPollRecords(prop.getProperty("consume.max.poll.records"));
            ridingConfigInfo.setConsumeMaxPollIntervalMs(prop.getProperty("consume.max.poll.interval.ms"));
            //flink
            ridingConfigInfo.setSize(Long.parseLong(prop.getProperty("count.window.size")));
            ridingConfigInfo.setSlide(Long.parseLong(prop.getProperty("count.window.slide")));


            //redis config
            ridingConfigInfo.setJedisHost(prop.getProperty("jedis.host"));
            ridingConfigInfo.setJedisTimeout(Integer.parseInt(prop.getProperty("jedis.timeout")));
            ridingConfigInfo.setJedisSoTimeout(Integer.parseInt(prop.getProperty("jedis.soTimeout")));
            ridingConfigInfo.setJedisMaxRedirections(Integer.parseInt(prop.getProperty("jedis.maxRedirections")));
            ridingConfigInfo.setJedisAuth(prop.getProperty("jedis.auth"));
        }
    }

    /**
     * 重新加载配置文件
     */
    public void loadConfigInfo(){
        initConfig();
    }
}
