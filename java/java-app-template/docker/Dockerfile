ARG IMAGE_NAME=eclipse-temurin
ARG IMAGE_TAG=17-jre-jammy

ARG APP_NAME=demo
ARG APP_VERSION=1.0.0
ARG APP_ARTIFACT=${APP_NAME}-${APP_VERSION}

ARG UID=1000
ARG GID=1000

# ------------------------------------------------------------------
# Stage 1: assemble the app layout
# ------------------------------------------------------------------
FROM debian:12-slim AS stage

ARG APP_ARTIFACT

RUN mkdir -p /app/lib /app/conf /app/db /app/data /app/logs

COPY lib/ /app/lib/
COPY conf/ /app/conf/
COPY db/ /app/db/

RUN mv /app/lib/${APP_ARTIFACT}.jar /app/lib/app.jar

# ------------------------------------------------------------------
# Stage 2: runtime
# ------------------------------------------------------------------
FROM ${IMAGE_NAME}:${IMAGE_TAG}

ARG UID
ARG GID

COPY --from=stage --chown=${UID}:${GID} /app /app

WORKDIR /app
USER ${UID}:${GID}
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.config.location=./conf/", "-jar", "./lib/app.jar"]


