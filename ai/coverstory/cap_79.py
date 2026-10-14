"""CAP-79 source freshness contract; the filtering behavior remains learner-owned."""
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta


@dataclass(frozen=True)
class SourceHit:
    source_id: str
    version: str
    content_sha256: str
    evidence_type: str
    observed_at: datetime
    source_label: str = "SYNTHETIC"


@dataclass(frozen=True)
class RejectedSource:
    source_id: str
    version: str
    reason: str


@dataclass(frozen=True)
class FreshnessResult:
    status: str
    retained: tuple[SourceHit, ...]
    rejected: tuple[RejectedSource, ...]
    quote_order_eligible: bool = False


def filter_fresh(hits: list[SourceHit], now: datetime, max_age: timedelta) -> FreshnessResult:
    """Retain dated source versions inside the inclusive freshness interval."""
    # LEARNER TODO: check timezone awareness before subtraction, preserve each
    # exact source/version, and emit STALE or INVALID_TIME rejection reasons.
    raise NotImplementedError("CAP-79 learner TODO: implement fixed-clock source freshness")
