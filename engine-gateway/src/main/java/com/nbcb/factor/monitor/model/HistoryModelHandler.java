package com.nbcb.factor.monitor.model;

import com.nbcb.factor.entity.forex.MonitorSetting;
import com.nbcb.factor.enums.ResultTypeEnum;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.monitor.signal.MonitorRuntimeContext;
import com.nbcb.factor.monitor.signal.SignalCacheMap;
import com.nbcb.factor.monitor.signal.TaTradeSignalStateHolder;
import com.nbcb.factor.output.FxOhlcResultOutputEvent;
import com.nbcb.factor.output.FxTaTradeSignalOutputEvent;
import com.nbcb.factor.output.IndexCalOutResult;
import com.nbcb.factor.output.forex.ForexOhlcOutputEvent;
import com.nbcb.factor.utils.TaScriptUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Map;

/**
 * 盯盘模式处理
 */
@Slf4j
public class HistoryModelHandler extends ModelSignalHandler {
    /**
     * 单例
     */
    private static volatile HistoryModelHandler INSTANCE;

    public static HistoryModelHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (HistoryModelHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HistoryModelHandler();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void signalHandler(MonitorSetting monitorSetting, MonitorRuntimeContext context, Collector<OutputEvent> out, FxOhlcResultOutputEvent inputEvent, boolean isHistory) {
        int monitorModel = monitorSetting.getMonitorModel();
        SignalCacheMap signalCacheMap = SignalCacheMap.getInstance();
        //当前的model是否和提供的RSI的RSI数据源类型相符
        if (!ResultTypeEnum.HIS.getCode().equals(inputEvent.getEventData().getSummaryType())) {
            return;
        }
        //key
        String instanceId = context.getIndexDTO().getInstanceId();
        String monitorName = monitorSetting.getMonitorName();
        String monitorId = monitorSetting.getMonitorId();
        String expression = monitorSetting.getExpression().replaceAll(" ", "");
        String assetPoolName = context.getIndexDTO().getAssetPoolName();
        //监控表达式计算结果
        Tuple2<Boolean, Map<String, Object>> result = TaScriptUtils.invoke(expression, context.getMap());
        TaTradeSignalStateHolder tradeSignalStateHolder = signalCacheMap.getTradeSignalStateHolder(context.getIndexDTO().getParameterSetting().getIndexCategory());
        //true:出现信号 && 首次出现
        if (result != null && result.f0 && tradeSignalStateHolder.addSignalState(instanceId, monitorId, expression, monitorModel)) {
            log.info("出现信号：assetPoolName:{} ,instanceId={},monitorName:{}, monitorId={}"
                    , assetPoolName, instanceId, monitorName, monitorId);
            signalLogOut(context,monitorSetting,isHistory);
            FxTaTradeSignalOutputEvent outputEvent = buildSignalOutput(context, inputEvent, monitorSetting, result.f1);
            out.collect(outputEvent);
        }
        //信号回落
        else if (result != null && !result.f0 && tradeSignalStateHolder.removeSignalState(instanceId, monitorId, expression, monitorModel)) {
            log.info("信号回落 :assetPoolName:{} ,instanceId={},monitorName:{}, monitorId={}"
                    , assetPoolName, instanceId, monitorName, monitorId);
        }
    }

    @Override
    public void geneSignalHandler(ProcessFunction<SymbolOutputEvent, OutputEvent>.Context ctx
            , Collector<OutputEvent> out
            , ForexOhlcOutputEvent forexOhlcOutputEvent) {
        IndexCalOutResult ohlcDetailResult = (IndexCalOutResult)forexOhlcOutputEvent.getOhlcDetailResult();
        //只对detail的行情数据进行监控处理
        if (!ResultTypeEnum.HIS.getCode().equals(ohlcDetailResult.getSummaryType())) {
            return;
        }
        SignalCacheMap signalCacheMap = SignalCacheMap.getInstance();
        TaTradeSignalStateHolder tradeSignalStateHolder = signalCacheMap
                .getTradeSignalStateHolder(ohlcDetailResult.getIndexCategory());
        String instanceId = ohlcDetailResult.getInstanceId();
        String monitorName = ohlcDetailResult.getMonitorName();
        String monitorId = ohlcDetailResult.getMonitorId();
        String expression = ohlcDetailResult.getTriggerRule();
        Integer monitorModel = ohlcDetailResult.getMonitorModel();
        String assetPoolName = ohlcDetailResult.getAssetPoolName();
        Boolean monitorResult = ohlcDetailResult.getMonitorResult();
        //true:出现信号 && 首次出现
        if (monitorResult != null && monitorResult
                && tradeSignalStateHolder.addSignalState(instanceId,monitorId,expression,monitorModel)) {
            log.info("出现信号：instanceId:{} ,monitorName:{},monitorId:{},assetPoolName:{}",
                    instanceId,monitorName,monitorId,assetPoolName);
            FxTaTradeSignalOutputEvent outputEvent = geneBuildSignalOutput(forexOhlcOutputEvent);
            out.collect(outputEvent);
        }
        //信号回落
        else if(monitorResult != null && monitorResult
                && tradeSignalStateHolder.removeSignalState(instanceId,monitorId,expression,monitorModel)){
            log.info("信号回落：instanceId:{} ,monitorName:{},monitorId:{},assetPoolName:{}",
                    instanceId,monitorName,monitorId,assetPoolName);
        }
    }

    @Override
    public int getModel() {
        return 1;
    }
}
