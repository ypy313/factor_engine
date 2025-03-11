package com.nbcb.factor.gateway.output;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

public class JsonSchemaTuple2 implements KafkaSerializationSchema<Tuple2<String,String>> {
    private static  final  long serialVersionUID = 1L;
    private final String topic;

    public JsonSchemaTuple2(String topic) {
        this.topic = topic;
    }


    @Override
    public ProducerRecord<byte[], byte[]> serialize(Tuple2<String, String> element, @Nullable Long timestamp) {
        return new ProducerRecord<>(element.f0,element.f1.getBytes(StandardCharsets.UTF_8));
    }
}
