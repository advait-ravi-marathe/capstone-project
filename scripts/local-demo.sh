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
export COVERSTORY_AI_PORT="$(python3 -c 'import socket; s=socket.socket(); s.bind(("127.0.0.1", 0)); print(s.getsockname()[1]); s.close()')"
ai_service_pid=""

cleanup() {
  if [[ -n "$ai_service_pid" ]]; then
    kill "$ai_service_pid" >/dev/null 2>&1 || true
    wait "$ai_service_pid" >/dev/null 2>&1 || true
  fi
  docker compose -p "$project_name" -f compose.local.yaml down --volumes --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT INT TERM

docker compose -p "$project_name" -f compose.local.yaml up -d --wait postgres
published_port="$(docker compose -p "$project_name" -f compose.local.yaml port postgres 5432 | awk -F: '{print $NF}')"
export COVERSTORY_DB_URL="jdbc:postgresql://127.0.0.1:${published_port}/coverstory_local"
python3 -m ai.coverstory_assistant.server --host 127.0.0.1 --port "$COVERSTORY_AI_PORT" >/dev/null 2>&1 &
ai_service_pid="$!"
for attempt in $(seq 1 40); do
  if python3 -c 'import sys,urllib.request; urllib.request.urlopen(sys.argv[1], timeout=0.25).read()' \
      "http://127.0.0.1:${COVERSTORY_AI_PORT}/health" >/dev/null 2>&1; then
    break
  fi
  if [[ "$attempt" -eq 40 ]]; then
    echo "Offline AI fixture service did not become ready on loopback port ${COVERSTORY_AI_PORT}." >&2
    exit 1
  fi
  sleep 0.1
done
echo "CoverStory local AI demo: http://127.0.0.1:${COVERSTORY_APP_PORT}/#/ai-first/coverstory"
java -jar "$jar_file" --spring.profiles.active=local --server.address=127.0.0.1 \
  --server.port="$COVERSTORY_APP_PORT" \
  --coverstory.ai.service-url="http://127.0.0.1:${COVERSTORY_AI_PORT}"
