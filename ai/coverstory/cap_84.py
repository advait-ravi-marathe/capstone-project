"""CAP-84 pure mock-provider callback schema/domain validation seam.

The validator consumes a trusted sandbox batch context, returns one normalized
eligible command input, and has no Payments mutation or provider client.
"""
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import Literal, Protocol

ProviderState = Literal["PENDING", "FAILED", "UNKNOWN", "SUCCEEDED"]
ValidationCode = Literal[
    "VALID", "INVALID_TYPE", "MISSING_FIELD", "UNKNOWN_FIELD", "INVALID_TIMEZONE",
    "OWNER_MISMATCH", "PAYMENT_MISMATCH", "UNSUPPORTED_SOURCE", "INVALID_SCHEMA",
    "INVALID_VALUE",
]

REQUIRED_FIELDS = frozenset({
    "ownerId", "eventId", "providerRef", "schemaVersion", "occurredAt",
    "amountMinor", "currency", "state",
})
ALLOWED_STATES = frozenset({"PENDING", "FAILED", "UNKNOWN", "SUCCEEDED"})


class WriteProbe(Protocol):
    """Test-only counter that records accidental side-effect attempts."""
    def attempted_write(self) -> None: ...


@dataclass(frozen=True)
class BatchContext:
    trusted_owner_id: str
    expected_amount_minor: int
    expected_currency: str
    provider_source: str
    provider_source_supported: bool
    write_probe: WriteProbe | None = None


@dataclass(frozen=True)
class NormalizedProviderEvent:
    owner_id: str
    event_id: str
    provider_ref: str
    provider_source: str
    schema_version: int
    occurred_at: datetime
    amount_minor: int
    currency: str
    state: ProviderState


@dataclass(frozen=True)
class ValidationOutcome:
    code: ValidationCode
    normalized: NormalizedProviderEvent | None
    diagnostics: tuple[str, ...] = ()
    eligible_command_input: bool = False
    writes: int = 0
    onboarding_unlocked: bool = False


def validate_provider_event(raw: dict, trusted: BatchContext) -> ValidationOutcome:
    """Validate shape, sandbox owner/source and exact expected money before state use."""
    # LEARNER TODO: implement exact JSON types (bool is not an integer), strict
    # required/unknown-field handling, aware-UTC normalization, trusted owner,
    # source authorization and exact amount/currency matching before state
    # classification. Return only safe diagnostics (field names/codes); never
    # echo values, perform writes, call a provider or unlock onboarding.
    raise NotImplementedError("CAP-84 learner TODO: validate provider event envelope")
