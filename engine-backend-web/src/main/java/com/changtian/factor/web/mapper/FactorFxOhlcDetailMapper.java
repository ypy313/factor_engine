package com.changtian.factor.web.mapper;

import com.changtian.factor.web.config.datasource.DataSource;
import com.changtian.factor.web.config.datasource.DataSourceType;
import com.changtian.factor.web.entity.OhlcValueEvent;

/**
 * 外汇因子ohlc成交mapper接口
 */
@DataSource(value = DataSourceType.smds)
public interface FactorFxOhlcDetailMapper {
    /**
     * 新增外汇因子ohlc成交数据
     */
    @DataSource(value = DataSourceType.smds)
    int insertFactorFxOhlcDetailList(OhlcValueEvent ohlcValueEvent);
}
