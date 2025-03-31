package com.nbcb.factor.web.job.calc.impl;

import com.nbcb.factor.common.*;
import com.nbcb.factor.entity.StrategyConfigVo;
import com.nbcb.factor.entity.localcurrency.IndexSettingsAndMonitorResult;
import com.nbcb.factor.entity.localcurrency.LocalCurrencyMarketDataSetting;
import com.nbcb.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.nbcb.factor.web.entity.ChinaBondSpreadVolatilityCache;
import com.nbcb.factor.web.job.calc.FactorCalcJob;
import com.nbcb.factor.web.response.exception.CalcException;
import com.nbcb.factor.web.service.BondVolatilityService;
import com.nbcb.factor.web.service.StrategyInstanceService;
import com.nbcb.factor.web.util.ExceptionUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ChinaBondSpreadVolatilityCalcJob implements FactorCalcJob {
    @Autowired
    private BondVolatilityService bondVolatilityService;

    @Autowired
    private StrategyInstanceService strategyInstanceService;

    private final RedisUtil redisUtil = RedisUtil.getInstance();


    @Override
    public ReturnT executeJob(String param) {
        try{
            //清理redis中历史波动率和平均值
            XxlJobLogger.log("ChinaBondSpreadVolatilityCalcJob volatilityJob");

            String allSpreadStrategy = (String) LoadConfig.getProp().get("SpreadStrategy");
            String[] spreadStrategyArr = allSpreadStrategy.split(",");
            if(spreadStrategyArr.length<1){
                XxlJobLogger.log("configuration error not found spreadStrategy");
                return ReturnT.FAIL;
            }
            redisUtil.delString(AllRedisConstants.FACTOR_SPREAD_CBOND_VOLATILITY);
            //获取配置
            for (int i = 0; i < spreadStrategyArr.length; i++) {
                cacheSpreadVolatility(spreadStrategyArr[i]);
            }
            return ReturnT.SUCCESS;

        }catch (Exception e){
            XxlJobLogger.log("跑批失败，原因为：{}", ExceptionUtils.toString(e));
            return ReturnT.FAIL;
        }
    }

    private void cacheSpreadVolatility(String strategyName){
        StrategyConfigVo strategyConfigVo = new StrategyConfigVo();
        strategyConfigVo.setStrategyName(strategyName);
        List<LocalCurrencyStrategyInstanceResult> configs = strategyInstanceService.getConfigJson(strategyConfigVo);
        for (LocalCurrencyStrategyInstanceResult config : configs) {
            LocalCurrencyMarketDataSetting marketDataSetting = config.getMarketDataSetting();
            String leg1 = marketDataSetting.getLeg1();
            String leg2 = marketDataSetting.getLeg2();
            List<IndexSettingsAndMonitorResult> indexSettings = config.getSpreadOnePoolMonitorSetting();
            for (IndexSettingsAndMonitorResult indexSetting : indexSettings) {
                if (Constants.FACTOR_SPREAD_CBOND_VOLATILITY.equals(indexSetting.getName())) {
                    int dataWindow = indexSetting.getDataWindow();
                    //波动率和平均值存入redis
                    setSpreadVolatility(leg1,leg2,dataWindow);
                }

            }
        }
    }
    /**
     * 存入redis
     */
    public void setSpreadVolatility(String leg1, String leg2, int days){
        //获取波动率和平均值
        Map<String, BigDecimal> map;
        try{
            map = bondVolatilityService.calcVolatility(leg1,leg2,days);
            if (map == null) {
                XxlJobLogger.log("error when calculating Volatility");
                return;
            }
        }catch (CalcException calcException){
            XxlJobLogger.log("occur calcException,leg1:{},leg2:{},days:{}", leg1, leg2, days, days);
            return;
        }catch (Exception e){
            XxlJobLogger.log("unknown exception\n{}",ExceptionUtils.toString(e));
            return;
        }
        ChinaBondSpreadVolatilityCache vol = new ChinaBondSpreadVolatilityCache();
        vol.setVolatilityDay(DateUtil.getDaystr());
        vol.setHistoryMean(map.get("average"));
        vol.setHistoryVolatility(map.get("std"));
        String json;
        try{
            json = JsonUtil.toJson(vol);
            redisUtil.setChinaBondVolatilityCache(leg1,leg2,(long) days,json);
            XxlJobLogger.log("volatility map has put into redis,leg1:{},leg2:{},dataWindow:{},json:{}"
                    ,leg1,leg2,days,json);
        }catch (Exception e){
            XxlJobLogger.log("Conversion of volatility to JSON failed,leg1{},leg2:{},dataWindow:{}\n{}"
                    ,leg1,leg2,days,ExceptionUtils.toString(e));

        }

    }
}
