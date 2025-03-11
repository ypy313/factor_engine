package com.nbcb.factor.web.mapper;

import com.nbcb.factor.web.config.datasource.DataSource;
import com.nbcb.factor.web.config.datasource.DataSourceType;
import com.nbcb.factor.web.entity.PmOhlcValueEvent;

/**
 * 贵金属因子OHLC成交Mapper
 */
@DataSource(value = DataSourceType.smds)
public interface FactorPmOhlcDetailMapper {
    /**
     * 新增金属因子ohlc成交数据
     */
    @DataSource(value = DataSourceType.smds)
    int insertFactorPmOhlcDetailList(PmOhlcValueEvent pmOhlcValueEvent);
}
