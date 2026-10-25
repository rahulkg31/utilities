#!/usr/bin/env bash
# Stops the application started via start.sh.
set -euo pipefail
cd "$(dirname "$0")/.."

APP_NAME="${APP_NAME:-demo}"

PID_FILE="$(pwd)/${APP_NAME}.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "No PID file found for $APP_NAME — is the service running?"
    exit 1
fi

PID="$(cat "$PID_FILE")"
if kill -0 "$PID" 2>/dev/null; then
    echo "Stopping the service (PID $PID)..."
    kill "$PID"
    rm -f "$PID_FILE"
    echo "Stopped."
else
    echo "Process $PID not running. Removing stale PID file."
    rm -f "$PID_FILE"
fi

