package com.changtian.factor.web.job.calc;

import com.xxl.job.core.biz.model.ReturnT;

/**
 * 早间批量计算任务接口
 */
public interface FactorCalcJob {
    ReturnT executeJob(String param);
}
