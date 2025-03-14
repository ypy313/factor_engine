package com.nbcb.factor.web.controller;

import com.nbcb.factor.entity.StrategyConfigVo;
import com.nbcb.factor.web.response.Response;
import com.nbcb.factor.web.service.StrategyInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 因子策略实例controller
 */
@RestController
public class StrategyInstanceController {
    @Autowired
    StrategyInstanceService strategyInstanceService;
    /**
     * 根据因子策略名称，获取所有本因子策略的实例配置列表，供系统初始化使用
     */
    @RequestMapping("/getStrategyInstanceConfiguration")
    public Response getStrategyInstanceConfiguration(@RequestBody StrategyConfigVo strategyConfigVo){
        //如果指定strategyName,但是无strategyInstanceName,则取因子策略的所有实例
        //如果指定strategyInstanceName,则取单个因子策略实例
        return new Response(strategyInstanceService.getConfigJson(strategyConfigVo));
    }
}
