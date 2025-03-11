package com.nbcb.factor.event;

import com.nbcb.factor.common.DateUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.PipedReader;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

/**
 * 数据处理通知
 */
@Setter@Getter@ToString
public class DataProcessNoticeOutput implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TOPIC = "FACTOR_DATA_PROCESS_NOTICE";
    private final long createTimeStamp = System.currentTimeMillis();

    private String eventId;//时间id
    private String eventName;//事件名称
    private String notice="finish";//命令
    private String key = MethodHandles.lookup().lookupClass().getSimpleName();
    private String noticeTime = DateUtil.getNowstr();//通知时间
    private String noticeType;//通知类型 BASIC_DATA_PROCESS

    public String getEventId() {
        return UUID.randomUUID().toString().replace("-","");
    }
    public String getEventName() {
        return this.getKey()+"-"+"数据处理通知";
    }
    public String getTopic(){
        return TOPIC;
    }
}
