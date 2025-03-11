package com.nbcb.factor.job.engine;

import com.nbcb.factor.event.BondInputEvent;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.job.Job;
import com.nbcb.factor.job.gateway.FactorOutputGateway;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
public abstract class AbstractEventEngine {
    protected static Job job;
    protected static Map<String, Map<String, Set<EventHandler>>> eventHandlers = new HashMap<>();//事件名称 ，金融工具id，事件处理器


    public void registerJob(Job job){
        AbstractEventEngine.job = job;
    }
    public abstract  void stop() ;
    public abstract  void start() ;
    public abstract void asyncProcEvent(BondInputEvent event);
    public abstract void setFactorOutputGateway(FactorOutputGateway factorOutput);
    public abstract   List<OutputEvent> syncProcEvent(BondInputEvent event);
    


    public  void unregister(String eventName,String instrumentId,EventHandler handler){
        Map<String, Set<EventHandler>> instrumentHandles =  eventHandlers.get(eventName);
        //如果事件从来没有登记过
        if (instrumentHandles == null || instrumentHandles.isEmpty()){
            log.info("{} eventName is not register",eventName);
        }else{//事件已经有登记
            Set<EventHandler>  handlerList =  instrumentHandles.get(instrumentId);
            //事件对应的债券未登记
            if (handlerList == null || handlerList.isEmpty()){
                log.info("{} eventName is registered {} instrumentId id  not register",eventName,instrumentId);
            }else{//对应债券已经登记
                handlerList.remove(handler);//删除
            }
        }
    }

    /**
     * 注册handler 类
     */
    public  void register(String eventName,String instrumentId,EventHandler handler){
        Map<String, Set<EventHandler>> instrumentHandles =  eventHandlers.get(eventName);
        //如果事件从来没有登记过
        if (instrumentHandles == null || instrumentHandles.isEmpty()){
            instrumentHandles = new HashMap<>();
            Set<EventHandler> handlerList = new HashSet<>();
            handlerList.add(handler);
            instrumentHandles.put(instrumentId,handlerList);
            eventHandlers.put(eventName,instrumentHandles);
        }else{//事件已经有登记
            Set<EventHandler>  handlerList =  instrumentHandles.get(instrumentId);
            //事件对应的债券未登记
            if (handlerList == null || handlerList.isEmpty()){
                handlerList = new HashSet<>();
                handlerList.add(handler);
                instrumentHandles.put(instrumentId,handlerList);
            }else{//对应事件和债券都已经登记，增加处理器
                handlerList.add(handler);
                log.info("eventname {} , instrument id {} already exists",eventName,instrumentId);
            }
        }
    }


}
