"""Learner-owned rehearsal and evidence-gate adapters."""
from typing import Any, Mapping
def validate_attempt(attempt: Mapping[str,Any]) -> dict[str,Any]: raise NotImplementedError("M5.1: validate one dated unseen attempt and allowances")
def score_gate(attempts: list[Mapping[str,Any]]) -> dict[str,Any]: raise NotImplementedError("M5.1: score two independent attempts per critical gate")
def remediation(gates: list[Mapping[str,Any]]) -> dict[str,Any]: raise NotImplementedError("M5.1: turn failed dimensions into bounded repair")
def validate_formal_assessment_ledger(entries: list[Mapping[str,Any]]) -> dict[str,Any]: raise NotImplementedError("M5.1: accrue non-DSA formal assessment across role-bank windows, excluding AWS")
