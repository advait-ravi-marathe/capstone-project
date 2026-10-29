"""Deterministic M4.3 mechanics. Reference scores are fixtures, not learner results."""
from __future__ import annotations
from datetime import date
import re
from typing import Any, Mapping
DIMENSIONS={"reproduction","root_cause","minimal_patch","regression","blast_radius"}
def validate_attempt(record: Mapping[str,Any]) -> dict[str,Any]:
    if not isinstance(record,Mapping): raise ValueError("attempt must be a mapping")
    required=("exercise_id","source_license","initial_hash","attempt_date","failure_receipt","root_cause","patch_hash","regression_receipt","blast_radius","assistance")
    if any(not isinstance(record.get(k),str) or not record[k].strip() for k in required): raise ValueError("attempt provenance and reproduce-patch-regression evidence are required")
    if not re.fullmatch(r"[0-9a-f]{64}",record["initial_hash"]) or not re.fullmatch(r"[0-9a-f]{64}",record["patch_hash"]): raise ValueError("exercise and patch hashes must be lowercase SHA-256")
    try: date.fromisoformat(record["attempt_date"])
    except ValueError: raise ValueError("attempt date must be an actual ISO YYYY-MM-DD date")
    if record["exercise_id"] not in {"A","B"}: raise ValueError("two distinct original exercises are required")
    if record.get("solution_seen_before_attempt") is not False: raise ValueError("attempt must be unseen at start")
    if record.get("assistant_authored_patch") is not False: raise ValueError("assistant patch cannot count as independent learner attempt")
    return {"valid":True,"exercise_id":record["exercise_id"],"status":"ATTEMPT_EVIDENCE_RECORDED"}
def score_attempt(record: Mapping[str,Any]) -> dict[str,Any]:
    validate_attempt(record); scores=record.get("scores")
    if not isinstance(scores,Mapping) or set(scores)!=DIMENSIONS or any(type(v) is not int or not 0<=v<=4 for v in scores.values()): raise ValueError("all five dimensions require integer scores 0..4")
    reviewer=record.get("reviewer_type")
    if reviewer not in {"self","assistant","peer"}: raise ValueError("reviewer type must be explicit")
    critical=record.get("critical_failure")
    if type(critical) is not bool: raise ValueError("critical failure flag is required")
    passed=all(v>=3 for v in scores.values()) and not critical
    return {"status":"MEETS_REHEARSAL_THRESHOLD" if passed else "REPAIR_REQUIRED","scores":dict(scores),"reviewer_type":reviewer,"employer_cutoff":False,"professional_tenure_claim":False}
def evaluate_handoff(receipts: Mapping[str,Any]) -> dict[str,Any]:
    if not isinstance(receipts,Mapping): raise ValueError("handoff receipts required")
    clean=receipts.get("clean_clone")
    if clean not in {"peer_verified","self_run","not_run","blocked"}: raise ValueError("clean clone provenance must be explicit")
    run=receipts.get("run_receipt")
    if clean in {"peer_verified","self_run"}:
        if not isinstance(run,Mapping) or any(not run.get(k) for k in ("command","empty_workspace_ref","environment_hash","output_ref")):
            raise ValueError("clean-run evidence needs command, empty workspace, environment hash and output receipt")
    feedback=receipts.get("review_feedback")
    if clean=="peer_verified" and feedback is None: raise ValueError("peer reproduction needs actual reviewer feedback evidence")
    if feedback is not None and (not isinstance(feedback,Mapping) or feedback.get("reviewer_type")!="peer" or not feedback.get("evidence_ref")):
        raise ValueError("peer feedback requires actual peer type and evidence reference")
    return {"clean_clone_status":clean,"peer_reproduction": "REQUIRES_REVIEW" if clean=="peer_verified" else ("BLOCKED" if clean=="blocked" else "NOT_EVIDENCED"),"feedback_status":"RECORDED_UNVERIFIED" if feedback else "NOT_AVAILABLE","independent_reviewer_fabricated":False}
