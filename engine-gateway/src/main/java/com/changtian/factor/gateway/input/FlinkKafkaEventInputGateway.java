package com.changtian.factor.gateway.input;

import com.changtian.factor.event.BondInputEvent;
import com.changtian.factor.event.EventFactory;
import com.changtian.factor.job.Job;
import com.changtian.factor.job.engine.AsyncEventEngine;
import com.changtian.factor.job.gateway.EventInputGateway;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

/**
 * 通过flink kafka connector 获取行情
 */
public class FlinkKafkaEventInputGateway implements EventInputGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    FlinkKafkaConsumer<String> kafkaConsumer = null;
    StreamExecutionEnvironment env = null;

    @Override
    public void init(Properties para) {
        Properties props = new Properties();
        String brokerList = (String) para.get("brokerList");
        String groupId = (String) para.get("consume.groupId");
        String topic = (String) para.get("consume.topics");
        List<String> topicList = Arrays.asList(topic.split(","));



        props.put(BOOTSTRAP_SERVERS_CONFIG, brokerList);
        props.put(GROUP_ID_CONFIG, groupId);
        props.put(ENABLE_AUTO_COMMIT_CONFIG, para.getProperty("consume.enable.auto.commit","true"));
        props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG,para.getProperty("consume.auto.commit.interval.ms", "5000"));
        props.put(AUTO_OFFSET_RESET_CONFIG,para.getProperty("consume.auto.offset.reset","latest"));
        props.put(MAX_POLL_RECORDS_CONFIG,para.getProperty("consume.max.poll.records","50"));//一次最大记录数
        props.put(MAX_POLL_INTERVAL_MS_CONFIG,para.getProperty("consume.max.poll.interval.ms", "864000"));//最长处理事件
        kafkaConsumer = new FlinkKafkaConsumer<>(topicList, new SimpleStringSchema(), props);
    }

    /**
     * 获取消费者
     * @return
     */
    public FlinkKafkaConsumer getConsumer(){
        kafkaConsumer.setStartFromLatest();//最新一条开始消费
        kafkaConsumer.setStartFromGroupOffsets();
        //kafkaConsumer.setCommitOffsetsOnCheckpoints(true);
        return  kafkaConsumer;
    }

    @Override
    public void start(Job job) {
        if (kafkaConsumer == null){
            LOGGER.error("FlinkKafkaEventInputGateway has not been  initialized.");
            return;
        }
        kafkaConsumer.setStartFromLatest();
        kafkaConsumer.setStartFromGroupOffsets();
      //  kafkaConsumer.setCommitOffsetsOnCheckpoints(true);

        //初始化env
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setBufferTimeout(1);
        env.disableOperatorChaining();//禁用算子链，多线程提高效率。一个算子链在一个线程中
        //env.enableCheckpointing(1000*60*10);// 失败时自动拉起
        DataStream<String> kafkaStream = env.addSource(kafkaConsumer).setParallelism(1);

        AsyncEventEngine asyncEventEngine = (AsyncEventEngine) job.getEventEngine();
        SingleOutputStreamOperator<Tuple1<BondInputEvent>> singleOutputStreamOperator =
                kafkaStream.map(new MapFunction<String, Tuple1<BondInputEvent>>() {
                    private static final long serialVersionUID = -2344324342343L;
                    @Override
                    public Tuple1<BondInputEvent> map(String s) throws Exception {

                        BondInputEvent e = buildEvent(s) ;
                        return new Tuple1<>(e);
                    }
                });
       singleOutputStreamOperator.addSink(new EventSinkFunction(asyncEventEngine));
        try {
            env.execute("FlinkKafkaEventInputGateway");
        } catch (Exception e) {
            LOGGER.error("Exception in FlinkKafkaEventInputGateway:",e);
        }


    }

    @Override
    public void stop() {

    }

    private static BondInputEvent buildEvent(String eventStr){
        BondInputEvent event = EventFactory.create(eventStr);
        return  event;
    }
}
