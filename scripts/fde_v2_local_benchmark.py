#!/usr/bin/env python3
"""Run twelve bounded requests against the real loopback CoverStory API only."""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import platform
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from ai.fde_v2.coverstory.models import MODEL_ID, MODEL_REVISION
from ai.fde_v2.coverstory.p1_closeout import require_loopback_url, validate_benchmark_record


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8765")
    parser.add_argument("--output", type=Path,
                        default=Path(".runtime/fde-v2/local-performance.json"))
    args = parser.parse_args()
    try:
        base = require_loopback_url(args.base_url)
    except ValueError as exc:
        parser.error(str(exc))
    output = args.output.resolve()
    output.parent.mkdir(parents=True, exist_ok=True)
    if output.exists():
        parser.error(f"refusing to overwrite existing measurement artifact: {output}")

    samples = []
    for sequence in range(1, 13):
        payload = json.dumps({"question": "What is the sample individual deductible?", "limit": 3}).encode()
        request = urllib.request.Request(base + "/ask", data=payload,
                                         headers={"Content-Type": "application/json"}, method="POST")
        started = time.perf_counter()
        try:
            with urllib.request.urlopen(request, timeout=65) as response:
                status = response.status
                body = json.loads(response.read())
        except (urllib.error.URLError, TimeoutError, json.JSONDecodeError) as exc:
            print(f"REAL_BENCHMARK_BLOCKED_OR_FAILED at request {sequence}: {type(exc).__name__}: {exc}",
                  file=sys.stderr)
            return 2
        elapsed = (time.perf_counter() - started) * 1000
        if status != 200 or body.get("model_id") != MODEL_ID or body.get("model_revision") != MODEL_REVISION:
            print(f"REAL_BENCHMARK_BLOCKED_OR_FAILED at request {sequence}: status/model identity mismatch",
                  file=sys.stderr)
            return 2
        timings = body.get("stage_timings_ms")
        answer = body.get("answer")
        if (not isinstance(timings, dict) or not isinstance(answer, str) or not answer.strip()
                or body.get("model_id") in {"test-double-only", "fixture"}):
            print(f"REAL_BENCHMARK_BLOCKED_OR_FAILED at request {sequence}: invalid real API response",
                  file=sys.stderr)
            return 2
        samples.append({"sequence": sequence, "status_code": status,
                        "client_elapsed_ms": round(elapsed, 3),
                        "retrieval_ms": timings.get("retrieval", 0.0),
                        "generation_ms": timings.get("generation", 0.0),
                        "answer_sha256": hashlib.sha256(answer.encode()).hexdigest(),
                        "abstained": body.get("abstained")})
    record = {"schema_version": 1, "evidence_kind": "actual_pinned_local_inference",
              "recorded_at_utc": datetime.now(timezone.utc).isoformat(),
              "model_id": MODEL_ID, "model_revision": MODEL_REVISION,
              "sample_count": len(samples), "concurrency": 1,
              "warm_cold_order": "request 1 cold-or-first; requests 2–12 follow on the same process",
              "machine": {"python": platform.python_version(), "platform": platform.platform(),
                          "cpu_count": os.cpu_count()},
              "resource_limits": "process RSS/CPU telemetry is not exposed by this local API; report as unavailable",
              "cost": "unavailable: local inference has no provider billing meter",
              "samples": samples}
    try:
        validate_benchmark_record(record)
    except ValueError as exc:
        print(f"REAL_BENCHMARK_INVALID: {exc}", file=sys.stderr)
        return 2
    # Exclusive creation prevents reruns from replacing the first measurement.
    try:
        with output.open("x", encoding="utf-8") as handle:
            json.dump(record, handle, indent=2, sort_keys=True)
            handle.write("\n")
    except FileExistsError:
        print(f"REAL_BENCHMARK_REFUSED_OVERWRITE: {output}", file=sys.stderr)
        return 2
    print(json.dumps({"status": "actual_local_measurement_recorded", "sample_count": 12,
                      "concurrency": 1, "artifact": str(output),
                      "cost": record["cost"], "resource_limits": record["resource_limits"]}, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
