package com.nbcb.factor.job.engine;

import com.nbcb.factor.event.SymbolInputEvent;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.flink.aviatorfun.entity.OhlcParam;
import com.nbcb.factor.output.OhlcDetailResult;

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
