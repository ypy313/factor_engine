package com.nbcb.factor.web.controller;

import com.nbcb.factor.web.response.Response;
import com.nbcb.factor.web.service.BondConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 债券配置信息controller
 */
@RestController
public class BondConfigController {
    @Autowired
    private BondConfigService bondConfigService;

    @RequestMapping("/getBondConfigDataList")
    public Response getBondConfigEntityList(){
        return new Response(bondConfigService.getBondConfigEntityList());
    }
}
