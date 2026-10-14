"""CAP-72 provider batch loading contracts; business behavior is learner-owned."""
from __future__ import annotations

from dataclasses import dataclass
from typing import ContextManager, Literal, Protocol, Sequence

ObservationState = Literal["INITIATED", "PENDING", "FAILED", "UNKNOWN", "RECONCILED", "SUCCEEDED"]


@dataclass(frozen=True)
class ValidatedProviderRow:
    attempt_id: str
    operation_id: str
    provider_id: str
    event_id: str
    payload_hash: str
    amount_minor: int
    currency: str
    state: ObservationState
    request_id: str
    provider_reference: str


@dataclass(frozen=True)
class LoadReport:
    code: Literal["LOADED", "ALREADY_LOADED", "BATCH_CONFLICT", "EVENT_HASH_CONFLICT"]
    receipt_id: str
    observation_count: int


@dataclass(frozen=True)
class Manifest:
    batch_id: str
    content_hash: str
    receipt_id: str
    status: Literal["COMPLETED"]


@dataclass(frozen=True)
class ProviderObservation:
    batch_id: str
    row: ValidatedProviderRow
    ordinal: int


@dataclass(frozen=True)
class TransactionAuditEvent:
    attempt_id: str
    operation_id: str
    sequence: int
    event_id: str
    state: ObservationState
    amount_minor: int
    currency: str
    request_id: str
    provider_reference: str


class ManifestPort(Protocol):
    def find_manifest(self, batch_id: str) -> Manifest | None: ...
    def save_manifest(self, manifest: Manifest) -> None: ...


class ProviderEventPort(Protocol):
    def find_event(self, provider_id: str, event_id: str) -> ProviderObservation | None: ...
    def append_observation(self, observation: ProviderObservation) -> None: ...


class TransactionAuditPort(Protocol):
    def next_sequence(self, attempt_id: str) -> int: ...
    def append_audit(self, event: TransactionAuditEvent) -> None: ...


class PaymentEffectReadPort(Protocol):
    """Read-only owning Payments facts. Audit rows are not money effects."""
    def posted_effect_total(self, operation_id: str) -> tuple[int, str]: ...


class PaymentsUnitOfWork(Protocol):
    manifests: ManifestPort
    provider_events: ProviderEventPort
    transaction_audit: TransactionAuditPort
    payment_effects: PaymentEffectReadPort

    def transaction(self) -> ContextManager[PaymentsUnitOfWork]: ...


def load_provider_batch(
    batch_id: str,
    validated_rows: Sequence[ValidatedProviderRow],
    unit_of_work: PaymentsUnitOfWork,
) -> LoadReport:
    """Persist a validated batch and its ordered append-only audit atomically.

    Learner-owned behavior must preserve idempotency, conflict detection,
    rollback, UNKNOWN history, and the owning module's single effect record.
    """
    # LEARNER TODO: hash canonical validated input; replay or detect conflicts;
    # atomically append provider observations/audits and complete the manifest.
    raise NotImplementedError("CAP-72 learner TODO: implement load_provider_batch")
