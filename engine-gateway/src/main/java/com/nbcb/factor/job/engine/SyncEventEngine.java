package com.nbcb.factor.job.engine;

import com.nbcb.factor.common.StrategyNameEnum;
import com.nbcb.factor.event.BondInputEvent;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.event.StrategyCommandBondInputEvent;
import com.nbcb.factor.job.Job;
import com.nbcb.factor.job.gateway.FactorOutputGateway;
import com.nbcb.factor.strategy.StrategyCmdEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 运行在flink 环境中的engine ，只做事件同步处理，多线程等交给flink 环境处理
 */
@Slf4j
public class SyncEventEngine extends AbstractEventEngine{
    private static String refreshFlag = "true";//接收到重新加载，启动停止等命名后，必须重新从web加载。默认为可以redis加载

    /**
     * 处理事件
     * @return
     */
    public  List<OutputEvent> syncProcEvent(BondInputEvent event) {
        //先处理策略命令相关事件。主线程中处理
        if (event instanceof StrategyCommandBondInputEvent){
            processStrategyCmd(event);
            return  null;
        }else{
            //线程池处理业务事件
            List<OutputEvent> resultLst =   processBusiEvent(event);
            return  resultLst;
        }

    }


    public static String getRefreshFlag() {
        return refreshFlag;
    }

    public static void setRefreshFlag(String refreshFlagIns) {
         refreshFlag=refreshFlagIns;
    }

    private List<OutputEvent> processBusiEvent(BondInputEvent event) {
        String eventName = event.getEventName();
        String instrumentId = event.getInstrumentId();


        Map<String,Set<EventHandler>> instrumentHandles = eventHandlers.get(eventName);
        if (instrumentHandles == null || instrumentHandles.size() ==0){
            log.info("can't find handles by eventName {}",eventName);
            return null;
        }
        //交给注册的handle 处理
        Set<EventHandler> handles = instrumentHandles.get(instrumentId);
        if (handles == null || handles.size() ==0){
            if (log.isDebugEnabled()){
                log.debug("can't find handles by instrumentId {}",instrumentId);

            }
            return null;
        }
        List<OutputEvent> result = new ArrayList<>();

        for (EventHandler  handle: handles) {
                List<OutputEvent> result_temp =  handle.processEvent(event);
                result.addAll(result_temp);
            }
      return result;
    }

    private void processStrategyCmd(BondInputEvent event) {
        //处理公共事件
        String eventName = event.getEventName();
        if (StringUtils.equals(eventName,"StrategyCommandEvent")){
            //针对不同命令进行处理
            String instrumentId = event.getInstrumentId();
            log.info("process StrategyCommandEvent eventName is {} ,instrumentId is {}  ",eventName,instrumentId);
            boolean verifyCmd =  StrategyCmdEnum.verifyStrategyCmd(instrumentId);
            if (!verifyCmd){
                log.error("the cmd not in the scope ");
                return ;//返回空
            }
            String strategyName = event.getStrategyName();
            if (StrategyNameEnum.SPREAD_ONE_POOL_STRATEGY.getName().equals(strategyName)) {
                //命令分发
                if (StringUtils.equals(instrumentId,"reload")){
                    //job.onReload(event);
                    refreshFlag = "false";//需要重新从web 加载
                }

                if (StringUtils.equals(instrumentId,"suspend")){
                    //job.onSuspend(event);
                    refreshFlag = "false";//需要重新从web 加载
                }
                if (StringUtils.equals(instrumentId,"stop")){
                    // job.onStop(event);
                    refreshFlag = "false";//需要重新从web 加载
                }

                if (StringUtils.equals(instrumentId,"start")){
                    // job.onStart(event);
                    refreshFlag = "false";//需要重新从web 加载
                }
                if (StringUtils.equals(instrumentId,"dump")){
                    Job.onDump(event);
                }
            }else {
                log.info("不支持当前策略：{}命令的操作！",strategyName);
            }
        }
    }

    @Override
    public void stop() {
        log.info("stop succ");
    }

    public  void start() {
        log.info("start succ");
    }

    @Override
    public void asyncProcEvent(BondInputEvent event) {
        log.info("put event {}",event);
    }

    /**
     * 设置因子输出网关
     * @param factorOutput
     */
    public void  setFactorOutputGateway(FactorOutputGateway factorOutput){
        log.info("factorOutput {}",factorOutput);
    }
}
