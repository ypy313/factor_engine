package com.nbcb.factor.job;

import cn.hutool.core.util.ClassUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加载配置工厂类型
 */
@Getter
@Setter
@Slf4j
public class LoadConfigFactory {
    private Map<String, LoadConfigHandler<?> > handlerMap = new ConcurrentHashMap<>();

    public LoadConfigFactory(){
        Set<Class<?>> classeSet = ClassUtil.scanPackageBySuper(LoadConfigHandler.class.getPackage().getName(),LoadConfigHandler.class);
        for(Class<?> clazz : classeSet){
            try{
                LoadConfigHandler<?> loadConfigHandler = (LoadConfigHandler<?>) clazz.newInstance();
                handlerMap.put(loadConfigHandler.strategyName(),loadConfigHandler);
            }catch (Exception e){
                log.error("实例化配置工厂失败",e);
            }
        }
    }
}
