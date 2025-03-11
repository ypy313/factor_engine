package com.nbcb.factor.web.job.history;

public interface FactorHistoryJob <T,ReturnT>{
    ReturnT executeJob(T param);
}
