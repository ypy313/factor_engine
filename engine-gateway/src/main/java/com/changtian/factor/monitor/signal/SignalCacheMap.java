package com.changtian.factor.monitor.signal;

import cn.hutool.core.lang.Assert;
import com.changtian.factor.enums.IndexCategoryEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 信号缓存数据存储
 */
public class SignalCacheMap implements Serializable {
    /**
     * 单例
     */
    private static volatile SignalCacheMap INSTANCE;
    /**
     * 单例
     */
    public static SignalCacheMap getInstance(){
        if(INSTANCE == null){
            synchronized (SignalCacheMap.class){
                if(INSTANCE == null){
                    INSTANCE = new SignalCacheMap();
                }
            }
        }
        return  INSTANCE;
    }

    /**
     * 不同指标类别的监控对应不同的状态队列
     */
    private final Map<String,TaTradeSignalStateHolder> tradeSignalStateHolderMap = new ConcurrentHashMap<>();
    private final Map<String,TradeSignalStateHolder> indexModelSignalHolderMap = new ConcurrentHashMap<>();

    /**
     * 发生修改的实例id集合
     */
    @Getter@Setter
    private List<String> instanceIds = Collections.synchronizedList(new ArrayList<>());

    //存储上次推送信号时间 key-instanceId_monitorId value-time
    @Getter@Setter
    private Map<String,String> signalTimeMap = new ConcurrentHashMap<>();

    //存储需要删除的上次推送信号时间key集合 key-instance_monitorId value-time,redis同步清理
    @Getter@Setter
    private List<String> signalTimeRedisList = Collections.synchronizedList(new ArrayList<>());

    public TaTradeSignalStateHolder getTradeSignalStateHolder(String indexCategory) throws RuntimeException{
        TaTradeSignalStateHolder holder = tradeSignalStateHolderMap.get(indexCategory);
        Assert.notNull(holder,"indexCategory can not be"+indexCategory);
        return holder;
    }

    public TradeSignalStateHolder getTimeTradeSignalStateHolder(String indexCategory) throws RuntimeException{
        TradeSignalStateHolder holder = indexModelSignalHolderMap.get(indexCategory);
        Assert.notNull(holder,"indexCategory can not be"+indexCategory);
        return holder;
    }

    /**
     * 去除内存中所有以修改实例id开头的信息数据（也包括旧信号的发送时间，重新开始计时）
     */
    public void removeAllInstanceIdStart(String instanceId) throws RuntimeException{
        for (IndexCategoryEnum value : IndexCategoryEnum.values()) {
            tradeSignalStateHolderMap.get(value.getCode()).removeSignalState(instanceId);
            indexModelSignalHolderMap.get(value.getCode()).removeSignalState(instanceId);
        }
        Set<String> set = new HashSet<>(signalTimeMap.keySet());
        for (String key : set) {
            if (key.startsWith(instanceId)) {
                signalTimeRedisList.add(key);
                signalTimeMap.compute(key, (k, v) -> null);
            }
        }
    }

    public void setIndexCategories(IndexCategoryEnum ... indexCategories){
        if (ArrayUtils.isEmpty(indexCategories)) {
            return;
        }
        for (IndexCategoryEnum indexCategory : indexCategories) {
            tradeSignalStateHolderMap.put(indexCategory.getCode(), TaTradeSignalStateHolder.create());
            indexModelSignalHolderMap.put(indexCategory.getCode(), TradeSignalStateHolder.creat());
        }
    }

    public List<String> getSignalTimeRedisList(){return signalTimeRedisList;}
    public void clearSignalTimeRedisList(){signalTimeRedisList.clear();}
    public void setAllSignalTimeMap(Map<String,String> signalTimeMap){this.signalTimeMap = signalTimeMap;}
    public Map<String,String> getSignalTimeMap(){return signalTimeMap;}
    public void setSignalTimeMap(String key,String value){
        if (StringUtils.isEmpty(value)) {
            return;
        }
        signalTimeMap.put(key, value);
    }
}
