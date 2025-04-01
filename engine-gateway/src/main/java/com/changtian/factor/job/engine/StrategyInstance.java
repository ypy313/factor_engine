package com.changtian.factor.job.engine;


import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 策略实例，实现消息处理
 */
public interface StrategyInstance extends EventHandler, Serializable {
    void init(Map paraMap);//初始化策略实例
    Set<String> getEventNames();//需要订阅的消息类型
    Set<String> getInstrumentIds();//需要订阅的InstrumentIds
    String getStrategyInstanceId();//获取StrategyInstanceID,实例的唯一标识
    void onDump();//输出实例的相关内存数据
    String getStrategyName();//获得策略模板名称
    String getStrategyInstanceName();//获取StrategyInstanceName
}
