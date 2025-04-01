package com.changtian.factor.job.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.changtian.factor.common.*;
import com.changtian.factor.entity.BondConfigEntity;
import com.changtian.factor.entity.StrategyConfigVo;
import com.changtian.factor.entity.market.SubscriptionAssetData;
import com.changtian.factor.event.StrategyCommandRidingInputEvent;
import com.changtian.factor.job.LoadConfigHandler;
import com.changtian.factor.strategy.StrategyCmdEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 融合市场行情处理接口实现
 */
@Slf4j
public class LoadMarketProcessHandler implements LoadConfigHandler {
    private static final Map<String, String> bondMapperMap = new ConcurrentHashMap<>();

    @Override
    public String strategyName() {
        return "BondPrice";
    }

    @Override
    public List<?> loadConfigHandler(String strategyId, String strategyName, Class strategyClass, Object event) {
        StrategyCommandRidingInputEvent inputEvent = (StrategyCommandRidingInputEvent) event;// 是否重新加载需要计算
        String cmd = "";
        if (null != inputEvent) {
            cmd = inputEvent.getEventData().getCmd();//命令 onTimer+reload stop start 需要backend重新加软
        }
        boolean isReload = StrategyCmdEnum.STOP.getCmd().equals(cmd) || StrategyCmdEnum.START.getCmd().equals(cmd)
                || StrategyCmdEnum.RELOAD.getCmd().equals(cmd) || StrategyCmdEnum.SUSPEND.getCmd().equals(cmd);
        // 初始化实例
        RedisUtil redisUtil = RedisUtil.getInstance();
        String configListStr = redisUtil.getRiding(AllRedisConstants.FACTOR_MARKET_PROCESSING_CONFIG_ASSET_DATA_LIST);
        List<SubscriptionAssetData> assetDataList = new ArrayList<>();
        if (isReload || StringUtils.isEmpty(configListStr)) {
            //需要从backend获取配需
            String strategyConfigUrl = LoadConfig.getProp().getProperty("strategy.config.url");
            StrategyConfigVo strategyConfigVo = new StrategyConfigVo();
            strategyConfigVo.setSymbolType(Constants.FACTOR_MARKET_PROCESSING_CONFIG_BOND_KEY);
            try {
                String result = HttpUtils.httpPost(strategyConfigUrl, JSONUtil.toJsonStr(strategyConfigVo));
                Map map = JSONUtil.toBean(result, Map.class);
                String status = MapUtil.getStr(map, Constants.HTTP_RESPONSE_STATUS); // 获取状态
                if (!Constants.HTTP_SUCC.equals(status)) {
                    log.error("The web server return fail:f}, Initializing loadRidinastrateayconfig failed.", result);
                    return Collections.emptyList();
                }
                String data = MapUtil.getStr(map, Constants.HTTP_RESPONSE_DATA);
                log.info("loadSubscriptionAssetData:{}", data);
                JSONArray objects = JSONUtil.parseArray(data);
                assetDataList = JSONUtil.toList(objects, SubscriptionAssetData.class);
                //存入redis
                if (!CollectionUtils.isEmpty(assetDataList)) {
                    redisUtil.setRiding(AllRedisConstants.FACTOR_MARKET_PROCESSING_CONFIG_ASSET_DATA_LIST, data);
                }
            } catch (IOException e) {
                log.error("loadSubscriptionAssetData exception:", e);
            }
        } else {
            //直接去redis的数据
            JSONArray objects = JSONUtil.parseArray(configListStr);
            assetDataList = JSONUtil.toList(objects, SubscriptionAssetData.class);
        }

        return assetDataList;
    }

    @Override
    public void loadBondConfig() {
        //加载配置
        String bondConfigDataUrl = LoadConfig.getProp().getProperty("bond,config.data.url");
        try {
            String result = HttpUtils.httpGet(bondConfigDataUrl);
            Map map = JSONUtil.toBean(result, Map.class);
            String status = MapUtil.getStr(map, Constants.HTTP_RESPONSE_STATUS); // 获取状态
            if (!Constants.HTTP_SUCC.equals(status)) {
                log.error("The web server return fail:{},Initializing getBondConfigDatalist failed.", result);
                return;

            }
            String data = MapUtil.getStr(map, Constants.HTTP_RESPONSE_DATA);
            JSONArray objects = JSONUtil.parseArray(data);
            List<BondConfigEntity> bondConfigEntityList = JSONUtil.toList(objects, BondConfigEntity.class);
            if (CollectionUtils.isEmpty(bondConfigEntityList)) {
                log.error("Initializing bondConfigEntity list is nul.");
                return;
            }
            log.info("加载配置结果数里为为:{}", bondConfigEntityList.size());
            //清空映射关系map
            bondMapperMap.clear();
            bondConfigEntityList.forEach(bond -> {
                bondMapperMap.put(bond.getSecurityId(), bond.getSecurityDecMapper() + "_" + bond.getSecurityKey());
            });
            JSONUtil.toJsonStr(bondMapperMap);
        } catch (Exception e) {
            log.error("Initializing getBondConfigDataList failed.", e);
        }
    }

    @Override
    public Map<String, String> getBondConfig() {
        return bondMapperMap;
    }

    public static Map<String, String> getStaticBondConfig() {
        return bondMapperMap;
    }
}
