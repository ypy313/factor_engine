package com.changtian.factor.job.engine;

import com.changtian.factor.event.SymbolInputEvent;
import com.changtian.factor.event.SymbolOutputEvent;
import com.changtian.factor.flink.aviatorfun.entity.OhlcParam;
import com.changtian.factor.output.OhlcDetailResult;

import java.util.List;

public interface ForexEventHandler <T,D extends OhlcDetailResult>{
    /**
     * 处理bar线时间
     * @param event bar线事件
     * @return 结果
     */
    List<SymbolOutputEvent<D>> processEvent(SymbolInputEvent<T> event, List<OhlcParam> ohlcParams);

    /**
     * 获取StrategyInstanceID 实例的唯一标识
     * @return 实例id
     */
    String getStrategyInstanceId();
}
