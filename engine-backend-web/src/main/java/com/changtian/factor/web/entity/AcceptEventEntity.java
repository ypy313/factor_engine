package com.changtian.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
@Getter
@Setter
@ToString
public class AcceptEventEntity<T> implements Serializable ,Cloneable{
    private String eventId;//事件id
    private String eventName;//事件名称
    private String srcTimestamp;//来源时间
    private String pkKey;//唯一id
    private String resTimestamp;//接收行情时间
    private String createTime;
    private String updateTime;//更新时间
    private String resultType;//结果类型
    private String instrumentId;
    private long count;//日内产生个数
    private String compressFlag;//压缩标记 0未压缩 1已经压缩
    private T eventData;//事件数据
}
