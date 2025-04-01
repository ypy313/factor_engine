package com.changtian.factor.job.engine;

import com.changtian.factor.event.SymbolInputEvent;
import com.changtian.factor.event.SymbolOutputEvent;

import java.util.List;

public interface FxEventHandler {
    /**
     * 一个行情产生多个计算结果
     */
    List<SymbolOutputEvent> processEvent(SymbolInputEvent event);
}
