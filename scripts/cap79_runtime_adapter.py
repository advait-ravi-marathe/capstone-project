"""Instructor-owned local bridge to CAP-79's learner seam; synthetic fixture only."""
from __future__ import annotations

import json
import sys
from datetime import datetime, timedelta
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from ai.coverstory.cap_79 import SourceHit, filter_fresh


def main() -> int:
    fixture = Path(__file__).resolve().parents[1] / "fixtures/coverstory/CAP-79/synthetic-v1.json"
    data = json.loads(fixture.read_text(encoding="utf-8"))
    case = next(row for row in data["cases"] if row["id"] == "CAP-79-CS-04")
    hits = []
    for row in case["input"]:
        observed = datetime.fromisoformat(row["observed_at"].replace("Z", "+00:00"))
        hits.append(SourceHit(row["source_id"], row["version"], row["content_sha256"],
                              row["evidence_type"], observed, row["source_label"]))
    now = datetime.fromisoformat(data["fixed_now"].replace("Z", "+00:00"))
    result = filter_fresh(hits, now, timedelta(seconds=data["max_age_seconds"]))
    rejected = {(row.source_id, row.version): row.reason for row in result.rejected}
    rows = [{"sourceId": row.source_id, "version": row.version,
             "status": "FRESH" if (row.source_id, row.version) in {(item.source_id, item.version) for item in result.retained}
                      else rejected[(row.source_id, row.version)],
             "evidenceType": row.evidence_type, "contentSha256": row.content_sha256}
            for row in hits]
    print(json.dumps({"schemaMajor": 1, "status": result.status,
                      "rows": rows, "quoteOrderEligible": False}, separators=(",", ":")))
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except NotImplementedError:
        print(json.dumps({"code": "LEARNER_TODO"}))
        raise SystemExit(2)
