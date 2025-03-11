package com.nbcb.factor.monitor.signal;

import com.nbcb.factor.common.Constants;
import com.nbcb.factor.entity.forex.MonitorSetting;
import com.nbcb.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.flink.aviatorfun.entity.OhlcParam;
import com.nbcb.factor.output.IndexCalOutResult;
import com.nbcb.factor.output.OhlcDetailResult;
import com.nbcb.factor.output.forex.ForexOhlcOutputEvent;
import com.nbcb.factor.utils.EntityConvertUtils;
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
