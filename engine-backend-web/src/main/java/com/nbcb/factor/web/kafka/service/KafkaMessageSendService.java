package com.nbcb.factor.web.kafka.service;

/**
 * 向kafka发送信息
 */
public interface KafkaMessageSendService {
    /**
     * 向kafka发送消息
     */
    void sendMessage(String topic,String message);
    /**
     * 异步向kafka发送消息
     */
    void asynSendMessage(String topic,String message);
}
