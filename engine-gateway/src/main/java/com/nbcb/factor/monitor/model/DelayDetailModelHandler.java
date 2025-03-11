package com.nbcb.factor.monitor.model;

import com.nbcb.factor.entity.forex.MonitorSetting;
import com.nbcb.factor.enums.ResultTypeEnum;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.monitor.signal.MonitorRuntimeContext;
import com.nbcb.factor.monitor.signal.SignalCacheMap;
import com.nbcb.factor.monitor.signal.TradeSignalStateHolder;
import com.nbcb.factor.output.FxOhlcResultOutputEvent;
import com.nbcb.factor.output.FxTaTradeSignalOutputEvent;
import com.nbcb.factor.output.IndexCalOutResult;
import com.nbcb.factor.output.forex.ForexOhlcOutputEvent;
import com.nbcb.factor.utils.TaScriptUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Map;

/**
 * 盯盘延迟模式
 */
@Slf4j
public class DelayDetailModelHandler extends ModelSignalHandler {
    /**
     * 单例
     */
    private static volatile DelayDetailModelHandler INSTANCE = null;

    /**
     * 单例
     */
    public static DelayDetailModelHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (DelayDetailModelHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DelayDetailModelHandler();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void signalHandler(MonitorSetting monitorSetting, MonitorRuntimeContext context, Collector<OutputEvent> out, FxOhlcResultOutputEvent inputEvent, boolean isHistory) {
        int monitorModel = monitorSetting.getMonitorModel();
        SignalCacheMap signalCacheMap = SignalCacheMap.getInstance();
        String instanceId = context.getIndexDTO().getInstanceId();

        //盯盘持续模式只监听盯盘行情数据
        if (!ResultTypeEnum.DETAIL.getCode().equals(inputEvent.getEventData().getSummaryType())) {
            return;
        }
        String signalDurationInfo = monitorSetting.getSignalDuration();
        if (StringUtils.isEmpty(signalDurationInfo)) {
            log.warn("instanceId:{} 中signalDuration配置为空！", instanceId);
        }
        long signalDuration = Long.parseLong(signalDurationInfo);
        //key
        String monitorName = monitorSetting.getMonitorName();
        String monitorId = monitorSetting.getMonitorId();
        String expression = monitorSetting.getExpression().replaceAll(" ", "");
        String assetPoolName = context.getIndexDTO().getAssetPoolName();
        //监控表达式计算结果
        Tuple2<Boolean, Map<String, Object>> result = TaScriptUtils.invoke(expression, context.getMap());
        TradeSignalStateHolder tradeSignalStateHolder = signalCacheMap
                .getTimeTradeSignalStateHolder(context.getIndexDTO().getParameterSetting().getIndexCategory());

        String key = key(instanceId, monitorId, expression);
        //true:出现信号 && 首次出现
        if (result != null && result.f0
                && tradeSignalStateHolder.addSignalState(key, monitorModel, inputEvent.getSrcTimestamp(), signalDuration)) {
            log.info("出现delay信号：instanceId:{} - monitorName:{} - monitorId:{} - assetPoolName:{}"
                    , instanceId, monitorName, monitorId, assetPoolName);
            signalLogOut(context,monitorSetting,isHistory);
            FxTaTradeSignalOutputEvent outputEvent = buildSignalOutput(context, inputEvent, monitorSetting, result.f1);
            out.collect(outputEvent);
        }
        //信号回落
        else if (result != null && !result.f0) {
            tradeSignalStateHolder.removeSignalState(key,monitorModel);
        }
    }

    @Override
    public void geneSignalHandler(ProcessFunction<SymbolOutputEvent, OutputEvent>.Context ctx
            , Collector<OutputEvent> out
            , ForexOhlcOutputEvent forexOhlcOutputEvent) {
        IndexCalOutResult ohlcDetailResult = (IndexCalOutResult)forexOhlcOutputEvent.getOhlcDetailResult();
        //只对detail的行情数据进行监控处理
        if (!ResultTypeEnum.DETAIL.getCode().equals(ohlcDetailResult.getSummaryType())) {
            return;
        }
        String instanceId = ohlcDetailResult.getInstanceId();
        String signalDurationInfo = ohlcDetailResult.getSignalDuration();
        if (StringUtils.isEmpty(signalDurationInfo)) {
            log.warn("instanceId:{} 中signalDuration配置为空！",instanceId);
            return;
        }

        long signalDuration = Long.parseLong(signalDurationInfo);

        SignalCacheMap signalCacheMap = SignalCacheMap.getInstance();
        TradeSignalStateHolder tradeSignalStateHolder = signalCacheMap
                .getTimeTradeSignalStateHolder(ohlcDetailResult.getIndexCategory());

        String monitorName = ohlcDetailResult.getMonitorName();
        String monitorId = ohlcDetailResult.getMonitorId();
        String expression = ohlcDetailResult.getTriggerRule();
        Integer monitorModel = ohlcDetailResult.getMonitorModel();
        String assetPoolName = ohlcDetailResult.getAssetPoolName();
        Boolean monitorResult = ohlcDetailResult.getMonitorResult();
        String key = key(instanceId, monitorId, expression);
        //true:出现信号 && 首次出现
        if (monitorResult != null && monitorResult
                && tradeSignalStateHolder
                .addSignalState(key,monitorModel, forexOhlcOutputEvent.getSrcTimestamp() ,signalDuration)) {
            log.info("出现delay信号：instanceId:{} ,monitorName:{},monitorId:{},assetPoolName:{}",
                    instanceId,monitorName,monitorId,assetPoolName);
            FxTaTradeSignalOutputEvent outputEvent = geneBuildSignalOutput(forexOhlcOutputEvent);
            out.collect(outputEvent);
        }
        //信号回落
        else if(monitorResult != null && monitorResult){
            tradeSignalStateHolder.removeSignalState(key,monitorModel);
        }
    }

    @Override
    public int getModel() {
        return 2;
    }
}
