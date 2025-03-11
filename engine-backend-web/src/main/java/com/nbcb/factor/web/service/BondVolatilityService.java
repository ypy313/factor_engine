package com.nbcb.factor.web.service;

import com.nbcb.factor.common.CalculateUtil;
import com.nbcb.factor.web.entity.CurrencyValuation;
import com.nbcb.factor.web.response.exception.CalcException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BondVolatilityService extends CBondBaseService{
    /**
     * 计算波动率和平均值
     */
    public Map<String, BigDecimal> calcVolatility(String leg1, String leg2, int days){
        Map<String, List<CurrencyValuation>> cnbdMapList = getCnbdMapList(leg1, leg2, days);
        //查询leg1的价格
        List<CurrencyValuation> leg1List = cnbdMapList.get("leg1List");
        //查询leg2的价格
        List<CurrencyValuation> leg2List = cnbdMapList.get("leg2List");
        List<BigDecimal> minusList = processData(leg1List, leg2List, days);
        if (minusList == null || minusList.size()==0) {
            log.error("Volatility spread array is empty,leg1:{},leg2:{},days:{}",leg1,leg2,days);
            throw new CalcException("Volatility spread array is empty");
        }
        Map<String, BigDecimal> map = new HashMap<>();
        //计算标准差
        BigDecimal std = CalculateUtil.mathStandardDeviation(minusList);
        //计算平均值
        BigDecimal average = CalculateUtil.average(minusList);
        if (null == std || null == average) {
            log.error("cal std or average error,leg1:{},leg2:{},dataWindow:{}",leg1,leg2,days);
            throw new CalcException("calc std or average error");
        }
        map.put("std",std);
        map.put("average",average);
        log.info("the result of volatility map:{}",map);
        return map;

    }
}
