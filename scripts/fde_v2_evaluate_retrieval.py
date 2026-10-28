#!/usr/bin/env python3
"""Run a saved retrieval evaluation; held-out use requires an explicit final check."""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from ai.fde_v2.coverstory import learner
from ai.fde_v2.coverstory.corpus import CorpusStore


def read_cases(path: Path) -> list[dict]:
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--split", choices=("dev", "heldout"), default="dev")
    parser.add_argument("--final-check", action="store_true",
                        help="required to run held-out once after configuration is frozen")
    parser.add_argument("--k", type=int, default=3)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    if args.split == "heldout" and not args.final_check:
        parser.error("held-out cases are for one final check only; pass --final-check after freezing config")
    if args.split == "dev" and args.final_check:
        parser.error("--final-check is only valid with --split heldout")
    data_dir = Path(os.environ.get("FDE_V2_DATA_DIR", ".runtime/fde-v2")).resolve()
    corpus = CorpusStore(data_dir / "corpus.sqlite3")
    chunks = corpus.chunks()
    if not chunks:
        raise SystemExit("no SQL chunks found; complete U011 and run fde_v2_prepare_chunks.py")
    fixture = ROOT / "ai/fde_v2/coverstory/evals" / f"questions-{args.split}.jsonl"
    cases = read_cases(fixture)
    result = learner.evaluate_retrieval(cases,
        lambda question: learner.keyword_search(question, chunks, limit=args.k), k=args.k)
    stable_config = {"method": "python-bm25-learner-seam", "k": args.k, "split": args.split}
    config_sha = hashlib.sha256(json.dumps(stable_config, sort_keys=True,
                                           separators=(",", ":")).encode()).hexdigest()
    record = {"recorded_at_utc": datetime.now(timezone.utc).isoformat(),
              "retriever": "learner.keyword_search; BM25 ranking values are not probabilities",
              "configuration": stable_config, "configuration_sha256": config_sha,
              "fixture": fixture.relative_to(ROOT).as_posix(),
              "heldout_is_final_check_only": args.split == "heldout",
              **result}
    output = args.output or data_dir / f"retrieval-{args.split}.json"
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(record, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(json.dumps({"status": "evaluation_written", "split": args.split,
                      "case_count": result["case_count"], "answerable_count": result["answerable_count"],
                      "output": str(output), "configuration_sha256": config_sha}, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
