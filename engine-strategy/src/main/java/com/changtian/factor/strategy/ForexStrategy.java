package com.changtian.factor.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.changtian.factor.common.JsonUtil;
import com.changtian.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.changtian.factor.event.SymbolInputEvent;
import com.changtian.factor.event.SymbolOutputEvent;
import com.changtian.factor.flink.aviatorfun.entity.OhlcParam;
import com.changtian.factor.job.engine.ForexAbstractEventEngine;
import com.changtian.factor.job.engine.ForexStrategyInstance;
import com.changtian.factor.output.OhlcDetailResult;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 外汇策略实现
 *
 * @param <T>
 * @param <D>
 */
@Slf4j
public class ForexStrategy<T, D extends OhlcDetailResult> implements ForexStrategyInstance<T, D> {
    /**
     * 当前策略名称
     */
    public static final String STRATEGY_NAME = "TA";
    /**
     * 策略的配置信息
     */
    private LocalCurrencyStrategyInstanceResult localCurrencyStrategy;
    /**
     * 当前策略处理的货币对（只会有一个货币对，为了同步系统架构，设置为set）
     */
    protected Set<String> instrumentIds = new HashSet<>();

    /**
     * 初始化策略
     *
     * @param t 参数
     */
    @Override
    public void init(T t) throws Exception {
        //缓存配置参数
        this.localCurrencyStrategy = (LocalCurrencyStrategyInstanceResult) t;
        log.info("init forex strategy config :{}", JsonUtil.toJson(this.localCurrencyStrategy));
        instrumentIds.add(this.localCurrencyStrategy.getInstanceId()
                + "_" + this.localCurrencyStrategy.getMarketDataSetting().getLeg()
                + "_" + this.localCurrencyStrategy.getPeriodsSetting().getOhlc());
    }

    @Override
    public Set<String> getInstrumentIds() {
        return this.instrumentIds;
    }

    /**
     * 处理bar线数据
     * @param event bar线事件
     * @return 结果
     */
    @Override
    @SuppressWarnings(value ={"unchecked"})
    public List<SymbolOutputEvent<D>> processEvent(SymbolInputEvent<T> event, List<OhlcParam> ohlcParams) {
        OhlcDetailResult ohlcDetailResult = (OhlcDetailResult) event.getEventData();
        if(null == ohlcDetailResult) {
            log.error("eventId:{},ohlc detail result is null !",event.getEventId());
            return null;
        }
        //根据不同类型，走不同的逻辑 点数图计算 高斯混合模型计算 常规指标计算
        LocalCurrencyStrategyInstanceResult localCurrencyStrategy = this.localCurrencyStrategy;
        return ForexAbstractEventEngine.getCalculationHandler(localCurrencyStrategy.getIndexCategory())
                .handler(localCurrencyStrategy,ohlcDetailResult,ohlcParams);
    }

    /**
     * 获取当前策略的实例id
     * @return 实例ID
     */
    @Override
    public String getStrategyInstanceId() {
        return localCurrencyStrategy.getInstanceId();
    }

    /**
     * 输出实例的相关内存数据
     */
    @Override
    public void onDump() {
        try{
            log.info("onDump,config message:{}",JsonUtil.toJson(this.localCurrencyStrategy));
        }catch (JsonProcessingException e) {
            log.error(e.getMessage(),e);
        }
    }

    /**
     * 策略名称
     * @return 结果
     */
    @Override
    public String getStrategyName() {
        return localCurrencyStrategy.getStrategyName();
    }

    /**
     * 获取实例名称
     * @return 结果
     */
    @Override
    public String getStrategyInstanceName() {
        return localCurrencyStrategy.getInstanceName();
    }
}
