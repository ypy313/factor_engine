package com.nbcb.factor.web.kafka.service.impl;

import com.nbcb.factor.web.kafka.service.KafkaMessageSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 向kafka发送消息实现
 */
@Slf4j
@Service(value = "kafkaMessageSendService")
public class KafkaMessageSendServiceImpl implements KafkaMessageSendService {
    private final KafkaTemplate<String,String> kafkaTemplate;
    @Autowired
    public KafkaMessageSendServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 向kafka发送消息
     * @param topic topic
     * @param message 消息
     */
    @Override
    public void sendMessage(String topic, String message) {
        log.info("[Send kafka message] - topic :{},message:{}",topic,message);
        this.kafkaTemplate.send(topic,message);
    }

    /**
     * 异步向kafka发送消息
     * @param topic topic
     * @param message 消息
     */
    @Override
    @Async
    public void asynSendMessage(String topic, String message) {
        log.info("[Send kafka message] - topic :{},message:{}",topic,message);
        this.kafkaTemplate.send(topic,message);
    }
}
