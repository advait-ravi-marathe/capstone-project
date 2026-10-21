#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"
jar_file="target/capstone-starter-1.0-SNAPSHOT-sprint5.jar"
evidence_file="target/sprint5-demo/package-evidence.json"
if [[ ! -s "$jar_file" || ! -s "$evidence_file" ]]; then
  echo "Run scripts/package-sprint5-demo.sh to create a fresh Sprint 5 archive first." >&2
  exit 1
fi
if ! command -v docker >/dev/null || ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose is required for this disposable local demonstration." >&2
  exit 1
fi

expected_head="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["sourceCommit"])' "$evidence_file")"
expected_source="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["sourceContentSha256"])' "$evidence_file")"
expected_artifact="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["artifactSha256"])' "$evidence_file")"
actual_source="$(python3 scripts/sprint5-source-fingerprint.py)"
actual_artifact="$(sha256sum "$jar_file" | cut -d' ' -f1)"
[[ "$(git rev-parse HEAD)" == "$expected_head" \
   && "$actual_source" == "$expected_source" && "$actual_artifact" == "$expected_artifact" ]] || {
  echo "Source or archive changed since the recorded Sprint 5 package; rebuild before running." >&2
  exit 1
}

project_name="coverstory-sprint5-$$"
app_pid=""
export COVERSTORY_DB_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export COVERSTORY_DB_USER=coverstory
export COVERSTORY_APP_PORT="$(python3 -c 'import socket; s=socket.socket(); s.bind(("127.0.0.1", 0)); print(s.getsockname()[1]); s.close()')"
cleanup() {
  if [[ -n "$app_pid" ]] && kill -0 "$app_pid" 2>/dev/null; then
    kill -TERM "$app_pid" 2>/dev/null || true
    wait "$app_pid" 2>/dev/null || true
  fi
  docker compose -p "$project_name" -f compose.local.yaml down --volumes --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

docker compose -p "$project_name" -f compose.local.yaml up -d --wait postgres
published_port="$(docker compose -p "$project_name" -f compose.local.yaml port postgres 5432 | awk -F: '{print $NF}')"
export COVERSTORY_DB_URL="jdbc:postgresql://127.0.0.1:${published_port}/coverstory_local"
echo "source=$expected_head"
echo "artifact_sha256=$actual_artifact"
java -jar "$jar_file" --spring.profiles.active=local,sprint5-local \
  --server.address=127.0.0.1 --server.port="$COVERSTORY_APP_PORT" &
app_pid=$!
echo "pid=$app_pid loopback=http://127.0.0.1:${COVERSTORY_APP_PORT}/ profile=sprint5-local"
wait "$app_pid"
