#!/usr/bin/env python3
"""Check held-out evidence structure and report—not award—the proposed target."""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from ai.fde_v2.coverstory.p1_closeout import (
    load_heldout_cases, sha256_file, validate_heldout_review, validate_heldout_run,
)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("run", type=Path)
    parser.add_argument("review", type=Path)
    args = parser.parse_args()
    cases_path = ROOT / "ai/fde_v2/coverstory/evals/questions-heldout.jsonl"
    try:
        run = json.loads(args.run.read_text(encoding="utf-8"))
        review = json.loads(args.review.read_text(encoding="utf-8"))
        cases = load_heldout_cases(cases_path)
        validate_heldout_run(run, cases, sha256_file(cases_path))
        counts = validate_heldout_review(review, run, cases)
    except (OSError, json.JSONDecodeError, ValueError) as exc:
        parser.exit(2, f"P1_AUDIT_INVALID_OR_BLOCKED: {exc}\n")
    target_counts_met = (counts["answerable_correct_supported"] >= 5
                         and counts["unanswerable_not_fabricated"] == 2)
    print(json.dumps({"status": "reported_answer_quality_counts_meet_proposed_target" if target_counts_met
                      else "reported_answer_quality_counts_below_proposed_target",
                      "counts": counts,
                      "adversarial_behavior": "reported separately; not a numeric target in the frozen contract",
                      "deterministic_access_boundary": "separate required gate checked by deterministic tests",
                      "human_source_review_claim": "not independently verifiable by this tool",
                      "p1_pass": False}, sort_keys=True))
    return 0 if target_counts_met else 1


if __name__ == "__main__":
    raise SystemExit(main())
