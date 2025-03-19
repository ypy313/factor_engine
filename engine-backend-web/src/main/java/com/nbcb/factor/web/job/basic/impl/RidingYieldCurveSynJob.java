package com.nbcb.factor.web.job.basic.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.json.JSONUtils;
import com.nbcb.factor.common.AllRedisConstants;
import com.nbcb.factor.common.JsonUtil;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.entity.BondConfigEntity;
import com.nbcb.factor.entity.StrategyConfigVo;
import com.nbcb.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.nbcb.factor.entity.riding.CbondCurveCnbd;
import com.nbcb.factor.entity.riding.RidingAssetPool;
import com.nbcb.factor.entity.riding.RidingStrategyInstance;
import com.nbcb.factor.event.SpecifyData;
import com.nbcb.factor.event.StrategyCommandRidingInputEvent;
import com.nbcb.factor.job.LoadConfigHandler;
import com.nbcb.factor.strategy.CmdTypeEnum;
import com.nbcb.factor.strategy.StrategyCmdEnum;
import com.nbcb.factor.web.enums.DefinitionNameEnum;
import com.nbcb.factor.web.job.basic.FactorBasicJob;
import com.nbcb.factor.web.kafka.service.KafkaMessageSendService;
import com.nbcb.factor.web.service.BondConfigService;
import com.nbcb.factor.web.service.HolidayService;
import com.nbcb.factor.web.service.RidingStrategyInstanceService;
import com.nbcb.factor.web.service.StrategyInstanceService;
import com.nbcb.factor.web.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.crypto.Mac;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private static final Map<String, String> bondMapper = new ConcurrentHashMap<>();
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
                bondMapper.put(bond.getSecurityId(), bond.getSecurityDecMapper() + "_" + bond.getSecurityKey());
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
}
