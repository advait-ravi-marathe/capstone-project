#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" && -x "$JAVA_HOME/bin/javac" ]]; then
  export PATH="$JAVA_HOME/bin:$PATH"
fi
if ! command -v java >/dev/null || ! command -v javac >/dev/null; then
  echo "A full JDK 21 is required (java and javac)." >&2
  exit 1
fi
if ! java -version 2>&1 | rg -q 'version "21\.'; then
  echo "Java 21 is required." >&2
  exit 1
fi

npm --prefix frontend ci --cache "${NPM_CACHE_DIR:-/tmp/coverstory-npm-cache}"
npm --prefix frontend run build
npm --prefix frontend run build:local

if [[ -z "${CHROMIUM_BIN:-}" ]]; then
  for browser_name in chromium chromium-browser google-chrome google-chrome-stable; do
    if command -v "$browser_name" >/dev/null; then
      export CHROMIUM_BIN="$(command -v "$browser_name")"
      break
    fi
  done
fi
if [[ -z "${CHROMIUM_BIN:-}" ]]; then
  (cd frontend && npx playwright install chromium)
fi

maven_args=(-B -ntp)
if [[ -n "${MAVEN_SETTINGS_FILE:-}" ]]; then
  maven_args+=(-s "$MAVEN_SETTINGS_FILE")
fi
if [[ -n "${MAVEN_REPO_LOCAL:-}" ]]; then
  maven_args+=("-Dmaven.repo.local=$MAVEN_REPO_LOCAL")
fi
./mvnw "${maven_args[@]}" test
./mvnw "${maven_args[@]}" -Plocal-app -DskipTests clean package
