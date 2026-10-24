"""Instructor-only independent reference; not learner work or completion evidence."""
from __future__ import annotations
import math
from typing import Any


def analyze_observation(observation: dict[str, Any]) -> dict[str, Any]:
    if not isinstance(observation, dict):
        raise ValueError("observation must be an object")
    try:
        expected, actual = observation["expected_ids"], observation["observed_rows"]
        root = observation["explain_json"][0]["Plan"]
        estimated, measured = root["Plan Rows"], root["Actual Rows"]
        hit, read = root["Shared Hit Blocks"], root["Shared Read Blocks"]
    except (KeyError, IndexError, TypeError) as exc:
        raise ValueError("missing required plan or result field") from exc
    if not isinstance(expected, list) or not isinstance(actual, list):
        raise ValueError("expected and observed rows must be arrays")
    keys = ("owner_id", "policy_id", "revision")
    for rows in (expected, actual):
        if any(not isinstance(row, dict) or set(row) != set(keys) for row in rows):
            raise ValueError("rows must contain exactly owner_id, policy_id, revision")
        if any(not isinstance(row["owner_id"], str) or not row["owner_id"] or
               not isinstance(row["policy_id"], str) or not row["policy_id"] or
               type(row["revision"]) is not int or row["revision"] < 1 for row in rows):
            raise ValueError("row fields have invalid types or values")
    if any(type(n) not in (int, float) or not math.isfinite(n) or n < 0
           for n in (estimated, measured)):
        raise ValueError("plan row estimates and observations must be finite nonnegative numbers")
    if any(type(n) is not int or n < 0 for n in (hit, read)):
        raise ValueError("buffer counters must be nonnegative integers")
    if estimated == measured == 0:
        ratio = 1.0
    elif estimated == 0 or measured == 0:
        ratio = None
    else:
        ratio = max(estimated / measured, measured / estimated)
    return {
        "row_correct": actual == expected,
        "expected_row_count": len(expected),
        "observed_row_count": len(actual),
        "estimate_rows": estimated,
        "actual_rows": measured,
        "q_error": ratio,
        "cardinality_state": "zero_cardinality_mismatch" if ratio is None else "comparable",
        "shared_hit_blocks": hit,
        "shared_read_blocks": read,
        "single_run_performance_claim": "not_supported",
    }
