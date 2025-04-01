package com.changtian.factor.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class JacksonUtils {
    public static final MapType MAP_TYPE;
    public static final ObjectMapper mapper;
    static {
        mapper = getObjectMapper();
        MAP_TYPE = constructMapType(HashMap.class, String.class, Object.class);
    }
    private static ObjectMapper getObjectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        return mapper;
    }
    public static TypeFactory getTypeFactory(){
        return TypeFactory.defaultInstance();
    }
    public static MapType constructMapType(Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass){
        return getTypeFactory().constructMapType(mapClass,keyClass,valueClass);
    }
    public static <T> T convertValue(Object fromValue,Class<T> valueClass){
        return mapper.convertValue(fromValue, valueClass);
    }
    public static <T> T convertValue(Object fromValue, JavaType toValueType){
        return mapper.convertValue(fromValue,toValueType);
    }
    public static CollectionType constructCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass){
        return getTypeFactory().constructCollectionType(collectionClass,elementClass);
    }

    public static <T> List<T> toList(String json,CollectionType type){
        if (json ==null || json.isEmpty()) {
            return new ArrayList<>(0);
        }
        try {
            return mapper.readValue(json,type);
        }catch (JsonProcessingException e){
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> List<T> toList(String json,Class<T> elementClass){
        return toList(json,constructCollectionType(List.class,elementClass));
    }

    /**
     * 字符串转对象
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference){
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(json,typeReference);
        }catch (JsonProcessingException e){
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 将List<String> 转化为List<T>
     */
    public static <T> List<T> convertList(List<?> sourceList,Class<T> targetType){
        List<T> targetList = new ArrayList<>();
        if (CollectionUtils.isEmpty(sourceList)) {
            return targetList;
        }
        for (Object o : sourceList) {
            T converted = convertValue(o,targetType);
            targetList.add(converted);
        }
        return targetList;
    }

}
