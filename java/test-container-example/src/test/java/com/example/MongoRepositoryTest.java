package com.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class MongoRepositoryTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    static MongoClient client;
    static MongoRepository repository;

    @BeforeAll
    static void setUp() {
        client = MongoClients.create(MONGO.getConnectionString());
        repository = new MongoRepository(client, "testdb");
    }

    @AfterAll
    static void tearDown() {
        client.close();
    }

    @Test
    void savesAndFindsUser() {
        repository.save("u1", "Alice");

        assertEquals("Alice", repository.findNameById("u1").orElseThrow());
        assertEquals(1, repository.count());
    }

    @Test
    void returnsEmptyForUnknownUser() {
        assertTrue(repository.findNameById("missing").isEmpty());
    }
}
