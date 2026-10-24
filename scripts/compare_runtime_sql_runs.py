#!/usr/bin/env python3
"""Compare exact SQL result identity while reporting, not judging, plan variation."""
from __future__ import annotations
import json
import math
import sys
from pathlib import Path


def load(path: str) -> dict:
    obj = json.loads(Path(path).read_text(encoding="utf-8"))
    if not isinstance(obj, dict) or obj.get("expected_ids") != obj.get("observed_rows"):
        raise ValueError(f"result rows do not match independent expected identities: {path}")
    plan = obj["explain_json"][0]["Plan"]
    time_ms = plan["Actual Total Time"]
    if type(time_ms) not in (float, int) or not math.isfinite(time_ms) or time_ms < 0:
        raise ValueError(f"invalid observed execution time: {path}")
    return obj


def compare(paths: list[str]) -> dict:
    if len(paths) < 2:
        raise ValueError("at least two captured runs are required")
    rows = [load(path) for path in paths]
    reference = rows[0]["expected_ids"]
    if any(row["observed_rows"] != reference for row in rows[1:]):
        raise ValueError("captured runs do not return identical ordered rows")
    reports = []
    for path, obj in zip(paths, rows):
        plan = obj["explain_json"][0]["Plan"]
        reports.append({"path": path, "variant": obj.get("variant", "unknown"),
                        "row_count": len(obj["observed_rows"]), "plan_rows": plan.get("Plan Rows"),
                        "actual_rows": plan.get("Actual Rows"), "shared_hit_blocks": plan.get("Shared Hit Blocks"),
                        "shared_read_blocks": plan.get("Shared Read Blocks"),
                        "actual_total_time_ms": plan["Actual Total Time"]})
    return {"ordered_rows_identical": True, "row_count": len(reference),
            "plan_values_are_observations_not_thresholds": reports}


if __name__ == "__main__":
    print(json.dumps(compare(sys.argv[1:]), indent=2, sort_keys=True))
