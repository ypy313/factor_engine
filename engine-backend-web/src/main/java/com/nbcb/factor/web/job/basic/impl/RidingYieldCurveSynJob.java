package com.nbcb.factor.web.job.basic.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.json.JSONUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nbcb.factor.common.*;
import com.nbcb.factor.entity.BondConfigEntity;
import com.nbcb.factor.entity.StrategyConfigVo;
import com.nbcb.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.nbcb.factor.entity.riding.*;
import com.nbcb.factor.enums.BusinessEnum;
import com.nbcb.factor.enums.SystemEnum;
import com.nbcb.factor.event.*;
import com.nbcb.factor.job.LoadConfigFactory;
import com.nbcb.factor.job.LoadConfigHandler;
import com.nbcb.factor.output.*;
import com.nbcb.factor.strategy.BondTypeEnum;
import com.nbcb.factor.strategy.CmdTypeEnum;
import com.nbcb.factor.strategy.CurveChangeEnum;
import com.nbcb.factor.strategy.StrategyCmdEnum;
import com.nbcb.factor.web.entity.rdi.RidingFunsRateMethodParam;
import com.nbcb.factor.web.entity.rdi.RidingOutEventParameter;
import com.nbcb.factor.web.enums.DefinitionNameEnum;
import com.nbcb.factor.web.job.basic.FactorBasicJob;
import com.nbcb.factor.web.kafka.service.KafkaMessageSendService;
import com.nbcb.factor.web.service.BondConfigService;
import com.nbcb.factor.web.service.HolidayService;
import com.nbcb.factor.web.service.RidingStrategyInstanceService;
import com.nbcb.factor.web.service.StrategyInstanceService;
import com.nbcb.factor.web.util.StringUtils;
import com.xxl.job.core.biz.model.ReturnT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static com.nbcb.factor.common.BondCalculatorUtils.*;
import static com.nbcb.factor.common.Constants.*;

/**
 * 骑乘计算job
 */
@Slf4j
@Component
public class RidingYieldCurveSynJob implements FactorBasicJob {
    private final RedisUtil redisUtil = RedisUtil.getInstance();
    private static final Map<String, LoadConfigHandler> ridingHandlerMap = new ConcurrentHashMap<>();
    public static final String STRATEGY_NAME = DefinitionNameEnum.RIDING_YIELD_CURVE.getName();//每个策略模型必须唯一声明
    public static final String STRATEGY_ID = DefinitionNameEnum.RIDING_YIELD_CURVE.getId();//每个策略模型必须唯一声明
    private static final Map<String, String> bondMapperMap = new ConcurrentHashMap<>();
    @Autowired
    private BondConfigService bondConfigService;
    @Autowired
    private RidingStrategyInstanceService ridingStrategyInstanceService;
    @Autowired
    private HolidayService holidayService;
    @Autowired
    private KafkaMessageSendService kafkaMessageSendService;
    @Autowired
    private StrategyInstanceService strategyInstanceService;

    @Override
    public Object executeJob(Object param) {
        //命令
        Monitorable dto = JSONUtil.toBean((String) param, Monitorable.class);
        StrategyCommandRidingInputEvent event = new StrategyCommandRidingInputEvent();
        StrategyCommand strategycommand = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()), StrategyCommand.class);// 排除非骑乘策略的命令
        //排除非骑乘策略的命令
        if (!(null != strategycommand && DefinitionNameEnum.RIDING_YIELD_CURVE.getName().equals(strategycommand.getStrategyName()))) {
            log.info("非骑乘策略不做处理");
            return ReturnT.SUCCESS;
        }
        event.setInstrumentId(strategycommand.getCmd());
        event.setEventData(strategycommand);
        // 加载因子实例
        if (ridingHandlerMap.isEmpty()) {
            //如果Map为空则创建工厂实例化对象
            LoadConfigFactory loadconfigFactory = new LoadConfigFactory();
            LoadConfigHandler loadconfigHandler = loadconfigFactory.getHandlerMap().get(STRATEGY_NAME);
            ridingHandlerMap.put(STRATEGY_NAME, loadconfigHandler);
        }
        List<?> ridingAssetPools = loadConfigHandler(event);
        List<RidingAssetPool> voList = JacksonUtils.convertList(ridingAssetPools, RidingAssetPool.class);
        //初始债券配置信息
        Map<String, String> configMap = bondMapperMap;
        log.info("--------------------init configMap:{}", configMap.keySet().size());
        if (CollectionUtils.isEmpty(configMap)) {
            //如果map是空重新加载
            loadBondConfig();
        }
        //计算末来曲线 和 实时曲线
        List<CbondCurveCnbd> curvecnbdList =
                getRidingYieldCurve();
        if (CollectionUtils.isEmpty(curvecnbdList)) {
            log.info("加载收益率曲线表为空:{}", STRATEGY_NAME);
            return ReturnT.FAIL;
        }
        try {
            log.info("计算开始:{}", STRATEGY_NAME);
            // 开始计算当前和未来收益率曲线
            calculateYieldCurve(voList, curvecnbdList);
            //计算table
            calculateRanking(voList, curvecnbdList);
            log.info("计算结束:{}", STRATEGY_NAME);
            //计算完成向kafka推送消息
            pushCmdToKafka(voList);
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            log.info("计算当前和未来收益率曲线或者排名异常", e);
            return ReturnT.FAIL;
        }
    }

    private List<?> loadConfigHandler(StrategyCommandRidingInputEvent event) {
        StrategyCommandRidingInputEvent inputEvent = (StrategyCommandRidingInputEvent) event;
        if (null == inputEvent) {
            return Collections.emptyList();
        }
        //是否重新加载以你实例标识
        String cmd = inputEvent.getEventData().getCmd();//命令onTime reload stop start 需要backend重新加载
        String cmdType = inputEvent.getEventData().getCmdType();//策略类型 ALL-全部 SPECIFY-指定
        boolean isReload = StrategyCmdEnum.STOP.getCmd().equals(cmd) || StrategyCmdEnum.START.getCmd().equals(cmd)
                || StrategyCmdEnum.RELOAD.getCmd().equals(cmd) || StrategyCmdEnum.SUSPEND.getCmd().equals(cmd);
        //初始实例
        String configListStr = redisUtil.getRiding(AllRedisConstants.FACTOR_CONFIG_RIDING_LIST);
        StrategyConfigVo strategyConfigVo = new StrategyConfigVo();
        //验证是全部还是指定
        if (CmdTypeEnum.ALL.getType().equals(cmdType)) {
            strategyConfigVo.setStrategyName(STRATEGY_NAME);
        } else if (CmdTypeEnum.SPECIFY.getType().equals(cmdType)) {
            List<SpecifyData> specifyDataList = inputEvent.getEventData().getData();
            String assetPoolIds = specifyDataList.stream().map(SpecifyData::getAssetPoolId)
                    .collect(Collectors.joining(","));
            strategyConfigVo.setStrategyName(STRATEGY_NAME);
            strategyConfigVo.setAssetPoolIds(assetPoolIds);
        }
        //初始化
        List<RidingAssetPool> configList = null;
        if (isReload || StringUtils.isEmpty(configListStr)) {
            try {
                List<LocalCurrencyStrategyInstanceResult> configJson = strategyInstanceService.getConfigJson(strategyConfigVo);
                JSONArray objects = JSONUtil.parseArray(configJson);

                configList = JSONUtil.toList(objects, RidingAssetPool.class);
                //存入redis
                if (!CollectionUtils.isEmpty(configList)) {
                    log.info("加载因子实例结果数据为：{}", configList.size());
                    String assetPoolIds = configList.stream().map(RidingAssetPool::getAssetPoolIdAndName).collect(Collectors.joining(","));
                    redisUtil.setRiding(AllRedisConstants.FACTOR_CONFIG_RIDING_LIST, JSONUtils.toJSONString(assetPoolIds));
                    configList.forEach(c -> redisUtil.setRiding(AllRedisConstants.FACTOR_CONFIG_RIDING_SINGLE
                            + c.getAssetPoolId(), JSONUtil.toJsonStr(c.getStrategyInstanceVoList())));
                }
            } catch (Exception e) {
                log.info("loadRidingStrategyConfig exception", e);

            }
        } else {
            //债券池id遍历redis取回配置
            List<String> assetPoolIds = JSONUtil.parseArray(configListStr).toList(String.class);
            configList = new ArrayList<>();
            for (String str : assetPoolIds) {
                String[] idAndName = str.split("__");
                RidingAssetPool ridingAssetPoolVo = new RidingAssetPool();
                ridingAssetPoolVo.setStrategyId(STRATEGY_ID);
                ridingAssetPoolVo.setStrategyName(STRATEGY_NAME);
                ridingAssetPoolVo.setAssetPoolId(idAndName[0]);
                ridingAssetPoolVo.setAssetPoolName(idAndName[1]);
                String strategyInstanceStr = redisUtil
                        .getRiding(AllRedisConstants.FACTOR_CONFIG_RIDING_SINGLE + idAndName[0]);

                try {
                    JSONArray objects = JSONUtil.parseArray(strategyInstanceStr);
                    ridingAssetPoolVo.setStrategyInstanceVoList(JSONUtil.toList(objects, RidingStrategyInstance.class));

                } catch (Exception e) {
                    log.info("loadRidingStrategyInstance to object error", e);
                }
                configList.add(ridingAssetPoolVo);
            }
        }
        return configList;
    }

    /**
     * 加载债券基础信息数据
     */
    public synchronized void loadBondConfig() {
        //加载配置
        try {
            List<BondConfigEntity> bondConfigEntityList = bondConfigService.getBondConfigEntityList();
            if (CollectionUtils.isEmpty(bondConfigEntityList)) {
                log.info("Initializing bondConfigEntity list is null");
                return;
            }
            bondConfigEntityList.forEach(bond -> {
                bondMapperMap.put(bond.getSecurityId(), bond.getSecurityDecMapper() + "_" + bond.getSecurityKey());
            });
        } catch (Exception e) {
            log.info("Initializing getBondConfigDataList failed", e);
        }
    }

    /**
     * 计算收益率曲线
     */
    private List<CbondCurveCnbd> getRidingYieldCurve() {
        List<CbondCurveCnbd> curveCnbdList = null;
        String curveCnbdStr = redisUtil.getRiding(AllRedisConstants.FACTOR_COMMON_CBOND_CURVE_CNBD);
        if (StringUtils.isNotEmpty(curveCnbdStr)) {
            try {
                curveCnbdList = JsonUtil.toList(curveCnbdStr, CbondCurveCnbd.class);
            } catch (Exception e) {
                log.info("getRidingYieldCurve to object error", e);
            }
        }
        //如果redis中没有去bakcend数据库中捞取
        if (CollectionUtils.isEmpty(curveCnbdList)) {
            try {
                String ridingBondCurveCNBD = ridingStrategyInstanceService.getRidingBondCurveCNBD();
                JSONArray objects = JSONUtil.parseArray(ridingBondCurveCNBD);
                curveCnbdList = JSONUtil.toList(objects, CbondCurveCnbd.class);
                //为空或者个数为0，重新加载
                if (CollectionUtils.isEmpty(curveCnbdList)) {
                    return Collections.emptyList();
                }
                //存入reids
                redisUtil.setRiding(AllRedisConstants.FACTOR_COMMON_CBOND_CURVE_CNBD, ridingBondCurveCNBD);
            } catch (Exception e) {
                log.info("getRidingYieldCurve exception", e);
            }
        }
        return curveCnbdList;
    }

    /**
     * 计算收益率曲线
     */
    private static void calculateYieldCurve(List<RidingAssetPool> voList, List<CbondCurveCnbd> curveCnbdList) {
        String maturityCnbd = LoadConfig.getProp().getProperty("maturity.cnbd");
        String step = LoadConfig.getProp().getProperty("maturity.cnbd.step");
        String start = LoadConfig.getProp().getProperty("maturity.cnbd.start");
        String end = LoadConfig.getProp().getProperty("maturity.cnbd.end");
        if (StringUtils.isEmpty(maturityCnbd) || StringUtils.isEmpty(step)
                || StringUtils.isEmpty(start) || StringUtils.isEmpty(end)) {
            return;
        }
        String[] maturityCnbdArray = maturityCnbd.split(",");
        //x坐标初始化
        BigDecimal[] xAxis = new BigDecimal[maturityCnbdArray.length];
        for (int i = 0; i < maturityCnbdArray.length; i++) {
            xAxis[i] = new BigDecimal(maturityCnbdArray[i]);
        }
        //初始化实时收益率曲线结果
        Map<String, RidingYieldCurveDetailResult> nowOutPutMap = new HashMap<>();
        //原始收益率曲线
        Map<String, BigDecimal[]> oriputPutMap = new HashMap<>();
        //根据债券类型进行分组
        Map<String, List<CbondCurveCnbd>> groupList = curveCnbdList.stream()
                .collect(Collectors.groupingBy(CbondCurveCnbd::getAnalCurvenumber));
        //arrange
        BigDecimal[] result = arange(new BigDecimal(start), new BigDecimal(end), new BigDecimal(step));
        //遍历生成实时的收益率曲线
        groupList.forEach((key, value) -> {
            //把value排序并转成数组
            List<Double> doubleList = value.stream().sorted(Comparator.comparing(CbondCurveCnbd::getAnalCurveterm))
                    .map(CbondCurveCnbd::getAnalYield).collect(Collectors.toList());
            BigDecimal[] bigDecimals = new BigDecimal[doubleList.size()];
            for (int j = 0; j < doubleList.size(); j++) {
                bigDecimals[j] = BigDecimal.valueOf(doubleList.get(j));
            }
            //设置值
            oriputPutMap.put(key, bigDecimals);
            //计算
            BigDecimal[] k = deriv(xAxis, bigDecimals);
            BigDecimal[] h = cubicHermiteInterp(xAxis, bigDecimals, k, result);
            RidingYieldCurveDetailResult detailResult = new RidingYieldCurveDetailResult();
            detailResult.setFlag(YIELD_NOW);
            detailResult.setXAxis(result);
            detailResult.setYAxis(h);
            nowOutPutMap.put(key, detailResult);
        });
        //计算未来曲线
        if (CollectionUtils.isEmpty(voList)) {
            //如果未来是空不进行计算
            return;
        }
        calculateFutureMethod(voList, maturityCnbdArray, xAxis, nowOutPutMap, oriputPutMap, result);
    }

    private static void calculateFutureMethod(List<RidingAssetPool> voList, String[] maturityCnbdArray,
                                              BigDecimal[] xAxis,
                                              Map<String, RidingYieldCurveDetailResult> nowOutPutMap,
                                              Map<String, BigDecimal[]> oriputPutMap, BigDecimal[] result) {
        for (RidingAssetPool ridingAssetPool : voList) {
            RidingYieldCurveOutputEvent event = new RidingYieldCurveOutputEvent(ridingAssetPool);
            //资产池下所有的收益率曲线属于公共的，取一条即可
            if (CollectionUtils.isEmpty(ridingAssetPool.getStrategyInstanceVoList())) {
                continue;
            }
            RidingStrategyInstance instance = ridingAssetPool.getStrategyInstanceVoList().get(0);
            if (null == instance) {
                break;
            }
            List<RidingInputConfig> inputConfigList = instance.getInputConfigList();
            if (CollectionUtils.isEmpty(inputConfigList)) {
                break;
            }
            //获取债券类型
            String bondCategory = getPropertyValue(inputConfigList, BOND_CATEGORY_KEY);
            //获取指定固定国债类型的值
            String type = Objects.requireNonNull(BondTypeEnum.getBondTypeEnumByKey(bondCategory)).getType();
            if (StringUtils.isEmpty(type)) {
                break;
            }
            //获取偏离类型
            String curveChang = getPropertyValue(inputConfigList, CURVE_CHANGE_KEY);
            RidingYieldCurveDetailResult detailResult = new RidingYieldCurveDetailResult();
            BigDecimal[] futureOutPut = new BigDecimal[maturityCnbdArray.length];
            if (CurveChangeEnum.NOCHANGE.getValue().equals(curveChang)) {
                //不变 值保持与收益率曲线一致
                futureOutPut = oriputPutMap.get(type);
            } else if (CurveChangeEnum.UPPER.getValue().equals(curveChang)
                    || CurveChangeEnum.DOWN.getValue().equals(curveChang)) {
                //向上 获取向上偏离值
                String curveDeviationValue = getPropertyValue(inputConfigList, CURVE_DEVIATION_VALUE_KEY);
                if (StringUtils.isEmpty(curveDeviationValue)) {
                    break;
                }
                BigDecimal bigDecimal = new BigDecimal(curveDeviationValue)
                        .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                futureOutPut = oriputPutMap.get(type);
                for (int i = 0; i < futureOutPut.length; i++) {
                    if (CurveChangeEnum.UPPER.getValue().equals(curveChang)) {
                        futureOutPut[i] = futureOutPut[i].add(bigDecimal);
                    } else {
                        futureOutPut[i] = futureOutPut[i].subtract(bigDecimal);
                    }
                }
            } else if (CurveChangeEnum.CUSTOM.getValue().equals(curveChang)) {
                //自定义
                JSONArray objects = JSONUtil.parseArray(getPropertyValue(inputConfigList, bondCategory));
                List<Double> doubleList = JSONUtil.toList(objects, Double.class);
                for (int j = 0; j < doubleList.size(); j++) {
                    futureOutPut[j] = BigDecimal.valueOf(doubleList.get(j));
                }
            } else {
                log.info("curveChange:{} error!", curveChang);
                break;
            }
            BigDecimal[] k = deriv(xAxis, futureOutPut);
            BigDecimal[] h = cubicHermiteInterp(xAxis, futureOutPut, k, result);
            detailResult.setFlag(YIELD_FUTURE);
            detailResult.setXAxis(result);
            detailResult.setYAxis(h);
            event.getEventData().add(detailResult);
            event.getEventData().add(nowOutPutMap.get(type));
            try {
                RedisUtil redisUtil = RedisUtil.getInstance();
                //保存到redis
                redisUtil.setRiding(AllRedisConstants.FACTOR_RIDING_FUR_RIDING_YIELD_CURVE
                        + ridingAssetPool.getAssetPoolId(), JsonUtil.toJson(event));
            } catch (JsonProcessingException e) {
                log.info("store into redis error", e);
            }
        }
    }


    /**
     * 计算排名
     */
    private void calculateRanking(List<RidingAssetPool> voList, List<CbondCurveCnbd> curveCnbdList) throws JsonProcessingException {
        if (CollectionUtils.isEmpty(voList)) {
            return;
        }
        String showHoldPeriod = LoadConfig.getProp().getProperty("riding.show.hold.period");
        String algorithm = LoadConfig.getProp().getProperty("riding.algorithm");
        if (StringUtils.isEmpty(showHoldPeriod) || StringUtils.isEmpty(algorithm)) {
            return;
        }
        //计算结算日
        String settlementDate = getSettlementDate(getCurrentDate(), SETTLEMENT_DATE_TYPE_T_1);
        //持有期限集合
        String[] showHoldPeriodArray = showHoldPeriod.split(",");
        //算法集合
        String[] algorithmArray = algorithm.split(",");
        //遍历持有期
        for (String shp : showHoldPeriodArray) {
            //遍历算法
            for (String algorithmStr : algorithmArray) {
                //遍历
                for (RidingAssetPool ridingAssetPool : voList) {
                    //资产池下所有实例
                    List<RidingStrategyInstance> strategyInstanceVoList = ridingAssetPool.getStrategyInstanceVoList();
                    if (CollectionUtils.isEmpty(strategyInstanceVoList)) {
                        log.info("当前资产池：{}下无实例，不进行计算！", ridingAssetPool.getAssetPoolId());
                        continue;
                    }
                    //到期日每个资产池都一样，取第一条即可
                    String dueDate = getPropertyValue(strategyInstanceVoList.get(0).getInputConfigList(), DUE_DATE);
                    //如果是期限自定义，且自定义值为空不进行计算
                    if (StringUtils.isEmpty(dueDate) && shp.equals(RIDING_SHOW_HOLD_PERIOD_CUSTOM)) {
                        log.info("当前资产池：{}，自定义值不存在！，不进行计算！", ridingAssetPool.getAssetPoolId());
                        continue;
                    }
                    //获取未来结算日
                    String futureSettlementDate = getSettlementDate(getFutureSettlementDate(shp, dueDate),
                            SETTLEMENT_DATE_TYPE_T_1);
                    //初始化输出事件
                    RidingBondRankingOutputEvent event = new RidingBondRankingOutputEvent(ridingAssetPool);
                    event.setShowHoldPeriod(shp);//持有期限
                    event.setShowHoldPeriod(algorithmStr);//估算类型
                    //初始化输出事件详情
                    List<RidingBondRankingDetailResult> eventData = new ArrayList<>();
                    RidingOutEventParameter riding = new RidingOutEventParameter();
                    riding.setVoList(voList);
                    riding.setCurveCnbdList(curveCnbdList);
                    riding.setSettlementDate(settlementDate);
                    riding.setShp(shp);
                    riding.setAlgorithmStr(algorithmStr);
                    riding.setRidingAssetPool(ridingAssetPool);
                    riding.setStrategyInstanceVoList(strategyInstanceVoList);
                    riding.setFutureSettlementDate(futureSettlementDate);
                    riding.setEvent(event);
                    riding.setEventDate(eventData);
                    createRidingOutEvent(riding);
                    //排序eventData
                    eventData = sortEventData(eventData);
                    event.setEventData(eventData);
                    try {
                        String resultStr = JsonUtil.toJson(event);
                        String redisKey = AllRedisConstants.FACTOR_RIDING_RIDING_CAL_DATA_TABLE + ridingAssetPool.getAssetPoolId() +
                                "_" + shp + "_" + algorithmStr;
                        log.info("当前资产池：{}，持有期限：{}，算法：{}，计算结果redis key 为：{}",
                                ridingAssetPool.getAssetPoolId(), shp, algorithmStr, redisKey);
                        RedisUtil redisUtil1 = RedisUtil.getInstance();
                        //保存到redis
                        redisUtil.setRiding(redisKey, resultStr);

                    } catch (JsonProcessingException e) {
                        log.info("store into redis error", e);
                    }
                }
            }
        }
    }

    private void createRidingOutEvent(RidingOutEventParameter parameter) throws JsonProcessingException {
        RidingBondRankingOutputEvent event = parameter.getEvent();
        RidingAssetPool ridingAssetPool = parameter.getRidingAssetPool();
        List<RidingBondRankingDetailResult> eventDate = parameter.getEventDate();
        String settlementDate = parameter.getSettlementDate();
        String shp = parameter.getShp();
        String algorithmStr = parameter.getAlgorithmStr();
        List<RidingAssetPool> voList = parameter.getVoList();
        List<CbondCurveCnbd> curveCnbdList = parameter.getCurveCnbdList();
        String futureSettlementDate = parameter.getFutureSettlementDate();
        for (RidingStrategyInstance strategyInstance : parameter.getStrategyInstanceVoList()) {
            RidingBondRankingDetailResult result = new RidingBondRankingDetailResult();
            String fundsRateKey = getPropertyValue(strategyInstance.getInputConfigList(), FUNDS_RATE_KEY);
            event.setShowFunsRate(fundsRateKey);//资金利率key
            String curveChang = getPropertyValue(strategyInstance.getInputConfigList(), CURVE_CHANGE_KEY);
            event.setCurveChange(curveChang);//未来曲线
            String futureDeviation = getPropertyValue(strategyInstance.getInputConfigList(), FUTURE_DEVIATION_TYPE);
            event.setFutureDeviation(futureDeviation);//未来曲线变化
            String interestRateValue = getPropertyValue(strategyInstance.getInputConfigList(), INTEREST_RATE_VALUE);
            event.setInterRate(interestRateValue);//利率值
            result.setInstanceId(strategyInstance.getInstanceId());
            result.setSecurityId(strategyInstance.getInstanceId());//实例ID
            result.setSecurityId(strategyInstance.getDisplayName().split(".IB")[0]);//实例名称
            //获取当前现金流
            List<BondCF> bondCFList = getBondCFByBonds(strategyInstance.getDisplayName());
            if (CollectionUtils.isEmpty(bondCFList)) {
                log.info("当前资产池：{}，实例：{} 没有现金流信息！，不进行计算！",
                        ridingAssetPool.getAssetPoolId(), strategyInstance.getInstanceName());
                eventDate.add(result);
                continue;
            }
            //获取债券类型
            String bondCategory = getPropertyValue(strategyInstance.getInputConfigList(), BOND_CATEGORY_KEY);
            //获取固定国债类型的值
            String type = Objects.requireNonNull(BondTypeEnum.getBondTypeEnumByKey(bondCategory)).getType();
            if (StringUtils.isEmpty(type)) {
                log.info("当前资产池：{}，实例：{} 国债类型不存在！，不进行计算！",
                        ridingAssetPool.getAssetPoolId(), strategyInstance.getInstanceName());
                eventDate.add(result);
                continue;
            }
            //获取当前的中债估值
            LoadConfigHandler loadConfigHandler = ridingHandlerMap.get("STRATEGY_NAME");
            Map<String, String> configMap = loadConfigHandler.getBondConfig();
            log.info("________________configMap :{}", configMap.keySet().size());
            if (CollectionUtils.isEmpty(configMap)) {
                //如果map是空重新加载
                LoadConfigFactory loadConfigFactory = new LoadConfigFactory();
                LoadConfigHandler<?> loadConfigFactoryInfo = loadConfigFactory.getHandlerMap().get(STRATEGY_NAME);
                ridingHandlerMap.put(STRATEGY_NAME, loadConfigFactoryInfo);
                loadConfigFactory.getHandlerMap().get("RidingYieldCurve").loadBondConfig();
            }
            String mapperName = configMap.get(strategyInstance.getDisplayName());
            String commonKey = SystemEnum.FACTOR.getKey() + "_" + BusinessEnum.COMMON.getKey() + "_" + mapperName;
            //静态变量数据 key前缀改为
            BondConfigEntity bondConfigEntity = null;
            String bondConfigEntityStr = redisUtil.getString(commonKey, strategyInstance.getDisplayName());
            if (StringUtils.isNotEmpty(bondConfigEntityStr)) {
                bondConfigEntity = JsonUtil.toObject(bondConfigEntityStr, BondConfigEntity.class);
            }
            //中债估值
            BigDecimal currentValuationYield = BigDecimal.ZERO;
            if (null != bondConfigEntity && StringUtils.isNotEmpty(bondConfigEntity.getValuation())) {
                currentValuationYield = new BigDecimal(bondConfigEntity.getValuation());
            }
            String bestYieldStr = redisUtil.getString(AllRedisConstants.FACTOR_MARKET_PROCESSING_BEST_YIELD, strategyInstance.getDisplayName());
            if (StringUtils.isNotEmpty(bestYieldStr)) {
                MarketProcessingOutPut output = JSONUtil.toBean(bestYieldStr, MarketProcessingOutPut.class);
                try {
                    BigDecimal bigDecimal = output.getCfetsBestYield().getBestMidYield();
                    log.info("当前资产池：{}，实例：{} 最优价为：{}！",
                            ridingAssetPool.getAssetPoolId(), strategyInstance.getInstanceName(), bigDecimal);
                    if (null != bigDecimal && bigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                        currentValuationYield = bigDecimal;
                    }
                } catch (Exception ex) {
                    log.info("当前资产池：{} ，实例：{},最优价获取失败：{}！",
                            ridingAssetPool.getAssetPoolId(), strategyInstance.getInstanceName(), ex.getMessage());

                }
            }
            if (currentValuationYield.compareTo(BigDecimal.ZERO) == 0) {
                log.info("当前资产池：{}，实例：{} 无中债估值！",
                        ridingAssetPool.getAssetPoolId(), strategyInstance.getInstanceName());
                eventDate.add(result);
                continue;
            }
            //设置当前估值
            result.setCurrentValuationYield(currentValuationYield.setScale(4, RoundingMode.HALF_UP) + "%");
            //获取当前期限的天数
            String customValue = getPropertyValue(strategyInstance.getInputConfigList(), DUE_DATE);
            int days = getDaysToShowHoldPeriod(shp, customValue, settlementDate);
            int nowDays = getDaysToShowHoldPeriod(shp, customValue, getCurrentDate());
            if (days == 0 || nowDays == 0) {
                log.info("当前资产池：{} 实例：{} 计算期限天数为0！ 不进行计算！",
                        ridingAssetPool.getAssetPoolId(), strategyInstance.getInstanceName());
                eventDate.add(result);
                continue;
            }
            RidingFunsRateMethodParam param = new RidingFunsRateMethodParam();
            RidingFunsRateMethodParam convertOne = param.convertOne(param, voList, curveCnbdList, settlementDate, algorithmStr);
            RidingFunsRateMethodParam convertTwo = convertOne.convertTwo(convertOne, ridingAssetPool, futureSettlementDate, eventDate, strategyInstance);
            RidingFunsRateMethodParam convertThree = convertTwo.convertThree(convertTwo, result, fundsRateKey, interestRateValue, bondCFList);
            RidingFunsRateMethodParam paramResult = convertThree.convertFour(convertThree, type, currentValuationYield, days, nowDays);
            //获取利率值
            getFundsRateMethod(paramResult);
        }
    }


    private void getFundsRateMethod(RidingFunsRateMethodParam param) {
        List<RidingBondRankingDetailResult> eventData = param.getEventData();
        RidingBondRankingDetailResult result = param.getResult();
        List<BondCF> bondCFList = param.getBondcFList();
        String settlementDate = param.getSettlementDate();
        RidingStrategyInstance strategyInstance = param.getStrategyInstance();
        RidingAssetPool ridingAssetPool = param.getRidingAssetPool();
        String futureSettlementDate = param.getFutureSettlementDate();
        List<CbondCurveCnbd> curvecnbdList = param.getCurvecnbdList();
        List<RidingAssetPool> voList = param.getVoList();
        BigDecimal currentValuationYield = param.getCurrentValuationYield();
        BigDecimal fundsRate;
        try {
            fundsRate = getFundsRate(param.getFundsRateKey(), param.getInterestRateValue(),
                    getPropertyValue(strategyInstance.getInputConfigList(),
                            INTEREST_RATE_VALUE_BY_CUSTOM_VALUE),
                    param.getNowDays());
        } catch (Exception ignored) {
            log.info("获取利率值异常", ignored);
            eventData.add(param.getResult());
            return;
        }
        fundsRate = fundsRate.divide(new BigDecimal(100), 6, RoundingMode.HALF_UP);

        // 获取当前未来债券计算信息
        BondCalculatorOutput nowOutput = calculator(bondCFList, settlementDate);
        BondCalculatorOutput futureOutput = calculator(bondCFList, futureSettlementDate);

        if (nowOutput.getN() == 0 || futureOutput.getN() == 0) {
            log.info("当前资产池:{}，实例:{}已超过兑付目，不进行计算!",
                    ridingAssetPool.getAssetPoolId(), strategyInstance.getInstanceName());
            eventData.add(result);
            return;
        }
// 剩余期限
        result.setRemainingMaturity(String.valueOf(nowOutput.getRemainYear()
                .setScale(4, RoundingMode.HALF_UP)));
        // 未来剩余期限
        result.setFutureRemainingMaturity(String.valueOf(futureOutput.getRemainYear()
                .setScale(4, RoundingMode.HALF_UP)));
        //剩余年限不在区间0.08- 15不进行计算
        if (nowOutput.getRemainYear().compareTo(BigDecimal.valueOf(15)) > 0
                || nowOutput.getRemainYear().compareTo(BigDecimal.valueOf(0.08)) < 0
                || futureOutput.getRemainYear().compareTo(BigDecimal.valueOf(15)) > 0
                || futureOutput.getRemainYear().compareTo(BigDecimal.valueOf(0.08)) < 0) {
            log.info("当前资产池:{}，实例:{}剩余年限不属于0.08~15区间!，不进行计算!",
                    ridingAssetPool.getAssetPoolId(), strategyInstance.getInstanceName());
            eventData.add(result);
            return;
        }
        // 获职当前曲线取点
        Map<String, BigDecimal> deviate = getDeviate(curvecnbdList, voList,
                nowOutput.getRemainYear(), futureOutput.getRemainYear(), param.getType());
        // 曲线估值
        BigDecimal nowDeviate = deviate.get("nowDeviate");
        BigDecimal futureDeviate = deviate.get("futureDeviate");
        if (null == nowDeviate || null == futureDeviate) {
            log.info("当前资产池:{} 实例:{}曲线取值为零!,不进行计算!", ridingAssetPool.getAssetPoolId()
                    , strategyInstance.getInstanceName());
            eventData.add(result);
            return;
        }
        result.setCurrentValuationPrice(nowDeviate.setScale(4, RoundingMode.HALF_UP) + "%");//当前曲线佶值
        result.setFutureCurveValuation(futureDeviate.setScale(4, RoundingMode.HALF_UP) + "%");// 未来曲线估值
        // 当前收益率转价格
        BigDecimal nowYtm = currentValuationYield.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal nowFullPrice = ytmToFullPrice(nowYtm, nowOutput.getF(), nowOutput.getN(), nowOutput.getW(),
                nowOutput.getD(), nowOutput.getTy().intValue(), bondCFList, settlementDate)
                .setScale(6, RoundingMode.HALF_UP);
        //、当前估值价格
        result.setCurrentCurveValuation(String.valueOf(nowFullPrice.setScale(4, RoundingMode.HALF_UP)));
        // 计算当前估值偏移
        BigDecimal multiply = (currentValuationYield.subtract(nowDeviate)).multiply(BigDecimal.valueOf(100));
        result.setCurrentDeviation(String.valueOf(multiply.setScale(4, RoundingMode.HALF_UP)));
        // 获取未来偏离
        BigDecimal deviationValue;
        String futureDeviationType = getPropertyValue(strategyInstance.getInputConfigList(), FUTURE_DEVIATION);
        if (FUTURE_DEVIATION_CURRENT_DEVIATION.equals(futureDeviationType)) {
            // 当前偏离
            deviationValue = multiply;
        } else if (FUTURE_DEVIATION_CURRENT_ALL.equals(futureDeviationType)) {
            // 全部为零
            deviationValue = BigDecimal.ZERO;
        } else {
            //全部为零和自定义
            deviationValue = new BigDecimal(getPropertyValue(strategyInstance.getInputConfigList(), DEVIATION_VALUE));
        }
        // 未来收益率估值
        BigDecimal futureYield = (futureDeviate.add(deviationValue.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)));
        // 设置未来估值
        result.setFutureValuationYield(futureYield.setScale(4, RoundingMode.HALF_UP) + "%");
        //未来偏离
        result.setFutureDeviation(String.valueOf(deviationValue.setScale(4, RoundingMode.HALF_UP).toString()));
        BigDecimal futureFullPrice = ytmToFullPrice(futureYield.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP), futureOutput.getF(), futureOutput.getN(),
                futureOutput.getW(), futureOutput.getD(), futureOutput.getTy().intValue(),
                bondCFList, futureSettlementDate).setScale(6, RoundingMode.HALF_UP);
        result.setFutureValuationPrice(futureFullPrice.setScale(4,RoundingMode.HALF_UP).toString());

        //计算久期均值 fautureYtm
        BigDecimal delYtm = BigDecimal.valueOf(0.0001D);
        BigDecimal nowDuration = durationCalculate(nowYtm, bondCFList, nowOutput.getF(), nowOutput.getN(), nowOutput.getW(), nowFullPrice,
                delYtm, nowOutput.getD(), nowOutput.getTy().intValue(), settlementDate);
        BigDecimal futureDuration = durationCalculate(futureYield.divide(BigDecimal.valueOf(100),6,RoundingMode.HALF_UP),
                bondCFList,futureOutput.getF(),futureOutput.getN(),futureOutput.getW(),futureFullPrice
                ,delYtm,futureOutput.getD(),futureOutput.getTy().intValue(),futureSettlementDate);
        BigDecimal meanDuration = (nowDuration.add(futureDuration)).divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
        //获取carry rolldow 综合损益
        BigDecimal infoCouponRate = BigDecimal.valueOf(bondCFList.get(0).getInfoCouponRate())
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        Map<String, BigDecimal> comprehensiveMap = getComprehensive(bondCFList, futureFullPrice, nowFullPrice
                , param.getAlgorithmStr(), infoCouponRate, nowYtm
                , fundsRate, futureYield.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP), meanDuration
                , param.getDays(), nowOutput.getF(), nowOutput.getN(), nowOutput.getW(), nowOutput.getD()
                , nowOutput.getTy().intValue(), settlementDate);

        BigDecimal carry = comprehensiveMap.get("carry");//carry损益
        BigDecimal rollDown = comprehensiveMap.get("rollDown");//rollDown损益
        BigDecimal comprehensive = comprehensiveMap.get("comprehensive");//综合损益
        BigDecimal breakEven = comprehensiveMap.get("breakEven");//盈亏平衡点
        result.setCarryProfitAndLoss(String.valueOf(carry));//carry损益
        result.setCarryProfitAndLossNum(carry);//carry损益
        result.setCarryProfitAndLossPrice(String.valueOf(carry.multiply(nowFullPrice).setScale(4,RoundingMode.HALF_UP)));
        result.setRollDownProfitAndLoss(String.valueOf(rollDown));//roll down损益
        result.setRollDownProfitAndLossPrice(String.valueOf(rollDown.multiply(nowFullPrice).setScale(4,RoundingMode.HALF_UP)));
        result.setProfitAndLossYield(String.valueOf(comprehensive));//综合损益
        result.setProfitAndLossYieldNum(comprehensive);//综合损益
        result.setProfitAndLossPrice(String.valueOf(comprehensive.multiply(nowFullPrice).setScale(4,RoundingMode.HALF_UP)));//综合损益价格
        result.setSurplusBalancePoint(breakEven.multiply(BigDecimal.valueOf(100)).setScale(4,RoundingMode.HALF_UP)+"%");//盈亏平衡点
        eventData.add(result);
    }

    /**
     * 获取结算日
     */
    private String getSettlementDate(String date, String type) {
        if (Constants.SETTLEMENT_DATE_TYPE_T_1.equals(type)) {
            //获取T1日期，首先判断日期rdi假日数据中是否存在，如果不存在再验证是否是星期日
            String t1Date = DateUtil.getAfterDays(date, 1);
            //redis 获取rdi节假日列表
            String holidayStr = redisUtil.getRiding(AllRedisConstants.FACTOR_COMMON_HOLIDAY);
            List<String> holidayList = new ArrayList<>();
            if (StringUtils.isNotEmpty(holidayStr)) {
                try {
                    holidayList = JsonUtil.toList(holidayStr, String.class);
                } catch (Exception e) {
                    log.info("getSettlementDate to list error", e);
                }
            }
            if (CollectionUtils.isEmpty(holidayList)) {
                try {
                    String rdiHoliday = holidayService.selectRdiHoliday();
                    JSONArray objects = JSONUtil.parseArray(rdiHoliday);
                    holidayList = JSONUtil.toList(objects, String.class);
                    if (!CollectionUtils.isEmpty(holidayList)) {
                        //缓存的reids
                        redisUtil.setRiding(AllRedisConstants.FACTOR_COMMON_HOLIDAY, rdiHoliday);
                    }
                } catch (Exception e) {
                    log.info("loading settlementDate exception", e);
                }
            }
            //判断日期t1日期是否在集合中存在
            while (listContains(holidayList, t1Date)) {
                t1Date = DateUtil.getAfterDays(t1Date, 1);
            }
            //判断那日期是不是星期日
            if (DateUtil.isSunDay(t1Date)) {
                return DateUtil.getAfterDays(t1Date, 1);
            } else {
                return t1Date;
            }
        }
        return date;
    }



    private boolean listContains(List<String> strList, String value) {
        return strList.contains(value);
    }

    /**
     * get未来结算日
     */
    private String getFutureSettlementDate(String showHoldPeiod, String customValue) {
        //过去 1M 3M 9M 1Y 自定义日期
        String tradeDt = "";
        //过去均值
        if (Constants.RIDING_SHOW_HOLD_PERIOD_1M.equals(showHoldPeiod)) {
            tradeDt = DateUtil.parseDateToStr(DateUtil.DAYSTR, DateUtil.getAfterMoth(1));
        } else if (Constants.RIDING_SHOW_HOLD_PERIOD_3M.equals(showHoldPeiod)) {
            tradeDt = DateUtil.parseDateToStr(DateUtil.DAYSTR, DateUtil.getAfterMoth(3));
        } else if (Constants.RIDING_SHOW_HOLD_PERIOD_6M.equals(showHoldPeiod)) {
            tradeDt = DateUtil.parseDateToStr(DateUtil.DAYSTR, DateUtil.getAfterMoth(6));
        } else if (Constants.RIDING_SHOW_HOLD_PERIOD_9M.equals(showHoldPeiod)) {
            tradeDt = DateUtil.parseDateToStr(DateUtil.DAYSTR, DateUtil.getAfterMoth(9));
        } else if (Constants.RIDING_SHOW_HOLD_PERIOD_1Y.equals(showHoldPeiod)) {
            tradeDt = DateUtil.parseDateToStr(DateUtil.DAYSTR, DateUtil.getAfterMoth(12));
        } else if (Constants.RIDING_SHOW_HOLD_PERIOD_CUSTOM.equals(showHoldPeiod)) {
            tradeDt = customValue.replaceAll("-", "");
        }
        log.info("持有期限：{},自定义值：{}，计算未来结算日为：{}", showHoldPeiod, customValue, tradeDt);
        return tradeDt;
    }

    /**
     * 加载债券现金流
     */
    private List<BondCF> getBondCFByBonds(String instanceName) {
        if (StringUtils.isEmpty(instanceName)) {
            log.info("riding instanceName is null");
            return Collections.emptyList();
        }
        List<BondCF> bondCFList = null;
        String curveCnbdStr = redisUtil.getRiding(AllRedisConstants.FACTOR_COMMON_CBONDCF);
        if (StringUtils.isNotEmpty(curveCnbdStr)) {
            try {
                bondCFList = JsonUtil.toList(curveCnbdStr, BondCF.class);
            } catch (Exception e) {
                log.info("getBondCFByBonds to object error", e);
            }
        }
        //如果redis中没有查询数据库
        if (CollectionUtils.isEmpty(bondCFList)) {
            try {
                String bondCFByBonds = ridingStrategyInstanceService.getBondCFByBonds(instanceName);
                log.info("The web service return data :{}", bondCFByBonds);
                JSONArray objects = JSONUtil.parseArray(bondCFByBonds);
                bondCFList = JSONUtil.toList(objects, BondCF.class);
                //为空或者个数为0，重新加载
                if (CollectionUtils.isEmpty(bondCFList)) {
                    log.info("the web service return data is null!");
                    return Collections.emptyList();
                }
                //存入redis
                redisUtil.setRiding(AllRedisConstants.FACTOR_COMMON_CBONDCF, bondCFByBonds);
            } catch (Exception e) {
                log.info("loadRidingYieldCurve exception", e);
            }
        }
        return bondCFList;
    }




    /**
     * 获取资金利率
     */
    private BigDecimal getFundsRate(String interRateType, String interestRateValue,
                                    String interestRateValueByCustomValue, int days) {
        List<Rate> rateList = null;
        String str;
        //判断是shibor7d 还是 repo7d
        if (Constants.FUNDS_RATE_KEY_REPO7D.equals(interRateType)) {
            str = redisUtil.getRiding(AllRedisConstants.FACTOR_COMMON_REPO_7D);
        } else if (Constants.FUNDS_RATE_KEY_SHIBOR7D.equals(interRateType)) {
            str = redisUtil.getRiding(AllRedisConstants.FACTOR_COMMON_SHIBOR_7D);
        } else {
            log.info("不支持当前资金类型：{}", interRateType);
            return BigDecimal.ZERO;
        }
        if (StringUtils.isNotEmpty(str)) {
            //如果不为空就解析str
            rateList = JsonUtil.toList(str, Rate.class);
        } else {
            log.info("资金利率值不存在不进行解析：{}", interRateType);
            return BigDecimal.ZERO;
        }

        if (CollectionUtils.isEmpty(rateList)) {
            log.info("获取资金利率列表为空！");
            return BigDecimal.ZERO;
        }
        //根据资金利率集合根据事件进行倒序
        rateList = rateList.stream().sorted(Comparator.comparing(Rate::getTradeDt).reversed()).collect(Collectors.toList());
        BigDecimal rateResult;
        //判断资金类型
        if (Constants.RIDING_INTEREST_RATE_TYPE_LATEST_VALUE.equals(interestRateValue)) {
            //最新值取第一条的利率
            rateResult = BigDecimal.valueOf(rateList.get(0).getRate());
        } else if (Constants.RIDING_INTEREST_RATE_TYPE_CUSTOM.equals(interestRateValue)) {
            //自定义的值
            rateResult = new BigDecimal(interestRateValueByCustomValue);
        } else if (Constants.RIDING_INTEREST_RATE_TYPE_PAST_NEW_VALUE.equals(interestRateValue)) {
            //过去1M 3M 6M 9M 1Y 自定义的日期
            String tradeDt = DateUtil.parseDateToStr(DateUtil.DAYSTR, DateUtil.calcDaysBefore(days));
            List<Rate> rates = new ArrayList<>();
            //计算当前日期的过去均值
            for (Rate rate : rateList) {
                if (isEffectiveDate(rate.getTradeDt(), tradeDt, getCurrentDate())) {
                    rates.add(rate);
                }
            }
            if (CollectionUtils.isEmpty(rates)) {
                return BigDecimal.valueOf(rateList.get(0).getRate());
            }
            rateResult = BigDecimal.valueOf(rates.stream().mapToDouble(Rate::getRate).average().getAsDouble());
        } else {
            log.info("利率值：{}错误！", interestRateValue);
            return BigDecimal.ZERO;
        }
        log.info("资金利率：{}，利率值：{}，资金利率为：{}", interRateType, interestRateValue, rateResult);
        return rateResult;
    }



    /**
     * 计算相关参数
     */
    private BondCalculatorOutput calculator(List<BondCF> bondCFList, String settlementDate) {
        BondCalculatorOutput output = new BondCalculatorOutput();
        //计算年付息频率 f
        int f = interestPaymentFrequency(bondCFList.get(0).getInfoCarryDate(),
                bondCFList.get(0).getInfoPaymentDate());
        output.setF(f);
        //付息次，单利还是复利
        int n = interestPaymentsCount(bondCFList, settlementDate);
        output.setN(n);
        if (n == 0) {
            return output;
        }
        //剩余年限与TY当前计息年度的实际天数，算头不算尾
        Map<String, BigDecimal> tyAndRemainYearMap = getTyAndRemainYear(bondCFList, settlementDate);
        BigDecimal remainYear = tyAndRemainYearMap.get("remainYear");
        BigDecimal ty = tyAndRemainYearMap.get("TY");
        output.setRemainYear(remainYear);//剩余年限
        output.setTy(ty);//当前计息年度的实际天数，算头不算尾
        //d t ts ai
        Map<String, BigDecimal> dAndTMap = getDAndT(settlementDate, bondCFList);
        BigDecimal d = dAndTMap.get("d");//券结算日至下一最近付息日之间的实际天数
        BigDecimal t = dAndTMap.get("t");//券结算日至上一最近付息日之间的实际天数
        BigDecimal ts = dAndTMap.get("TS");//当前付息周期的实际天数
        BigDecimal ai = accruedInterest(getNowRate(bondCFList, settlementDate), t, ts);
        output.setD(d);
        output.setT(t);
        output.setTs(ts);
        output.setAi(ai);//应付利息
        //w
        BigDecimal w = d.divide(ts, 6, RoundingMode.HALF_UP);
        output.setW(w);
        output.setSettlementDate(settlementDate);//结算日
        return output;

    }



    /**
     * 排序
     */
    private List<RidingBondRankingDetailResult> sortEventData(List<RidingBondRankingDetailResult> eventData) {
        if (CollectionUtils.isEmpty(eventData)) {
            return eventData;
        }
        //根据Carry损益排序排序并设置排序编号
        LongAdder carryLongAdder = new LongAdder();
        eventData = eventData.stream().sorted(Comparator.comparing(RidingBondRankingDetailResult::getCarryProfitAndLossNum
                , Comparator.nullsFirst(Comparator.naturalOrder())).reversed()).peek(data -> {
            carryLongAdder.increment();
            data.setCarryProfitAndLossSort(carryLongAdder.intValue());
            if (StringUtils.isNotEmpty(data.getCarryProfitAndLoss())) {
                data.setCarryProfitAndLoss(new BigDecimal(data.getCarryProfitAndLoss())
                        .multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP) + "%");
            }
        }).collect(Collectors.toList());
        //根据roll down 损益排序并设置排序偏好
        LongAdder rollDownLongAdder = new LongAdder();
        eventData = eventData.stream().sorted(Comparator.comparing(
                RidingBondRankingDetailResult::getRollDownProfitAndLossNum, Comparator.nullsFirst(Comparator.naturalOrder())
        ).reversed()).peek(data -> {
            rollDownLongAdder.increment();
            data.setRollDownProfitAndLossSort(rollDownLongAdder.intValue());
            if (StringUtils.isNotEmpty(data.getRollDownProfitAndLoss())) {
                data.setRollDownProfitAndLoss(new BigDecimal(data.getRollDownProfitAndLoss())
                        .multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP) + "%");

            }
        }).collect(Collectors.toList());
        //根据综合损益
        LongAdder longAdder = new LongAdder();
        eventData = eventData.stream().sorted(Comparator.comparing(
                RidingBondRankingDetailResult::getProfitAndLossYieldNum, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()
        ).peek(data -> {
            longAdder.increment();
            data.setProfitAndLossSort(longAdder.intValue());
            if (StringUtils.isNotEmpty(data.getProfitAndLossYield())) {
                data.setProfitAndLossYield(new BigDecimal(data.getProfitAndLossYield()).multiply(BigDecimal.valueOf(100))
                        .setScale(4, RoundingMode.HALF_UP) + "%");
            }
        }).collect(Collectors.toList());
        return eventData;

    }

    /**
     * 推送完成命令到kafka
     */
    private void pushCmdToKafka(List<RidingAssetPool> voList) {
        //初始化命令对象
        StrategyNoticeOutput output = new StrategyNoticeOutput(voList);
        try {
            log.info("定时骑乘数据处理完成消息推送开始！");
            this.kafkaMessageSendService.sendMessage(StrategyNoticeOutput.TOPIC,
                    JsonUtil.toJson(output));
            log.info("定时骑乘数据处理完成消息推送完成！");
        } catch (Exception e) {
            log.info("定时骑乘数据处理完成消息异常：{}", e.getMessage());

        }
    }


}
