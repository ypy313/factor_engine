package com.nbcb.factor.web.kafka.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.nbcb.factor.common.AllRedisConstants;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.output.SpreadTradeSignalResultOutput;
import com.nbcb.factor.web.kafka.BondSpreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 交易信号处理
 */
@Slf4j
@Service(OutputEvent.TRADE_SIGNAL)
public class TradeSignalService implements BondSpreadService {
    private final RedisUtil redisUtil = RedisUtil.getInstance();


    @Override
    public void process(String jsonStr) {
        SpreadTradeSignalResultOutput spreadTradeSignalResult = JSONUtil.toBean(jsonStr, SpreadTradeSignalResultOutput.class);
        log.info("KafkaConsumer process TradeSignal");
        //如果有则更新时间，增加更新次数，没有则添加
        String pKKey = spreadTradeSignalResult.getEventData().getInstanceId();
        //压缩规则 实例id 同样的实例，只保留一条
        //判断是否存在key
        String tradeSignalJson;
        if (redisUtil.keyExists(AllRedisConstants.FACTOR_SPREAD_CBOND_TRADESIGNAL,pKKey)) {
            //存在则取出对应value更新
            String json = redisUtil.getString(AllRedisConstants.FACTOR_SPREAD_CBOND_TRADESIGNAL, pKKey);
            SpreadTradeSignalResultOutput resultInRedis = JSONUtil.toBean(json, SpreadTradeSignalResultOutput.class);
            //修改更新时间 更改次数+1
            resultInRedis.setUpdateTime(spreadTradeSignalResult.getUpdateTime());
            resultInRedis.setCount(resultInRedis.getCount()+1);
            spreadTradeSignalResult.setCount(resultInRedis.getCount());
            tradeSignalJson = isRepalce(resultInRedis,spreadTradeSignalResult);
        }else {
            //不存在则添加新value
            tradeSignalJson = JSONUtil.toJsonStr(spreadTradeSignalResult);
        }
        redisUtil.setString(AllRedisConstants.FACTOR_SPREAD_CBOND_TRADESIGNAL,pKKey, tradeSignalJson);
        log.info("tradeSignalJson:{}",tradeSignalJson);
        log.info("tradeSignal has updated in redis,the pkKey is{},eventId is {}",pKKey,spreadTradeSignalResult.getEventId());
    }

    private String isRepalce(SpreadTradeSignalResultOutput resultOutput, SpreadTradeSignalResultOutput spreadTradeSignalResult) {
        if (StringUtils.equals(resultOutput.getEventId(), spreadTradeSignalResult.getEventId())) {
            if (spreadTradeSignalResult.getEventData().getSignal().ordinal() < resultOutput.getEventData().getSignal().ordinal()) {
                return JSONUtil.toJsonStr(resultOutput);
            } else if (spreadTradeSignalResult.getEventData().getSignal().ordinal() > resultOutput.getEventData().getSignal().ordinal()) {
                return JSONUtil.toJsonStr(spreadTradeSignalResult);
            } else {
                if ("ChinaBondSpreadPercentile".equals(spreadTradeSignalResult.getEventData().getFactorName())) {
                    return JSONUtil.toJsonStr(spreadTradeSignalResult);
                } else if ("ChinaBondSpreadPercentile".equals(resultOutput.getEventData().getFactorName())) {
                    return JSONUtil.toJsonStr(resultOutput);
                } else {
                    return JSONUtil.toJsonStr(spreadTradeSignalResult);
                }
            }
        } else {
            return JSONUtil.toJsonStr(spreadTradeSignalResult);
        }
    }
}
