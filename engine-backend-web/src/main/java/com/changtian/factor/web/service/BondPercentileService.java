package com.changtian.factor.web.service;

import com.changtian.factor.common.CalculateUtil;
import com.changtian.factor.web.entity.CurrencyValuation;
import com.changtian.factor.web.response.exception.CalcException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BondPercentileService extends CBondBaseService{

    /**
     * 计算百分位
     */
    public Map<BigDecimal, BigDecimal> calcPercentile(String leg1, String leg2, int days, int depth){
        Map<String,List<CurrencyValuation>> cnbdMapList = getCnbdMapList(leg1,leg2,days);
        //查询leg1的价格
        List<CurrencyValuation> leg1List = cnbdMapList.get("leg1List");
        //查询leg2的价格
        List<CurrencyValuation> leg2List = cnbdMapList.get("leg2List");

        List<BigDecimal> minusList = processData(leg1List, leg2List, days);

        if(minusList == null || minusList.isEmpty()){
            log.error("Percentile spread array is empty,leg1:{},leg2:{} days:{} depth:{}",leg1,leg2,days,depth);
            throw new CalcException("Percentile spread array is empty");
        }

        Map<BigDecimal,BigDecimal> ret = CalculateUtil.calcPercentile(minusList,depth);
        if (ret == null){
            log.error("Percentile ret is null,leg1:{},leg2:{} days:{} depth:{}",leg1,leg2,days,depth);
            throw new CalcException("Percentile ret is null");
        }
        return ret;
    }

}
