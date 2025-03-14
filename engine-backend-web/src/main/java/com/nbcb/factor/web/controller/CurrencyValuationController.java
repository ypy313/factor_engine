package com.nbcb.factor.web.controller;

import com.nbcb.factor.web.response.Response;
import com.nbcb.factor.web.service.CurrencyValuationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 中债估值web调用接口
 */
@RestController
public class CurrencyValuationController {
    @Autowired
    private CurrencyValuationService currencyValuationService;

    /**
     * 创建今日中债贵物化视图
     */
    @RequestMapping("/createMaterializedView")
    public Response createMaterializedView() {
        return new Response(currencyValuationService.createMaterializedView());
    }

    /**
     * 删除今日中债估值物化视图
     */
    @RequestMapping("/deleteMaterializedView")
    public Response deleteMaterializedView() {
        return new Response(currencyValuationService.deleteMaterializedView());
    }
    /**
     * 创建今日中债估值物化视图索引
     */
    @RequestMapping("/createMaterializedViewIndex")
    public Response createMaterializedViewIndex() {
        return new Response(currencyValuationService.createMaterializedViewIndex());
    }
}
