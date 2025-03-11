package com.nbcb.factor.common;

import org.apache.commons.lang3.StringUtils;

/**
 * 跟踪工具
 */
public class TraceUtil {
    /**
     * 行情跟踪是否打开
     */
    public static boolean isTrace(){
        String trace_mode = LoadConfig.getProp().getProperty("trace_mode");
        if (StringUtils.equals(trace_mode,"yes")){
            return  true;
        }else
        {
            return  false;
        }
    }
}
