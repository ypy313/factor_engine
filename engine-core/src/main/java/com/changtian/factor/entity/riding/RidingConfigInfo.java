package com.changtian.factor.entity.riding;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 配置参数
 */
@Getter@Setter@ToString
public class RidingConfigInfo implements Serializable {
    private String kafkaBrokerList;//broker集合","隔开的字符串
    private String consumeGroupId;//消费组id
    private String consumeTopics;//消费topic集合
    private boolean consumeEnableAutoCommit = false;
    private String consumeAutoOffsetReset;
    private String consumeMaxPollRecords;//一次最大记录数
    private String consumeMaxPollIntervalMs;//最长处理时间一天

    private long size;
    private long slide;

    private String jedisHost;//redis主机配置
    private int jedisTimeout=3000;//redis超时时间
    private int jedisSoTimeout=3000;
    private int jedisMaxRedirections=6;
    private String jedisAuth;
}
