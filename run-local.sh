#!/usr/bin/env bash
set -euo pipefail

if [ ! -f ".env" ]; then
  echo ".env not found. Copy .env.example to .env and fill values first."
  exit 1
fi

set -a
. ./.env
set +a

gradle bootRun
