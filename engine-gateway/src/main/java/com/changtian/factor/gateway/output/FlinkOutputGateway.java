package com.changtian.factor.gateway.output;

import com.changtian.factor.event.OutputEvent;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Properties;

public class FlinkOutputGateway {
    public static FlinkKafkaProducer<OutputEvent> createKafkaSinkFunction(String topicName, Properties prop){
        return new FlinkKafkaProducer<OutputEvent>(topicName,new JsonSchema(topicName),prop,
                FlinkKafkaProducer.Semantic.AT_LEAST_ONCE);
    }
}
