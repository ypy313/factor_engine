package com.nbcb.factor.job;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nbcb.factor.common.Constants;
import com.nbcb.factor.common.HttpUtils;
import com.nbcb.factor.common.JsonUtil;
import com.nbcb.factor.entity.StrategyConfigVo;
import com.nbcb.factor.event.*;
import com.nbcb.factor.job.engine.AbstractEventEngine;
import com.nbcb.factor.job.gateway.FactorOutputGateway;
import com.nbcb.factor.job.engine.FxAbstractEventEngine;
import com.nbcb.factor.job.engine.StrategyInstance;
import com.nbcb.factor.job.gateway.EventInputGateway;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * 任务类
 * 计算任务的入门 子job 是需实现main方法
 * 子job 从取得StrategyInstance 的配置 加载到job中
 */
@Slf4j
public abstract class Job {
    //Job关联的strategyInstance
    private static Set<StrategyInstance> strategyInstanceArrayList = new HashSet<>(100);
    @Getter
    protected static AbstractEventEngine eventEngine;
    protected  static FxAbstractEventEngine fxEventEngine;
    private List<EventInputGateway> eventInputGatewayList = new ArrayList<>();//行情输入
    /**
     * -- SETTER --
     *  设置因子输出网关
     */
    @Setter
    private FactorOutputGateway factorOutputGateway;//因子输出

    private final static String HTTP_RESPONSE_STATUS = "status";
    private final static String HTTP_RESPONSE_DATA = "data";
    /**
     *  增加strategy instance;
     *  逐一注册到事件引擎
     */
    public void addStrategyInstance(List<StrategyInstance> strategyInstanceArrayList){
        strategyInstanceArrayList.addAll(strategyInstanceArrayList);
        //循环注册到事件处理引擎
        for(StrategyInstance strategyInstance:strategyInstanceArrayList) {
            String instanceId = strategyInstance.getStrategyInstanceId();


            Set<String> eventNames = strategyInstance.getEventNames();
            Set<String> instrumentIds = strategyInstance.getInstrumentIds();
            for (String eventName : eventNames) {
                for (String instrumentId : instrumentIds) {
                    eventEngine.register(eventName, instrumentId, strategyInstance);
                }
            }
        }
    }

    /**
     *  增加strategy instance;
     *  单一注册到事件引擎
     */
    public static  void addStrategyInstance(StrategyInstance strategyInstance){
        strategyInstanceArrayList.remove(strategyInstance);
        strategyInstanceArrayList.add(strategyInstance);
        //循环注册到事件处理引擎

        Set<String> eventNames = strategyInstance.getEventNames();
        Set<String> instrumentIds = strategyInstance.getInstrumentIds();
        //先删后增加
        for (String eventName : eventNames) {
            for (String instrumentId : instrumentIds) {
                eventEngine.unregister(eventName, instrumentId, strategyInstance);
                eventEngine.register(eventName, instrumentId, strategyInstance);
            }
        }
    }

    /**
     * Job 打印
     */
    public static  void onDump(BondInputEvent event){

        StrategyCommandBondInputEvent cmd = (StrategyCommandBondInputEvent) event;
        String strategyInstanceId =cmd.getEventData().getStrategyInstanceId();
        log.info("job onDump {} strategyInstanceId is {} ",event.getEventName(),strategyInstanceId);
        StrategyInstance strategyInstance = findStrategyInstanceById(strategyInstanceId);
        if (strategyInstance != null){
            strategyInstance.onDump();
        }else{
            log.info("cant find strategyInstanceId  {}",strategyInstanceId);
        }
    }

    /**
     * Job 启动。由main 方法调用
     */
    public void start(){
        //启动时完成检查
        if (factorOutputGateway == null){
            log.error("please init factorOutputGateway before start");
            return;
        }

        if (eventInputGatewayList == null){
            log.error("please init eventInputGateway before start");
            return;
        }

        if (strategyInstanceArrayList == null || strategyInstanceArrayList.isEmpty()){
            log.error("please init strategyInstanceArrayList before start");
            return;
        }

        eventEngine.registerJob(this);

        eventEngine.setFactorOutputGateway(factorOutputGateway);
        eventEngine.start();//事件引擎开始处理

        for(EventInputGateway eventInputGateway:eventInputGatewayList){
            eventInputGateway.start(this);
        }
    }
    /**
     * Job 启动。由main 方法调用
     */
    public static  void startInFlink(){

//        if (strategyInstanceArrayList != null){
//            strategyInstanceArrayList.clear();
//            return;
//        }
    }

    /**
     * 设置因子输入网关
     */
    public void addEventInputGateway(EventInputGateway inputGateway){
        eventInputGatewayList.add(inputGateway);
    }

    /**
     * 处理事件
     */
    public void putEvent(BondInputEvent event){
        eventEngine.syncProcEvent(event);
    }

    public static List<OutputEvent> putEventInFlink(BondInputEvent event) {
        return eventEngine.syncProcEvent(event);
    }

    public static List<SymbolOutputEvent> putFxEventInFlink(SymbolInputEvent event) {
        return fxEventEngine.syncProcEvent(event);
    }

    /**
     * 判断list中是否包含某个值
     * @param strList 集合
     * @param value 值
     * @return 结果 true false
     */
    private static boolean listContains(List<String> strList,String value){
        return strList.contains(value);
    }

    /**
     * 获得配置Json list
     */
    public static List<String> getConfigList(String strategyConfigUrl, StrategyConfigVo strategyConfigVo){
        List<String> configList = new ArrayList<>();
        try{
            String ret = HttpUtils.httpPost(strategyConfigUrl, JSONUtil.toJsonStr(strategyConfigVo));
            Map<String, Object> responseData = JsonUtil.toObject(ret, new TypeReference<Map<String, Object>>() {
            });
            if (Constants.HTTP_SUCC.equals(responseData.get("status"))) {
                log.error("The web sercer return fail:{},Initializing loadStrategyConfig failed",ret);
                return null;
            }
            ArrayList<Map> instanceList = (ArrayList<Map>)responseData.get("data");
            for (Map instance : instanceList) {
                String retJson = JsonUtil.toJson(instance);
                configList.add(retJson);
            }
        } catch (IOException e) {
            log.error("loadStrategyConfig exception",e);
        }
        return configList;
    }

    /**
     * 根据实例id，找出在内存中的实例
     */
    protected static StrategyInstance findStrategyInstanceById(String inStrategyInstanceId){
        for (StrategyInstance strategyInstance : strategyInstanceArrayList) {
            String strategyInstanceId = strategyInstance.getStrategyInstanceId();
            if (StringUtils.equals(strategyInstanceId,inStrategyInstanceId)) {
                return strategyInstance;
            }
        }
        return null;
    }
}
