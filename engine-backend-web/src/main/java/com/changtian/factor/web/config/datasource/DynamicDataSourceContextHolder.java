package com.changtian.factor.web.config.datasource;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据源切换处理
 */
@Slf4j
public class DynamicDataSourceContextHolder {
    /**
     * 使用ThreadLocal维护变量，
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置数据源的变量
     */
    public static void setDataSourceType(String dsType){
        log.info("切换到{}数据源",dsType);
        CONTEXT_HOLDER.set(dsType);
    }
    /**
     * 获得数据源的变量
     */
    public static String getDataSourceType(){
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清空数据源变量
     */
    public static void clearDataSourceType(){
        CONTEXT_HOLDER.remove();
    }

}
