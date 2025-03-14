package com.nbcb.factor.web.controller;

import com.nbcb.factor.web.service.FdsDataInterFaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 与fds交互web调用接口
 */
@RestController
public class FdsDataInterfaceController {
    @Autowired
    private FdsDataInterFaceService fdsDataInterFaceService;

    /**
     * 获取irs现金流
     */
    @RequestMapping("/getIrsCashFlowToFds")
    public String getIrsCashFlowToFds(@RequestBody String message) {
        return fdsDataInterFaceService.getIrsCashFlow(message);
    }
}
