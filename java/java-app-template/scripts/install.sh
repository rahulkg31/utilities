#!/usr/bin/env bash

set -euo pipefail
cd "$(dirname "$0")/.."

mkdir -p logs/ data/ config/certs

echo "Install complete."
echo "Next: ./bin/start.sh"
