# test-container-example

Plain Maven project (no Spring) that tests MongoDB, JDBC and Kafka code against real containers using Testcontainers.

## Requirements
- JDK 17+
- Maven 3.9+
- A running Docker daemon

## Layout
- `MongoRepository` - MongoDB repository (mongodb-driver-sync)
- `KafkaEventPublisher` / `KafkaEventConsumer` - Kafka producer and consumer (kafka-clients)
- `JdbcRepository` - plain JDBC repository for PostgreSQL (postgresql driver)
- `JdbcRepositoryTest` - starts a `postgres:16-alpine` container
- `MongoRepositoryTest` - starts a `mongo:7.0` container
- `KafkaEventsTest` - starts a `confluentinc/cp-kafka:7.6.1` container

## Run
```
mvn test
```
The first run pulls the Docker images, so it takes a bit longer.
