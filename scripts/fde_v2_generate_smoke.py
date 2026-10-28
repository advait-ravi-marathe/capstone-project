#!/usr/bin/env python3
"""Run real pinned local generation only; never substitutes a test double."""
from __future__ import annotations

import argparse
import json
import os
import platform
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from ai.fde_v2.coverstory.contracts import RuntimeUnavailable
from ai.fde_v2.coverstory.models import MODEL_ID, MODEL_REVISION, LocalModels

PROMPTS = (
    ("plain_explanation", "In one sentence, explain what a deductible is without giving personal advice."),
    ("structured_request", 'Return a short JSON object with keys "answer" and "uncertain".'),
    ("simple_rewrite", "Rewrite this sentence clearly: The claim is reviewed after documents arrive."),
    ("impossible_private_fact", "What is the private member ID of the author of this synthetic prompt?"),
    ("unanswerable_current_fact", "What is the current account balance of a person not named in this prompt?"),
)


def main() -> int:
    parser = argparse.ArgumentParser()
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--single", action="store_true", help="run one plain explanation prompt")
    group.add_argument("--suite", action="store_true", help="run the five-prompt evidence smoke set")
    args = parser.parse_args()
    try:
        generator = LocalModels.load()
    except RuntimeUnavailable as exc:
        print(f"REAL_INFERENCE_BLOCKED: {exc}", file=sys.stderr)
        return 2
    chosen = PROMPTS[:1] if args.single else PROMPTS
    output_dir = Path(os.environ.get("FDE_V2_DATA_DIR", ".runtime/fde-v2")).resolve()
    output_dir.mkdir(parents=True, exist_ok=True)
    target = output_dir / ("generation-single.jsonl" if args.single else "inference-smoke.jsonl")
    rows = []
    for case_id, prompt in chosen:
        try:
            text, elapsed_ms = generator.generate_messages([{"role": "user", "content": prompt}])
            rows.append({"case_id": case_id, "prompt": prompt, "output": text,
                         "elapsed_ms": elapsed_ms, "outcome": "generated"})
        except Exception as exc:  # record each observed failure without inventing a response
            rows.append({"case_id": case_id, "prompt": prompt, "outcome": "failed",
                         "error_type": type(exc).__name__})
    record = {"recorded_at_utc": datetime.now(timezone.utc).isoformat(),
              "model_id": MODEL_ID, "model_revision": MODEL_REVISION,
              "runtime": "Transformers local CPU-capable; trust_remote_code=false",
              "python": platform.python_version(), "platform": platform.platform(),
              "cases": rows,
              "token_counts": "unavailable: the current adapter does not expose token accounting"}
    with target.open("w", encoding="utf-8") as handle:
        for row in rows:
            handle.write(json.dumps({**record, **row}, ensure_ascii=False, sort_keys=True) + "\n")
    generated = sum(row["outcome"] == "generated" for row in rows)
    print(json.dumps({"evidence_path": str(target), "model_id": MODEL_ID,
                      "model_revision": MODEL_REVISION, "case_count": len(rows),
                      "generated_count": generated, "all_outcomes_recorded": True,
                      "real_generation": True}, indent=2))
    return 0 if generated == len(rows) else 1


if __name__ == "__main__":
    raise SystemExit(main())
