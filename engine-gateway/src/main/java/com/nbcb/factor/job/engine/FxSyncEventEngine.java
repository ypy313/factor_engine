package com.nbcb.factor.job.engine;

import com.nbcb.factor.common.StrategyNameEnum;
import com.nbcb.factor.event.*;
import com.nbcb.factor.job.gateway.FactorOutputGateway;
import com.nbcb.factor.strategy.CmdTypeEnum;
import com.nbcb.factor.strategy.StrategyCmdEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 运行在flink环境中的engine ,只做事件同步处理，多线程等交给flink环境处理
 */
@Slf4j
public class FxSyncEventEngine extends FxAbstractEventEngine{
    private static volatile boolean fxRefreshFlag = true;//外汇是否重新加载因子实例
    private static volatile boolean isDelete = false;//外汇是否为删除实例
    private static List<SpecifyData> cmdsSpecifyData = new ArrayList<>();//外汇个别修改数据
    private static volatile boolean isUpdateTimeMap = true;//是否更新过滤信号map中时间

    public static boolean getFxRefreshFlag() {return fxRefreshFlag;}
    public static void setFxRefreshFlag(boolean flag) {fxRefreshFlag = flag;}
    public static boolean isFxRefreshFlag(){return fxRefreshFlag;}
    public static List<SpecifyData> getCmdsSpecifyData() {return cmdsSpecifyData;}
    public static void setCmdsSpecifyData(List<SpecifyData> cmdsSpecifyData) {FxSyncEventEngine.cmdsSpecifyData = cmdsSpecifyData;}
    public static boolean getIsDelete() {return isDelete;}
    public static void setIsDelete(boolean isDelete) {FxSyncEventEngine.isDelete = isDelete;}
    public static boolean isIsUpdateTimeMap(){return isUpdateTimeMap;}
    public static void setIsUpdateTimeMap(boolean isUpdateTimeMap){FxSyncEventEngine.isUpdateTimeMap = isUpdateTimeMap;}
    @Override
    public void stop() {
        log.info("stop succ");
    }

    @Override
    public void start() {
        log.info("start succ");
    }
    @Override
    public void asyncProcEvent(SymbolInputEvent event){
        log.info("put event {}",event);
    }

    /**
     * 设置因子输出网关
     */
    @Override
    public void setFactorOutPutGateway(FactorOutputGateway factorOutput) {
        log.info("factorOutput {}",factorOutput);
    }

    /**
     * 处理事件
     * @param event
     * @return
     */
    public List<SymbolOutputEvent> syncProcEvent(SymbolInputEvent event){
        //先处理策略命令相关事件，主线程中处理
        if (event instanceof StrategyCommandSymbolInputEvent) {
            processStrategyCmd(event);
            return null;
        }else {
            //线程池处理业务事件
            return new ArrayList<>();
        }
    }

    private void processStrategyCmd(SymbolInputEvent event){
        //处理公共事件
        String eventName = event.getEventName();
        if (StringUtils.equals(eventName,"StrategyCommandEvent")) {
            //针对不同命令进行处理
            String instrumentId = event.getInstrumentId();
            log.info("process StrategyCommandEvent eventName is {},instrumentId is {}",eventName,instrumentId);
            boolean verifyCmd = StrategyCmdEnum.verifyStrategyCmd(instrumentId);
            if (!verifyCmd) {
                log.error("the cmd not in the scope");
                return;//返回空
            }
            String strategyName = event.getStrategyName();

            if (StrategyNameEnum.TA.getName().equals(strategyName)
                    || StrategyNameEnum.PM.getName().equals(strategyName)) {
                StrategyCommand eventData = (StrategyCommand) event.getEventData();
                if (eventData.getCmdType().equals(CmdTypeEnum.SPECIFY.getType())) {
                    cmdsSpecifyData = eventData.getData();
                }else {
                    cmdsSpecifyData = new ArrayList<>();
                }
                //命令分发
                if (StringUtils.equals(instrumentId,"reload")) {
                    fxRefreshFlag = false;//需要重新从web加载
                    isUpdateTimeMap = false;
                }
                if (StringUtils.equals(instrumentId,"suspend")) {
                    fxRefreshFlag = false;//需要重新从web加载
                    isUpdateTimeMap = false;
                }
                if (StringUtils.equals(instrumentId,"stop")) {
                    fxRefreshFlag = false;//需要重新从web加载
                    isUpdateTimeMap = false;
                }
                if (StringUtils.equals(instrumentId,"start")) {
                    fxRefreshFlag = false;//需要重新从web加载
                    isUpdateTimeMap = false;
                }
                if (StringUtils.equals(instrumentId,"delete")) {
                    fxRefreshFlag = false;//需要重新从web加载
                    isUpdateTimeMap = false;
                    isUpdateTimeMap = false;
                }
            }else {
                log.info("不支持当前策略：{}命令的操作！",strategyName);
            }
        }
    }
}
