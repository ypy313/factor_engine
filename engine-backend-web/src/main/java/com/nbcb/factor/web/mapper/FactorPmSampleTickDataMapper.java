package com.nbcb.factor.web.mapper;

import com.nbcb.factor.web.config.datasource.DataSource;
import com.nbcb.factor.web.config.datasource.DataSourceType;
import org.apache.ibatis.annotations.Param;

/**
 * 抽样tick数据
 */
public interface FactorPmSampleTickDataMapper {
    /**
     * 查询当亲货币对TICK颗粒度在x1Time与x2Time之间的行情
     */
    @DataSource(value = DataSourceType.smds)
    int selectTickBarNum(String symbol,long x1Time);
    /**
     * 查询历史tick数据 ，返回每个货币对的一条最新的tick数据
     */
    @DataSource(value = DataSourceType.smds)
    int selectTickInfo(@Param("symbol") String symbol,@Param("time") String time);
}
