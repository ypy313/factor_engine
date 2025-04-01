package com.changtian.factor.web.job.calc.impl;

import com.changtian.factor.web.job.calc.FactorCalcJob;
import com.changtian.factor.web.mapper.ClearTableMapper;
import com.changtian.factor.web.util.ExceptionUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * 清理OHLC成交表和指标计算结果表数据
 * 每天4:00执行，清理 T-1 以前的数据
 * FACTOR_FX_OHLC_DETAIL 外汇OHLC成交
 * FACTOR_PM_OHLC_DETAIL 贵金属OHLC成交
 * FACTOR_CAL_RESULT 指标计算结果
 */
@Component
public class ClearOhlcDetailOrCalResultJob implements FactorCalcJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private ClearTableMapper clearTableMapper;

    @Override
    public ReturnT executeJob(String param) {
        try {
            XxlJobLogger.log("ClearOhlcDetailOrCalResultJob is start");
            // 清理FACTOR_CAL_RESULT
            clearCalResult();
            // 清理FACTOR_FX_OHLC_DETAIL
            clearFxdetail();
            // 清理FACTOR_PM_OHLC_DETAIL
            clearPmdetail();
            XxlJobLogger.log("ClearOhlcDetailOrCalResultJob is end");
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("跑批失败，原因为：{}", ExceptionUtils.toString(e));
            return ReturnT.FAIL;
        }
    }

    /**
     * 清理 FACTOR_CAL_RESULT 表空间
     */
    public void clearCalResult() {
        LOGGER.info("clear FACTOR_CAL_RESULT is start");
        // 创建临时表
        clearTableMapper.createCalTempTable();
        // 清空当前表
        clearTableMapper.clearCalTable();
        // 临时表复制到当前表
        clearTableMapper.copyToCalTable();
        // 临时表删除
        clearTableMapper.dropCalTempTable();
        LOGGER.info("clear FACTOR_CAL_RESULT is end");
    }

    /**
     * 清理 FACTOR_FX_OHLC_DETAIL 表空间
     */
    public void clearFxdetail() {
        LOGGER.info("clear FACTOR_FX_OHLC_DETAIL is start");
        // 创建临时表
        clearTableMapper.createFxTempTable();
        // 清空当前表
        clearTableMapper.clearFxTable();
        // 临时表复制到当前表
        clearTableMapper.copyToFxTable();
        // 临时表删除
        clearTableMapper.dropFxTempTable();
        LOGGER.info("clear FACTOR_FX_OHLC_DETAIL is end");
    }

    /**
     * 清理 FACTOR_PM_OHLC_DETAIL 表空间
     */
    public void clearPmdetail() {
        LOGGER.info("clear FACTOR_PM_OHLC_DETAIL is start");
        // 创建临时表
        clearTableMapper.createPmTempTable();
        // 清空当前表
        clearTableMapper.clearPmTable();
        // 临时表复制到当前表
        clearTableMapper.copyToPmTable();
        // 临时表删除
        clearTableMapper.dropPmTempTable();
        LOGGER.info("clear FACTOR_PM_OHLC_DETAIL is end");
    }
}