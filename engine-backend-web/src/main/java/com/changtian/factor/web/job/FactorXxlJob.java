package com.changtian.factor.web.job;

import com.changtian.factor.web.job.calc.impl.ChinaBondSpreadPercentileCalcJob;
import com.changtian.factor.web.job.calc.impl.ChinaBondSpreadVolatilityCalcJob;
import com.changtian.factor.web.job.calc.impl.FactorStrategyInstanceCalcJob;
import com.changtian.factor.web.job.history.impl.HistorySyncJob;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 接入xxl job
 */
@Component
@Slf4j
public class FactorXxlJob{
    public static final ReturnT RETURN_SUCCESS = ReturnT.SUCCESS;
    public static final ReturnT RETURN_FAIL = ReturnT.FAIL;

    @Autowired
    private ChinaBondSpreadPercentileCalcJob chinaBondSpreadPercentileJob;

    @Autowired
    private ChinaBondSpreadVolatilityCalcJob chinabondSpreadVolatilityJob;

    @Autowired
    private FactorStrategyInstanceCalcJob factorStrategyInstanceCalcJob;

    @Autowired
    private HistorySyncJob historySyncJob;

    @XxlJob("chinaBondSpreadPercentileCalcJob")
    public ReturnT chinaBondSpreadPercentileJobHandler(String param) throws Exception{
        log.info("receive chinaBondSpreadPercentileJobHandler job");
        return chinaBondSpreadPercentileJob.executeJob(param);
    }

    @XxlJob("chinaBondSpreadVolatilityCalcJob")
    public ReturnT chinaBondSpreadVolatilityJobHandler(String param) throws Exception{
        log.info("receive chinaBondSpreadVolatilityJobHandler job");
        return chinabondSpreadVolatilityJob.executeJob(param);
    }

    public ReturnT cleanStrategyInstanceCalcJob(String param) throws Exception{
        log.info("receive cleanStrategyInstanceCalcJob job");
        return factorStrategyInstanceCalcJob.executeJob(param);
    }

    public ReturnT historySyncJob(String param) throws Exception{
        log.info("receive historySyncJob job");
        return historySyncJob.executeJob(param);
    }
}
