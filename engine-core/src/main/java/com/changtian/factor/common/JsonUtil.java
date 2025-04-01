package com.changtian.factor.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * json工具类
 */
@Slf4j
public class JsonUtil {
    private static final String DEFAULT_DATE_FORMAT = "yyyyMMdd HH:mm:ss";
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        return mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT))
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(DeserializationFeature.USE_LONG_FOR_INTS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 把字符串转出对象
     */
    public static <T> T toObject(String json,Class<T> type) throws JsonProcessingException {
        if (null == json ) return null;
        return OBJECT_MAPPER.readValue(json,type);
    }

    /**
     * 把字符串转出对象
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) throws JsonProcessingException{
        if (null == json ) return  null;

        return OBJECT_MAPPER.readValue(json,typeReference);
    }

    @NonNull
    public static <T> List<T> toList(String json, CollectionType type){
        if (json == null || json.isEmpty()) {
            return new ArrayList<>(0);
        }
        try{
            return OBJECT_MAPPER.readValue(json,type);
        }catch (JsonProcessingException e){
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> List<T> toList(String json,Class<T> elementClass){
        return toList(json,constructCollectionType(List.class,elementClass));
    }

    public static CollectionType constructCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass){
        return getTypeFactory().constructCollectionType(collectionClass,elementClass);
    }

    public static TypeFactory getTypeFactory(){
        return TypeFactory.defaultInstance();
    }

    /**
     * 对象转出字符串
     */
    public static String toJson(Object obj) throws JsonProcessingException{
        if (null == obj) return  null;
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    public static String safeToJson(Object obj){
        try{
            return toJson(obj);
        }catch (JsonProcessingException e){
            log.error("serial failed",e);
            return null;
        }
    }
}
