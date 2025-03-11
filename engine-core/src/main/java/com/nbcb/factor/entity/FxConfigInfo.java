package com.nbcb.factor.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class FxConfigInfo implements Serializable {
    private String kafkaBrokerList;//broker集合",",隔开的字符串
    private String consumerGroupId;//消费组ID
    private String consumeTopics;//消费topic集合
    private boolean consumeEnableAutoCommit = false; //是否自动提交
    private String consumerAutoOffsetReset;
    private String consumerMaxPollRecords;//一次最大记录数
    private String consumeMaxPollIntervalMs;//最长处理时间一天

    private String producerGroupId;//生产组
    private String producerDetailTopic;
    private String producerHisTopic;
    private String producerRsiTopic;

    private String jedisHost;//redis主机配置
    private int jedisTimeout=3000;//redis超时时间
    private int jedisSoTimeout = 2000;
    private int jedisMaxRedirections = 6;
    private String jedisAuth;
    private String strategyConfigUrl;//backend获取因子实例路径

    private int ohlcUpdateFrequency;//ohlc更新频率

    private List<String> ohlcPeriodsList = new ArrayList<>();//OHLC周期集合
    private List<Integer> frames = new ArrayList<>();//点数图格值
}
