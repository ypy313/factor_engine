package com.nbcb.factor.job.engine;

import com.nbcb.factor.cache.GeneQueueManager;
import com.nbcb.factor.common.StrategyNameEnum;
import com.nbcb.factor.event.*;
import com.nbcb.factor.flink.aviatorfun.entity.OhlcParam;
import com.nbcb.factor.job.gateway.FactorOutputGateway;
import com.nbcb.factor.monitor.signal.SignalCacheMap;
import com.nbcb.factor.output.OhlcDetailResult;
import com.nbcb.factor.strategy.StrategyCmdEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 运行在flink环境中的engine,只做事件同步处理，多线程等交给flink环境处理
 */
@Slf4j
@SuppressWarnings({"rawtypes"})
public class ForexSyncEventEngine extends ForexAbstractEventEngine {
    /**
     * 外汇是否重新加载因子实例true加载 false 不加载
     */
    @Setter
    @Getter
    private static volatile boolean forexRefreshFlag = true;
    /**
     * 外汇是否为删除实例
     */
    @Getter
    @Setter
    private static volatile boolean isDelete = false;
    /**
     * 修改实例（有限实例条数）
     */
    @Getter
    private static List<SpecifyData> cmdSpecifyDate = new ArrayList<>();

    /**
     * 处理事件
     *
     * @return 结果
     */
    public List<SymbolOutputEvent> syncProcEvent(SymbolInputEvent event) {
        //先处理策略命令相关事件。主线程中处理
        if (event instanceof StrategyCommandSymbolInputEvent) {
            processStrategyCmd(event);
            return Collections.emptyList();
        } else {
            GeneQueueManager queueManager = GeneQueueManager.getInstance();
            //获取更新detail bar线
            List<OhlcParam> ohlcParamList = queueManager.getAndUpdateDetail(event);
            //线程池处理业务事件
            List<SymbolOutputEvent> symbolOutputEvents = processBusinessEvent(event, ohlcParamList);
            //更新历史bar
            queueManager.updateHisOhlcBar(event);
            return symbolOutputEvents;
        }
    }

    /**
     * 处理业务
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<SymbolOutputEvent> processBusinessEvent(SymbolInputEvent event, List<OhlcParam> ohlcParamList) {
        OhlcDetailResult ohlcDetailResult = (OhlcDetailResult) event.getEventData();
        if (null == ohlcDetailResult) {
            log.error("ohlc detail result is null");
            return Collections.emptyList();
        }
        //获取货币兑
        String symbolPeriod = ohlcDetailResult.getSymbol() + "_" + ohlcDetailResult.getPeriod();
        //获取货币对对应的所有handler
        Map<String, Set<ForexEventHandler>> handlesMap = eventHandlers.entrySet().stream()
                .filter(entry -> entry.getKey().contains(symbolPeriod))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (handlesMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<SymbolOutputEvent>> result = new ArrayList<>();
        handlesMap.forEach((key, handles) -> handles.forEach(handle -> {
                    List list = handle.processEvent(event, new ArrayList<>(ohlcParamList));
                    if (!CollectionUtils.isEmpty(list)) {
                        //能计算出指标结果的数据进行添加
                        result.add(list);
                    }
                }
        ));
        return result.stream().flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * 处理命令类事件
     *
     * @param event 事件数据
     */
    private void processStrategyCmd(SymbolInputEvent event) {
        // 事件类型（当前只处理StrategyCommandEvent的数据）
        String eventName = event.getEventName();
        // 策略名称
        String strategyName = event.getStrategyName();
        // 命令类型
        String instrumentId = event.getInstrumentId();

        // 事件类型、策略名称、命令类型不能为空
        // 且策略名称为TA、事件类型为StrategyCommandEvent、命令类型在能处理的范围内
        if (!StringUtils.isBlank(eventName) && !StringUtils.isBlank(strategyName)
                && !StringUtils.isBlank(instrumentId)
                && StringUtils.equals(eventName, "StrategyCommandEvent")
                && StringUtils.equals(strategyName, StrategyNameEnum.TA.getName())
                && StrategyCmdEnum.verifyStrategyCmd(instrumentId)) {

            log.info("process StrategyCommandEvent eventName is {}, instrumentId is {}", eventName, instrumentId);

            // 如果存在只要更新部分实例
            StrategyCommand eventData = (StrategyCommand) event.getEventData();
            cmdSpecifyDate.addAll(eventData.getData());

            // 修改的实例id集合赋值
            SignalCacheMap instance = SignalCacheMap.getInstance();
            List<String> instanceIds = Optional.ofNullable(eventData.getData()).orElse(new ArrayList<>())
                    .stream().map(SpecifyData::getInstanceId).collect(Collectors.toList());
            instance.setInstanceIds(instanceIds);

            // 命令分发
            if (StringUtils.equals(instrumentId, "reload")
                    || StringUtils.equals(instrumentId, "suspend")
                    || StringUtils.equals(instrumentId, "stop")
                    || StringUtils.equals(instrumentId, "start")
                    || StringUtils.equals(instrumentId, "delete")) {

                ForexSyncEventEngine.setForexRefreshFlag(true); // 需要重新从web加载
            }

            if (StringUtils.equals(instrumentId, "delete")) {
                ForexSyncEventEngine.setDelete(true); // 为删除时状态修改
            }
        } else {
            log.warn("Not supported strategy: {} StrategyCommandEvent eventName is {}, instrumentId is {}",
                    strategyName, eventName, instrumentId);
        }
    }


    @Override
    public void stop() {
        log.info("stop successfully");
    }

    @Override
    public void start() {
        log.info("start successfully");
    }

    @Override
    public void asyncProcEvent(SymbolInputEvent<?> event) {
        log.info("put event {}",event);
    }

    /**
     * 设置因子输出网关
     * @param factorOutput factorOutput
     */
    @Override
    public void setFactorOutputGateway(FactorOutputGateway factorOutput) {
        log.info("factorOutput {}",factorOutput);
    }
}
