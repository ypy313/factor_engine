package com.changtian.factor.web.mapper;

import com.changtian.factor.web.config.datasource.DataSource;
import com.changtian.factor.web.config.datasource.DataSourceType;
import com.changtian.factor.web.entity.FactorOhlcHis;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 外汇因子OHLC历史Mapper接口
 */
public interface FactorFxOhlcHisMapper {
    /**
     * 查询历史ohlc数据
     */
    @DataSource(value = DataSourceType.smds)
    List<FactorOhlcHis> selectHistoryFactorFxOhlcHisList(String symbol, String period, int cout);
    /**
     * 查询当前货币对颗粒度在x1Time与x2Time之间的行情
     */
    @DataSource(value = DataSourceType.smds)
    int selectBarNum(String symbol,String period,String x1Time,String x2Time);

    /**
     * 查询历史ohlc数据
     */
    @DataSource(value = DataSourceType.smds)
    int selectHasBar(@Param("symbol") String symbol,@Param("period") String period,@Param("time")String time);
}
