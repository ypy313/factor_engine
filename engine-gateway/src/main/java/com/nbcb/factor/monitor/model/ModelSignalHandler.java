package com.nbcb.factor.monitor.model;

import cn.hutool.json.JSONUtil;
import com.nbcb.factor.cache.TaCacheManager;
import com.nbcb.factor.common.Constants;
import com.nbcb.factor.entity.forex.MonitorSetting;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.monitor.signal.MonitorRuntimeContext;
import com.nbcb.factor.output.FxOhlcResultOutputEvent;
import com.nbcb.factor.output.FxTaTradeSignalOutputEvent;
import com.nbcb.factor.output.IndexCalOutResult;
import com.nbcb.factor.output.OhlcDetailResult;
import com.nbcb.factor.output.forex.ForexOhlcOutputEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 各个模式信号处理接口
 */
@Slf4j
public abstract class ModelSignalHandler {
    /**
     * 生成key
     */
    public String key(String instanceId, String monitorId, String monitorExpression) {
        return instanceId + "," + monitorId + "," + monitorExpression;
    }

    /**
     * 各模块信号处理接口
     */
    public abstract void signalHandler(MonitorSetting monitorSetting, MonitorRuntimeContext context
            , Collector<OutputEvent> out, FxOhlcResultOutputEvent inputEvent, boolean isHistory);

    /**
     * 指标自动生成中各模式信号处理接口
     */
    public abstract void geneSignalHandler(ProcessFunction<SymbolOutputEvent, OutputEvent>.Context ctx
            , Collector<OutputEvent> out, ForexOhlcOutputEvent forexOhlcOutputEvent);

    public abstract int getModel();

    /**
     * 构造输出事件
     *
     * @param rsiContext     rsi
     * @param inputEvent     ohlc输入
     * @param monitorSetting monitor规则
     * @param executeArgs    运行monitor计算时候的参数
     */
    protected static FxTaTradeSignalOutputEvent buildSignalOutput(MonitorRuntimeContext rsiContext,
                                                                  FxOhlcResultOutputEvent inputEvent,
                                                                  MonitorSetting monitorSetting,
                                                                  Map<String, Object> executeArgs) {
        FxTaTradeSignalOutputEvent event = new FxTaTradeSignalOutputEvent().convert(inputEvent);
        FxTaTradeSignalOutputEvent.SignalDetailResult data = event.getEventData();
        Map<String, Object> executeArgsResult = executeArgs.entrySet().stream().filter(e -> !e.getKey().startsWith("list_"))
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        //from index
        TaCacheManager.IndexDTO indexDTO = rsiContext.getIndexDTO();
        data.setStrategyName(indexDTO.getStrategyName());
        data.setInstanceId(indexDTO.getInstanceId());
        data.setInstanceName(indexDTO.getInstanceName());
        data.setAssetPoolName(indexDTO.getAssetPoolName());
        data.setIndexCategory(indexDTO.getParameterSetting().getIndexCategory());
        //from ohlc
        OhlcDetailResult ohlc = inputEvent.getEventData();
        data.setSource(ohlc.getSource());
        data.setSymbol(ohlc.getSymbol());
        data.setRic(ohlc.getRic());
        data.setPeriod(ohlc.getPeriod());
        data.setRelationId(ohlc.getRelationId());
        data.setSummaryType(ohlc.getSummaryType());
        data.setBeginTime(ohlc.getBeginTime());
        data.setEndTime(ohlc.getEndTime());
        //from monitor
        data.setMonitorId(monitorSetting.getMonitorId());
        data.setMonitorName(monitorSetting.getMonitorName());
        data.setSignal(monitorSetting.getSignal());
        data.setCalResult(executeArgsResult);
        data.setTriggerRule(monitorSetting.getExpression());
        data.setAction(monitorSetting.getAction());
        data.setReminderInterval(monitorSetting.getReminderInterval() == null ? 0L : monitorSetting.getReminderInterval());
        data.setSendingText(monitorSetting.getSendingText() == null ? "noSend" : monitorSetting.getSendingText());
        //pop弹窗，noPop不弹窗（默认
        data.setSignalPopup(monitorSetting.getSignalPopup() == null ? "noPop" : monitorSetting.getSignalPopup());
        //周期提醒-periodicReminder 普通提醒-normalReminder
        data.setReminderCycle(monitorSetting.getReminderCycle() == null ? "normalReminder" : monitorSetting.getReminderCycle());

        data.setPushStartTime(indexDTO.getPushStartTime());
        data.setPushEndTime(indexDTO.getPushEndTime());
        return event;
    }

    /**
     * 构造输出事件
     *
     * @param outputEvent 指标计算等
     * @return 信号对象
     */
    protected static FxTaTradeSignalOutputEvent geneBuildSignalOutput(ForexOhlcOutputEvent outputEvent) {
        FxTaTradeSignalOutputEvent event = new FxTaTradeSignalOutputEvent().convert(outputEvent);
        Map<String, Object> calculationResultMap = outputEvent.getCalculationResultMap();
        Map<String, Object> executeArgsResult = calculationResultMap.entrySet().stream()
                .filter(k -> !k.getKey().startsWith("list_") && !k.getKey().startsWith("TICK"))
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        FxTaTradeSignalOutputEvent.SignalDetailResult data = event.getEventData();
        IndexCalOutResult ohlcDetailResult = (IndexCalOutResult) outputEvent.getOhlcDetailResult();
        //from index
        data.setStrategyName(ohlcDetailResult.getStrategyName());
        data.setInstanceId(ohlcDetailResult.getInstanceId());
        data.setInstanceName(ohlcDetailResult.getInstanceName());
        data.setAssetPoolName(ohlcDetailResult.getAssetPoolName());
        data.setIndexCategory(ohlcDetailResult.getIndexCategory());
        //from ohlc
        data.setSource(ohlcDetailResult.getSource());
        data.setSymbol(ohlcDetailResult.getSymbol());
        data.setRic(ohlcDetailResult.getRic());
        data.setPeriod(ohlcDetailResult.getPeriod());
        data.setRelationId(ohlcDetailResult.getRelationId());
        data.setSummaryType(ohlcDetailResult.getSummaryType());
        data.setBeginTime(ohlcDetailResult.getBeginTime());
        data.setEndTime(ohlcDetailResult.getEndTime());
        //from monitor
        data.setMonitorId(ohlcDetailResult.getMonitorId());
        data.setMonitorName(ohlcDetailResult.getMonitorName());
        data.setSignal(ohlcDetailResult.getSignal());
        data.setCalResult(executeArgsResult);
        data.setTriggerRule(ohlcDetailResult.getTriggerRule());
        data.setAction(ohlcDetailResult.getAction());
        data.setReminderInterval(ohlcDetailResult.getReminderInterval() == null ? 0L : ohlcDetailResult.getReminderInterval());
        data.setSendingText(ohlcDetailResult.getSendingText() == null ? "noSend" : ohlcDetailResult.getSendingText());
        data.setSignalPopup(ohlcDetailResult.getSignalPopup() == null ? "noPop" : ohlcDetailResult.getSignalPopup());
        data.setReminderCycle(ohlcDetailResult.getReminderCycle() == null ? "normalReminder" : ohlcDetailResult.getReminderCycle());
        data.setPushStartTime(ohlcDetailResult.getPushStartTime());
        data.setPushEndTime(ohlcDetailResult.getPushEndTime());
        return event;
    }

    protected void signalLogOut(MonitorRuntimeContext context, MonitorSetting monitorSetting, boolean isHistory) {
        String instanceId = context.getIndexDTO().getInstanceId();
        String monitorName = monitorSetting.getMonitorName();
        String monitorId = monitorSetting.getMonitorId();
        String assetPoolName = context.getIndexDTO().getAssetPoolName();
        //打印出现信号时函数计算的参数，过滤单个ohlc,tick参数数据
        Map<String, Object> logInfo = context.getMap().entrySet().stream().filter(e ->
                        !Constants.OPEN.equals(e.getKey()) && !Constants.HIGH.equals(e.getKey())
                                && !Constants.LOW.equals(e.getKey()) && !Constants.CLOSE.equals(e.getKey())
                                && !Constants.TICK.equals(e.getKey()) && !Constants.TA_LIST_OPEN.equals(e.getKey())
                                && !Constants.TA_LIST_HIGH.equals(e.getKey()) && !Constants.TA_LIST_LOW.equals(e.getKey())
                                && !Constants.TA_LIST_CLOSE.equals(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        for (Map.Entry<String, Object> calInfo : logInfo.entrySet()) {
            String value;
            if (calInfo.getValue() instanceof List) {
                List values = (List) calInfo.getValue();
                value = JSONUtil.toJsonStr(values.subList(values.size() < 50 ? 0 : values.size() - 50, values.size()));
            } else {
                value = calInfo.getValue().toString();
            }
            log.info("isHistory:{} instanceId：{} -monitorName:{} - monitorId：{}-assetPoolName:{},keyCal:{},value:{}"
                    , isHistory, instanceId, monitorName, monitorId, assetPoolName, calInfo.getKey(), value);
        }
    }
}
