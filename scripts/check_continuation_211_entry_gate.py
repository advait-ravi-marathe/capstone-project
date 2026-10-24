#!/usr/bin/env python3
"""Read-only eligibility check; absence is an honest not_met result."""
import argparse, hashlib, json, re, sys
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
RECORD=ROOT/"evidence/continuation-197-210/day-210-entry-review.json"
HEX=re.compile(r"^[0-9a-f]{64}$")
def check():
    if not RECORD.exists():
        return {"status":"not_met","reason":"learner-owned Day 210 boundary review is absent","record":str(RECORD.relative_to(ROOT)),"completion_awarded":False}
    raw=RECORD.read_bytes()
    try: d=json.loads(raw)
    except Exception: return {"status":"blocked","reason":"record is not valid JSON","completion_awarded":False}
    required={"day","learner_owned","status","synthetic_only","owner_boundary_verified","source_version_verified","citation_boundary_verified","source_sha","artifact_sha256"}
    valid=(isinstance(d,dict) and set(d)==required and d.get("day")==210 and d.get("learner_owned") is True and d.get("status")=="pass" and d.get("synthetic_only") is True and all(d.get(k) is True for k in ("owner_boundary_verified","source_version_verified","citation_boundary_verified")) and isinstance(d.get("source_sha"),str) and HEX.fullmatch(d["source_sha"]) and isinstance(d.get("artifact_sha256"),str) and HEX.fullmatch(d["artifact_sha256"]))
    return {"status":"eligible_to_attempt" if valid else "blocked","reason":"verified learner-owned synthetic boundary record" if valid else "record fields or evidence do not satisfy the strict schema","record_sha256":hashlib.sha256(raw).hexdigest(),"completion_awarded":False}
if __name__=="__main__":
    ap=argparse.ArgumentParser();ap.add_argument("--require-ready",action="store_true");a=ap.parse_args();r=check();print(json.dumps(r,sort_keys=True));sys.exit(1 if a.require_ready and r["status"]!="eligible_to_attempt" else 0)
