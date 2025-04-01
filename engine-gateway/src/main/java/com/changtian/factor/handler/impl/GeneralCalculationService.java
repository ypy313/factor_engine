package com.changtian.factor.handler.impl;

import com.changtian.factor.common.StringUtil;
import com.changtian.factor.entity.forex.GlobalParameter;
import com.changtian.factor.entity.forex.ParameterSetting;
import com.changtian.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.changtian.factor.enums.CalculationTypeEnum;
import com.changtian.factor.enums.DataTypeEnum;
import com.changtian.factor.event.SymbolOutputEvent;
import com.changtian.factor.flink.aviatorfun.entity.OhlcParam;
import com.changtian.factor.handler.CalculationHandler;
import com.changtian.factor.monitor.signal.CalculationMonitor;
import com.changtian.factor.output.IndexCalOutResult;
import com.changtian.factor.output.OhlcDetailResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class GeneralCalculationService
        <T extends LocalCurrencyStrategyInstanceResult, D extends OhlcDetailResult, F extends List<OhlcParam>>
        extends CalculationMonitor
        implements CalculationHandler<T, D, F> {

    public static final String FACTOR_FOREX_HISTORY_OHLC_QUEUE = StringUtil
            .redisKeyFxJoint(DataTypeEnum.FOREX_HISTORY_OHLC_QUEUE.getKey());

    /**
     * 计算指标
     * @param config 配置
     * @param ohlcDetail 消息实体(此处暂时为K线数据)
     * @param ohlcParamList ohlc行特队列
     * @return 结果
     */
    @SuppressWarnings({"rawtypes","unchecked"})
    @Override
    public List<SymbolOutputEvent<IndexCalOutResult>> handler(LocalCurrencyStrategyInstanceResult config
    ,OhlcDetailResult ohlcDetail,List ohlcParamList){
        if (null ==config.getPeriodsSetting()
                ||!config.getPeriodsSetting().getOhlc().equals(ohlcDetail.getPeriod())
                ||!config.getMarketDataSetting().getLeg().equals(ohlcDetail.getSymbol())) {
            return new ArrayList<>();
        }
        Map<String, Object> evn = new HashMap<>();
        List<ParameterSetting> parameterSetting = config.getParameterSetting();
        if (!CollectionUtils.isEmpty(parameterSetting)) {
            parameterSetting.forEach(param->evn.put(param.getIndexName(),param.getIndexPreferences()));
        }
        List<GlobalParameter> globalParameter = config.getGlobalParameter();
        if (!CollectionUtils.isEmpty(globalParameter)) {
            globalParameter.forEach(param->evn.put(param.getParameterName(),param.getParameterValue()));
        }

        evn.put("MARKET_DATA",ohlcParamList);
        //获取计算公式
        String formula = config.getFormula();
        if (StringUtils.isEmpty(formula)) {
            log.warn("formula is empty,{},{}",config.getInstanceId(),config.getInstanceName());
            return new ArrayList<>();
        }
        Map<String,Object> resultMap = new HashMap<>();
        try{
            resultMap = (Map<String,Object>) FunctionCalculator.execute(
                    FunctionExpressionCarrier.builder()
                            .expression(config.getFormula()).env(env).build());
        }catch (Exception e){
            log.error("calculate fail,config:{},exception is:{}",config,e);
        }
        //返回指标配置监控与结果deng
        return calculateMonitor(config,resultMap,ohlcParamList,ohlcDetail);
    }

    @Override
    public String getCalculationType(){return CalculationTypeEnum.GENERAL.getName();
    }

}
