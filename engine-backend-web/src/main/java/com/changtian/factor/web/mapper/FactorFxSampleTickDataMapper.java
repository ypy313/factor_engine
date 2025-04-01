package com.changtian.factor.web.mapper;

import com.changtian.factor.web.config.datasource.DataSource;
import com.changtian.factor.web.config.datasource.DataSourceType;
import org.apache.ibatis.annotations.Param;

/**
 * 抽样tick数据
 */
public interface FactorFxSampleTickDataMapper {
    /**
     * 查询当前货币对TICK颗粒度在x1Time与x2Time之间的行情
     */
    @DataSource(value = DataSourceType.smds)
    int selectTickBarNum(String symbol,long x1Time,long x2Time);
    /**
     * 查询历史TICK数据 返回每个货币对的一条最新的tick数据
     */
    @DataSource(value = DataSourceType.smds)
    int selectTickInfo(@Param("symbol") String symbol,@Param("time") String time);
}
