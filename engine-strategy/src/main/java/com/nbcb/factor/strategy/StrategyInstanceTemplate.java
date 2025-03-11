package com.nbcb.factor.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nbcb.factor.SpreadTradeSignalDetailResult;
import com.nbcb.factor.common.*;
import com.nbcb.factor.event.BondInputEvent;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.job.engine.StrategyInstance;
import com.nbcb.factor.monitorfactor.Factor;
import com.nbcb.factor.monitorfactor.TradeSignal;
import com.nbcb.factor.output.SpreadCalcDetailResult;
import com.nbcb.factor.output.SpreadCalcResultOutput;
import com.nbcb.factor.output.SpreadTradeSignalResultOutput;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 策略实例模板
 * { "strategyName":"SpreadFactor","instanceName":"SpreadFactor.instanceName.111",
 * "marketDataSetting":{"leg1":"String","dataSource":"All","dataDirect":"bid","preLoad":10},
 * "monitorSetting":[{"name":"realSpread","chiName":"实时价差","dataWindow":-1,"yellowUpper":0,"yellowDown":0,"orangeUpper":0,"orangeDown":0,"redUpper":0,"redDown":0},
 * {"name":"chinaBondSpread","chiName":"中债历史价差","dataWindow":0,"yellowUpper":0,"yellowDown":0,"orangeUpper":0,"orangeDown":0,"redUpper":0,"redDown":0},
 * {"name":"XX","chiName":"自定义","dataWindow":0,"yellowUpper":0,"yellowDown":-1,"orangeUpper":0,"orangeDown":-1,"redUpper":0,"redDown":-1}]};
 */
@Slf4j
public abstract class StrategyInstanceTemplate implements StrategyInstance {
    protected static final String MARKETDATASETTING = "marketDataSetting";
    protected static final String LEG1 = "leg1";
    protected static final String LEG2 = "leg2";
    protected Set<Factor> factorList = new HashSet<>();//因子
    protected Set<String> instrumentIds = new HashSet<>();//资产
    protected Set<String> eventNames = new HashSet<>();//事件

    protected String instanceId;//实例id
    protected String instanceName;//实例名称

    protected Map<String, Map<String, Map<String, Object>>> leg1AllSourceYieldMap = new HashMap<>();
    protected Map<String, Map<String, Map<String, Object>>> leg2AllSourceYieldMap = new HashMap<>();

    protected Map<String, Map<String, Object>> bestLeg1YieldMap = new HashMap<>();
    protected Map<String, Map<String, Object>> bestLeg2YieldMap = new HashMap<>();

    protected Factor factor;//注册百分位的factor

    /**
     * 创建价差计算结果
     *
     * @param event
     * @param factorName
     * @param instanceId
     * @param calMap
     * @return
     */
    protected OutputEvent createCalcEventDealResult(BondInputEvent event, String factorName, String instanceId, Map calMap) {
        Set leg1Set = bestLeg1YieldMap.keySet();
        Set leg2Set = bestLeg2YieldMap.keySet();

        //构造价差的计算结果
        String leg1Name = (String) leg1Set.toArray()[0];
        String leg2Name = (String) leg2Set.toArray()[0];
        SpreadCalcDetailResult calcDetailResult = new SpreadCalcDetailResult();
        calcDetailResult.setCalResult((BigDecimal) calMap.get("mid"));
        calcDetailResult.setBestCalResult((BigDecimal) calMap.get("best"));
        calcDetailResult.setFactorName(factorName);
        calcDetailResult.setInstanceId(instanceId);
        calcDetailResult.setLeg1(leg1Name);
        calcDetailResult.setLeg2(leg2Name);
        Map allLegMap = new HashMap();
        allLegMap.put(LEG1, bestLeg1YieldMap);
        allLegMap.put(LEG2, bestLeg2YieldMap);
        calcDetailResult.setAllLegYieldMap(allLegMap);//参与计算的leg明细值
        calcDetailResult.setInstanceName(getStrategyInstanceName());//实例名称
        calcDetailResult.setStrategyName(getStrategyName());//策略名称
        OutputEvent result = new SpreadCalcResultOutput(event, calcDetailResult);
        return result;
    }

    protected OutputEvent createSignalEventDealResult(BondInputEvent event, String factorName,
                                                      String instanceId, String action,
                                                      Map calcResMap, Map tradeSignalMap) {
        Set leg1Set = bestLeg1YieldMap.keySet();
        Set leg2Set = bestLeg2YieldMap.keySet();

        //构造价差的计算结果
        String leg1Name = (String) leg1Set.toArray()[0];
        String leg2Name = (String) leg2Set.toArray()[0];
        SpreadTradeSignalDetailResult calcDetailResult = new SpreadTradeSignalDetailResult();
        calcDetailResult.setCalResult(CalculateUtil.castBigDecimal(calcResMap.get("mid")));//中间价
        calcDetailResult.setBestCalResult(CalculateUtil.castBigDecimal(calcResMap.get("best")));//最优
        calcDetailResult.setFactorNameCn((String) tradeSignalMap.get("factorNameCn"));
        calcDetailResult.setFactorName(factorName);
        calcDetailResult.setInstanceId(instanceId);
        calcDetailResult.setLeg1(leg1Name);
        calcDetailResult.setLeg2(leg2Name);
        calcDetailResult.setAction(action);//交易动作
        calcDetailResult.setSignal((TradeSignal) tradeSignalMap.get("signal"));//交易信号
        calcDetailResult.setInstanceName(getStrategyInstanceName());//实例名称
        calcDetailResult.setStrategyName(getStrategyName());//策略名称
        calcDetailResult.setTriggerRule((String) tradeSignalMap.get("triggerRule"));

        OutputEvent result = new SpreadTradeSignalResultOutput(event, calcDetailResult);
        return result;
    }

    @Override
    public List<OutputEvent> processEvent(BondInputEvent event) {
        //更新交易腿，包括全渠道和最优
        updateLegMap(event);
        boolean resBefore = beforeProcessEvent(event);
        if (resBefore) {
            //中间价价差
            BigDecimal midPercentile = calCurrentPercentile(event);
            BigDecimal midYieldSpread = calcTwoLegMidYield(bestLeg1YieldMap, bestLeg2YieldMap);//中间价价差
            //最优可成交价差
            BigDecimal bestYieldSpread = calcMostPossibilityDealYieldSpread(bestLeg1YieldMap, bestLeg2YieldMap, midPercentile);
            BigDecimal bestPercentile = getBestPercentile(bestYieldSpread, "best");


            List<OutputEvent> rstLst = processEventByFactor(event, midYieldSpread, bestYieldSpread, midPercentile, bestPercentile);
            afterProcessEvent(event, bestYieldSpread, rstLst);
            //如果处于debug 级别，则打印整个event 数据和内存变量
            if (TraceUtil.isTrace()) {
                dumpOnAfterEvent(event);
            }
            return rstLst;
        } else {
            //即使不满足，仍然要更新leg map
            List<OutputEvent> rstLst = new ArrayList<>();
            afterProcessEvent(event, BigDecimal.ZERO, rstLst);
            log.info("beforeProcessEvent check is false");
            return rstLst;
        }
    }

    private BigDecimal getBestPercentile(BigDecimal bestYieldSpread, String key) {
        Map map = new HashMap();
        map.put(key, bestYieldSpread);
        Map bestPercentileMap = (Map) factor.calc(map);
        return (BigDecimal) bestPercentileMap.get(key);
    }

    /**
     * 因子处理结果
     *
     * @param event
     * @return
     */
    private List<OutputEvent> processEventByFactor(BondInputEvent event, BigDecimal midYieldSpread,
                                                   BigDecimal bestYieldSpread, BigDecimal midPercentile, BigDecimal bestPercentile) {
        log.info("processEvent is {} ,event content is {} strategyCalRes is {} ", event.getEventId(), event.getEventData(), bestYieldSpread);
        if (factorList == null || factorList.isEmpty()) {
            log.error("factorList is empty");
        }


        //在日间开始的几条数据中间加尚无法算出的情况下，因子不能计算
        List<OutputEvent> rstLst = new ArrayList<>();
        if (bestYieldSpread == null) {
            log.info("bestYieldSpread {} midPercentile {} bestPercentile {} is null end calc!!!", bestYieldSpread, midPercentile, bestPercentile);
            return rstLst;
        }
        String directFlag = getDirectFlagByPercentile(midPercentile, bestPercentile);

        Map inputPriceMap = new HashMap();
        inputPriceMap.put("mid", midYieldSpread);
        inputPriceMap.put("best", bestYieldSpread);

        for (Factor factor : factorList) {
            //因子数值计算结果
            Map calcRes = (Map) factor.calc(inputPriceMap);//取得因子的计算结果
            //如果计算值为空，则直接返回。
            if (calcRes == null || calcRes.size() == 0) {
                log.error("factor calc result is null ");
                continue;//计算值为空，无需往下
            }
            OutputEvent result = createCalcEventDealResult(event, factor.getFactorName(),
                    getStrategyInstanceId(), calcRes);
            rstLst.add(result);

            //因子信号计算结果
            Map calResMap = new HashMap();
            calResMap.put("best", (BigDecimal) calcRes.get("best"));//最优来计算是否触发信号
            calResMap.put("direct", directFlag);//方向标记
            Map tradeSignalMap = factor.calcSignal(calResMap);

            TradeSignal tradeSignal = (TradeSignal) tradeSignalMap.get("signal");
//            String triggerRule = (String)tradeSignalMap.get("triggerRule");
            //有信号才生成信号事件，并加入到输出队列中
            if (tradeSignal != TradeSignal.NOTEXISTS) {
                String action = getAction(getStrategyInstanceId(), bestLeg1YieldMap, bestLeg2YieldMap, midPercentile);
                OutputEvent signalResult =
                        createSignalEventDealResult(event, factor.getFactorName(), getStrategyInstanceId(),
                                action, calcRes, tradeSignalMap);
                rstLst.add(signalResult);
            }
        }
        return rstLst;
    }

    /**
     * 策略实例处理结果
     *
     * @param event
     * @return 默认根据最优可成交价格生成
     * 取得策略的最优可成交价格
     */
    protected BigDecimal calCurrentPercentile(BondInputEvent event) {
//        BigDecimal bidYield = event.calcBestBidYield();
//        BigDecimal ofrYield = event.calcBestOfrYield();
//        String instrumentId = event.getInstrumentId();
//
//        //加载到对应的leg 中
//        updateLegMap(instrumentId, bidYield, ofrYield,event.getEventId());

        //1.2 分别取leg1 和 leg 2 的mid 值，
        BigDecimal midYield = calcTwoLegMidYield(bestLeg1YieldMap, bestLeg2YieldMap);

        //1.4 查询mid所处分位数位置，即低于50分位数
        BigDecimal percentile = getBestPercentile(midYield, "mid");
        return percentile;
    }

    /**
     * 处理事件前,调用子策略的结果，如果不为true ，则后续不再处理
     * 用户预加载行情个数判断等场景
     *
     * @param event
     */
    abstract protected boolean beforeProcessEvent(BondInputEvent event);

    /**
     * 处理事件后,
     * 用户预加载行情个数存入redis中操作
     *
     * @param event
     */
    abstract protected boolean afterProcessEvent(BondInputEvent event, BigDecimal calc, List<OutputEvent> rstLst);


    /**
     * 内部事件
     */
    @Override
    public Set<String> getEventNames() {
        //注册公共的事件，包括测试事件，策略命令事件
        eventNames.add("DummyEvent");
        eventNames.add("StrategyCommandEvent");
        return subscribeEvents();
    }

    protected void setPercentileFactor(Factor inFactor) {
        factor = inFactor;
    }


    /**
     * 订阅事件。注册业务事件
     */
    abstract protected Set subscribeEvents();

    /**
     * 更新当前策略的两条腿bid ofr 价格
     *
     * @param event legYiledMap 格式为 {"instrumentId"，{('bid',bidYield),('ofr',ofrYield)}}
     *              如果参数中的instrumentId 和 map 中匹配上了，则更新对应后面的数据，为0的，为null 的收益率保持不变，不能更新
     *              如果没有匹配上，需要留一个日志
     */
    private void updateLegMap(BondInputEvent event) {
        // 更新全渠道
        log.info("before leglAllsourceYieldMap {} bestleg1YieldMap {}", leg1AllSourceYieldMap, bestLeg1YieldMap);
        log.info("before leg2AllSourceYieldMap {} bestLeg2YieldMap {}", leg2AllSourceYieldMap, bestLeg2YieldMap);
        updateAllSourceLegMap(event);
        log.info("after updateAllSourceLegMap leg1AllSounceYieldMap {}", leg1AllSourceYieldMap);
        log.info("after updateAllSourceLegMap leg2AllSourceYieldMap }", leg2AllSourceYieldMap);
        // 更新最优
        calcBestFromAllSource(bestLeg1YieldMap, leg1AllSourceYieldMap);
        log.info("after leg1AllSourceYieldMap {} bestLeglYieldMap {}", leg1AllSourceYieldMap, bestLeg1YieldMap);
        calcBestFromAllSource(bestLeg2YieldMap, leg2AllSourceYieldMap);
        log.info("after leg2AllSourceYieldMap {} bestLeg2YieldMap {}", leg2AllSourceYieldMap, bestLeg2YieldMap);
        //如果最优中出现单边为空或者全部没有行情了，使用中债估值进行补偿
    }

    /**
     * 从全渠道中算出最优
     *
     * @param bestLegYieldMap
     * @param legAllSourceYieldMap
     */
    protected void calcBestFromAllSource(Map<String, Map<String, Object>> bestLegYieldMap,
                                         Map<String, Map<String, Map<String, Object>>> legAllSourceYieldMap) {
        //初始化数据
        Map.Entry<String, Map<String, Map<String, Object>>> allSourceEntry = legAllSourceYieldMap.entrySet().iterator().next();
        //金融工具代码
        String instrumentId = allSourceEntry.getKey();
        Map<String, Map<String, Object>> allSourceMap = allSourceEntry.getValue();

        String ofrEventId = "";
        String bidEventId = "";
        BigDecimal bidYield = BigDecimal.ZERO;
        BigDecimal ofrYield = BigDecimal.ZERO;

        //获取最优行情数据
        Map<String, Object> marketDataMap = allSourceMap.get("AllMarket");
        if (null != marketDataMap) {
            BigDecimal currBid = CalculateUtil.castBigDecimal(marketDataMap.get("bid"));
            if (currBid != null &&
                    (bidYield.compareTo(currBid) > 0 || bidYield.compareTo(BigDecimal.ZERO) == 0)) {
                bidEventId = (String) marketDataMap.get("bidEventId");
                bidYield = currBid;
            }

            BigDecimal currOfr = CalculateUtil.castBigDecimal(marketDataMap.get("ofr"));
            if (currOfr != null &&
                    (ofrYield.compareTo(currOfr) > 0 || ofrYield.compareTo(BigDecimal.ZERO) == 0)) {
                ofrEventId = (String) marketDataMap.get("ofrEventId");
                ofrYield = currOfr;
            }
        }
        //构建行情最优行情数据map
        Map<String, Object> yieldMap = new HashMap<>();
        yieldMap.put("ofrEventId", ofrEventId);
        yieldMap.put("bidEventId", bidEventId);
        yieldMap.put("bid", bidYield);
        yieldMap.put("ofr", ofrYield);

        bestLegYieldMap.put(instrumentId, yieldMap);
    }

    /**
     * 更新全渠道map
     *
     * @param event
     */
    private void updateAllSourceLegMap(BondInputEvent event) {
        BigDecimal bidYield = event.calcBestBidYield();
        BigDecimal ofrYield = event.calcBestOfrYield();
        String instrumentId = event.getInstrumentId();
        String eventId = event.getEventId();
        String source = event.getSource();//来源渠道
        Map.Entry<String, Map<String, Map<String, Object>>> entry = leg1AllSourceYieldMap.entrySet().iterator().next();
        HashMap<String, Object> yieldMap = new HashMap<>();
        String bidEventId = eventId;
        String ofrEventId = eventId;
        if (StringUtils.equals(entry.getKey(), instrumentId)) {
            //匹配第一条腿
            log.info("updateAllSourceLegMap leg1AllSourceYieldmap {} bidEventId {} bidYield:{} ofrYield:{} source {} instrumentId {}",
                    leg1AllSourceYieldMap, eventId, bidYield, ofrYield, source, instrumentId);
            //重新从redis中加载，以redis为准
            loadAllSourceLegMap(instrumentId, leg1AllSourceYieldMap);
            Map<String, Map<String, Object>> allSource = entry.getValue();
            allSource.remove("INIT");//有了行情后，需要去除初始化空值
            Map<String, Object> legMap = allSource.get(source);
            if (legMap != null && legMap.size() > 0) {
                //如果渠道有了，则更新
                legMap.put("bidEventId", bidEventId);
                legMap.put("ofrEventId", ofrEventId);
                legMap.put("bid", bidYield);
                legMap.put("ofr", ofrYield);
            } else {
                //如果渠道还没有，则新建
                yieldMap.put("bidEventId", bidEventId);
                yieldMap.put("ofrEventId", ofrEventId);
                yieldMap.put("bid", bidYield);
                yieldMap.put("ofr", ofrYield);
                allSource.put(source, yieldMap);
            }
            //存储到redis中
            storeIntoRedis(instrumentId, allSource);
        } else {
            Map.Entry<String, Map<String, Map<String, Object>>> leg2Entry = leg2AllSourceYieldMap.entrySet().iterator().next();
            if (StringUtils.equals(leg2Entry.getKey(), instrumentId)) {
                //匹配第二条腿
                log.info("updateAllSourceLegMap leg2AllSourceYieldMap {} bidEventId {} bidYield {} ofrYield {} source {} instrumentId {}",
                        leg2AllSourceYieldMap, eventId, bidYield, ofrYield, source, instrumentId);
                //重新总redsi中加载，以redis为准
                loadAllSourceLegMap(instrumentId, leg2AllSourceYieldMap);
                Map<String, Map<String, Object>> allSource = leg2Entry.getValue();
                allSource.remove("INIT");//有了行情后，需要移除中债
                Map<String, Object> legMap = allSource.get(source);
                Map<String, Object> leg2YieldMap = new HashMap<>();
                if (legMap != null && legMap.size() > 0) {
                    legMap.put("bidEventId", bidEventId);
                    legMap.put("ofrEventId", ofrEventId);
                    legMap.put("bid", bidYield);
                    legMap.put("ofr", ofrYield);
                } else {
                    //如果渠道还没有，则新建
                    leg2YieldMap.put("bidEventId", bidEventId);
                    leg2YieldMap.put("ofrEventId", ofrEventId);
                    leg2YieldMap.put("bid", bidYield);
                    leg2YieldMap.put("ofr", ofrYield);
                    allSource.put(source, leg2YieldMap);
                }
                //存储到redis中
                storeIntoRedis(instrumentId, allSource);
            }
        }
    }

    /**
     * 加载全渠道行情
     *
     * @param leq
     * @param leqMap
     */
    protected void loadAllSourceLegMap(String leq, Map leqMap) {
        // Load from Redis
        RedisUtil redisUtil = RedisUtil.getInstance();
        String leq1Valuestr = redisUtil.getString(AllRedisConstants.FACTOR_LEG_VALUE, leq);

        if (StringUtils.isNotBlank(leq1Valuestr) && !StringUtils.equals("-1", leq1Valuestr)) {
            try {
                Map<String, Map<String, BigDecimal>> leq1map = JsonUtil.toObject(leq1Valuestr, Map.class);
                leqMap.put(leq, leq1map);
                return;
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException", e);
            }
        }

        // 没有加载成功，给空
        // 初始化策略的两条腿的价格
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("bid", null);
        map.put("ofr", null);
        Map<String, Map<String, BigDecimal>> cnbcMap = new HashMap<>();
        cnbcMap.put("INIT", map); // 初始化中债来源为0
        leqMap.put(leq, cnbcMap);
    }

    private void storeIntoRedis(String instrumentId, Map yieldMap) {
        RedisUtil redisUtil = RedisUtil.getInstance();
        String value = "";
        try {
            value = JsonUtil.toJson(yieldMap);
        } catch (JsonProcessingException e) {
            log.info("storeIntoRedis", e);
            redisUtil.setString(AllRedisConstants.FACTOR_LEG_VALUE, instrumentId, value);
        }
    }

    /**
     * 计算两条腿的中间价价差
     *
     * @param leg1YieldMap
     * @param leg2YieldMap
     * @return
     */
    private BigDecimal calcTwoLegMidYield(Map<String, Map<String, Object>> leg1YieldMap,
                                          Map<String, Map<String, Object>> leg2YieldMap) {
        //先要判断空值， 如果有一个leg 都没有值，则返回null

        //获取leg1的bid 报价和ofr 报价
        // 获取leg2 的bid 报价和ofr 报价
        //leg 的mid 为 (bid + ofr) /2
        // 价差为leg1 的mid 减去 leg2  的mid

        //如果leg 中只有bid或者ofr ，则使用bid 或者ofr


        BigDecimal leg1Bid = CalculateUtil.castBigDecimal(leg1YieldMap.entrySet().iterator().next().getValue().get("bid"));
        BigDecimal leg1Ofr = CalculateUtil.castBigDecimal(leg1YieldMap.entrySet().iterator().next().getValue().get("ofr"));
        BigDecimal leg2Bid = CalculateUtil.castBigDecimal(leg2YieldMap.entrySet().iterator().next().getValue().get("bid"));
        BigDecimal leg2Ofr = CalculateUtil.castBigDecimal(leg2YieldMap.entrySet().iterator().next().getValue().get("ofr"));
        if (leg1Bid == null && leg1Ofr == null) {
            log.error("leg1 has not bid or ofr.");
            return null;
        }
        if (leg2Bid == null && leg2Ofr == null) {
            log.error("leg2 has not bid or ofr.");
            return null;
        }
        if (leg1Bid == null)
            leg1Bid = leg1Ofr;
        if (leg1Ofr == null)
            leg1Ofr = leg1Bid;
        if (leg2Bid == null)
            leg2Bid = leg2Ofr;
        if (leg2Ofr == null)
            leg2Ofr = leg2Bid;

        return leg1Bid.add(leg1Ofr).divide(new BigDecimal("2"), 8, RoundingMode.HALF_UP)
                .subtract(leg2Bid.add(leg2Ofr).divide(new BigDecimal("2"), 8, RoundingMode.HALF_UP))
                .setScale(5, RoundingMode.HALF_UP);
    }

    /**
     * 计算最大可能成交的价差
     *
     * @param leg1YieldMap
     * @param leg2YieldMap
     * @param percentile
     * @return
     */
    private BigDecimal calcMostPossibilityDealYieldSpread(Map<String, Map<String, Object>> leg1YieldMap,
                                                          Map<String, Map<String, Object>> leg2YieldMap,
                                                          BigDecimal percentile) {
        // 如果percentile 为null 则返回null
        // 如果处于下分位数小于50 ，则取leg1.bid - leg2.ofr , 反之leg1.ofr - leg2.bid
        // 小于百分之50，以为着，leg1的收益率以后会上涨，那么我方应该卖掉对应的券，所以观察报价的bid 边
        // 反之亦然
        //如果对应的变没有价格，则记录错误日志，返回null
        log.info("leg1map {} leg2map {} percentile {} ", leg1YieldMap, leg2YieldMap, percentile);

        if (percentile == null) {
            log.info("percentile is null,can not  do calcMostPossiblilityDealYieldSpread.");
            return null;
        }
        BigDecimal leg1Bid = CalculateUtil.castBigDecimal(leg1YieldMap.entrySet().iterator().next().getValue().get("bid"));
        BigDecimal leg1Ofr = CalculateUtil.castBigDecimal(leg1YieldMap.entrySet().iterator().next().getValue().get("ofr"));
        BigDecimal leg2Bid = CalculateUtil.castBigDecimal(leg2YieldMap.entrySet().iterator().next().getValue().get("bid"));
        BigDecimal leg2Ofr = CalculateUtil.castBigDecimal(leg2YieldMap.entrySet().iterator().next().getValue().get("ofr"));
        if (percentile.compareTo(new BigDecimal("50")) < 0) {
            //如果有一遍为空或者为0，最优价不能计算
            if (leg1Bid == null || leg2Ofr == null
                    || leg1Bid.compareTo(BigDecimal.ZERO) == 0
                    || leg2Ofr.compareTo(BigDecimal.ZERO) == 0) {
                log.error("leg1 bid is nulll or leg2 leg2Ofr is null.");
                return null;
            }
            log.info("percentile < 50 leg1Bid {} ,leg2Ofr {}", leg1Bid, leg2Ofr);
            return leg1Bid.subtract(leg2Ofr);
        } else {
            //如果有一遍为空或者为0，最优价不能计算
            if (leg1Ofr == null || leg2Bid == null
                    || leg1Ofr.compareTo(BigDecimal.ZERO) == 0
                    || leg2Bid.compareTo(BigDecimal.ZERO) == 0
            ) {
                log.error("leg1 ofr is  null or leg2 ofr is null.");
                return null;
            }
            log.info("percentile > 50 leg1Ofr {} ,leg2Bid {}", leg1Ofr, leg2Bid);
            return leg1Ofr.subtract(leg2Bid);
        }
    }


    /**
     * 获取信号动作
     *
     * @param leg1
     * @param leg2
     * @param percentile
     * @return
     */
    private String getAction(String strategyInstanceName, Map<String, Map<String, Object>> leg1, Map<String, Map<String, Object>> leg2, BigDecimal percentile) {
        Set leg1Set = leg1.keySet();
        Set leg2Set = leg2.keySet();
        String leg1Name = (String) leg1Set.toArray()[0];
        String leg2Name = (String) leg2Set.toArray()[0];

        //第分位数区域
        String leg1Long = createAction(leg1Name, "1", (String) leg1.get(leg1Name).get("bidEventId"));
        String leg1Short = createAction(leg1Name, "0", (String) leg1.get(leg1Name).get("bidEventId"));
        String leg2Long = createAction(leg1Name, "1", (String) leg2.get(leg2Name).get("bidEventId"));
        String leg2Short = createAction(leg1Name, "0", (String) leg2.get(leg2Name).get("bidEventId"));
        //第分位数区域
        if (percentile.compareTo(new BigDecimal("50")) < 0) {
            String actionFormat = "做多利差,做空%s,做多%s";
            String action = String.format(actionFormat, leg1Name, leg2Name);
            return action;
        } else { //高分位数区域
            String actionFormat = "做空利差,做多%s,做空%s";
            String action = String.format(actionFormat, leg1Name, leg2Name);
            return action;
        }
    }

    private String createAction(String legName, String direction, String eventId) {
        if (direction.equals("1")) {
            if (StringUtils.contains(eventId, "XSWAP")) {
                return "卖出" + legName;
            } else {
                return "做多" + legName;

            }
        } else {
            if (StringUtils.contains(eventId, "XSWAP")) {
                return "买入" + legName;
            } else {
                return "做空" + legName;
            }
        }
    }


    /**
     * 输出打印策略实例内存变量，用于跟踪
     */
    @Override
    public void onDump() {
        log.info("dump strategy instance......");
        log.info("FactorList: {}", factorList);
        log.info("InstrumentIds: {}", instrumentIds);
        log.info("EventNames: {}", eventNames);
        log.info("InstanceId: {}", instanceId);
        log.info("Leg1AllSourceYieldMap {} Leg1Yield: {}", leg1AllSourceYieldMap, bestLeg1YieldMap);
        log.info("Leg2AllSourceYieldMap {} Leg2Yield: {}", leg2AllSourceYieldMap, bestLeg2YieldMap);
    }

    /**
     * 如果自己的debug模式打开，则跳到event按钮。这对应内存变量
     *
     * @param event
     */
    public void dumpOnAfterEvent(BondInputEvent event) {
        log.info("**********dump begin********************");
        log.info("dumpOnAfterEvent event: {}", event.getEventId());
        log.info("Event name: {}", event.getEventName());
        log.info("Event data: {}", event.getEventData());
        log.info("Event Time: {}", event.getCreateTimestamp());
        log.info("Instrument id: {}", event.getInstrumentId());
        log.info("Best bid yield: {}", event.calcBestBidYield());
        log.info("Best offer yield: {}", event.calcBestOfrYield());
        onDump();
        log.info("**********dump end********************");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StrategyInstanceTemplate that = (StrategyInstanceTemplate) o;
        return Objects.equals(instanceId, that.instanceId);
    }

    /**
     * 如果按中位值算出的百分位和按最优法算出的百分位同时在50%以上或者同时在50%一下，则返回true,否则false
     *
     * @param midPercentile
     * @param bestPercentile
     * @return
     */
    protected String getDirectFlagByPercentile(BigDecimal midPercentile, BigDecimal bestPercentile) {
        if (midPercentile == null || bestPercentile == null) {
            return "false";
        }
        if (midPercentile.compareTo(new BigDecimal("50")) > 0 &&
                bestPercentile.compareTo(new BigDecimal("50")) > 0) {
            return "true";
        }

        if (midPercentile.compareTo(new BigDecimal("50")) < 0 &&
                bestPercentile.compareTo(new BigDecimal("50")) < 0) {
            return "true";
        }
        return "false";
    }

    /**
     * 获得中债估值
     *
     * @param cmbcBondValueUrl
     * @param urlPara
     * @return
     */
    protected static Map<String, Object> getBondValueFromWeb(String cmbcBondValueUrl, String urlPara) {
        Map<String, Object> bondValueMap = new HashMap<>();
        try {
            String ret = HttpUtils.httpGet(cmbcBondValueUrl + urlPara);
            Map<String, Object> responseData = JsonUtil.toObject(ret, new TypeReference<Map<String, Object>>() {
            });

            if (!StringUtils.equals((String) responseData.get("status"), Constants.HTTP_SUCC)) {
                log.error("The web server return fail: {}", ret);
                return null;
            }

            Map<String, Object> bondValue = (Map<String, Object>) responseData.get("data");
            if (bondValue != null && !bondValue.isEmpty()) {
                bondValueMap.putAll(bondValue);
            }
        } catch (IOException e) {
            log.error("getBondValue exception", e);
        }
        return bondValueMap;
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId);
    }

    @Override
    public Set<String> getInstrumentIds() {
        return instrumentIds;
    }


    @Override
    public String getStrategyInstanceId() {
        return instanceId;
    }

    @Override
    public String getStrategyInstanceName() {
        return instanceName;
    }

}
