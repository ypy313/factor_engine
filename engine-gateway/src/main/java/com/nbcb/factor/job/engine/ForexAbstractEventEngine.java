package com.nbcb.factor.job.engine;

import com.nbcb.factor.enums.CalculationTypeEnum;
import com.nbcb.factor.event.SymbolInputEvent;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.handler.CalculationHandler;
import com.nbcb.factor.job.ForexJob;
import com.nbcb.factor.job.gateway.FactorOutputGateway;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象事件引擎
 */
@Slf4j
@SuppressWarnings("rawtypes")
public abstract class ForexAbstractEventEngine {
    /**
     * job
     */
    @Setter
    protected static ForexJob forexJob;
    /**
     * 事件处理器
     * key 为货币对，有哪一些因子实例的handler要处理这些k线数据
     */
    protected static Map<String, Set<ForexEventHandler>> eventHandlers = new HashMap<>();
    /**
     * 计算handler map
     */
    protected static final Map<String, CalculationHandler> calculationHandlerMap = new ConcurrentHashMap<>();

    public static void putCalculationHandlerMap(String calculationType,CalculationHandler calculationHandler){
        calculationHandlerMap.put(calculationType,calculationHandler);
    }

    public static CalculationHandler getCalculationHandler(String calculationType){
        CalculationHandler calculationHandler = calculationHandlerMap.get(calculationType);
        if(calculationHandler == null){
            //如果为空，执行通用的计算
            calculationHandler = calculationHandlerMap.get(CalculationTypeEnum.GENERAL.getName());
        }
        return calculationHandler;
    }
    /**
     * 注册job
     */
    public void registerJob(ForexJob forexJob){
        ForexAbstractEventEngine.setForexJob(forexJob);
    }
    public abstract void stop();
    public abstract void start();
    public abstract void asyncProcEvent(SymbolInputEvent<?> event);
    public abstract void setFactorOutputGateway(FactorOutputGateway factorOutput);

    @SuppressWarnings("rawtypes")
    public abstract List<SymbolOutputEvent> syncProcEvent(SymbolInputEvent events);

    /**
     * 注册handler
     * @param instrumentId    事件对ID, 其消息（访问初始化，其消息名称不进行更改）
     * @param forexEventHandler 事件处理器
     */
    public void unregister(String instrumentId, ForexEventHandler forexEventHandler) {
        // 如果事件从未登记过
        if (eventHandlers == null || eventHandlers.isEmpty()) {
            log.info("process symbol:{} event handlers is empty", instrumentId);
        } else {
            // 事件已经有登记
            Set<ForexEventHandler> handlerSet = eventHandlers.get(instrumentId);
            // 事件对应的货币尚未登记
            if (handlerSet == null || handlerSet.isEmpty()) {
                log.info("process symbol:{} is not register", instrumentId);
            } else {
                // 移除对应货币的handler
                handlerSet.removeIf(handler ->
                        handler.getStrategyInstanceId().equals(forexEventHandler.getStrategyInstanceId())
                );
                eventHandlers.put(instrumentId, handlerSet);
            }
        }
    }

    /**
     * 注册handler
     * @param instrumentId 货币对
     * @param forexEventHandler 处理类
     */
    public void register(String instrumentId, ForexEventHandler forexEventHandler) {
        // 获取当前货币对的所有handler
        Set<ForexEventHandler> forexEventHandlerSet = eventHandlers.get(instrumentId);
        // 如果事件未登记
        if (forexEventHandlerSet == null || forexEventHandlerSet.isEmpty()) {
            forexEventHandlerSet = new HashSet<>();
            forexEventHandlerSet.add(forexEventHandler);
            eventHandlers.put(instrumentId, forexEventHandlerSet);
        } else {
            // 事件已经登记
            Optional<ForexEventHandler> optionalForexEventHandler = forexEventHandlerSet.stream().filter(
                    handler -> handler.getStrategyInstanceId().equals(forexEventHandler.getStrategyInstanceId())
            ).findFirst();
            ForexEventHandler eventHandler = optionalForexEventHandler.orElse(null);
            // 事件对应的货币对请求未登记
            if (eventHandler == null) {
                forexEventHandlerSet.add(forexEventHandler);
                eventHandlers.put(instrumentId, forexEventHandlerSet);
                log.info("instrument id {} not exists, add handler!", instrumentId);
            } else {
                // 对已登记的事件，先移除后添加，避免重复
                forexEventHandlerSet.removeIf(handler ->
                        handler.getStrategyInstanceId().equals(forexEventHandler.getStrategyInstanceId())
                );
                forexEventHandlerSet.add(forexEventHandler);
                eventHandlers.put(instrumentId, forexEventHandlerSet);
                log.info("instrument id {} already exists, delete first, add later!", instrumentId);
            }
        }
    }
}
