package com.nbcb.factor.web.controller;

import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.web.util.StringUtils;
import com.sun.org.apache.regexp.internal.RE;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * redis操作接口
 */
@Slf4j
@RestController
public class RedisLinkDateController {
    private static RedisUtil redisUtil = RedisUtil.getInstance();
    /**
     * 查询redis接口
     */
    @RequestMapping("/selectRedisValueByKey")
    public Object selectRedisValueByKey(@RequestParam(required = false,value = "key")String key,
                                        @RequestParam(required = false,value = "hashKey")String hashKey,
                                        @RequestParam(value = "token") String token) throws Exception{
        //token校验，约定为当前日期后3天加*
        if (!tokenDate(token)) {
            throw new Exception("token 校验异常");
        }
        //查询redis
        if (StringUtils.isNotEmpty(key) && StringUtils.isEmpty(hashKey)) {
            Map<String,String> string = redisUtil.getString(key);
            return string.toString();
        }
        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(hashKey)) {
            String string = redisUtil.getString(key,hashKey);
            return string;
        }
        return null;
    }

    /**
     * 删除redis接口
     */
    @RequestMapping("/redisDByKey")
    public Object redisDelByKey(@RequestParam(required = false,value = "key")String key,
                                @RequestParam(required = false,value = "hashKey")String hashKey,
                                @RequestParam(value = "token") String token) throws Exception{
        boolean aBoolean = tokenDate(token);
        //token校验，约定为当前日期后3天加*
        if (!aBoolean) {
            throw new Exception("token校验异常");
        }
        if (StringUtils.isNotEmpty(key) && StringUtils.isEmpty(hashKey)) {
            return redisUtil.delString(key);
        }
        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(hashKey)) {
            return redisUtil.delKeyString(key,hashKey);
        }
        return null;
    }

    /**
     * token校验
     */
    public boolean tokenDate(String token){
        //token校验，约定为当前日期后3天yyyyMMdd
        long time = new Date().getTime()+3*24*60*60*1000l;
        Date date = DateUtil.transferLongToDate(DateUtil.DAYSTR,time);
        String dateInfo = DateUtil.format(date,DateUtil.DAYSTR)+"*";
        if (!dateInfo.equals(token)) {
            log.error("token错误");
            return false;
        }
        return true;

    }
}
