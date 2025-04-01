package com.changtian.factor.web.mapper;

import com.changtian.factor.web.config.datasource.DataSource;
import com.changtian.factor.web.config.datasource.DataSourceType;
import com.changtian.factor.web.entity.FactorOhlcHis;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 贵金属因子OHLC历史Mapper接口
 */
public interface FactorPmOhlcHisMapper {
    /**
     * 查询历史ohlc数据
     */
    @DataSource(value = DataSourceType.smds)
    List<FactorOhlcHis> selectHistoryFactorPmOhlcHisList(String symbol, String period, int count);
    /**
     * 查询历史ohlc数据
     */
    @DataSource(value = DataSourceType.smds)
    List<FactorOhlcHis> selectHistoryFactorFxOhlcHisList(String symbol,String period,int count);
    /**
     * 查询当亲啊货币对颗粒度在x1Time 与x2Time之间的行情
     */
    @DataSource(value = DataSourceType.smds)
    int selectBarNum(String symbol,String period,String x1Time,String x2Time);
    /**
     * 查询历史ohlc数据
     */
    @DataSource(value = DataSourceType.smds)
    int selectHasBar(@Param("symbol") String symbol,@Param("period") String period,@Param("time")String time);

}
