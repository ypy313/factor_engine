package com.nbcb.factor.monitor.api;

import com.nbcb.factor.common.Constants;
import com.nbcb.factor.enums.IndexCategoryEnum;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.monitor.model.DelayDetailModelHandler;
import com.nbcb.factor.monitor.model.DetailModelHandler;
import com.nbcb.factor.monitor.model.HistoryModelHandler;
import com.nbcb.factor.monitor.model.ModelSignalHandler;
import com.nbcb.factor.monitor.signal.SignalCacheMap;
import com.nbcb.factor.output.IndexCalOutResult;
import com.nbcb.factor.output.forex.ForexOhlcOutputEvent;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 计算指标处理结果
 */
public class SignalModelHandlerFunction extends ProcessFunction<SymbolOutputEvent, OutputEvent> {
    private static final Map<Integer, ModelSignalHandler> signalHandlerMap = new ConcurrentHashMap<>();
    protected SignalCacheMap signalCacheMap;
    static {
        signalHandlerMap.put(Constants.MODEL_DETAIL, DetailModelHandler.getInstance());
        signalHandlerMap.put(Constants.MODEL_HISTORY, HistoryModelHandler.getInstance());
        signalHandlerMap.put(Constants.MODEL_DELAY_DETAIL, DelayDetailModelHandler.getInstance());
    }
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        signalCacheMap = SignalCacheMap.getInstance();
        signalCacheMap.setIndexCategories(IndexCategoryEnum.values());
    }
    @Override
    public void processElement(SymbolOutputEvent value
            , ProcessFunction<SymbolOutputEvent, OutputEvent>.Context ctx
            , Collector<OutputEvent> out) throws Exception {
        ForexOhlcOutputEvent<IndexCalOutResult> forexOhlcOutputEvent = (ForexOhlcOutputEvent) value;
        //判断instanceIds中是否有修改的实例id是否存在
        //存在则先清理监控实例缓存数据，后清除instanceIdsList
        SignalCacheMap signalCacheMap = SignalCacheMap.getInstance();
        List<String> instanceIds = signalCacheMap.getInstanceIds();
        if (!CollectionUtils.isEmpty(instanceIds)) {
            HashSet<String> instanceIdSet = new HashSet<>(instanceIds);
            for (String instanceId : instanceIdSet) {
                signalCacheMap.removeAllInstanceIdStart(instanceId);
            }
            instanceIds.clear();
        }
        //判断模式进行不同处理
        Integer monitorModel = forexOhlcOutputEvent.getOhlcDetailResult().getMonitorModel();
        signalHandlerMap.get(monitorModel).geneSignalHandler(ctx,out,forexOhlcOutputEvent);

    }
}
