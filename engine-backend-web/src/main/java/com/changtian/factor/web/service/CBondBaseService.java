package com.changtian.factor.web.service;

import com.changtian.factor.common.DateUtil;
import com.changtian.factor.web.entity.CurrencyValuation;
import com.changtian.factor.web.mapper.CurrencyValuationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
@Slf4j
@Service
public class CBondBaseService {


    @Autowired
    public CurrencyValuationMapper currencyValuationMapper;

    /**
     * 查询leg1和leg2的价格
     */
    public Map<String,List<CurrencyValuation>> getCnbdMapList(String leg1,String leg2,int days){
        //开始日期
        String startDate = DateUtil.format(DateUtil.calcDaysBefore(days),DateUtil.DAYSTR);
        //结束日期
        String endDate = DateUtil.format(DateUtil.calcDaysBefore(1),DateUtil.DAYSTR);
        //sql查询,返回的数据
        List<CurrencyValuation> list = currencyValuationMapper.selectCurrencyValuationList(startDate,endDate,leg1,leg2);

        if(list == null || list.size()==0){
            log.warn("getCurrencyValuationMapList query database data is empty,leg1:{},leg2:{} , days :{}",leg1,leg2,days);
            return new HashMap<>();
        }
        List<CurrencyValuation> leg1List = new ArrayList<>(100);
        List<CurrencyValuation> leg2List = new ArrayList<>(100);
        list.forEach(item ->{
            if(leg1.equals(item.getSecurityId())){
                leg1List.add(item);
            }else {
                leg2List.add(item);
            }
        });

        Map<String,List<CurrencyValuation>> map = new HashMap<>();
        map.put("leg1List",leg1List);
        map.put("leg2List",leg2List);

        return map;
    }

    /**
     * 过滤值为0和日期不匹配的项
     */
    public List<BigDecimal> processData(List<CurrencyValuation> legList1, List<CurrencyValuation> legList2, int days){
        //key日期  value收益率  按日期从小到大排序
        TreeMap<String,List<BigDecimal>> dayMap = new TreeMap<>();
        //把需要查询的所有日期设为key
        for(int i=1;i<days;i++){
            String date = DateUtil.format(DateUtil.calcDaysBefore(i),DateUtil.DAYSTR);
            dayMap.put(date,new ArrayList<>(2));
        }
        //leg1数据 根据日期把收益率放入list
        for(CurrencyValuation currencyValuation: legList1){
            String date = currencyValuation.getTradeDt();
           List list =  dayMap.get(date) ;
           if (list != null){
               list.add(currencyValuation.getRateValue());
           }

        }
        //leg2数据 根据日期把收益率放入list
        for(CurrencyValuation currencyValuation: legList2){
            String date = currencyValuation.getTradeDt();
            List list =  dayMap.get(date) ;
            if (list != null){
                list.add(currencyValuation.getRateValue());
            }
        }

        //计算leg1-leg2
        List<BigDecimal> minusList = new ArrayList<>(200);
        BigDecimal zero = new BigDecimal(0);
        //循环判断对应日期是否有2个对应的值，并且过滤值为0的项
        dayMap.forEach((key,value) ->{
            if(value.size()==2){
                if(value.get(0).compareTo(zero) > 0 && value.get(1).compareTo(zero) > 0){
                    minusList.add(value.get(0).subtract(value.get(1)));
                }
            }
        });
        return minusList;
    }
}
