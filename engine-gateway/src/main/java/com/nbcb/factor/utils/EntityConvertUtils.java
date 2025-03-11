package com.nbcb.factor.utils;

import com.nbcb.factor.common.Constants;
import com.nbcb.factor.flink.aviatorfun.entity.IndexCalcResult;
import com.nbcb.factor.flink.aviatorfun.entity.OhlcParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据结构转化方法
 */
public class EntityConvertUtils {
    /**
     * Double 数据转为IndexCalcResult
     */
    public static List<IndexCalcResult> calcOpenConvert(OhlcParam[] ohlcParams){
        return Arrays.stream(ohlcParams).map(e->{
            IndexCalcResult indexCalcResult = new IndexCalcResult();
            indexCalcResult.setBeginTime(e.getBeginTime());
            indexCalcResult.setEndTime(e.getEndTime());
            indexCalcResult.setValue(BigDecimal.valueOf(e.getOpenPrice()));
            return indexCalcResult;
        }).collect(Collectors.toList());
    }

    public static List<IndexCalcResult> calcHighConvert(OhlcParam[] ohlcParams){
        return Arrays.stream(ohlcParams).map(e->{
            IndexCalcResult indexCalcResult = new IndexCalcResult();
            indexCalcResult.setBeginTime(e.getBeginTime());
            indexCalcResult.setEndTime(e.getEndTime());
            indexCalcResult.setValue(BigDecimal.valueOf(e.getHighPrice()));
            return indexCalcResult;
        }).collect(Collectors.toList());
    }

    public static List<IndexCalcResult> calcLowConvert(OhlcParam[] ohlcParams){
        return Arrays.stream(ohlcParams).map(e->{
            IndexCalcResult indexCalcResult = new IndexCalcResult();
            indexCalcResult.setBeginTime(e.getBeginTime());
            indexCalcResult.setEndTime(e.getEndTime());
            indexCalcResult.setValue(BigDecimal.valueOf(e.getLowPrice()));
            return indexCalcResult;
        }).collect(Collectors.toList());
    }

    public static List<IndexCalcResult> calcCloseConvert(OhlcParam[] ohlcParams){
        return Arrays.stream(ohlcParams).map(e->{
            IndexCalcResult indexCalcResult = new IndexCalcResult();
            indexCalcResult.setBeginTime(e.getBeginTime());
            indexCalcResult.setEndTime(e.getEndTime());
            indexCalcResult.setValue(BigDecimal.valueOf(e.getClosePrice()));
            return indexCalcResult;
        }).collect(Collectors.toList());
    }

    public static List<Double> getPrice(List<OhlcParam> ohlcParamList,String type){
        int fromIndex = ohlcParamList.size()>201 ? ohlcParamList.size()-201:0;
        Function<OhlcParam,Double> mapper = null;
        switch (type){
            case Constants.OPEN:
                mapper = OhlcParam::getOpenPrice;
                break;
            case Constants.HIGH:
                mapper = OhlcParam::getHighPrice;
                break;
            case Constants.LOW:
                mapper = OhlcParam::getLowPrice;
                break;
            case Constants.CLOSE:
                mapper = OhlcParam::getClosePrice;
                break;
        }
        if(mapper == null){
            return new ArrayList<>();
        }
        return ohlcParamList.stream().skip(fromIndex).map(mapper).collect(Collectors.toList());
    }
}
