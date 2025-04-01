package com.changtian.factor.web.controller;

import com.changtian.factor.web.response.Response;
import com.changtian.factor.web.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 假日相关web调用接口
 */
@RestController
public class RdiHolidayController {
    @Autowired
    private HolidayService holidayService;

    /**
     * 百分位计算接口 返回为百分位表
     */
    @RequestMapping("/selectRdiHoliday")
    public Response getPercentile() {
        return new Response(holidayService.selectRdiHoliday());
    }
    /**
     * 计算历史波动率和平均值 参数为leg1,leg2,days返回为历史波动率（标准差）和平均值
     */
    @RequestMapping("/selectRdiHolidayByDate")
    public Response getVolatility(@RequestParam(value = "date")String date) {
        return new Response(holidayService.selectRdiHolidayByDate(date));
    }
}
