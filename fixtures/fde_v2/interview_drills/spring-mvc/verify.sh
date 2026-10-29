#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
SCRATCH="$(mktemp -d "${TMPDIR:-/tmp}/spring-mvc-drill.XXXXXX")"
trap 'rm -rf "$SCRATCH"' EXIT
mkdir -p "$SCRATCH/starter" "$SCRATCH/reference"
cp "$ROOT/pom.xml" -t "$SCRATCH/starter"
cp -R "$ROOT/src" "$SCRATCH/starter/src"

set +e
(cd "$SCRATCH/starter" && "$ROOT/mvnw" -Dmaven.repo.local="$ROOT/.mvn/repository" test) >"$SCRATCH/starter.log" 2>&1
starter_rc=$?
set -e
if [[ $starter_rc -eq 0 ]] || ! grep -Fq 'Failures: 1' "$SCRATCH/starter.log" || ! grep -Fq 'invalidBodyReturnsStableFieldErrorBeforeServiceInvocation' "$SCRATCH/starter.log"; then
  cat "$SCRATCH/starter.log" >&2
  echo "starter did not fail at the intended validation-response assertion" >&2
  exit 1
fi

cp "$ROOT/pom.xml" -t "$SCRATCH/reference"
cp -R "$ROOT/src" "$SCRATCH/reference/src"
cp "$ROOT/reference/src/main/java/dev/coverstory/drills/PilotControllerAdvice.java" \
  "$SCRATCH/reference/src/main/java/dev/coverstory/drills/PilotControllerAdvice.java"
(cd "$SCRATCH/reference" && "$ROOT/mvnw" -Dmaven.repo.local="$ROOT/.mvn/repository" test)
