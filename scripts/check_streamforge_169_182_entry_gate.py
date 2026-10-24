#!/usr/bin/env python3
"""Re-evaluate the real learner-owned Days 153/154 A/B evidence gate.

Exit 0 means the evidence qualifies. Exit 2 means recovery is required. This
checker never edits evidence or curriculum records.
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
RECORD_PATHS = (
    "evidence/continuation-141-154/day-153.json",
    "evidence/continuation-141-154/day-154.json",
)
SHA40 = re.compile(r"^[0-9a-f]{40}$")
SHA64 = re.compile(r"^[0-9a-f]{64}$")


def qualifies(record: dict[str, Any]) -> bool:
    hashes = record.get("artifact_hashes", [])
    by_role = {
        item.get("role"): item.get("sha256")
        for item in hashes
        if isinstance(item, dict)
    } if isinstance(hashes, list) else {}
    provenance = record.get("source_provenance", {})
    if not isinstance(provenance, dict):
        return False
    return all((
        record.get("status") == "completed",
        record.get("evidence_status") == "verified",
        record.get("learner_completion") is True,
        record.get("temporary_witness") is False,
        isinstance(record.get("tested_source_sha"), str)
        and SHA40.fullmatch(record["tested_source_sha"]) is not None,
        {"baseline", "candidate"}.issubset(by_role),
        by_role.get("baseline") != by_role.get("candidate"),
        all(isinstance(value, str) and SHA64.fullmatch(value) for value in by_role.values()),
        isinstance(record.get("matrix_selector"), str) and bool(record["matrix_selector"].strip()),
        record.get("matrix_exit_code") == 0,
        record.get("matrix_result") == "passed",
        all(
            isinstance(provenance.get(role), dict)
            and bool(provenance[role].get("source"))
            and bool(provenance[role].get("build_recipe"))
            for role in ("baseline", "candidate")
        ),
        isinstance(record.get("proof_limits"), str) and bool(record["proof_limits"].strip()),
    ))


def evaluate_records(records: list[dict[str, Any]]) -> dict[str, Any]:
    if len(records) != 2:
        raise ValueError("Exactly the Day 153 and Day 154 evidence records are required")
    qualified = [qualifies(record) for record in records]
    return {
        "gate": "met" if all(qualified) else "not_met",
        "required_days": [153, 154],
        "records": [
            {
                "day": day,
                "qualifies": result,
                "status": record.get("status"),
                "evidence_status": record.get("evidence_status"),
                "path": RECORD_PATHS[index],
            }
            for index, (day, record, result) in enumerate(zip((153, 154), records, qualified))
        ],
        "rule": "Only genuine learner-owned evidence qualifies; scaffold and temporary witnesses do not.",
    }


def read_actual_records(root: Path = ROOT) -> list[dict[str, Any]]:
    records = []
    for relative in RECORD_PATHS:
        path = root / relative
        if not path.is_file():
            raise FileNotFoundError(f"Authoritative evidence record is missing: {relative}")
        value = json.loads(path.read_text(encoding="utf-8"))
        if not isinstance(value, dict):
            raise ValueError(f"Evidence record must be a JSON object: {relative}")
        records.append(value)
    return records


def main() -> int:
    try:
        result = evaluate_records(read_actual_records())
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(json.dumps({"gate": "unreadable", "error": str(error)}, sort_keys=True))
        return 3
    print(json.dumps(result, sort_keys=True))
    return 0 if result["gate"] == "met" else 2


if __name__ == "__main__":
    sys.exit(main())
