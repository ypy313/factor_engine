package com.changtian.factor.event;

/**
 * 顶层事件类
 */
public interface Event {
    String getEventId();//获取事件id
    String getEventName();//获取事件名称
    String getInstrumentId();//获取产生事件的id
}
