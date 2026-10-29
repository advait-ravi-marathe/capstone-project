"""Interview rehearsal plumbing; only actual attempts/reviewer evidence can score a gate."""
from __future__ import annotations
import re
from collections.abc import Mapping
from . import reference as application_adapter
from .reference import GATES


def assess(attempts_by_gate, formal_ledger=None, *, adapter=application_adapter):
    if not isinstance(attempts_by_gate, Mapping):
        raise ValueError("attempt mapping required")
    reports = []
    seen_ids: set[str] = set()
    seen_hashes: set[str] = set()
    seen_content_hashes: set[str] = set()
    for gate, attempts in attempts_by_gate.items():
        if gate not in GATES:
            raise ValueError("unknown gate")
        if not isinstance(attempts, list) or len(attempts) != 2:
            raise ValueError("each supplied gate needs exactly two attempts")
        dates: set[str] = set()
        gate_ids: set[str] = set()
        gate_hashes: set[str] = set()
        gate_content_hashes: set[str] = set()
        for attempt in attempts:
            if not isinstance(attempt, Mapping):
                raise ValueError("attempt must be a mapping")
            if attempt.get("gate") != gate or attempt.get("task_type") != gate:
                raise ValueError("attempt gate and task_type must match their report key")
            attempt_id, task_hash, actual_date = (attempt.get("attempt_id"), attempt.get("task_hash"),
                                                   attempt.get("actual_date"))
            content_hash = attempt.get("task_content_sha256")
            if not isinstance(attempt_id, str) or not attempt_id.strip():
                raise ValueError("attempt id is required")
            if not isinstance(task_hash, str) or re.fullmatch(r"[0-9a-f]{64}", task_hash) is None:
                raise ValueError("task_hash must be a lowercase SHA-256 digest")
            if not isinstance(content_hash, str) or re.fullmatch(r"[0-9a-f]{64}", content_hash) is None:
                raise ValueError("task_content_sha256 must be a lowercase SHA-256 digest")
            expected_task_hash = __import__("hashlib").sha256(f"{gate}:{content_hash}".encode()).hexdigest()
            if task_hash != expected_task_hash:
                raise ValueError("task hash is not bound to this gate and task content")
            if not isinstance(actual_date, str) or not actual_date:
                raise ValueError("actual attempt date is required")
            if (attempt_id in seen_ids or task_hash in seen_hashes
                    or content_hash in seen_content_hashes):
                raise ValueError("attempt or original task was reused across gates")
            seen_ids.add(attempt_id)
            seen_hashes.add(task_hash)
            seen_content_hashes.add(content_hash)
            gate_ids.add(attempt_id)
            gate_hashes.add(task_hash)
            gate_content_hashes.add(content_hash)
            dates.add(actual_date)
        if (len(gate_ids) != 2 or len(gate_hashes) != 2
                or len(gate_content_hashes) != 2 or len(dates) != 2):
            raise ValueError("each gate requires two distinct original tasks on separate actual dates")
        report = adapter.score_gate(attempts)
        if not isinstance(report, Mapping) or report.get("gate") != gate:
            raise ValueError("scoring adapter returned a report for the wrong gate")
        scored_ids = {row.get("attempt_id") for row in report.get("attempts", [])
                      if isinstance(row, Mapping)}
        if scored_ids != gate_ids:
            raise ValueError("scoring adapter report does not bind both supplied attempts")
        score_rows={row.get("attempt_id"):row for row in report.get("attempts",[])
                    if isinstance(row,Mapping)}
        for attempt in attempts:
            expected_pass=(isinstance(attempt.get("scores"),Mapping)
                and all(type(value) is int and 3<=value<=4 for value in attempt["scores"].values())
                and len(attempt["scores"])==4 and attempt.get("critical_failure") is False)
            row=score_rows[attempt["attempt_id"]]
            if (row.get("scores")!=attempt.get("scores")
                    or row.get("reviewer_type")!=attempt.get("reviewer_type")
                    or row.get("passed") is not expected_pass):
                raise ValueError("scoring adapter report must preserve and correctly apply supplied dimensions")
        expected_status=("REHEARSAL_THRESHOLD_MET" if all(
            score_rows[a["attempt_id"]].get("passed") is True for a in attempts)
            else "REPAIR_REQUIRED")
        if report.get("status")!=expected_status:
            raise ValueError("scoring adapter gate status does not match its two attempt outcomes")
        reports.append(dict(report))
    missing = sorted(GATES - set(attempts_by_gate))
    repair = adapter.remediation(reports)
    ledger = adapter.validate_formal_assessment_ledger(formal_ledger or [])
    all_gates_pass = not missing and all(r.get("status") == "REHEARSAL_THRESHOLD_MET" for r in reports)
    overall = ("INCOMPLETE" if missing or not all_gates_pass or ledger["status"] != "20_HOUR_TARGET_MET"
               else "REHEARSAL_THRESHOLD_MET")
    return {"gate_reports": reports, "remediation": repair, "missing_gates": missing,
            "overall_status": overall, "formal_assessment_ledger": ledger, "employer_cutoff": False}
