package com.nbcb.factor.web.controller;

import com.nbcb.factor.web.response.Response;
import com.nbcb.factor.web.service.SummitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 与summit交互web调用接口
 */
@RestController
public class SummitController {
    @Autowired
    private SummitService summitService;

    /**
     * 获取irs现金流
     */
    @RequestMapping("/getIrsCashFlow")
    public Response getIrsCashFlow(@RequestBody String message){
        return summitService.getIrsCashFlow(message);
    }
}
