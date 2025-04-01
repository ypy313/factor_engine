package com.changtian.factor.web.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 用于xxljob异常日志
 */
public class ExceptionUtils {
    /**
     * 异常转字符串
     * @param e 异常
     * @return 字符串
     */
    public static String toString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
