#!/usr/bin/env bash
set -euo pipefail

image='postgres:18.6@sha256:74935e72241653ca55e0414067e6d8763aceb8a810eb51b452253ec3dcfc4336'
output="${1:-labs/runtime_sql/fixtures/observation.json}"
repo_root="$(cd "$(dirname "$0")/.." && pwd)"
if [[ "$output" = /* ]]; then output_path="$output"; else output_path="$repo_root/$output"; fi
mkdir -p "$(dirname "$output_path")"
container_id=''
cleanup() { if [[ -n "$container_id" ]]; then docker rm -f "$container_id" >/dev/null 2>&1 || true; fi; }
trap cleanup EXIT
container_id="$(docker run -d --rm -e POSTGRES_HOST_AUTH_METHOD=trust -p 127.0.0.1::5432 "$image")"
port=''
for _ in $(seq 1 60); do
  port="$(docker port "$container_id" 5432/tcp | sed 's/.*://')"
  if [[ -n "$port" ]] && docker exec "$container_id" pg_isready -U postgres >/dev/null 2>&1; then break; fi
  sleep 1
done
if [[ -z "$port" ]] || ! docker exec "$container_id" pg_isready -U postgres >/dev/null 2>&1; then
  echo 'PostgreSQL 18.6 disposable container did not become ready' >&2; exit 2
fi
docker exec -i "$container_id" psql -X -v ON_ERROR_STOP=1 -U postgres -d postgres < "$repo_root/labs/runtime_sql/schema.sql" >/dev/null
plan="$(docker exec "$container_id" psql -X -A -t -v ON_ERROR_STOP=1 -U postgres -d postgres -c "EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) SELECT owner_id, policy_id, revision FROM training_policy_event WHERE owner_id = 'owner-07' AND policy_id = 'policy-0012' ORDER BY revision DESC LIMIT 5;")"
rows="$(docker exec "$container_id" psql -X -A -t -v ON_ERROR_STOP=1 -U postgres -d postgres -c "SELECT json_agg(json_build_object('owner_id', owner_id, 'policy_id', policy_id, 'revision', revision) ORDER BY revision DESC)::text FROM (SELECT owner_id, policy_id, revision FROM training_policy_event WHERE owner_id = 'owner-07' AND policy_id = 'policy-0012' ORDER BY revision DESC LIMIT 5) AS selected;")"
python3 - "$output_path" "$plan" "$rows" <<'PY'
import json, pathlib, sys
path=pathlib.Path(sys.argv[1])
plan=json.loads(sys.argv[2])
rows=json.loads(sys.argv[3])
obj={"fixture_id":"postgres18-owner-policy-revision-v1","postgres_image":"postgres:18.6@sha256:74935e72241653ca55e0414067e6d8763aceb8a810eb51b452253ec3dcfc4336","expected_ids":[{"owner_id":"owner-07","policy_id":"policy-0012","revision":r} for r in range(8,3,-1)],"observed_rows":rows,"explain_json":plan}
path.write_text(json.dumps(obj,indent=2,sort_keys=True)+"\n",encoding="utf-8")
PY
printf 'wrote %s\n' "$output_path"
