package com.changtian.factor.job.engine;

import com.changtian.factor.output.OhlcDetailResult;

import java.io.Serializable;
import java.util.Set;

public interface ForexStrategyInstance <T,D extends OhlcDetailResult> extends ForexEventHandler<T,D>, Serializable {
    /**
     * 初始化策略实例
     * @param t 参数
     */
    void init(T t) throws Exception;

    /**
     * 需要订阅的InstrumentIds
     * @return 货币对（暂时只有一个货币对）
     */
    Set<String> getInstrumentIds();

    /**
     * 获取StrategyInstanceId 实例的唯一标识
     * @return 实例id
     */
    String getStrategyInstanceId();

    /**
     * 输出实例的相关内存数据
     */
    void onDump();

    /**
     * 获得策略模板名称
     * @return 策略名称
     */
    String getStrategyName();

    /**
     * 获取因子实例名称
     * @return 结果
     */
    String getStrategyInstanceName();
}
