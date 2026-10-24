"""Learner starter seams for Days 225–238: local review of policy drafts.

This module is intentionally incomplete. Inputs and examples are synthetic;
there is no model client, network access, persistent store, or policy mutation.
The learner owns the functions marked TODO and must keep approval a separate,
explicit human review action.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Literal

ABSTAIN_TEXT = "Not enough approved evidence to answer."
DECISIONS = ("approve", "reject")


@dataclass(frozen=True)
class SourceExcerpt:
    owner_id: str
    source_id: str
    version: str
    text: str

    @property
    def citation(self) -> str:
        return f"{self.source_id}@{self.version}"


@dataclass(frozen=True)
class PolicySnapshot:
    owner_id: str
    policy_version: str
    snapshot_sha256: str
    excerpts: tuple[SourceExcerpt, ...]


@dataclass(frozen=True)
class DraftCandidate:
    draft_id: str
    owner_id: str
    policy_version: str
    snapshot_sha256: str
    text: str
    citations: tuple[str, ...]


@dataclass(frozen=True)
class ExplanationDraft:
    draft_id: str
    owner_id: str
    policy_version: str
    snapshot_sha256: str
    text: str
    citations: tuple[str, ...]
    status: Literal["DRAFT", "APPROVED", "REJECTED"] = "DRAFT"


@dataclass(frozen=True)
class AuditEvent:
    sequence: int
    draft_id: str
    owner_id: str
    reviewer_id: str
    decision: Literal["approve", "reject"]
    from_status: str
    to_status: str
    policy_version: str
    snapshot_sha256: str
    idempotency_key: str


@dataclass(frozen=True)
class ReviewReceipt:
    idempotency_key: str
    command_fingerprint: tuple[str, ...]
    event_sequence: int


@dataclass(frozen=True)
class ReviewLedger:
    drafts: tuple[ExplanationDraft, ...] = ()
    audit: tuple[AuditEvent, ...] = ()
    receipts: tuple[ReviewReceipt, ...] = ()

    @property
    def revision(self) -> int:
        return len(self.audit)


@dataclass(frozen=True)
class ReviewCommand:
    draft_id: str
    reviewer_id: str
    reviewer_owner_id: str
    decision: Literal["approve", "reject"]
    expected_policy_version: str
    expected_snapshot_sha256: str
    expected_revision: int
    idempotency_key: str


@dataclass(frozen=True)
class ReviewResult:
    status: Literal["approved", "rejected", "denied", "conflict", "replayed"]
    ledger: ReviewLedger
    event: AuditEvent | None = None


@dataclass(frozen=True)
class EvaluationCase:
    case_id: str
    owner_id: str
    candidate: DraftCandidate | None
    expected_status: str
    expected_citations: tuple[str, ...]


def prepare_draft(snapshot: PolicySnapshot, candidate: DraftCandidate) -> ExplanationDraft:
    """Validate exact source/version citations and return an immutable DRAFT.

    TODO (Days 226–227): reject stale snapshots, altered or unsupported text,
    foreign-owner/missing/stale citations, and malformed candidate IDs. A
    rejected candidate must lead to abstention; never infer an approval.
    """
    raise NotImplementedError("Days 226–227 learner TODO: bind a draft to exact authorized evidence")


def add_draft(ledger: ReviewLedger, draft: ExplanationDraft) -> ReviewLedger:
    """Add one unique DRAFT without changing policy/source state.

    TODO (Days 228–229): accept only a well-formed DRAFT; reject duplicate IDs
    and never create audit events or change the synthetic policy snapshot.
    """
    raise NotImplementedError("Days 228–229 learner TODO: stage an immutable draft without approval")


def review_draft(ledger: ReviewLedger, snapshot: PolicySnapshot, command: ReviewCommand) -> ReviewResult:
    """Apply a version-bound, owner-scoped, idempotent human review decision.

    TODO (Days 230–234, 237): enforce human reviewer/owner scope, allowed
    DRAFT→APPROVED or DRAFT→REJECTED transitions, current policy version and
    expected ledger revision. Exact replays return the original event without
    adding an audit row; key reuse with different intent and stale revisions
    conflict without mutation. Audit entries are append-only and minimal.
    """
    raise NotImplementedError("Days 230–234 learner TODO: authorize, version, replay, and audit review")


def render_explanation(ledger: ReviewLedger, draft_id: str, viewer_owner_id: str) -> dict[str, object]:
    """Expose an explanation only after approval and only within its owner.

    TODO (Days 235, 238): return exact cited text for an approved same-owner
    draft; otherwise return the exact abstention with no citations or content.
    """
    raise NotImplementedError("Days 235 and 238 learner TODO: expose only approved owner-scoped text")


def classify_generation_failure(error_code: str) -> dict[str, object]:
    """Map a synthetic adapter failure to a fail-closed, non-mutating result.

    TODO (Days 236, 238): timeout, unavailable, malformed and unknown failures
    all abstain; no failure may create a draft, approve, or append an audit.
    Do not add retries or a live provider client.
    """
    raise NotImplementedError("Days 236 and 238 learner TODO: fail closed on adapter errors")


def evaluate_cases(cases: tuple[EvaluationCase, ...], snapshot: PolicySnapshot) -> dict[str, int]:
    """Compute fixed local metrics against labels authored outside the system.

    TODO (Days 225, 235, 238): count exact citation, abstention, leakage and
    approval-state outcomes from independent case labels; never call a model or
    derive expected results from prepare_draft/review_draft.
    """
    raise NotImplementedError("Days 225, 235, and 238 learner TODO: measure independent local cases")
