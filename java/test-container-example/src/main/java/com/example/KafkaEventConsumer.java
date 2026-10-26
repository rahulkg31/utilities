package com.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Minimal Kafka consumer that polls string events. */
public class KafkaEventConsumer implements AutoCloseable {

    private final KafkaConsumer<String, String> consumer;

    public KafkaEventConsumer(String bootstrapServers, String topic, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(List.of(topic));
    }

    /** Polls until at least {@code expected} records arrive or the timeout elapses. */
    public List<ConsumerRecord<String, String>> poll(int expected, Duration timeout) {
        List<ConsumerRecord<String, String>> result = new ArrayList<>();
        long deadline = System.nanoTime() + timeout.toNanos();
        while (result.size() < expected && System.nanoTime() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            records.forEach(result::add);
        }
        return result;
    }

    @Override
    public void close() {
        consumer.close();
    }
}
