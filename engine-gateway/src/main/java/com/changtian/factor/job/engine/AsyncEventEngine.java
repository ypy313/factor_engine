package com.changtian.factor.job.engine;

import com.changtian.factor.event.BondInputEvent;
import com.changtian.factor.event.OutputEvent;
import com.changtian.factor.event.StrategyCommandBondInputEvent;
import com.changtian.factor.job.gateway.FactorOutputGateway;
import com.changtian.factor.strategy.StrategyCmdEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 事件处理引擎
 * 收到事件到放入到queue 队列
 * 另外线程从队列中取出处理
 * job 启动的时候，调用start初始化线程池等；
 *    调用 register 装入对应的策略处理器
 *    事件产生的时候，调用putEvent 插入事件
 */
@Slf4j
public class AsyncEventEngine extends AbstractEventEngine implements Serializable {
    @Getter@Setter
    private static String refreshFlag = "true";
    private static BlockingDeque<BondInputEvent> eventQueue = new LinkedBlockingDeque();//事件队列

    private static Thread collectorThread;//事件处理主线程
    private static ThreadPoolTaskExecutor threadPool ;// 事件分发线程池
    private static FactorOutputGateway factorOutputGateway;



    /**
     * 事件处理引擎开始处理
     */
    public  void start() {
        collectorThread = new Thread(() -> {
            while (true) {
                try {
                    log.debug("AsyncEventEngine deal event");
                    //取出事件
                    BondInputEvent event = eventQueue.take();

                    log.info("AsyncEventEngine take event id is {}",event.getEventId());

                    //先处理策略命令相关事件。主线程中处理
                    if (event instanceof StrategyCommandBondInputEvent){
                        processStrategyCmd(event);
                    }else{
                        //线程池处理业务事件
                        processBusiEvent(event);
                    }

                } catch (Exception e) {
                    log.error("take event from queue exception",e);
                }

            }
        });

        collectorThread.start();
        threadPool = threadPool();
    }

    private void processBusiEvent(BondInputEvent event) {
        String eventName = event.getEventName();
        String instrumentId = event.getInstrumentId();


        Map instrumentHandles = eventHandlers.get(eventName);
        if (instrumentHandles == null || instrumentHandles.size() ==0){
            log.info("can't find handles by eventName {}",eventName);
            return;
        }
        //交给注册的handle 处理
        List<EventHandler> handles = (List) instrumentHandles.get(instrumentId);
        if (handles == null || handles.size() ==0){
            if (log.isDebugEnabled()){
                log.debug("can't find handles by instrumentId {}",instrumentId);
            }
            return;
        }

        threadPool.execute(()->{
            for (EventHandler  handle: handles) {
                List<OutputEvent> result =  handle.processEvent(event);
                factorOutputGateway.output(result);//输出处理结果
            }
        });
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
            //命令分发
            if (StringUtils.equals(instrumentId,"reload")){
                refreshFlag = "false";
            }

            if (StringUtils.equals(instrumentId,"suspend")){
                refreshFlag = "false";
            }
            if (StringUtils.equals(instrumentId,"stop")){
                refreshFlag = "false";
            }

            if (StringUtils.equals(instrumentId,"start")){
                refreshFlag = "false";
            }
            if (StringUtils.equals(instrumentId,"dump")){
                job.onDump(event);
            }
        }

    }

    /**
     * 事件处理引擎停止处理
     */
    public  void stop() {
        try {
            collectorThread.interrupt();
            threadPool.shutdown();
        }finally {
            log.info("AsyncEventEngine stop");
        }
    }



    /**
     * 装入事件
     * @param event
     */
    @Override
    public  void asyncProcEvent(BondInputEvent event)
    {
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            log.info("AsyncEventEngine putEvent exception",e);
        }
    }



    /**
     * 初始化线程池
     * @return
     */
    private static  ThreadPoolTaskExecutor threadPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.initialize();
        threadPoolTaskExecutor.setCorePoolSize(3);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.setQueueCapacity(100);
        threadPoolTaskExecutor.setKeepAliveSeconds(300);
        threadPoolTaskExecutor.setThreadNamePrefix("event_engine");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPoolTaskExecutor;
    }

    /**
     * 设置因子输出网关
     * @param factorOutput
     */
    public void  setFactorOutputGateway(FactorOutputGateway factorOutput){
        factorOutputGateway = factorOutput;
    }



    @Override
    public List<OutputEvent> syncProcEvent(BondInputEvent event) {
        log.info("AsyncEventEngine putEventIn Flink");
        return null;
    }


}
