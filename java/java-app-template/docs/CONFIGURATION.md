# Configuration Reference

All settings live in `application.yml` and are exposed as environment variables using `${ENV_VAR:default}` placeholders. Set the env var to override the default; you rarely need to edit the file.

## Layering

Spring Boot resolves properties in this order (later wins):

`application.yml` → `application-{profile}.yml` → external config location (`--spring.config.location`) → environment variables → command-line args.

How this applies per run mode:

| Run mode                | Config source                                                |
| ----------------------- | ------------------------------------------------------------ |
| Executable jar          | Bundled `application.yml` + environment variables            |
| Built package (scripts) | `conf/application.yml`, `conf/application-default.yml`, `conf/log4j2.xml` |
| Docker image            | `/app/conf/` (set via `-Dspring.config.location=./conf/`) + environment variables |
| Kubernetes              | ConfigMap / Secret mapped to env vars or `/app/conf` (see [K8S.md](https://claude.ai/chat/K8S.md)) |

## Server

| Property    | Env var     | Default | Description                  |
| ----------- | ----------- | ------- | ---------------------------- |
| server.port | SERVER_PORT | 8080    | HTTP port the service binds. |

## Database Selection

| Property | Env var | Default | Description                                                  |
| -------- | ------- | ------- | ------------------------------------------------------------ |
| db.type  | DB_TYPE | jdbc    | Which persistence backend to use: `jdbc` (H2 / PostgreSQL / any JDBC DB) or `mongodb`. Only the matching group below is used. |

## JDBC (`db.type=jdbc`)

### Connection

| Property         | Env var       | Default                                   | Description                                                  |
| ---------------- | ------------- | ----------------------------------------- | ------------------------------------------------------------ |
| db.jdbc.type     | JDBC_TYPE     | h2                                        | `h2` or `postgres`. Only needed when `url`, `driver` and `hibernate-dialect` are not all set explicitly. |
| db.jdbc.database | JDBC_DATABASE | file:./data/testdb;DB_CLOSE_ON_EXIT=FALSE | H2: full spec (`file:./data/testdb`, `mem:testdb`). Postgres: database name only. |
| db.jdbc.username | JDBC_USERNAME | sa                                        | Database user.                                               |
| db.jdbc.password | JDBC_PASSWORD | (empty)                                   | **Always override for any non-local database; never run production with the empty default.** |

### PostgreSQL only (ignored for H2)

| Property             | Env var           | Default     | Description                                            |
| -------------------- | ----------------- | ----------- | ------------------------------------------------------ |
| db.jdbc.host         | JDBC_HOST         | localhost   | Database host.                                         |
| db.jdbc.port         | JDBC_PORT         | 5432        | Database port.                                         |
| db.jdbc.ssl-enabled  | JDBC_SSL_ENABLED  | false       | Enable TLS to the database.                            |
| db.jdbc.ssl-mode     | JDBC_SSL_MODE     | verify-full | PostgreSQL `sslmode`, used when SSL is enabled.        |
| db.jdbc.mtls-enabled | JDBC_MTLS_ENABLED | false       | Enable mutual TLS (client certificate authentication). |
| db.jdbc.root-cert    | JDBC_ROOT_CERT    | (empty)     | Path to the CA certificate used to verify the server.  |
| db.jdbc.client-cert  | JDBC_CLIENT_CERT  | (empty)     | Path to the client certificate (mTLS).                 |
| db.jdbc.client-key   | JDBC_CLIENT_KEY   | (empty)     | Path to the client private key (mTLS).                 |

### Escape hatches

Leave these empty normally. Use them for databases or setups the simple settings above don't cover.

| Property                  | Env var                | Default | Description                                                  |
| ------------------------- | ---------------------- | ------- | ------------------------------------------------------------ |
| db.jdbc.url               | JDBC_URL               | (empty) | Full JDBC URL, used verbatim if set (any JDBC database). Overrides `type`, `database`, `host`, `port`. |
| db.jdbc.driver            | JDBC_DRIVER            | (empty) | JDBC driver class. Needed only with a custom `url`.          |
| db.jdbc.hibernate-dialect | JDBC_HIBERNATE_DIALECT | (empty) | Hibernate dialect class. Needed only with a custom `url`.    |
| db.jdbc.extra-params      | JDBC_EXTRA_PARAMS      | (empty) | Extra connection parameters. H2: appended after `;`. PostgreSQL: appended after `?` or `&`. |

### Connection pool

| Property                                 | Env var                               | Default     | Description                                                  |
| ---------------------------------------- | ------------------------------------- | ----------- | ------------------------------------------------------------ |
| db.jdbc.pool.pool-name                   | JDBC_POOL_NAME                        | h2-dev-pool | Pool name shown in logs and metrics. Consider renaming when not using H2. |
| db.jdbc.pool.maximum-pool-size           | JDBC_POOL_MAXIMUM_SIZE                | 10          | Max connections this instance holds open. Tune against your database's connection limit divided by instance count. |
| db.jdbc.pool.minimum-idle                | JDBC_POOL_MINIMUM_IDLE                | 2           | Idle connections kept ready.                                 |
| db.jdbc.pool.connection-timeout-ms       | JDBC_POOL_CONNECTION_TIMEOUT_MS       | 30000       | Max wait (ms) for a connection from the pool.                |
| db.jdbc.pool.idle-timeout-ms             | JDBC_POOL_IDLE_TIMEOUT_MS             | 600000      | How long (ms) a connection may sit idle before being retired. |
| db.jdbc.pool.max-lifetime-ms             | JDBC_POOL_MAX_LIFETIME_MS             | 1800000     | Max lifetime (ms) of a connection. Keep below any database or proxy connection timeout. |
| db.jdbc.pool.leak-detection-threshold-ms | JDBC_POOL_LEAK_DETECTION_THRESHOLD_MS | 0           | Log a warning if a connection is held longer than this (ms). `0` disables detection. |

## MongoDB (`db.type=mongodb`)

Only used when `db.type=mongodb`.

| Property                       | Env var                     | Default   | Description                                             |
| ------------------------------ | --------------------------- | --------- | ------------------------------------------------------- |
| db.mongodb.url                 | MONGODB_URL                 | (empty)   | Full connection string, used verbatim if set.           |
| db.mongodb.host                | MONGODB_HOST                | localhost | Host, used when `url` is not set.                       |
| db.mongodb.port                | MONGODB_PORT                | 27017     | Port, used when `url` is not set.                       |
| db.mongodb.database            | MONGODB_DATABASE            | demo      | Database name. Always required, even when `url` is set. |
| db.mongodb.username            | MONGODB_USERNAME            | (empty)   | Database user.                                          |
| db.mongodb.password            | MONGODB_PASSWORD            | (empty)   | **Always override outside local development.**          |
| db.mongodb.ssl-enabled         | MONGODB_SSL_ENABLED         | false     | Enable TLS.                                             |
| db.mongodb.mtls-enabled        | MONGODB_MTLS_ENABLED        | false     | Enable mutual TLS (client certificate authentication).  |
| db.mongodb.keystore            | MONGODB_KEYSTORE            | (empty)   | Path to the client keystore (mTLS).                     |
| db.mongodb.keystore-password   | MONGODB_KEYSTORE_PASSWORD   | (empty)   | Keystore password.                                      |
| db.mongodb.truststore          | MONGODB_TRUSTSTORE          | (empty)   | Path to the truststore used to verify the server.       |
| db.mongodb.truststore-password | MONGODB_TRUSTSTORE_PASSWORD | (empty)   | Truststore password.                                    |
| db.mongodb.extra-params        | MONGODB_EXTRA_PARAMS        | (empty)   | Extra connection-string options appended to the URI.    |

## Examples

H2 file database (default, local development):

```bash
java -jar target/demo-1.0.0-executable.jar
```

PostgreSQL with TLS:

```bash
DB_TYPE=jdbc JDBC_TYPE=postgres \
JDBC_HOST=db.internal JDBC_DATABASE=demo \
JDBC_USERNAME=demo JDBC_PASSWORD=secret \
JDBC_SSL_ENABLED=true JDBC_SSL_MODE=verify-full JDBC_ROOT_CERT=/certs/ca.pem \
java -jar target/demo-1.0.0-executable.jar
```

MongoDB with a connection string:

```bash
DB_TYPE=mongodb \
MONGODB_URL='mongodb://user:secret@mongo.internal:27017' \
MONGODB_DATABASE=demo \
java -jar target/demo-1.0.0-executable.jar
```

## Logging

Logging uses Log4j2 only (Logback is banned by the build). Configure it in `log4j2.xml` (`src/main/resources/`, or `conf/` in the built package).

## Other Settings

The Spring Boot Actuator and springdoc (Swagger UI) use their standard Spring Boot properties, which are not overridden in `application.yml`. See the Spring Boot and springdoc documentation for `management.*` and `springdoc.*`.

## Where to Add a New Property

1. Add the property to `application.yml` using the `${ENV_VAR:default}` form, under the appropriate `db.*` or top-level group.
2. Bind it in the matching configuration class, or read it with `@Value` / `@ConfigurationProperties`.
3. If it differs for the distribution, update `conf/application.yml` handling in the build (the pom copies `application.yml`, `application-default.yml` and `log4j2.xml` into `conf/`).
4. Add a row to the appropriate table above, including the env var and default.
5. If it is security-sensitive (passwords, keys), add the "always override" warning.
6. If the setting is needed for Kubernetes, add it to the ConfigMap/Secret in `k8s/` and mention it in [K8S.md](https://claude.ai/chat/K8S.md).