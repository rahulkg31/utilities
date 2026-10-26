package com.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class JdbcRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static JdbcRepository repository;

    @BeforeAll
    static void setUp() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(POSTGRES.getJdbcUrl());
        dataSource.setUser(POSTGRES.getUsername());
        dataSource.setPassword(POSTGRES.getPassword());

        repository = new JdbcRepository(dataSource);
        repository.createSchema();
    }

    @Test
    void savesAndFindsOrder() {
        long id = repository.save("alice", new BigDecimal("19.99"));

        JdbcRepository.Order order = repository.findById(id).orElseThrow();
        assertEquals("alice", order.customer());
        assertEquals(new BigDecimal("19.99"), order.amount());
    }

    @Test
    void findsOrdersByCustomer() {
        repository.save("bob", new BigDecimal("5.00"));
        repository.save("bob", new BigDecimal("7.50"));

        List<JdbcRepository.Order> orders = repository.findByCustomer("bob");

        assertEquals(2, orders.size());
        assertEquals(new BigDecimal("5.00"), orders.get(0).amount());
        assertEquals(new BigDecimal("7.50"), orders.get(1).amount());
    }

    @Test
    void deletesOrder() {
        long id = repository.save("carol", new BigDecimal("1.00"));

        assertTrue(repository.deleteById(id));
        assertFalse(repository.findById(id).isPresent());
        assertFalse(repository.deleteById(id));
    }
}
