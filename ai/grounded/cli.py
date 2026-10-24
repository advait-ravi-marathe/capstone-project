"""Offline fixed-fixture report CLI with canonical JSON output."""
from __future__ import annotations
import json
from .evaluation import CASES, evaluate
from .fixtures import CLAUSES

def report_bytes() -> bytes:
    report = evaluate(CASES, CLAUSES)
    return (json.dumps(report, sort_keys=True, separators=(",", ":"), ensure_ascii=False) + "\n").encode("utf-8")

def main() -> int:
    import sys
    sys.stdout.buffer.write(report_bytes())
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
