package com.changtian.factor.web.job.calc.impl;

import com.changtian.factor.common.AllRedisConstants;
import com.changtian.factor.common.RedisConstant;
import com.changtian.factor.common.RedisUtil;
import com.changtian.factor.web.job.FactorXxlJob;
import com.changtian.factor.web.job.calc.FactorCalcJob;
import com.changtian.factor.web.util.ExceptionUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class FactorStrategyInstanceCalcJob implements FactorCalcJob {
    private final RedisUtil redisUtil = RedisUtil.getInstance();

    @Override
    public ReturnT executeJob(String param) {
        try{
            XxlJobLogger.log("ChinaBondSpreadVolatilityJob cleanStrategyInstanceCalcJob");

            //因子策略计算结果
            redisUtil.delString(AllRedisConstants.FACTOR_SPREAD_CALC);
            //删除日内的计算值和交易信号
            redisUtil.delString(AllRedisConstants.FACTOR_SPREAD_CBOND_VOLATILITY);
            //删除因子实例
            redisUtil.delString(AllRedisConstants.FACTOR_CONFIG_FLINK);
            //删除债券价格缓存数据
            redisUtil.delString(AllRedisConstants.FACTOR_LEG_VALUE);
            //删除web访问计数器
            redisUtil.delString(RedisConstant.FACTOR_WEB_FAILURE);
            //删除价差结果
            redisUtil.deleteRedisKeyStartWith(AllRedisConstants.FACTOR_SPREAD_CBOND_VOLATILITY);
            //删除交易信号
            redisUtil.deleteRedisKeyStartWith(AllRedisConstants.FACTOR_SPREAD_CBOND_TRADESIGNAL);
            //清除redis中价差昨日值
            redisUtil.delString(AllRedisConstants.FACTOR_LASTDAY_VALUE);
            return FactorXxlJob.RETURN_SUCCESS;
        }catch (Exception e){
            XxlJobLogger.log("跑批失败，原因为：{}", ExceptionUtils.toString(e));
            return ReturnT.FAIL;
        }
    }
}
