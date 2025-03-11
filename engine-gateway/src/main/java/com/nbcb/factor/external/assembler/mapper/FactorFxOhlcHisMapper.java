package com.nbcb.factor.external.assembler.mapper;

import com.nbcb.factor.external.dto.FactorFxOhlcHis;
import com.nbcb.factor.flink.aviatorfun.entity.OhlcParam;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactorFxOhlcHisMapper {
    public static final FactorFxOhlcHisMapper INSTANCE = new FactorFxOhlcHisMapper();

    public OhlcParam convert(FactorFxOhlcHis his){
        OhlcParam ohlcParam = new OhlcParam();
        ohlcParam.setSymbol(his.getSymbol());
        ohlcParam.setPeriod(his.getPeriod());
        ohlcParam.setBeginTime(his.getBeginTime());
        ohlcParam.setEndTime(his.getEndTime());
        ohlcParam.setOpenPrice(his.getOpenPrice());
        ohlcParam.setHighPrice(his.getHighPrice());
        ohlcParam.setLowPrice(his.getLowPrice());
        ohlcParam.setClosePrice(his.getClosePrice());
        return ohlcParam;
    }

    public List<OhlcParam> convert(List<FactorFxOhlcHis> hisList){
        if (CollectionUtils.isEmpty(hisList)) {
            return Collections.emptyList();
        }
        List<OhlcParam> list = new ArrayList<>(hisList.size());
        for (FactorFxOhlcHis e : hisList) {
            list.add(convert(e));
        }
        return list;
    }
}
