package com.nbcb.factor.gateway.input;

import com.nbcb.factor.common.FxLoadConfig;
import com.nbcb.factor.job.Job;
import com.nbcb.factor.job.gateway.EventInputGateway;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @description：
 * @author： yanpy
 * @create： 2024/7/17 10:42
 */
public class FlinkFxKafkaEventInputGateway implements EventInputGateway {
    FlinkKafkaConsumer<Tuple2<String, String>> kafkaConsumer = null;

    @Override
    public void init(Properties para) {
        Properties props = new Properties();
        String brokerList = para.getProperty(FxLoadConfig.BROKER_LIST);
        String groupId = para.getProperty("consume.groupId");
        String topics = para.getProperty("consume.topics");
        List<String> topicList = Arrays.asList(topics.split(","));

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, para.getProperty("consume.enable.auto.commit", "true"));
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, para.getProperty("consume.auto.commit.interval.ms", "5000"));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, para.getProperty("consume.auto.offset.reset", "latest"));
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, para.getProperty("consume.max.poll.records", "50"));//一次最大记录数
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, para.getProperty("consume.max.poll.interval.ms", "86400"));//最长处理时间一天
        new FlinkKafkaConsumer<>(topicList, new CustomKafkaDeserializationSchema(), props);
    }

    /**
     * 获取消费着
     * @return
     */
    public FlinkKafkaConsumer<Tuple2<String, String>> getConsumer(){
        kafkaConsumer.setStartFromLatest();//最新一条开始消费
        kafkaConsumer.setStartFromGroupOffsets();
        return kafkaConsumer;
    }
    @Override
    public void start(Job job) {

    }

    @Override
    public void stop() {

    }
}
