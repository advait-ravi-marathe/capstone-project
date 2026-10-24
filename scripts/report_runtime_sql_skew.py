#!/usr/bin/env python3
"""Report actual and estimated scan rows from the before/after ANALYZE experiment."""
from __future__ import annotations
import json
import math
import sys
from pathlib import Path


def scans(node: dict):
    if isinstance(node, dict):
        if "Plan Rows" in node and "Actual Rows" in node:
            loops = node.get("Actual Loops", 1)
            actual = node["Actual Rows"] * loops
            estimated = node["Plan Rows"]
            q_error = (1.0 if estimated == actual == 0 else None if estimated == 0 or actual == 0
                       else max(estimated / actual, actual / estimated))
            yield {"node_type": node.get("Node Type"), "estimated_rows": estimated,
                   "actual_rows_per_loop": node["Actual Rows"], "loops": loops,
                   "total_actual_rows": actual, "q_error": q_error}
        for child in node.get("Plans", []):
            yield from scans(child)


def report(path: str) -> dict:
    data = json.loads(Path(path).read_text(encoding="utf-8"))
    experiment = data["skew_statistics_experiment"]
    if experiment["expected_owner_rows"] != experiment["observed_owner_rows"]:
        raise ValueError("skew fixture owner row count differs from its independent expected count")
    phases = {}
    for name in ("before_analyze", "after_analyze"):
        tree = experiment[name][0]["Plan"]
        phases[name] = list(scans(tree))
        if not phases[name]:
            raise ValueError(f"no row-estimating plan nodes found for {name}")
    return {"variant": data["variant"], "owner_rows": experiment["observed_owner_rows"],
            "before_analyze": phases["before_analyze"], "after_analyze": phases["after_analyze"],
            "estimate_change_required": False,
            "interpretation": "Observed plans only; ANALYZE refreshes statistics but does not guarantee a particular estimate or plan."}


if __name__ == "__main__":
    print(json.dumps(report(sys.argv[1]), indent=2, sort_keys=True))
