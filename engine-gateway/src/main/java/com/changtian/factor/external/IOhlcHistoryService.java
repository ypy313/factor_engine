package com.changtian.factor.external;

import com.changtian.factor.external.dto.FactorFxOhlcHis;
import com.changtian.factor.external.impl.OhlcHistoryServiceImpl;

import java.util.List;

public interface IOhlcHistoryService {
    List<FactorFxOhlcHis> listBy(String symbol,String period,int count,String sourceType);

    static IOhlcHistoryService getInstance() {return new OhlcHistoryServiceImpl();}
}
