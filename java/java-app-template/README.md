# Demo Port Registry Service

Spring Boot REST service for creating, searching, assigning and deleting port registries, backed by either a JDBC database (H2 / PostgreSQL) or MongoDB.

## Tech Stack

- Java 17
- Spring Boot 4.2.0-M1 (Web, Validation, Actuator, Data JPA, Data MongoDB)
- springdoc-openapi 3.0.0 (Swagger UI)
- Log4j2 (Logback is banned by the build)
- H2 (default, local) / PostgreSQL / MongoDB
- Maven
- Testing: JUnit, Testcontainers (PostgreSQL, MongoDB), JaCoCo 0.8.12

## Project Layout

```
demo/
├── bin/                     # start/stop scripts (.sh, .bat) and demo.service (systemd)
├── docker/                  # Dockerfile-less helpers, docker compose reference files
├── k8s/                     # Kubernetes manifests
├── scripts/                 # install.sh, healthcheck.sh
├── docs/                    # API.md, CONFIGURATION.md, INSTALL.md, K8S.md
├── src/main/java/           # controllers, services, models (com.example.demo)
├── src/main/resources/      # application.yml, application-default.yml, log4j2.xml, db/
├── src/test/java/           # unit + Testcontainers integration tests
└── pom.xml
```

## Quick Config

| Property            | Env var          | Default                                   |
| ------------------- | ---------------- | ----------------------------------------- |
| server.port         | SERVER_PORT      | 8080                                      |
| db.type             | DB_TYPE          | jdbc (`jdbc` or `mongodb`)                |
| db.jdbc.type        | JDBC_TYPE        | h2 (`h2` or `postgres`)                   |
| db.jdbc.database    | JDBC_DATABASE    | file:./data/testdb;DB_CLOSE_ON_EXIT=FALSE |
| db.jdbc.username    | JDBC_USERNAME    | sa                                        |
| db.jdbc.password    | JDBC_PASSWORD    | (empty)                                   |
| db.mongodb.database | MONGODB_DATABASE | demo                                      |

See [CONFIGURATION.md](https://claude.ai/chat/docs/CONFIGURATION.md) for the full list, including PostgreSQL SSL/mTLS, MongoDB TLS, connection pool and escape-hatch settings.

## Building

### Jars and distribution

```bash
mvn clean package
```

Produces in `target/`:

| Output                       | Description                                                  |
| ---------------------------- | ------------------------------------------------------------ |
| `demo-1.0.0.jar`             | Plain jar (main class in manifest; dependencies live in `lib/`) |
| `demo-1.0.0-executable.jar`  | Executable Spring Boot fat jar                               |
| `demo-1.0.0/`                | Unpacked distribution (staging directory)                    |
| `demo-1.0.0.tar.gz` / `.zip` | Packaged distribution                                        |
| `*.sha256`                   | SHA-256 checksums for the archives                           |

The distribution (directory and archives) contains:

```
demo-1.0.0/
├── bin/        # start/stop scripts, systemd unit
├── conf/       # application.yml, application-default.yml, log4j2.xml
├── db/         # database scripts
├── docker/     # docker files and scripts (dev/build-only files excluded)
├── docs/       # documentation
├── k8s/        # Kubernetes manifests (dev-only files excluded)
├── lib/        # demo-1.0.0.jar + runtime dependency jars
├── scripts/    # install.sh, healthcheck.sh
├── README.md
└── VERSION
```

Notes:

- Skip the distribution step with `-Dskip.dist=true`.
- Every source directory (`bin`, `docker`, `k8s`, `docs`, `scripts`, `src/main/resources/db`) must exist. 
- The build fails if Logback is on the classpath (Log4j2 only).

### Docker image

The image is built from the distribution, so run `mvn clean package` first. The `docker/Dockerfile` copies `lib/`, `conf/` and `db/` from the build context, so use the unpacked distribution directory as the context:

bash

```bash
cd target/demo-1.0.0
docker build -f docker/Dockerfile -t demo:1.0.0 .
```

It uses a multi-stage build with an `eclipse-temurin:17-jre-jammy` runtime, runs as a non-root user (UID/GID 1000), and exposes port 8080.

## Running

With no configuration, the app uses a file-based H2 database under `./data/`. Configure anything else through environment variables (see [CONFIGURATION.md](https://claude.ai/chat/docs/CONFIGURATION.md)).

Once running, the service is available at `http://localhost:8080`:

- Health check: `GET /health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### Run the executable jar

```bash
java -jar target/demo-1.0.0-executable.jar
```

With PostgreSQL:

```bash
JDBC_TYPE=postgres JDBC_HOST=localhost JDBC_PORT=5432 \
JDBC_DATABASE=demo JDBC_USERNAME=postgres JDBC_PASSWORD=secret \
java -jar target/demo-1.0.0-executable.jar
```

With MongoDB:

```bash
DB_TYPE=mongodb MONGODB_HOST=localhost MONGODB_DATABASE=demo \
java -jar target/demo-1.0.0-executable.jar
```

### Run with scripts (built package)

Extract the archive (or use `target/demo-1.0.0/` directly) and use the scripts:

```bash
tar -xzf target/demo-1.0.0.tar.gz
cd demo-1.0.0

./bin/start.sh            # start the service (bin\start.bat on Windows)
./scripts/healthcheck.sh  # check it is up
./bin/stop.sh             # stop the service (bin\stop.bat on Windows)
```

Edit `conf/application.yml` or set environment variables before starting. To install as a system service (`bin/demo.service`), use `scripts/install.sh`; see [INSTALL.md](https://claude.ai/chat/docs/INSTALL.md).

### Run with Docker Compose

Compose files in `docker/` are provided for reference.

```bash
cd docker/
docker compose up -d          # start in background
docker compose down           # stop and remove containers
```

### Run on Kubernetes

See [K8S.md](https://claude.ai/chat/docs/K8S.md).

## Running Tests

```bash
mvn test       # unit tests
mvn verify     # full build + JaCoCo report (target/site/jacoco/index.html)
```

Integration tests use Testcontainers (PostgreSQL, MongoDB), so Docker must be running.

## Docs

- [CONFIGURATION.md](https://claude.ai/chat/docs/CONFIGURATION.md): all settings
- [API.md](https://claude.ai/chat/docs/API.md): endpoint reference
- [INSTALL.md](https://claude.ai/chat/docs/INSTALL.md): installing from the distribution
- [K8S.md](https://claude.ai/chat/docs/K8S.md): Kubernetes deployment
