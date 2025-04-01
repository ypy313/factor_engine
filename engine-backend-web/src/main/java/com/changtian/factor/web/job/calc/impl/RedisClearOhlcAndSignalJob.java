package com.changtian.factor.web.job.calc.impl;

import com.changtian.factor.common.RedisUtil;
import com.changtian.factor.common.StringUtil;
import com.changtian.factor.enums.DataTypeEnum;
import com.changtian.factor.web.job.calc.FactorCalcJob;
import com.changtian.factor.web.util.ExceptionUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.stereotype.Component;

/**
 * 外汇和贵金属-定时清理redis数据
 *
 * @Description
 * @Author k11866
 * @Date 2023/2/20 14:56
 */
@Component
public class RedisClearOhlcAndSignalJob implements FactorCalcJob {
    private final RedisUtil redisUtil = RedisUtil.getInstance();
    private static final String FACTOR_FX_TRADE_SIGNAL = StringUtil.redisKeyFxJoint(DataTypeEnum.TRADE_SIGNAL.getKey());
    private static final String FACTOR_FX_LAST_HIS_OHLC = StringUtil.redisKeyFxJoint(DataTypeEnum.LAST_HIS_OHLC.getKey());
    private static final String FACTOR_PM_TRADE_SIGNAL = StringUtil.redisKeyPMJoint(DataTypeEnum.TRADE_SIGNAL.getKey());
    private static final String FACTOR_PM_LAST_HIS_OHLC = StringUtil.redisKeyPMJoint(DataTypeEnum.LAST_HIS_OHLC.getKey());

    @Override
    public ReturnT executeJob(String param) {
        try {
            // 外汇 信号数据
            redisUtil.delString(FACTOR_FX_TRADE_SIGNAL);
            // 外汇 k线数据
            redisUtil.delString(FACTOR_FX_LAST_HIS_OHLC);
            // 贵金属 信号数据
            redisUtil.delString(FACTOR_PM_TRADE_SIGNAL);
            // 贵金属 k线数据
            redisUtil.delString(FACTOR_PM_LAST_HIS_OHLC);
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log( "跑批失败，原因为：{}", ExceptionUtils.toString(e));
            return ReturnT.FAIL;
        }
    }
}