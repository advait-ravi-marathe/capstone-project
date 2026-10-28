#!/usr/bin/env python3
"""Run the frozen ten-case P1 set once against the real loopback RAG API."""
from __future__ import annotations

import argparse
import hashlib
import json
import sys
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from ai.fde_v2.coverstory.models import MODEL_ID, MODEL_REVISION
from ai.fde_v2.coverstory.p1_closeout import load_heldout_cases, require_loopback_url, sha256_file, validate_heldout_run


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8765")
    parser.add_argument("--output", type=Path,
                        default=Path(".runtime/fde-v2/p1-heldout-run.json"))
    args = parser.parse_args()
    try:
        base = require_loopback_url(args.base_url)
        fixture = ROOT / "ai/fde_v2/coverstory/evals/questions-heldout.jsonl"
        cases = load_heldout_cases(fixture)
    except (ValueError, OSError, json.JSONDecodeError) as exc:
        parser.error(str(exc))
    output = args.output.resolve()
    output.parent.mkdir(parents=True, exist_ok=True)
    if output.exists():
        parser.error(f"held-out set is one-shot; refusing to overwrite: {output}")

    results = []
    all_real = True
    for case in cases:
        request = urllib.request.Request(
            base + "/ask",
            data=json.dumps({"question": case["question"], "limit": 3}).encode(),
            headers={"Content-Type": "application/json"}, method="POST")
        try:
            with urllib.request.urlopen(request, timeout=65) as response:
                status = response.status
                body = json.loads(response.read())
        except urllib.error.HTTPError as exc:
            status = exc.code
            try:
                body = {"error": json.loads(exc.read()).get("detail", "http_error")}
            except (json.JSONDecodeError, AttributeError):
                body = {"error": "http_error"}
        except (urllib.error.URLError, TimeoutError, json.JSONDecodeError) as exc:
            status = 0
            body = {"error": type(exc).__name__}
        real = (status == 200 and body.get("model_id") == MODEL_ID
                and body.get("model_revision") == MODEL_REVISION)
        all_real = all_real and real
        results.append({"case_id": case["case_id"], "status_code": status,
                        "model_id": body.get("model_id"), "model_revision": body.get("model_revision"),
                        "answer": body.get("answer") if isinstance(body.get("answer"), str) else "",
                        "citations": body.get("citations") if isinstance(body.get("citations"), list) else [],
                        "sources": body.get("sources") if isinstance(body.get("sources"), list) else [],
                        "abstained": body.get("abstained") if type(body.get("abstained")) is bool else None,
                        "error": body.get("error"), "real_pinned_model": real})

    run = {"schema_version": 1,
           "evidence_kind": "actual_pinned_local_rag_run" if all_real else "blocked_or_failed_attempt",
           "recorded_at_utc": datetime.now(timezone.utc).isoformat(),
           "model_id": MODEL_ID if all_real else "unavailable",
           "model_revision": MODEL_REVISION if all_real else "unavailable",
           "fixture_path": fixture.relative_to(ROOT).as_posix(),
           "fixture_sha256": sha256_file(fixture),
           "configuration_sha256": hashlib.sha256(
               b"".join((ROOT / path).read_bytes() for path in (
                   "ai/fde_v2/coverstory/model_lock.json",
                   "ai/fde_v2/coverstory/learner.py",
                   "ai/fde_v2/coverstory/pipeline.py"))).hexdigest(),
           "contains_manual_correctness_judgment": False,
           "results": results}
    # A failed/blocked attempt is saved as such, never rewritten into a pass.
    with output.open("x", encoding="utf-8") as handle:
        json.dump(run, handle, indent=2, sort_keys=True)
        handle.write("\n")
    print(json.dumps({"status": "actual_run_recorded" if all_real else "real_model_gate_blocked_or_failed",
                      "case_count": len(results), "all_real_pinned_model": all_real,
                      "artifact": str(output), "semantic_correctness": "requires separate human source review"},
                     indent=2))
    if not all_real:
        return 2
    try:
        validate_heldout_run(run, cases, sha256_file(fixture))
    except ValueError as exc:
        print(f"HELDOUT_RUN_INVALID: {exc}", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
