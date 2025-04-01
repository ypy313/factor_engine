package com.changtian.factor.web.response.exception;

/**
 * 计算异常处理类
 */
public class CalcException extends RuntimeException {
    public CalcException(String message){
        super(message);
    }

    public CalcException(String message, Throwable cause){
        super(message,cause);
    }
}
