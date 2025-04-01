package com.changtian.factor.job.engine;

import com.changtian.factor.event.SymbolInputEvent;
import com.changtian.factor.event.SymbolOutputEvent;
import com.changtian.factor.job.Job;
import com.changtian.factor.job.gateway.FactorOutputGateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class FxAbstractEventEngine {
    protected static Job job;
    protected static Map<String, Map<String, Set<FxEventHandler>>> eventHandlers = new HashMap<>();

    public void registerJob(Job job){
        FxAbstractEventEngine.job = job;}

    public abstract void stop();

    public abstract void start();

    public abstract void asyncProcEvent(SymbolInputEvent event);

    public abstract void setFactorOutPutGateway(FactorOutputGateway factorOutput);

    public abstract List<SymbolOutputEvent> syncProcEvent(SymbolInputEvent event);

}
