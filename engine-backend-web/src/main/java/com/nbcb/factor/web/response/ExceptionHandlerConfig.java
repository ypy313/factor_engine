package com.nbcb.factor.web.response;

import com.nbcb.factor.web.response.exception.CalcException;
import com.nbcb.factor.web.response.exception.SqlException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandlerConfig {
    /**
     * 未知异常处理
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Response exceptionHandler(Exception e) {
        log.error("请求异常",e);
        return new Response(999,e.getMessage());
    }
    /**
     * 计算异常处理
     */
    @ExceptionHandler(value = CalcException.class)
    @ResponseBody
    public Response CalcExceptionHandler(Exception e) {
        log.error("计算异常",e);
        return new Response(998,e.getMessage());
    }
    /**
     * 数据库异常处理
     */
    @ExceptionHandler(value = SqlException.class)
    @ResponseBody
    public Response SqlExceptionHandler(Exception e) {
        log.error("sql异常",e);
        return new Response(997,e.getMessage());
    }
}
