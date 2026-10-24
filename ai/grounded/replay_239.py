"""Day 251 learner seam for fresh-process canonical evaluation report replay.

Reads one JSON report from stdin and emits canonical UTF-8 JSON plus one
newline. No fixture generation, provider, network, or cloud access occurs.
"""
from __future__ import annotations

import sys

MAX_REPORT_BYTES = 16_384


def canonical_report_bytes(raw: bytes) -> bytes:
    """TODO Day 251: validate the fixed report schema and serialize canonically.

    Require one JSON object with fixture_version, cases, and metrics. Reject
    duplicate JSON object keys, extra/missing top-level fields, invalid UTF-8,
    or over-limit input. Emit sorted-key compact UTF-8 JSON and exactly one LF.
    """
    raise NotImplementedError("TODO: canonical report serialization")


def main() -> int:
    raw = sys.stdin.buffer.read(MAX_REPORT_BYTES + 1)
    if len(raw) > MAX_REPORT_BYTES:
        sys.stderr.write("report too large\n")
        return 2
    try:
        result = canonical_report_bytes(raw)
    except (ValueError, UnicodeError, NotImplementedError) as exc:
        sys.stderr.write(f"invalid report: {exc}\n")
        return 2
    sys.stdout.buffer.write(result)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
