package com.example;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class KafkaEventsTest {

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Test
    void publishedEventIsConsumed() {
        String topic = "user-events";

        try (KafkaEventConsumer consumer = new KafkaEventConsumer(KAFKA.getBootstrapServers(), topic, "test-group");
             KafkaEventPublisher publisher = new KafkaEventPublisher(KAFKA.getBootstrapServers(), topic)) {

            publisher.publish("u1", "user-created");

            List<ConsumerRecord<String, String>> records = consumer.poll(1, Duration.ofSeconds(30));

            assertEquals(1, records.size());
            assertEquals("u1", records.get(0).key());
            assertEquals("user-created", records.get(0).value());
        }
    }
}
