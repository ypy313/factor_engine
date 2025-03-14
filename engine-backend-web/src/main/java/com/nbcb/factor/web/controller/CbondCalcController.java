package com.nbcb.factor.web.controller;

import com.nbcb.factor.web.entity.CurrencyValuation;
import com.nbcb.factor.web.response.Response;
import com.nbcb.factor.web.service.BondPercentileService;
import com.nbcb.factor.web.service.BondVolatilityService;
import com.nbcb.factor.web.service.CurrencyValuationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 债券计算相关web调用接口
 */
@RestController
public class CbondCalcController {
    @Autowired
    private BondPercentileService bondPercentileService;
    @Autowired
    private BondVolatilityService bondVolatilityService;
    @Autowired
    private CurrencyValuationService currencyValuationService;

    /**
     * 百分位计算接口 返回为百分位表
     */
    @RequestMapping("/percentile")
    public Response getPercentile(@RequestParam(value = "leg1") String leg1,
                                  @RequestParam(value = "leg2") String leg2,
                                  @RequestParam(value = "days") int days,
                                  @RequestParam(value = "depth") int depth) {
        return new Response(bondPercentileService.calcPercentile(leg1, leg2, days, depth));
    }

    /**
     * 计算历史波动率和平均值，参数leg1,leg2,days返回为历史波动率（标准差）和平均值
     */
    @RequestMapping("/volatility")
    public Response getVolatility(@RequestParam(value = "leg1") String leg1,
                                  @RequestParam(value = "leg2") String leg2,
                                  @RequestParam(value = "days") int days) {
        return new Response(bondVolatilityService.calcVolatility(leg1, leg2, days));
    }
    /**
     * 返回海泉代码的最新中债贵，考虑国庆假期或者春节假期，查最长10天数据，按时间排序后，返回第一条
     */
    @RequestMapping("/cnbdvalue")
    public Response getCnbcValue(@RequestParam(value = "bondcode") String bondcode) {
        return new Response(currencyValuationService.getCurrencyValuation(bondcode));
    }
}
