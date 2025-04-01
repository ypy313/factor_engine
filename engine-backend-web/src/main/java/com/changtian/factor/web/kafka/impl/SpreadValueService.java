package com.changtian.factor.web.kafka.impl;

import cn.hutool.json.JSONUtil;
import com.changtian.factor.common.AllRedisConstants;
import com.changtian.factor.common.RedisUtil;
import com.changtian.factor.event.OutputEvent;
import com.changtian.factor.output.SpreadCalcResultOutput;
import com.changtian.factor.web.kafka.BondSpreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 价差信号处理
 */
@Slf4j
@Service(OutputEvent.SPREAD_VALUE)
public class SpreadValueService implements BondSpreadService {
    private final RedisUtil redisUtil = RedisUtil.getInstance();

    @Override
    public void process(String jsonStr) {
        SpreadCalcResultOutput spreadCalcResult = JSONUtil.toBean(jsonStr, SpreadCalcResultOutput.class);
        String instanceId = spreadCalcResult.getEventData().getInstanceId();
        String factorName = spreadCalcResult.getEventData().getFactorName();
        String redisKey = AllRedisConstants.FACTOR_SPREAD_CBOND_VALUESINGAL+":"+instanceId+":"+factorName;
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("srcTimestamp",spreadCalcResult.getSrcTimestamp());
        dataMap.put("calRes",spreadCalcResult.getEventData().getCalResult());
        dataMap.put("bestCalResult",spreadCalcResult.getEventData().getBestCalResult());
        dataMap.put("allLegYieldMap",spreadCalcResult.getEventData().getAllLegYieldMap());

        redisUtil.setString(redisKey,spreadCalcResult.getPkKey(),JSONUtil.toJsonStr(dataMap));
        log.info("spreadValue has put into redis,the key is {}",redisKey);

    }
}
