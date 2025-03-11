package com.nbcb.factor.handler.impl;

import com.nbcb.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.flink.aviatorfun.entity.OhlcParam;
import com.nbcb.factor.handler.CalculationHandler;
import com.nbcb.factor.monitor.signal.CalculationMonitor;
import com.nbcb.factor.output.OhlcDetailResult;

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
