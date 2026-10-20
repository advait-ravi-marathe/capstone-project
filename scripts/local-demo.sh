#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"
jar_file="target/capstone-starter-1.0-SNAPSHOT.jar"
if [[ ! -f "$jar_file" ]]; then
  echo "Packaged app not found. Run scripts/package-local-demo.sh first." >&2
  exit 1
fi
if ! command -v docker >/dev/null; then
  echo "Docker is required to start the disposable PostgreSQL container." >&2
  exit 1
fi

project_name="coverstory-local-$$"
export COVERSTORY_DB_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export COVERSTORY_DB_USER=coverstory
export COVERSTORY_APP_PORT="$(python3 -c 'import socket; s=socket.socket(); s.bind(("127.0.0.1", 0)); print(s.getsockname()[1]); s.close()')"

cleanup() {
  docker compose -p "$project_name" -f compose.local.yaml down --volumes --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT INT TERM

docker compose -p "$project_name" -f compose.local.yaml up -d --wait postgres
published_port="$(docker compose -p "$project_name" -f compose.local.yaml port postgres 5432 | awk -F: '{print $NF}')"
export COVERSTORY_DB_URL="jdbc:postgresql://127.0.0.1:${published_port}/coverstory_local"
echo "CoverStory local app: http://127.0.0.1:${COVERSTORY_APP_PORT}/"
java -jar "$jar_file" --spring.profiles.active=local --server.address=127.0.0.1 --server.port="$COVERSTORY_APP_PORT"
