package com.changtian.factor.gateway.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.changtian.factor.common.JsonUtil;
import com.changtian.factor.event.OutputEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

@Slf4j
public class JsonSchema implements KafkaSerializationSchema<OutputEvent> {
    private final String topic;
    public JsonSchema(String topic) {
        this.topic = topic;
    }
    @Override
    public ProducerRecord<byte[], byte[]> serialize(OutputEvent outputEvent, @Nullable Long aLong) {
        String json = "";
        try{
            json = JsonUtil.toJson(outputEvent);
        }catch (JsonProcessingException e){
            log.error("error in JsonSchema",e);
        }
        return new ProducerRecord<>(topic,json.getBytes(StandardCharsets.UTF_8));
    }
}
