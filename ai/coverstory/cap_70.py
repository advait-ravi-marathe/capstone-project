"""CAP-70 instructor types; immutable protected-payload staging stays learner-owned."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Iterable, Protocol


class StageError(ValueError):
    """Safe diagnostic containing only a physical input line and stable error code."""

    def __init__(self, line_number: int, code: str):
        super().__init__(f"line {line_number}: {code}")
        self.line_number = line_number
        self.code = code


@dataclass(frozen=True)
class ProtectedEnvelope:
    envelope_id: str
    owner_id: str
    object_id: str
    object_revision: int
    ciphertext_with_tag: bytes
    nonce: bytes
    wrapped_dek: bytes
    kek_reference: str
    encryption_version: int


@dataclass(frozen=True)
class StagedEnvelope:
    envelope: ProtectedEnvelope
    source_name: str
    source_line: int
    ciphertext_sha256: str


@dataclass(frozen=True)
class StagedEvidence:
    envelope_id: str
    object_id: str
    object_revision: int
    source_name: str
    source_line: int
    ciphertext_sha256: str


@dataclass(frozen=True)
class StageReport:
    batch_id: str
    records: tuple[StagedEvidence, ...]
    skipped_blank_lines: int


class ProtectedEnvelopeSink(Protocol):
    """Adapter boundary. Implementations must authorize before authenticating/decrypting."""

    def owns_reference(self, owner_id: str, object_id: str, revision: int) -> bool: ...
    def validate_authenticated_envelope(self, envelope: ProtectedEnvelope) -> None: ...
    def store_immutable_batch(self, batch_id: str, rows: tuple[StagedEnvelope, ...]) -> None: ...


def stage_envelopes(batch_id: str, lines: Iterable[str], protectedEnvelopeSink: ProtectedEnvelopeSink) -> StageReport:
    # LEARNER TODO: parse physical lines with a strict field allowlist, preserve
    # blank-line numbering, authorize ownership before envelope validation,
    # reject plaintext/unknown fields with safe line/code diagnostics, hash exact
    # ciphertext bytes, and atomically stage the full valid batch as immutable
    # ciphertext-only rows. Keep fixed vectors separate from generated nonce tests.
    raise NotImplementedError("CAP-70 learner TODO: stage protected envelope batch")
