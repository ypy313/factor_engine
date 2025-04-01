package com.changtian.factor.web.service;

import cn.hutool.http.HttpRequest;
import com.alibaba.druid.util.StringUtils;
import com.changtian.factor.common.LoadConfig;
import com.changtian.factor.common.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Properties;

import static com.changtian.factor.common.RedisConstant.NEBULA_X_CLIENT_TOKEN;
import static com.changtian.factor.common.RedisConstant.NEBULA_X_CLIENT_TOKEN_KEY;

/**
 * fds service
 */
@Slf4j
@Service
public class FdsDataInterFaceService {
    /**
     * 获取现金流数据
     */
    public String getIrsCashFlow(String message){
        log.info("调用星云请求报文-fds现金流计算：{}",message);
        if (StringUtils.isEmpty(message)) {
            log.error("调用星云请求报文-参数为空");
            return "调用星云请求报文-参数为空";
        }
        RedisUtil redisUtil = RedisUtil.getInstance();
        //获取token
        String token = redisUtil.getString(NEBULA_X_CLIENT_TOKEN,NEBULA_X_CLIENT_TOKEN_KEY);
        if (StringUtils.isEmpty(token)) {
            log.error("调用星云请求报文-tokenW为空");
            return "调用星云请求报文-token为空";
        }
        //获取请求路径
        Properties configProp = LoadConfig.getProp();
        String baseUtl = configProp.getProperty("nebula.request.api.url");
        String caseFlowUrl = configProp.getProperty("nebula.request.api.case.fds.flow.url");
        if (StringUtils.isEmpty(baseUtl) || StringUtils.isEmpty(caseFlowUrl)) {
            log.error("调用星云请求报文-config url为空");
            return "调用星云请求报文-请求地址为空";
        }
        //调用星云获取结果
        String resultStr = HttpRequest.post(baseUtl+caseFlowUrl)
                .header("x-client-token",token)
                .body(message).execute().body();
        log.info("调用星云返回-fds现金流计算：{}",resultStr);
        if (StringUtils.isEmpty(resultStr)) {
            log.error("调用星云返回-fds现金流计算失败");
            return "调用星云返回-fds现金流计算失败";
        }
        return resultStr;
    }
}
