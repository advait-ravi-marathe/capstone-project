#!/usr/bin/env python3
"""Summarize repeated same-variant PostgreSQL timings without a pass threshold."""
from __future__ import annotations
import json
import math
import statistics
import sys
from pathlib import Path


def summarize(paths: list[str]) -> dict:
    if len(paths) < 3:
        raise ValueError("at least three independent baseline captures are required")
    rows = [json.loads(Path(path).read_text(encoding="utf-8")) for path in paths]
    if any(row.get("variant") != "baseline" for row in rows):
        raise ValueError("timing comparison requires the same baseline variant")
    expected = rows[0]["expected_ids"]
    if any(row.get("expected_ids") != expected or row.get("observed_rows") != expected for row in rows):
        raise ValueError("repeated runs must preserve complete expected result identities")
    times = [row["explain_json"][0]["Plan"]["Actual Total Time"] for row in rows]
    if any(type(value) not in (int, float) or not math.isfinite(value) or value < 0 for value in times):
        raise ValueError("execution times must be finite nonnegative observations")
    return {"run_count": len(times), "median_ms": statistics.median(times),
            "population_stdev_ms": statistics.pstdev(times), "min_ms": min(times), "max_ms": max(times),
            "interpretation": "small local sample only; no performance or production claim"}


if __name__ == "__main__":
    print(json.dumps(summarize(sys.argv[1:]), indent=2, sort_keys=True))
