"""CAP-81 authoritative audit completeness report contract.

The learner implements a pure report over instructor-owned, owner-scoped read
models. This module does not open a database, decrypt payloads, repair state, or
write analytics.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Literal, Mapping, Sequence


@dataclass(frozen=True)
class RunMetadata:
    commit_sha: str | None
    command: str | None


@dataclass(frozen=True)
class BusinessTransition:
    transition_id: str
    owner_id: str
    entity_type: str
    entity_id: str
    entity_version: int
    journey_id: str
    journey_version: int
    linked_effect_id: str | None = None


@dataclass(frozen=True)
class AuditRecord:
    audit_id: str
    owner_id: str
    transition_id: str
    entity_type: str
    entity_id: str
    entity_version: int
    journey_version: int
    linked_effect_id: str | None = None


@dataclass(frozen=True)
class JourneyProjection:
    journey_id: str
    owner_id: str
    version: int
    application_id: str
    application_revision: int
    protected_envelope_id: str | None


@dataclass(frozen=True)
class JourneyStepEvent:
    event_id: str
    owner_id: str
    journey_id: str
    transition_id: str
    journey_version: int
    sequence: int


@dataclass(frozen=True)
class PaymentObservation:
    observation_id: str
    owner_id: str
    operation_id: str
    state: Literal["FAILED", "UNKNOWN", "SUCCEEDED"]
    sequence: int
    provider_reference_id: str
    reconciled: bool = False
    linked_effect_id: str | None = None


@dataclass(frozen=True)
class PaymentEffect:
    effect_id: str
    owner_id: str
    operation_id: str
    amount_minor: int
    currency: str
    posted: bool


@dataclass(frozen=True)
class ProtectedPayloadReference:
    reference_id: str
    owner_id: str
    application_id: str
    application_revision: int
    envelope_id: str
    fingerprint_sha256: str
    storage_location: str
    plaintext_present: bool


@dataclass(frozen=True)
class RollbackProbe:
    boundary: str
    before: Mapping[str, tuple[str, ...]]
    after: Mapping[str, tuple[str, ...]]


@dataclass(frozen=True)
class AuthoritativeJourneySnapshot:
    owner_id: str
    run_metadata: RunMetadata
    transitions: Sequence[BusinessTransition]
    audit_records: Sequence[AuditRecord]
    journey: JourneyProjection | None
    journey_step_events: Sequence[JourneyStepEvent]
    history_sequences: Sequence[int]
    analytics_transition_ids: Sequence[str]
    payment_observations: Sequence[PaymentObservation]
    payment_effects: Sequence[PaymentEffect]
    protected_payload_references: Sequence[ProtectedPayloadReference]
    rollback_probes: Sequence[RollbackProbe]


@dataclass(frozen=True)
class Violation:
    code: str
    reference_id: str


@dataclass(frozen=True)
class InvariantReport:
    status: Literal["PASS", "FAIL", "UNVERIFIED"]
    violations: tuple[Violation, ...]
    counts: Mapping[str, int]


def check_invariants(snapshot: AuthoritativeJourneySnapshot) -> InvariantReport:
    """Report authoritative audit completeness without echoing payload content."""
    # LEARNER TODO: match each committed transition to its owner-scoped audit
    # identity and journey version/step event; verify current revision and
    # protected-envelope references; preserve append-only payment history and
    # count money only from linked payment effects. Ignore analytics as proof.
    # Check rollback snapshots, owner mismatches, duplicate plaintext state,
    # contiguous history and commit/command metadata. Return codes and safe IDs
    # only: never include draft text, hashes, request payloads or secrets.
    raise NotImplementedError("CAP-81 learner TODO: implement authoritative audit completeness report")
