package com.nbcb.factor.web.util;

public class StringUtils {
    /**
     * 判断一个对象是否非空
     * @param object  object
     * @return true:非空 false:空
     */
    public static boolean isNotNull(Object object){
        return !isNull(object);
    }

    /**
     * 判断一个对象是否为空
     * @param object object
     * @return true:非空 false:空
     */
    public static boolean isNull(Object object){
        return object == null;
    }
}
