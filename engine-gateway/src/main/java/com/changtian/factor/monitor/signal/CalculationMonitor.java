package com.changtian.factor.monitor.signal;

import com.changtian.factor.common.Constants;
import com.changtian.factor.entity.forex.MonitorSetting;
import com.changtian.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.changtian.factor.event.SymbolOutputEvent;
import com.changtian.factor.flink.aviatorfun.entity.OhlcParam;
import com.changtian.factor.output.IndexCalOutResult;
import com.changtian.factor.output.OhlcDetailResult;
import com.changtian.factor.output.forex.ForexOhlcOutputEvent;
import com.changtian.factor.utils.EntityConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 监控表达式计算
 */
@Slf4j
public class CalculationMonitor {

    public List<SymbolOutputEvent<IndexCalOutResult>> calculateMonitor(LocalCurrencyStrategyInstanceResult config
    , Map<String, Object> env, List<OhlcParam> ohlcParamList, OhlcDetailResult ohlcDetail){
        //添加计算监控表达式参数
        List<Double> openList = EntityConvertUtils.getPrice(ohlcParamList, Constants.OPEN);
        List<Double> highList = EntityConvertUtils.getPrice(ohlcParamList, Constants.HIGH);
        List<Double> lowList = EntityConvertUtils.getPrice(ohlcParamList, Constants.LOW);
        List<Double> closeList = EntityConvertUtils.getPrice(ohlcParamList, Constants.CLOSE);
        env.put(Constants.TA_LIST_OPEN, openList);
        env.put(Constants.TA_LIST_HIGH, highList);
        env.put(Constants.TA_LIST_LOW, lowList);
        env.put(Constants.TA_LIST_CLOSE, closeList);
        env.put(Constants.TICK, ohlcDetail.getClosePrice());

        List<SymbolOutputEvent<IndexCalOutResult>> monitorResultList = new ArrayList<>();
        for (MonitorSetting monitorSetting : config.getMonitorSetting()) {
            //监控表达式函数计算
            String expression = monitorSetting.getExpression();
            boolean execute = false;
            try{
                execute = (Boolean) FunctionCalculator.execute(FunctionExpressionCarrier.builder()
                        .expression(expression).env(env).build());
            }catch (Exception e){
                log.error("calculate fail,instanceId:{},exception is :{}",config,e);
            }
            //输出结果实体组装
            ForexOhlcOutputEvent<IndexCalOutResult> forexOhlcOutputEvent = new ForexOhlcOutputEvent<>();
            //组装ForexOhlcOutputEvent
            forexOhlcOutputEvent.setCalculationResultMap(env);

            IndexCalOutResult indexCalOutResult = new IndexCalOutResult();
            BeanUtils.copyProperties(monitorSetting,indexCalOutResult);
            BeanUtils.copyProperties(config,indexCalOutResult);

            indexCalOutResult.setMonitorResult(execute);
            indexCalOutResult.setTriggerRule(monitorSetting.getExpression());
            indexCalOutResult.setPushStartTime(config.getMarketDataSetting().getPushStartTime());
            indexCalOutResult.setPushEndTime(config.getMarketDataSetting().getPushEndTime());
            indexCalOutResult.convert(ohlcDetail);

            forexOhlcOutputEvent.setOhlcDetailResult(indexCalOutResult);
            monitorResultList.add(forexOhlcOutputEvent);
        }
        return monitorResultList;
    }
}
