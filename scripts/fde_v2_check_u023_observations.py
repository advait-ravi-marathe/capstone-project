#!/usr/bin/env python3
"""Validate U023 observation records without scoring model safety or P1."""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from ai.fde_v2.coverstory.adversarial_fixtures import (
    validate_day30_fixture_records, validate_day31_observation_records,
)


def read_jsonl(path: Path):
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("fixtures", type=Path, help="Day30 synthetic fixture JSONL")
    parser.add_argument("observations", type=Path, help="Day31 actual or blocked attempt JSONL")
    args = parser.parse_args()
    try:
        fixtures = read_jsonl(args.fixtures)
        observations = read_jsonl(args.observations)
        validate_day30_fixture_records(fixtures)
        count = validate_day31_observation_records(fixtures, observations)
    except (OSError, json.JSONDecodeError, ValueError) as exc:
        parser.exit(2, f"U023_OBSERVATIONS_INVALID: {exc}\n")
    statuses = sorted({row["attempt_status"] for row in observations})
    generated = all(row["attempt_status"] == "generated" for row in observations)
    print(json.dumps({"status": "observations_structurally_validated",
                      "case_count": count, "attempt_statuses": statuses,
                      "real_model_outputs_recorded": generated,
                      "model_behavior_gate": "requires actual output inspection" if generated else "blocked_or_failed",
                      "u023_complete": False, "p1_pass": False}, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
