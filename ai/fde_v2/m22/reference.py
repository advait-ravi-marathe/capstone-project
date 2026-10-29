"""Instructor-only deterministic mechanics for the conditional M2.2 starter.

This reference is not an identity provider, deployment, or learner evidence.
"""
from __future__ import annotations

import math
from collections.abc import Mapping, Sequence
from typing import Any


def verify_identity(token, verified_claims_provider, subject_lookup, *, expected_issuer,
                    expected_audience, now):
    """Adapter mechanics after, and only after, an established verifier succeeds.

    The callback is expected to reject invalid signatures/algorithms. A test
    callback is only a mechanics fixture, never actual authentication evidence.
    """
    if not isinstance(token, str) or not token.strip() or not callable(verified_claims_provider) or not callable(subject_lookup):
        raise ValueError("token and verified identity adapters are required")
    claims = verified_claims_provider(token)
    if not isinstance(claims, Mapping):
        raise ValueError("verified token claims must be an object")
    subject = claims.get("sub")
    issuer = claims.get("iss")
    audience = claims.get("aud")
    expires = claims.get("exp")
    audiences = audience if isinstance(audience, (list, tuple)) else [audience]
    if (not isinstance(subject, str) or not subject.strip() or issuer != expected_issuer
            or expected_audience not in audiences or type(expires) not in (int, float)
            or not math.isfinite(expires) or expires <= now):
        raise ValueError("token claims do not satisfy the identity contract")
    identity = subject_lookup(subject)
    if not isinstance(identity, Mapping) or identity.get("enabled") is not True:
        raise ValueError("subject is absent or disabled")
    tenant = identity.get("tenant_id")
    if not isinstance(tenant, str) or not tenant.strip():
        raise ValueError("subject has no active tenant mapping")
    return {"subject_id": subject, "tenant_id": tenant}


def authorize_document(context: Mapping[str, Any], document: Mapping[str, Any], action: str) -> bool:
    """Small policy oracle: tenant match plus explicit subject ACL, default deny."""
    if not isinstance(context, Mapping) or not isinstance(document, Mapping):
        return False
    subject = context.get("subject_id")
    tenant = context.get("tenant_id")
    if (not isinstance(subject, str) or not subject.strip()
            or not isinstance(tenant, str) or not tenant.strip()
            or document.get("tenant_id") != tenant or document.get("deleted") is not False):
        return False
    owner = document.get("owner_id")
    readers = document.get("reader_ids", [])
    writers = document.get("writer_ids", [])
    if not isinstance(readers, (list, tuple)) or not isinstance(writers, (list, tuple)):
        return False
    if action == "read":
        return subject == owner or subject in readers or subject in writers
    if action in {"write", "delete"}:
        return subject == owner or subject in writers
    return False


def filter_retrieved_hits(context: Mapping[str, Any], hits: Sequence[Mapping[str, Any]],
                          authoritative_documents: Mapping[str, Mapping[str, Any]]) -> list[Mapping[str, Any]]:
    """Post-filter candidates; callers must also apply a server-built DB filter."""
    if not isinstance(hits, (list, tuple)) or not isinstance(authoritative_documents, Mapping):
        return []
    allowed = []
    seen = set()
    for hit in hits:
        if not isinstance(hit, Mapping):
            continue
        document_id = hit.get("document_id")
        if not isinstance(document_id, str) or document_id in seen:
            continue
        policy = authoritative_documents.get(document_id)
        if isinstance(policy, Mapping) and authorize_document(context, policy, "read"):
            allowed.append(dict(hit))
            seen.add(document_id)
    return allowed


def safe_audit_event(event: Mapping[str, Any]) -> dict[str, str]:
    """Allowlist metadata; never forward arbitrary request/model exception fields."""
    required = ("subject_pseudonym", "action", "outcome", "trace_id", "timestamp")
    if not isinstance(event, Mapping) or any(
            not isinstance(event.get(key), str) or not event[key].strip() for key in required):
        raise ValueError("audit event needs nonempty allowlisted metadata")
    return {key: event[key].strip() for key in required}


def remaining_budget(deadline: float, now: float) -> float:
    if any(type(value) not in (int, float) or not math.isfinite(value) for value in (deadline, now)):
        raise ValueError("deadline and current monotonic time must be finite numbers")
    return max(0.0, float(deadline - now))


def retry_decision(error_kind: str, attempt: int, remaining_seconds: float) -> bool:
    if type(attempt) is not int or attempt < 1:
        raise ValueError("attempt starts at one")
    if type(remaining_seconds) not in (int, float) or not math.isfinite(remaining_seconds) or remaining_seconds < 0:
        raise ValueError("remaining_seconds must be finite and nonnegative")
    return error_kind == "transient" and attempt < 2 and remaining_seconds > 0


REQUIRED_EVIDENCE = frozenset({
    "m21_prerequisite_passed", "identity_flow_actual", "server_access_tests_actual",
    "retrieval_boundary_actual", "threat_model_reviewed", "secret_log_scan_actual",
    "retention_deletion_actual", "clean_reproducible_build_actual",
    "trace_sample_redacted_actual", "cost_performance_budget_reviewed", "learner_explanation_reviewed",
})


def evaluate_evidence(receipts: Mapping[str, Any]) -> dict[str, Any]:
    """Only grant independent-review eligibility when every actual gate is evidenced."""
    if not isinstance(receipts, Mapping):
        raise ValueError("receipts must be an object")
    missing = sorted(REQUIRED_EVIDENCE - set(receipts))
    false_or_nonactual = sorted(key for key in REQUIRED_EVIDENCE & set(receipts)
                                if receipts.get(key) is not True)
    if missing or false_or_nonactual:
        return {"status": "BLOCKED", "m22_pass": False,
                "missing": missing, "not_verified": false_or_nonactual}
    return {"status": "ELIGIBLE_FOR_INDEPENDENT_REVIEW", "m22_pass": False,
            "missing": [], "not_verified": [],
            "note": "evidence still requires independent review; this helper cannot pass a learner milestone"}
