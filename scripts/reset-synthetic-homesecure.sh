#!/usr/bin/env bash
set -euo pipefail

database_url=""
confirmed=false
while (($#)); do
  case "$1" in
    --database-url)
      (($# >= 2)) || { echo "missing value for --database-url" >&2; exit 2; }
      database_url="$2"
      shift 2
      ;;
    --confirm-disposable)
      confirmed=true
      shift
      ;;
    *) echo "unsupported argument: $1" >&2; exit 2 ;;
  esac
done

[[ -n "$database_url" ]] || { echo "--database-url is required" >&2; exit 2; }
[[ "$confirmed" == true ]] || { echo "refusing reset without --confirm-disposable" >&2; exit 2; }
[[ "$database_url" =~ ^postgres(ql)?://[^/]+/([a-zA-Z0-9_]+)(\?.*)?$ ]] || {
  echo "database URL must be a PostgreSQL URI with a plain database name" >&2; exit 2;
}
database_name="${BASH_REMATCH[2]}"
[[ "$database_name" =~ _test$ ]] || { echo "refusing reset: database name must end in _test" >&2; exit 2; }

command -v psql >/dev/null 2>&1 || { echo "psql is required to reset a disposable database" >&2; exit 127; }
psql --set=ON_ERROR_STOP=1 "$database_url" <<'SQL'
BEGIN;
TRUNCATE selections, quote_versions, journeys RESTART IDENTITY CASCADE;
INSERT INTO journeys (journey_id) VALUES ('homesecure-training-journey');
INSERT INTO quote_versions (
  journey_id, quote_version_id, product_code, product_version, rate_version,
  rebuild_amount_rupees, coverage_code, currency, annual_premium_rupees,
  created_at, expires_at
) VALUES (
  'homesecure-training-journey', 'home-secure-training-v1', 'homesecure',
  'home-secure-v1', 'home-secure-training-rate-v1', 100000.00, 'BUILDING',
  'INR', 125.00, TIMESTAMPTZ '2026-10-20 12:00:00+00',
  TIMESTAMPTZ '2026-10-20 12:15:00+00'
);
COMMIT;
SQL
