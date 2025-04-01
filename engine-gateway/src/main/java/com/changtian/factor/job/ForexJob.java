package com.changtian.factor.job;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.changtian.factor.common.Constants;
import com.changtian.factor.common.HttpUtils;
import com.changtian.factor.common.LoadConfig;
import com.changtian.factor.entity.StrategyConfigVo;
import com.changtian.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.changtian.factor.event.SymbolInputEvent;
import com.changtian.factor.event.SymbolOutputEvent;
import com.changtian.factor.handler.CalculationHandler;
import com.changtian.factor.job.engine.ForexAbstractEventEngine;
import com.changtian.factor.job.engine.ForexStrategyInstance;
import com.changtian.factor.job.engine.ForexSyncEventEngine;
import com.changtian.factor.job.engine.FxSyncEventEngine;
import com.changtian.factor.output.OhlcDetailResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务类
 * 计算任务的入口，子job 是需要实现main方法
 * 子job 从取得strategyInstance 的配置，加载到job中
 */
@Slf4j
@SuppressWarnings(value = {"rawtypes"})
public abstract class ForexJob {
    /**
     * 事件处理器
     */
    protected static ForexAbstractEventEngine forexAbstractEventEngine;
    /**
     * 重连次数
     */
    protected static int disConnectCount;
    /**
     * 策略集合 默认最大值200
     */
    private static final Map<String, ForexStrategyInstance<?,?>> forexStrategyInstanceMap = new ConcurrentHashMap<>(200);

    @SuppressWarnings("rawtypes")
    public static List<SymbolOutputEvent> putForexEventInFlink(SymbolInputEvent event){
        return forexAbstractEventEngine.syncProcEvent(event);
    }

    /**
     * 校验当前bar线是否在因子实例的计算范围，如果没有忽略
     * @param forexConfigList 因子实例配置信息
     * @param ohlcDetailResult k线数据
     * @return 结果true 是因子需要的k线，false不是
     */
    protected static boolean filterByConfigList(List<LocalCurrencyStrategyInstanceResult> forexConfigList,
                                                OhlcDetailResult ohlcDetailResult){
        for (LocalCurrencyStrategyInstanceResult forexConfig : forexConfigList) {
            //只要有一个货币兑和周期满足因子实例，就视为满足条件
            if(forexConfig.getMarketDataSetting().getLeg().equals(ohlcDetailResult.getSymbol())
            && forexConfig.getPeriodsSetting().getOhlc().equals(ohlcDetailResult.getPeriod())){
                log.info("instanceId:{},symbol:{},period:{},meet the conditions",
                        ohlcDetailResult.getInstanceId(),forexConfig.getSymbol(),ohlcDetailResult.getPeriod());
                return true;
            }
        }
        return false;
    }

    protected static void addStrategyInstance(String instanceIdOrSymbool,ForexStrategyInstance<LocalCurrencyStrategyInstanceResult,
            OhlcDetailResult> forexStrategyInstance){
        //先删除后增加
        forexStrategyInstanceMap.remove(instanceIdOrSymbool);
        forexStrategyInstanceMap.put(instanceIdOrSymbool, forexStrategyInstance);
        //循环注册到事件处理引擎，获取当前货币对的所有
        Set<String> instrumentIds = forexStrategyInstance.getInstrumentIds();
        //先删除后增加
        for (String instrumentId : instrumentIds) {
            forexAbstractEventEngine.unregister(instrumentId,forexStrategyInstance);
            forexAbstractEventEngine.register(instrumentId,forexStrategyInstance);
        }
    }

    /**
     * 注入计算handler
     */
    protected static void injectCalculationHandler(){
        Set<Class<?>> classSet = ClassUtil.scanPackageBySuper(CalculationHandler.class.getPackage().getName(),CalculationHandler.class);
        for (Class<?> aClass : classSet) {
            try{
                CalculationHandler calculationHandler = (CalculationHandler) aClass.newInstance();
                ForexAbstractEventEngine.putCalculationHandlerMap(calculationHandler.getCalculationType(),calculationHandler);
            }catch (Exception e){
                log.error("inject calculation handler fail!",e);
            }

        }
    }

    /**
     * 验证是否刷新配置
     * @return true 刷新 false 不刷新
     */
    protected static boolean checkIsRefreshConfig(){
        return ForexSyncEventEngine.isForexRefreshFlag();

    }
    /**
     * 加载外汇配置
     * @param strategyName 策略名称
     * @return 结果
     */
    protected synchronized static List<LocalCurrencyStrategyInstanceResult> loadForexStrategyConfig(String strategyName) {
        boolean fxRefreshFlag = ForexSyncEventEngine.isForexRefreshFlag();
        // 初始化
        List<LocalCurrencyStrategyInstanceResult> configList = new ArrayList<>();
        // 需要刷新，查询数据库
        if (fxRefreshFlag) {
            String strategyConfigUrl = LoadConfig.getProp().getProperty("strategy_config.url");
            StrategyConfigVo strategyConfigVo = new StrategyConfigVo();
            strategyConfigVo.setStrategyName(strategyName);
            log.info("loadFxStrategyConfig from web ,params is {} fxRefreshFlag is {}",
                    strategyConfigUrl + JSONUtil.toJsonStr(strategyConfigVo), fxRefreshFlag);
            try {
                String result = HttpUtils.httpPost(strategyConfigUrl, JSONUtil.toJsonStr(strategyConfigVo));
                Map map = JSONUtil.toBean(result, Map.class);
                String status = MapUtil.getStr(map, Constants.HTTP_RESPONSE_STATUS); // 获取状态
                if (!Constants.HTTP_SUCC.equals(status)) {
                    log.error("The web server return fail:{},InitialLizing loadFxStrategyConfig faild", result);
                    return Collections.emptyList();
                }
                String data = MapUtil.getStr(map, Constants.HTTP_RESPONSE_DATA);
                log.info("The web server return data:{}", data);
                JSONArray objects = JSONUtil.parseArray(data);
                configList = JSONUtil.toList(objects, LocalCurrencyStrategyInstanceResult.class);
                // 为空或者个数为0，重新加载
                if (CollectionUtils.isEmpty(configList)) {
                    log.info("The web server return data is null!");
                    return Collections.emptyList();
                }
                // 后面可以从redis中取数
            } catch (IOException e) {
                disConnectCount++;
                if (disConnectCount >= 3) {
                    FxSyncEventEngine.setFxRefreshFlag(true);
                    disConnectCount++;
                }
                log.error("loadFxStrategyConfig exception", e);
            }
            log.info("loadFxStrategyConfig from web .configList is {}", configList);
        }
        return configList;
    }
}
