package com.nbcb.factor.monitor.signal;

import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.entity.forex.MonitorSetting;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 信号状态辅助类
 */
@Slf4j
public class TradeSignalStateHolder implements Serializable {
    //key-模式 value-key --instanceId_monitorId_monitorExpression value-value -- signalInfoEntity
    private final Map<Integer, ConcurrentHashMap<String, SignalInfoEntity>> SIGNAL_STATE;

    //如需添加新模式需添加新模式初始化
    public TradeSignalStateHolder() {
        SIGNAL_STATE = new ConcurrentHashMap<>();
        SIGNAL_STATE.put(MonitorSetting.MONITOR_MODEL_DELAY_DETAIL, new ConcurrentHashMap<>());
    }

    /**
     * 创建信号辅助类
     */
    public static TradeSignalStateHolder creat() {
        return new TradeSignalStateHolder();
    }

    /**
     * false->true 获取信号数据状态
     * key-instanceId_monitorId_monitorExpression
     */
    public boolean addSignalState(String key, int monitorModel, String srcTime, long signalDuration) {
        SignalInfoEntity signalInfoEntity = SIGNAL_STATE.get(monitorModel).get(key);
        if (signalInfoEntity == null) {
            signalInfoEntity = new SignalInfoEntity();
            signalInfoEntity.setSignalStartTime(srcTime);
            signalInfoEntity.setSignalSendFlag(false);
            log.info("delay monitor 首次成立 ：key:{}", key);
            SIGNAL_STATE.get(monitorModel).put(key, signalInfoEntity);
        } else {
            String signalStartTime = signalInfoEntity.getSignalStartTime();
            long signalCacheTime = DateUtil.stringToDate(signalStartTime, DateUtil.DT_FORMAT_PATTERN).getTime();
            long srcSignalTime = DateUtil.stringToDate(srcTime, DateUtil.DT_FORMAT_PATTERN).getTime();
            //超过信号持续时间，更新时间
            if (signalCacheTime + signalDuration < srcSignalTime && !signalInfoEntity.isSignalSendFlag()) {
                signalInfoEntity.setSignalStartTime(srcTime);
                log.info("delay monitor 出现信号：key:{}", key);
                signalInfoEntity.setSignalSendFlag(true);
                SIGNAL_STATE.get(monitorModel).put(key, signalInfoEntity);
                return true;
            }
        }
        return false;
    }

    /**
     * 当状态从true->false的时候进行移除
     */
    public boolean removeSignalState(String key,int monitorModel) {
        SignalInfoEntity remove = SIGNAL_STATE.get(monitorModel).remove(key);
        if(remove != null){
            log.info("delay monitor 信号记录移除：key:{}",key);
            return true;
        }else {
            return false;
        }
    }

    public void removeSignalState(String instanceId){
        for (Integer model : MonitorSetting.getAllModelList()) {
            SIGNAL_STATE.computeIfPresent(model,(k,v)->{
                for (String instanceInfo : v.keySet()) {
                    if (instanceInfo.startsWith(instanceId)) {
                        v.remove(instanceInfo);
                    }
                }
                return v;
            });
        }
    }

}
