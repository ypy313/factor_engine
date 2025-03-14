package com.nbcb.factor.web.service;

import com.nbcb.factor.common.MarketTypeEnum;
import com.nbcb.factor.web.config.datasource.DataSource;
import com.nbcb.factor.web.config.datasource.DataSourceType;
import com.nbcb.factor.web.entity.FactorOhlcHis;
import com.nbcb.factor.web.mapper.FactorFxOhlcHisMapper;
import com.nbcb.factor.web.mapper.FactorPmOhlcHisMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 外汇因子ohlc历史表service
 */
@Service
public class FactorFxOhlcHisService {
    @Autowired
    private FactorFxOhlcHisMapper factorFxOhlcHisMapper;
    @Autowired
    private FactorPmOhlcHisMapper factorPmOhlcHisMapper;

    /**
     * 查询历史ohlc数据
     */
    @DataSource(value = DataSourceType.smds)
    public List<FactorOhlcHis> selectHistoryFactorFxOhlcHisList(String symbol, String period, int count) {
        if (count == 0) {
            count = 10;
        }
        if (symbol.contains(MarketTypeEnum.SGE.getEndName())
                || symbol.contains(MarketTypeEnum.SHFE.getEndName())
                || symbol.contains(MarketTypeEnum.FXALL.getEndName())) {
            String[] split = symbol.split("@");
            List<FactorOhlcHis> factorOhlcHis = factorPmOhlcHisMapper.selectHistoryFactorPmOhlcHisList(split[0], period, count);
            return factorOhlcHis;
        } else {
            return factorFxOhlcHisMapper.selectHistoryFactorFxOhlcHisList(symbol, period, count);
        }
    }

    /**
     * 查詢当前货币对颗粒度在x1Time与x2Time之间的行情bar个数
     */
    @DataSource(value = DataSourceType.smds)
    public int selectBarNum(String symbol, String period, String x1Time, String x2Time) {
        return factorFxOhlcHisMapper.selectBarNum(symbol, period, x1Time, x2Time);
    }
}

