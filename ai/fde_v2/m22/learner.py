"""Learner-owned M2.2 policy and evidence seams; intentionally unsolved.

These seams are not progression-selectable until P1 and M2.1 evidence passes.
They do not implement cryptography or substitute for an established identity
provider. Real identity verification must be supplied by a reviewed adapter.
"""
from __future__ import annotations

from collections.abc import Mapping, Sequence
from typing import Any


def verify_identity(token: str, verified_claims_provider, subject_lookup, *,
                    expected_issuer: str, expected_audience: str, now: float) -> dict[str, Any]:
    """Stage S02: adapt a maintained signature-verifying JWT library to app identity."""
    raise NotImplementedError("M2.2: integrate a maintained verified-token provider")


def authorize_document(context: Mapping[str, Any], document: Mapping[str, Any], action: str) -> bool:
    """Stage S01/S03: decide one server-side action from authenticated context."""
    raise NotImplementedError("M2.2: implement default-deny document authorization")


def filter_retrieved_hits(context: Mapping[str, Any], hits: Sequence[Mapping[str, Any]],
                          authoritative_documents: Mapping[str, Mapping[str, Any]]) -> list[Mapping[str, Any]]:
    """Stage S04: revalidate vector candidates against authoritative access state."""
    raise NotImplementedError("M2.2: implement retrieval-boundary access validation")


def safe_audit_event(event: Mapping[str, Any]) -> dict[str, str]:
    """Stage S05: construct an allowlisted audit record without protected text."""
    raise NotImplementedError("M2.2: implement safe structured audit fields")


def remaining_budget(deadline: float, now: float) -> float:
    """Stage S08: return nonnegative time remaining under one request deadline."""
    raise NotImplementedError("M2.2: implement monotonic deadline accounting")


def retry_decision(error_kind: str, attempt: int, remaining_seconds: float) -> bool:
    """Stage S08: permit bounded retry for transient failures only."""
    raise NotImplementedError("M2.2: implement bounded retry policy")


def evaluate_evidence(receipts: Mapping[str, Any]) -> dict[str, Any]:
    """Stage S10: fail closed; issue review eligibility, never learner mastery."""
    raise NotImplementedError("M2.2: implement evidence gate")
