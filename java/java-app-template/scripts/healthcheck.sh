#!/usr/bin/env bash
set -euo pipefail

PORT="${SERVER_PORT:-8080}"

if curl -fsS --max-time 5 "http://localhost:${PORT}/health" \
     | grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"'; then
    echo "Service is healthy on port ${PORT}."
    exit 0
else
    echo "Service health check FAILED on port ${PORT}."
    exit 1
fi
