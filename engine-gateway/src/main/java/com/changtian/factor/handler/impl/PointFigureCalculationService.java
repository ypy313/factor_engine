package com.changtian.factor.handler.impl;

import com.changtian.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.changtian.factor.event.SymbolOutputEvent;
import com.changtian.factor.flink.aviatorfun.entity.OhlcParam;
import com.changtian.factor.handler.CalculationHandler;
import com.changtian.factor.monitor.signal.CalculationMonitor;
import com.changtian.factor.output.OhlcDetailResult;

import java.util.Collections;
import java.util.List;

public class PointFigureCalculationService
        <T extends LocalCurrencyStrategyInstanceResult, D extends OhlcDetailResult, F extends List<OhlcParam>>
        extends CalculationMonitor
        implements CalculationHandler<T, D, F> {
    @Override
    public List<SymbolOutputEvent<D>> handler(T t, D d, F ohlcParams) {
        return Collections.emptyList();
    }

    @Override
    public String getCalculationType() {
        return "";
    }
}
