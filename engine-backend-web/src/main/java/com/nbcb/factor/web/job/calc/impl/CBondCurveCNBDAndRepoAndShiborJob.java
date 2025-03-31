package com.nbcb.factor.web.job.calc.impl;

import cn.hutool.json.JSONUtil;
import com.nbcb.factor.common.AllRedisConstants;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.web.entity.RateVo;
import com.nbcb.factor.web.job.calc.FactorCalcJob;
import com.nbcb.factor.web.mapper.RidingYieldCurveMapper;
import com.nbcb.factor.web.mapper.StrategyInstanceMapper;
import com.nbcb.factor.web.util.ExceptionUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.nbcb.factor.common.Constants.*;

/**
 * 收益率曲线/回购(repo)利率表/shibor利率表数据跑批
 *
 * @author k11866
 * @date 2023/5/9 16:12
 */
@Component
public class CBondCurveCNBDAndRepoAndShiborJob implements FactorCalcJob {

    @Autowired
    private RidingYieldCurveMapper ridingYieldCurveMapper;

    @Autowired
    private StrategyInstanceMapper strategyInstanceMapper;

    private final RedisUtil redisUtil = RedisUtil.getInstance();

    @Override
    public ReturnT executeJob(String param) {
        try {
            XxlJobLogger.log("同步回购利率表开始了……");
            // 回购(repo)利率表
            if (ridingYieldCurveMapper.selectInsertCountAboutRepo() > 0) {
                ridingYieldCurveMapper.truncateShiborprices();
                ridingYieldCurveMapper.insertCbondInBankRateCfets();
            } else {
                // 删除前的查询全量更新的数据是否有值，如果没有，打印日志
                XxlJobLogger.log("没有回购利率表repo同步的数据，回购利率表数据不同步！");
            }

            XxlJobLogger.log("同步回购利率表结束！");

            XxlJobLogger.log("同步shibor利率表开始了……");

            if (ridingYieldCurveMapper.selectInsertCountAboutShibor() > 0) {
                ridingYieldCurveMapper.truncateShiborprices();
                ridingYieldCurveMapper.insertShiborprices();
            } else {
                // 删除前的查询全量更新的数据是否有值，如果没有，打印日志
                XxlJobLogger.log("没有shibor利率表同步的数据，shibor利率表数据不同步！");
            }
            XxlJobLogger.log("同步shibor利率表结束！");

            // 缓存shibor7D数据(最近一年)
            XxlJobLogger.log("缓存shibor7D数据(最近一年)开始了……");
            List<RateVo> shiborRateVoList = this.strategyInstanceMapper.queryAverageValueShiborPrices(SHIBOR1W_IR);
            if (!CollectionUtils.isEmpty(shiborRateVoList)) {
                redisUtil.setRiding(AllRedisConstants.FACTOR_COMMON_REPO_7D,
                        JSONUtil.toJsonStr(shiborRateVoList));
            }
            XxlJobLogger.log("缓存shibor7D数据(最近一年)结束！");

            // 缓存shibor3M数据(最近一年)
            XxlJobLogger.log("缓存shibor3M数据(最近一年)开始了……");
            List<RateVo> shiborRate3MVoList = this.strategyInstanceMapper.queryAverageValueShiborPrices(SHIBOR3M_IR);
            if (!CollectionUtils.isEmpty(shiborRateVoList)) {
                redisUtil.setRiding(AllRedisConstants.FACTOR_COMMON_SHIBOR_3M,
                        JSONUtil.toJsonStr(shiborRate3MVoList));
            }
            XxlJobLogger.log("缓存shibor3M数据(最近一年)结束！");

            // 缓存repo7D数据(最近一年)
            XxlJobLogger.log("缓存repo7D数据(最近一年)开始了……");
            List<RateVo> repoRateVoList = this.strategyInstanceMapper.queryAverageValueShiborPrices(FROO7_IR);
            if (!CollectionUtils.isEmpty(repoRateVoList)) {
                redisUtil.setRiding(AllRedisConstants.FACTOR_COMMON_REPO_7D,
                        JSONUtil.toJsonStr(repoRateVoList));
            }
            XxlJobLogger.log("缓存repo7D数据(最近一年)结束！");
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("跑批失败，原因为：{}", ExceptionUtils.toString(e));
            return ReturnT.FAIL;
        }
    }
}
