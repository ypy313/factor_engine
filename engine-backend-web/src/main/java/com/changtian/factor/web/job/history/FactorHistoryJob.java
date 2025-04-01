package com.changtian.factor.web.job.history;

public interface FactorHistoryJob <T,ReturnT>{
    ReturnT executeJob(T param);
}
