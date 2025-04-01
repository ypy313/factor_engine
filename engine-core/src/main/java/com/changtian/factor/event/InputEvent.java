package com.changtian.factor.event;


public interface InputEvent <T> extends Event {
    String getCreateTimestamp();//获取时间时间戳
    String getScQuoteTime();//原始报价时间 精确到毫秒
    String getSource();//获取报价源
    T getEventData();//获取事件数据
    void setInstrumentId(String instrumentId);//设置事件关联资产
}
