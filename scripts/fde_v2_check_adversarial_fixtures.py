"""Check the Day 30 U023 fixture-authoring artifact (not model behavior)."""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from ai.fde_v2.coverstory.adversarial_fixtures import validate_day30_fixture_records


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("fixtures", type=Path,
                        help="learner-authored JSONL, e.g. artifacts/u023-day30/adversarial-dev.jsonl")
    args = parser.parse_args()
    try:
        if args.fixtures.stat().st_size > 50_000:
            raise ValueError("fixture artifact exceeds the 50,000-byte checker limit")
        rows = [json.loads(line) for line in args.fixtures.read_text(encoding="utf-8").splitlines() if line.strip()]
        count = validate_day30_fixture_records(rows)
    except (OSError, json.JSONDecodeError, ValueError) as exc:
        parser.exit(2, f"U023_DAY30_FIXTURES_INVALID: {exc}\n")
    print(json.dumps({"status": "fixture_authoring_validated", "case_count": count,
                      "model_behavior": "not_run_day30_scope", "u023_complete": False}, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
