# DB Migration

Liquibase migration runner for PostgreSQL/H2 (JDBC) and MongoDB, selected by Spring profile; runs once and exits.

## Tech Stack
- Java 17
- Spring Boot 4.1.1
- Liquibase 5.0.3 (`liquibase-core` + `liquibase-mongodb`, pinned together)
- PostgreSQL, H2
- MongoDB 
- HikariCP (via `spring-boot-starter-jdbc`)
- Log4j2
- Maven 3.x

## Project Layout
```
db-migration/
├── pom.xml                                  # dependencies, pinned liquibase.version
├── src/main/java/com/example/dbmigration/
│   ├── Application.java                     # entry point; exits when migration finishes
│   └── config/                              # JDBC DataSource + SpringLiquibase, Mongo Liquibase runner, properties classes
│   └── enums/                               # DB types
└── src/main/resources/
    ├── application.yml                      # shared settings (non-web app, Boot's Liquibase autoconfig off)
    ├── application-<profile>.yml            # one per target: Postgres, H2, Mongo
    └── db/changelog/
       ├── jdbc/                            # master changelog + SQL changesets (changes/)
       └── mongo/                           # master changelog + YAML changesets (changes/)

```

## Quick Config

| Property                      | Env var                   | Default / note                                      |
|-------------------------------|---------------------------|-----------------------------------------------------|
| db.type                  | DB_TYPE               | (required) `jdbc` or `mongo`                        |
| spring.profiles.active        | SPRING_PROFILES_ACTIVE    | (none), set it to the profile you want to run       |

## Running Locally

**H2 (embedded, quickest check, no setup):**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=<h2-profile>
```
Creates `./data/mydb.*`. Re-running applies nothing, which confirms Liquibase tracking works.

**PostgreSQL:** start Postgres (see below), make sure the database exists and the YAML (or env vars) use valid credentials, then:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=<jdbc-profile>
```

**MongoDB:** start Mongo (see below), then:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=<mongo-profile>
```

The process exits when the migration is done (non-zero exit code on failure).

## Building the Distribution
```bash
mvn clean package -DskipTests
```
Produces `target/db-migration-0.0.1-SNAPSHOT.jar` (executable fat jar). YAML files are packed inside the jar, so rebuild after editing them.

**JDBC (Postgres/H2)** can run directly from the fat jar:
```bash
java -jar target/db-migration-0.0.1-SNAPSHOT.jar --spring.profiles.active=<jdbc-profile>
```

**MongoDB must run from the extracted jar.** `liquibase-mongodb` fails with `nestedEntryName must not be empty` when nested inside a fat jar:
```bash
java -Djarmode=tools -jar target/db-migration-0.0.1-SNAPSHOT.jar extract --destination target/extracted
java -jar target/extracted/db-migration-0.0.1-SNAPSHOT.jar --spring.profiles.active=<mongo-profile>
```
Use the extracted layout for all environments if you want a single way of running both targets.

