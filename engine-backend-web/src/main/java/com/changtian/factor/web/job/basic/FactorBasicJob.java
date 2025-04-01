package com.changtian.factor.web.job.basic;

/**
 * 因子基础数据处理任务接口
 */
public interface FactorBasicJob<T,ReturnT> {
    ReturnT executeJob(T param);
}
