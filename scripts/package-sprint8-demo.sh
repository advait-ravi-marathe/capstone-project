#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

build_mode="clean"
if [[ "${1:-}" == "--no-clean" ]]; then
  build_mode="incremental"
elif [[ $# -gt 0 ]]; then
  echo "Usage: $0 [--no-clean]" >&2
  exit 2
fi

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" && -x "$JAVA_HOME/bin/javac" ]]; then
  export PATH="$JAVA_HOME/bin:$PATH"
fi
if ! command -v java >/dev/null || ! command -v javac >/dev/null \
    || ! java -version 2>&1 | grep -Eq 'version "21\.'; then
  echo "A full JDK 21 is required for the Sprint 8 package." >&2
  exit 1
fi

npm --prefix frontend run build:local
maven_args=(-B -ntp)
if [[ -n "${MAVEN_SETTINGS_FILE:-}" ]]; then maven_args+=(-s "$MAVEN_SETTINGS_FILE"); fi
if [[ -n "${MAVEN_REPO_LOCAL:-}" ]]; then maven_args+=("-Dmaven.repo.local=$MAVEN_REPO_LOCAL"); fi
if [[ "$build_mode" == "clean" ]]; then
  ./mvnw "${maven_args[@]}" -Plocal-app,sprint8-local -DskipTests clean package
else
  ./mvnw "${maven_args[@]}" -Plocal-app,sprint8-local -DskipTests package
fi

jar_file="target/capstone-starter-1.0-SNAPSHOT-sprint8.jar"
[[ -s "$jar_file" ]] || { echo "Fresh Sprint 8 archive is missing: $jar_file" >&2; exit 1; }
manifest="$(unzip -p "$jar_file" META-INF/MANIFEST.MF | tr -d '\r')"
grep -q '^Start-Class: assignments.sprint8.Sprint8LocalApplication' <<<"$manifest" \
  || { echo "Unexpected Sprint 8 Start-Class." >&2; exit 1; }
jar tf "$jar_file" | grep -q '^BOOT-INF/classes/static/index.html$' \
  || { echo "Packaged UI entry is missing." >&2; exit 1; }
jar tf "$jar_file" | grep -q '^BOOT-INF/classes/static/assets/' \
  || { echo "Packaged UI assets are missing." >&2; exit 1; }

head_sha="$(git rev-parse HEAD)"
tree_status="$(git status --porcelain --untracked-files=all)"
source_sha="$(python3 scripts/sprint8-source-fingerprint.py)"
jar_sha="$(sha256sum "$jar_file" | cut -d' ' -f1)"
manifest_sha="$(printf '%s' "$manifest" | sha256sum | cut -d' ' -f1)"
mkdir -p target/sprint8-demo
cat > target/sprint8-demo/package-evidence.json <<EOF
{
  "sourceCommit": "$head_sha",
  "workingTreeStatus": "$(if [[ -z "$tree_status" ]]; then printf clean; else printf dirty; fi)",
  "sourceContentSha256": "$source_sha",
  "artifact": "$jar_file",
  "artifactSha256": "$jar_sha",
  "manifestSha256": "$manifest_sha",
  "startClass": "assignments.sprint8.Sprint8LocalApplication",
  "flywayTarget": 4,
  "buildMode": "$build_mode",
  "builtAtUtc": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF
cat target/sprint8-demo/package-evidence.json
