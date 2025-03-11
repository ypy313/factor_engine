package com.nbcb.factor.job.gateway;

import com.nbcb.factor.job.Job;

import java.io.Serializable;
import java.util.Properties;

/**
 * 事件网关，负责从收取事件
 * 可以扩展 kafka，数据库，TCP 长连接等
 */
public interface EventInputGateway extends Serializable {
    public void init(Properties para);//初始化配置
    public void start(Job job);//开始接收消息
    public void stop();//停止接收消息
}
