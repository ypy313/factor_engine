package com.nbcb.factor.web.util;

import org.springframework.cglib.beans.BeanMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用工具类
 */
public class CommonUtils {


    /**
     * 对象转map
     *
     * @param object object
     * @return 结果
     */
    public static Map<String, Object> objectToMap(Object object) {
        Map<String, Object> map = new HashMap<>();
        BeanMap beanMap = BeanMap.create(object);
        for (Object key : beanMap.keySet()) {
            map.put(key.toString(), beanMap.get(key));
        }
        return map;
    }
}
