#!/usr/bin/env python3
"""Read-only dynamic eligibility check for the Days 239–252 starter window."""
from __future__ import annotations

import hashlib
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DAY210 = ROOT / "evidence/continuation-197-210/day-210-entry-review.json"
DAY238 = ROOT / "evidence/continuation-225-238/day-238-evaluation-closeout.json"
HEX40 = re.compile(r"^[0-9a-f]{40}$")
HEX64 = re.compile(r"^[0-9a-f]{64}$")


def valid_day210(raw: bytes) -> bool:
    try:
        row = json.loads(raw)
    except Exception:
        return False
    required = {"day", "learner_owned", "status", "synthetic_only", "owner_boundary_verified",
                "source_version_verified", "citation_boundary_verified", "source_sha", "artifact_sha256"}
    return (isinstance(row, dict) and set(row) == required and row.get("day") == 210 and
            row.get("learner_owned") is True and row.get("status") == "pass" and
            row.get("synthetic_only") is True and
            all(row.get(k) is True for k in ("owner_boundary_verified", "source_version_verified", "citation_boundary_verified")) and
            isinstance(row.get("source_sha"), str) and HEX40.fullmatch(row["source_sha"]) is not None and
            isinstance(row.get("artifact_sha256"), str) and HEX64.fullmatch(row["artifact_sha256"]) is not None)


def valid_day238(raw: bytes) -> bool:
    try:
        row = json.loads(raw)
    except Exception:
        return False
    required = {"day", "learner_owned", "status", "synthetic_only", "generated_draft_only",
                "preapproval_render_abstained", "explicit_human_review_recorded",
                "approved_same_owner_rendered_exact_text", "policy_snapshot_unchanged",
                "source_sha", "artifact_sha256"}
    return (isinstance(row, dict) and set(row) == required and row.get("day") == 238 and
            row.get("learner_owned") is True and row.get("status") == "pass" and
            row.get("synthetic_only") is True and
            all(row.get(k) is True for k in ("generated_draft_only", "preapproval_render_abstained",
                                               "explicit_human_review_recorded", "approved_same_owner_rendered_exact_text",
                                               "policy_snapshot_unchanged")) and
            isinstance(row.get("source_sha"), str) and HEX40.fullmatch(row["source_sha"]) is not None and
            isinstance(row.get("artifact_sha256"), str) and HEX64.fullmatch(row["artifact_sha256"]) is not None)


def check() -> dict[str, object]:
    missing = []
    malformed = []
    for path, validator, label in ((DAY210, valid_day210, "Day 210"), (DAY238, valid_day238, "Day 238")):
        if not path.exists():
            missing.append(label)
        elif not validator(path.read_bytes()):
            malformed.append(label)
    if malformed:
        status, reason = "blocked", "learner evidence record fails the strict schema"
    elif missing:
        status, reason = "not_met", "required learner-owned evidence is absent: " + ", ".join(missing)
    else:
        status, reason = "eligible_to_attempt", "verified learner-owned prerequisite records"
    records = {}
    for path, label in ((DAY210, "day_210"), (DAY238, "day_238")):
        if path.exists():
            records[label] = hashlib.sha256(path.read_bytes()).hexdigest()
    return {"status": status, "reason": reason, "records_sha256": records,
            "eligibility_only": True, "completion_awarded": False}


if __name__ == "__main__":
    print(json.dumps(check(), sort_keys=True))
    sys.exit(0)
