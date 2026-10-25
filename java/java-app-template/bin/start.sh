#!/usr/bin/env bash

set -euo pipefail
cd "$(dirname "$0")/.."

APP_NAME="${APP_NAME:-demo}"
APP_VERSION="${APP_VERSION:-1.0.0}"

APP_HOME="$(pwd)"
JAR="$APP_HOME/lib/${APP_NAME}-${APP_VERSION}.jar"
CONFIG_DIR="$APP_HOME/conf"
PID_FILE="$APP_HOME/${APP_NAME}.pid"

if [ ! -f "$JAR" ]; then
    echo "ERROR: $JAR not found" >&2
    exit 1
fi

if [ ! -d "$CONFIG_DIR" ]; then
    echo "ERROR: config directory $CONFIG_DIR not found" >&2
    exit 1
fi

if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    echo "$APP_NAME is already running (PID $(cat "$PID_FILE"))."
    exit 1
fi

echo "Starting the service..."
nohup java -Dspring.config.location="$CONFIG_DIR/" \
    -jar "$JAR" \
    > /dev/null 2>&1 &

echo $! > "$PID_FILE"
echo "Service started with PID $(cat "$PID_FILE")."
