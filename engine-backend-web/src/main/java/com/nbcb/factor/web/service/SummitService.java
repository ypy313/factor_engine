package com.nbcb.factor.web.service;

import cn.hutool.http.HttpRequest;
import com.nbcb.factor.common.LoadConfig;
import com.nbcb.factor.common.RedisConstant;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.web.response.Response;
import com.nbcb.factor.web.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * summit service
 */
@Slf4j
@Service
public class SummitService {
    /**
     * 获取现金流数据
     */
    public Response getIrsCashFlow(String message){
        log.info("调用星云请求报文：{}",message);
        if (StringUtils.isEmpty(message)) {
            log.error("message is null");
            return new Response(999,"message is null");
        }
        RedisUtil redisUtil = RedisUtil.getInstance();
        //获取token
        String token = redisUtil
                .getString(RedisConstant.NEBULA_X_CLIENT_TOKEN,
                        RedisConstant.NEBULA_X_CLIENT_TOKEN_KEY);
        if (StringUtils.isEmpty(token)) {
            log.error("token is null");
            return new Response(999,"token is null");
        }
        //获取请求路径
        Properties configProp = LoadConfig.getProp();
        String baseUrl = configProp.getProperty("nebula.request.api.url");
        String caseFlowUrl = configProp.getProperty("nebula.request.api.case.flow.url");
        if (StringUtils.isEmpty(baseUrl) || StringUtils.isEmpty(caseFlowUrl)) {
            log.error("token is null");
            return  new Response(999,"config url is null");
        }
        //调用星云获取结果
        String resultStr = HttpRequest.post(baseUrl+caseFlowUrl)
                .header("x-client-token",token).body(message).execute().body();
        log.info("调用星云返回：{}",resultStr);
        if (StringUtils.isEmpty(resultStr)) {
            log.error("get case flow is null!");
            return new Response(999,"get case flow is null");
        }
        return  new Response("200",resultStr);
    }
}
