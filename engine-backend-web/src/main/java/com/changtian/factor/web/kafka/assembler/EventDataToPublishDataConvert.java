package com.changtian.factor.web.kafka.assembler;


import org.springframework.cglib.beans.BeanCopier;
import org.springframework.core.convert.converter.Converter;

/**
 * 事件转换器
 * @param <S>
 * @param <T>
 */
public abstract class EventDataToPublishDataConvert <S,T> implements Converter<S,T> {
    private final BeanCopier copier;

    public EventDataToPublishDataConvert(Class<S> sourceClass,Class<T> targetClass) {
        this.copier = BeanCopier.create(sourceClass, targetClass, false);
    }
    @Override
    public T convert(S source) {
        T target = createTarget();
        copier.copy(source, target, null);
        return target;
    }

    protected  abstract T createTarget();
}
