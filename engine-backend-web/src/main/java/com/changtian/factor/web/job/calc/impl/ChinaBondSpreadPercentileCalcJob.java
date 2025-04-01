package com.changtian.factor.web.job.calc.impl;

import com.changtian.factor.common.*;
import com.changtian.factor.entity.StrategyConfigVo;
import com.changtian.factor.entity.localcurrency.IndexSettingsAndMonitorResult;
import com.changtian.factor.entity.localcurrency.LocalCurrencyMarketDataSetting;
import com.changtian.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.changtian.factor.web.entity.ChinaBondSpreadPercentileCache;
import com.changtian.factor.web.job.calc.FactorCalcJob;
import com.changtian.factor.web.response.exception.CalcException;
import com.changtian.factor.web.service.BondPercentileService;
import com.changtian.factor.web.service.StrategyInstanceService;
import com.changtian.factor.web.util.ExceptionUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ChinaBondSpreadPercentileCalcJob implements FactorCalcJob {
    @Value("5")
    private int depth;

    @Autowired
    private BondPercentileService bondPercentileService;

    @Autowired
    private StrategyInstanceService strategyInstanceService;

    private final RedisUtil redisUtil = RedisUtil.getInstance();

    @Override
    public ReturnT executeJob(String param) {
        try {
            XxlJobLogger.log("ChinaBondSpreadPercentileJob percentileJob");

            String allSpreadStrategy = (String) LoadConfig.getProp().get("SpreadStrategy");
            String[] spreadStrategyArr = allSpreadStrategy.split(",");
            if (spreadStrategyArr.length < 1) {
                XxlJobLogger.log("configuration error not found SpreadStrategy");
                return ReturnT.FAIL;
            }
            //清除redis中中债百分位表
            redisUtil.delString(AllRedisConstants.FACTOR_SPREAD_CBOND_PERCENT);
            for (int i = 0; i < spreadStrategyArr.length; i++) {
                cachePercentileTable(spreadStrategyArr[i]);
            }
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("跑批失败，原因为：{}", ExceptionUtils.toString(e));
            return ReturnT.FAIL;
        }
    }

    /**
     * * String configjson = "{" +
     * * "\"strategyName\":\"SpreadOnePoolStrategy\"," +
     * * "\"strategyInstanceName\":\"SpreadOnePoolStrategy.111\"," +
     * * "\"marketDataSetting\":{\"leg1\":\"220203.IB\",\"leg2\":\"101800431.IB\",\"dataSource\":" +
     * * "\"All\",\"dataDirect\":\"bid\",\"preLoad\":10}," +
     * * "\"monitorSetting\":[" +
     * * "{\"name\":\"realSpread\",\"chiName\":\"实时价差\",\"dataWindow\":-1,\"yellowUpper\":0,\"yellowDown\":0,\"orangeUpper\":0,\"orangeDown\":0,\"redUpper\":0,\"redDown\":0}," +
     * * "{\"name\":\"ChinaBondSpreadPercentile\",\"chiName\":\"中债历史价差\",\"dataWindow\":0,\"yellowUpper\":0,\"yellowDown\":0,\"orangeUpper\":0,\"orangeDown\":0,\"redUpper\":0,\"redDown\":0}," +
     * * "{\"name\":\"XX\",\"chiName\":\"自定义\",\"dataWindow\":0,\"yellowUpper\":0,\"yellowDown\":-1,\"orangeUpper\":0,\"orangeDown\":-1,\"redUpper\":0,\"redDown\":-1}" +
     * * "]" +
     * * "};";
     */
    private void cachePercentileTable(String strategyName) {
        StrategyConfigVo strategyConfigVo = new StrategyConfigVo();
        strategyConfigVo.setStrategyName(strategyName);
        //获取配置
        List<LocalCurrencyStrategyInstanceResult> configJson = strategyInstanceService.getConfigJson(strategyConfigVo);
        for (LocalCurrencyStrategyInstanceResult localCurrencyStrategyInstanceResponse : configJson) {
            LocalCurrencyMarketDataSetting marketDataSetting = localCurrencyStrategyInstanceResponse.getMarketDataSetting();
            String leg1 = marketDataSetting.getLeg1();
            String leg2 = marketDataSetting.getLeg2();
            for (IndexSettingsAndMonitorResult indexSetting : localCurrencyStrategyInstanceResponse.getSpreadOnePoolMonitorSetting()) {
                int indexPreferences = indexSetting.getDataWindow();
                //百分位表存入redis
                setPercentileTable(leg1, leg2, indexPreferences, depth);
            }
        }
    }


    /**
     * 存入redis
     */
    private void setPercentileTable(String leg1, String leg2, int days, int depth) {
        Map<BigDecimal, BigDecimal> map;
        //获取百分位表
        try {
            map = bondPercentileService.calcPercentile(leg1, leg2, days, depth);
            if (map == null) {
                log.error("error when calculating percentile table");
                XxlJobLogger.log("error when calculating percentile table");
                return;
            }
        } catch (CalcException calcException) {
            log.error("occur calcException,leg1:{},leg2:{},datawindow:{}", leg1, leg2, days);
            XxlJobLogger.log("occur calcException,leg1:{},leg2:{},datawindow:{}", leg1, leg2, days);
            return;
        } catch (Exception e) {
            XxlJobLogger.log("unknown exception", ExceptionUtils.toString(e));
            log.error("unknown exception", ExceptionUtils.toString(e));
            return;
        }

        ChinaBondSpreadPercentileCache percentileCache = new ChinaBondSpreadPercentileCache();
        percentileCache.setPercentileMapDay(DateUtil.getDaystr());
        percentileCache.setPercentileMap(map);
        String json;
        try {
            json = JsonUtil.toJson(percentileCache);
            redisUtil.setChinaBondSpreadCache(leg1, leg2, (long) days, json);
            XxlJobLogger.log("percentile map has put into redis,leg1:{},leg2{},datawindow:{}", leg1, leg2, days);
        } catch (Exception e) {
            XxlJobLogger.log("percentile table to JSON failed,leg1:{},leg2{},datawindow:{}", leg1, leg2, days, ExceptionUtils.toString(e));
        }
    }


}
