package com.nbcb.factor.job;

import java.util.List;
import java.util.Map;

/**
 * 加载配置处理接口
 * @param <T>
 */
public interface LoadConfigHandler<T> {
    /**
     * 获取策略名称
     * @return 策略名称
     */
    String strategyName();

    /**
     * 加载配置信息
     * @param strategyId 策略id
     * @param strategyName 策略名称
     * @param strategyClass 策略实体
     * @param event 事件对象
     * @return 结果
     */
    List<T> loadConfigHandler(String strategyId,String strategyName,Class strategyClass,Object event);

    /**
     * 加载债券配置信息数据
     */
    void loadBondConfig();

    /**
     * KEY 债券id
     * value 值
     * @return
     */
    Map<String,String> getBondConfig();
}
