package com.nbcb.factor.web.controller;

import com.nbcb.factor.web.entity.FactorOhlcHis;
import com.nbcb.factor.web.response.Response;
import com.nbcb.factor.web.service.FactorFxOhlcHisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 外汇ohlc请求Controller
 */
@RestController
public class ForexFactorFxOhlcHisController {
    @Autowired
    private FactorFxOhlcHisService factorFxOhlcHisService;
    @RequestMapping("/forexOHlc")
    public Response selectKlineOrTick(@RequestParam(value = "symbol")String symbol,
                                      @RequestParam(value = "period") String period,
                                      @RequestParam(value = "count")int count) {
        //k线历史数据
        List<FactorOhlcHis> factorOhlcHisList = factorFxOhlcHisService
                .selectHistoryFactorFxOhlcHisList(symbol,period,count);
        return new Response(factorOhlcHisList);
    }
}
