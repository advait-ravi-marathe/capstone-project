"""Learner starter for an offline, evidence-based certification review."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Any


@dataclass(frozen=True)
class Readiness:
    status: str
    reason_codes: tuple[str, ...]
    assessment_count: int
    exam_passed: bool = False
    booking_authorized: bool = False


def review_readiness(record: dict[str, Any], current_guide: dict[str, Any]) -> Readiness:
    """TODO Days 269–270: apply the proposal's internal evidence threshold.

    Require the exact currently verified guide/version/date/domain set, at least two
    distinct current-official-material timed assessments, >=80% on each,
    reviewed misses, and no unstudied domains. If official domain diagnostics
    are supplied, require each >=70%. This is only a booking-review signal;
    never claim a pass, readiness guarantee or authorization to book.
    """
    raise NotImplementedError("TODO: offline certification evidence review")
