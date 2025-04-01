package com.changtian.factor.monitor.signal;

import com.changtian.factor.entity.forex.MonitorSetting;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 信号状态辅助类
 */
@Slf4j
public class TaTradeSignalStateHolder implements Serializable {
    //key-模式 value-key --- instanceId_monitorId_monitorExpression value-value --signalInfoEntity
    private final Map<Integer, ConcurrentHashMap.KeySetView<String,Boolean>> SIGNAL_STATE;

    //如需添加新模式需添加新模式初始化
    public TaTradeSignalStateHolder(){
        SIGNAL_STATE = new ConcurrentHashMap<>();
        SIGNAL_STATE.put(MonitorSetting.MONITOR_MODEL_DETAIL,ConcurrentHashMap.newKeySet(32));
        SIGNAL_STATE.put(MonitorSetting.MONITOR_MODEL_HISTORY,ConcurrentHashMap.newKeySet(32));
    }

    public static TaTradeSignalStateHolder create(){
        return new TaTradeSignalStateHolder();
    }
    /**
     * false ->true 的时候进行记录
     */
    public boolean addSignalState(String instanceId,String monitorId,String monitorExpression,int monitorModel){
        return SIGNAL_STATE.get(monitorModel).add(instanceId+','+monitorId+','+monitorExpression);
    }

    /**
     * 当状态从true->false的时候进行移除
     */
    public boolean removeSignalState(String instanceId,String monitorId,String monitorExpression,int monitorModel){
        return SIGNAL_STATE.get(monitorModel).remove(instanceId+','+monitorId+','+monitorExpression);
    }

    public void removeSignalState(String instanceId){
        for (String key : SIGNAL_STATE.get(MonitorSetting.MONITOR_MODEL_DETAIL).getMap().keySet()) {
            if (key.startsWith(instanceId)) {
                SIGNAL_STATE.get(MonitorSetting.MONITOR_MODEL_DETAIL).remove(key);
            }
        }
        for (String key : SIGNAL_STATE.get(MonitorSetting.MONITOR_MODEL_HISTORY).getMap().keySet()) {
            if (key.startsWith(instanceId)) {
                SIGNAL_STATE.get(MonitorSetting.MONITOR_MODEL_HISTORY).remove(key);
            }
        }
    }
}
