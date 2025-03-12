package com.nbcb.factor.web.response.exception;

/**
 * 数据库处理异常
 */
public class SqlException extends RuntimeException{
    public SqlException(String message) {super(message);}

    public SqlException(String message, Throwable cause) {super(message, cause);}

}
