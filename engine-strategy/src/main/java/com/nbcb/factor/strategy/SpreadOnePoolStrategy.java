package com.nbcb.factor.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nbcb.factor.common.JsonUtil;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.event.BondInputEvent;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.monitorfactor.Factor;
import com.nbcb.factor.monitorfactor.impl.ChinaBondSpreadPercentileFactor;
import com.nbcb.factor.monitorfactor.impl.ChinaBondSpreadVolatilityFactor;
import com.nbcb.factor.monitorfactor.impl.RealTimeSpreadFactor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 价差债券池策略
 */
@Slf4j
public class SpreadOnePoolStrategy extends StrategyInstanceTemplate{
    private int preLoad = 0;
    public static String STRATEGY_NAME = "SpreadOnePoolStrategy";//每个策略模型必须唯一声明
    private String dataSource = "All";//默认是全部数据源

    @Override
    public void init(Map paraMap) {
        Map marketDataSetting = (Map) paraMap.get(StrategyInstanceTemplate.MARKETDATASETTING);
        String leg1 = (String) marketDataSetting.get(StrategyInstanceTemplate.LEG1);
        String leg2 = (String) marketDataSetting.get(StrategyInstanceTemplate.LEG2);

        //优先从redis中加载，次优空值
        //先清理，保证每次都是加载redis
        leg1AllSourceYieldMap.clear();
        leg2AllSourceYieldMap.clear();
        loadAllSourceLegMap(leg1,leg1AllSourceYieldMap);
        loadAllSourceLegMap(leg2,leg2AllSourceYieldMap);

        //从全渠道计算最优
        calcBestFromAllSource(bestLeg1YieldMap,leg1AllSourceYieldMap);
        calcBestFromAllSource(bestLeg2YieldMap,leg2AllSourceYieldMap);

        //初始化两条腿
        String id = (String) paraMap.get("instanceId");//instance id
        instanceId = id;
        String strategyInstanceName = (String) paraMap.get("instanceName");//instance id
        instanceName = strategyInstanceName;
        instrumentIds.add(leg1);
        instrumentIds.add(leg2);


        //初始化预加载个数
        Long preLoadLong = Long.parseLong(marketDataSetting.get("preLoad").toString());//preLoad
        preLoad = preLoadLong.intValue();

        //初始化数据源配置
        String datasourceStr = (String) marketDataSetting.get("dataSource");//preLoad
        if (StringUtils.isNotBlank(datasourceStr)) {
            dataSource = datasourceStr;
        }

        //关联百分位数
        Factor chinaBondSpread = new ChinaBondSpreadPercentileFactor();
        chinaBondSpread.init(paraMap);//初始化factor
        factorList.add(chinaBondSpread);

        //关联波动率
        Factor chinaBondVolatility = new ChinaBondSpreadVolatilityFactor();
        chinaBondVolatility.init(paraMap);
        factorList.add(chinaBondVolatility);

        //关联实时价差
        Factor realTimeSpreadFactor = new RealTimeSpreadFactor();
        realTimeSpreadFactor.init(paraMap);
        factorList.add(realTimeSpreadFactor);


        this.setPercentileFactor(chinaBondSpread);//注册百分位查询的计算器

        log.info("init SpreadOnePoolStrategy paraMap is {}",paraMap);
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    protected boolean beforeProcessEvent(BondInputEvent event) {
        if (preLoad > 0) { //个数预检
            String strStraInsCalc = RedisUtil.getInstance().getStraInsCalc(instanceId);
            try {
                StrategyInstanceCalculate strategyInstanceCalculate =
                        JsonUtil.toObject(strStraInsCalc, StrategyInstanceCalculate.class);
                int countMD = 0;
                if (strategyInstanceCalculate != null) {
                    countMD = strategyInstanceCalculate.getCountMd();
                }

                int intCountMd = countMD;
                //判断事件数量是否已经足够
                if (intCountMd > preLoad) {
                    return true;
                } else {
                    log.info("preload event number need  {} , count md is {}", preLoad, intCountMd);
                    return false;
                }

            } catch (JsonProcessingException e) {
                log.error("beforeProcessEvent", e);
                return false;
            }
        }
        log.info("SpreadOnePoolStrategy beforeProcessEvent");
        return true;
    }

    @Override
    protected boolean afterProcessEvent(BondInputEvent event, BigDecimal calc, List<OutputEvent> rstLst) {
        if (preLoad > 0) { //个数预检
            String strStraInsCalc = RedisUtil.getInstance().getStraInsCalc(instanceId);
            try {
                StrategyInstanceCalculate strategyInstanceCalculate =
                        JsonUtil.toObject(strStraInsCalc, StrategyInstanceCalculate.class);
                int countMD = 0;
                if (strategyInstanceCalculate != null) {
                    countMD = strategyInstanceCalculate.getCountMd();
                } else {
                    strategyInstanceCalculate = new StrategyInstanceCalculate();
                }

                int intCountMd = countMD;
                intCountMd = intCountMd + 1;//增加一条
                strategyInstanceCalculate.setCountMd(intCountMd);
                log.info("afterProcessEvent ,count md is {}", intCountMd);
                RedisUtil.getInstance().setStraInsCalc(instanceId, strategyInstanceCalculate);
            } catch (JsonProcessingException e) {
                log.error("afterProcessEvent", e);
                return false;
            }
        }
        log.info("SpreadOnePoolStrategy afterProcessEvent event id is {}", event.getEventId());
        return true;
    }

    @Override
    protected Set<String> subscribeEvents() {

        log.info("strategy dataSource is {}", dataSource);

        if (StringUtils.equals("All", dataSource)) {
            eventNames.add("TestEvent");
            eventNames.add("KafkaEvent");
            eventNames.add("BondDealEvent");
            eventNames.add("MarketDataSnapshotFullRefreshEvent");
            eventNames.add("BondBestOfferEvent");
            eventNames.add("BrokerBondBestOfferEvent");
            eventNames.add("AllMarketDataEvent");
        }

        if (StringUtils.equals("Cdh", dataSource)) {
            eventNames.add("BondBestOfferEvent");
            eventNames.add("BondDealEvent");
            eventNames.add("AllMarketDataEvent");
        }

        if (StringUtils.equals("Cfets", dataSource)) {
            eventNames.add("MarketDataSnapshotFullRefreshEvent");
            eventNames.add("AllMarketDataEvent");
        }
        if (StringUtils.equals("Broker", dataSource)) {
            eventNames.add("BrokerBondBestOfferEvent");
            eventNames.add("AllMarketDataEvent");
        }

        return eventNames;
    }
}
