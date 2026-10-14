"""CAP-69 typed simulation seam; queue accounting remains learner-owned."""
from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class LoadRun:
    offered: int
    capacity: int
    rate_per_second: int
    strategy: str = "bounded"


@dataclass(frozen=True)
class QueueDecisionEvidence:
    strategy: str
    offered: int
    accepted: int
    rejected: int
    completed: int
    queue_max: int
    p95_ms: int | None
    drain_ms: int
    overload_eliminated: bool


def summarize(run: LoadRun) -> QueueDecisionEvidence:
    # LEARNER TODO: validate finite positive capacity/rate, then simulate the
    # deterministic burst. Capacity includes the active worker and waiting work.
    raise NotImplementedError("CAP-69 learner TODO: implement summarize")


def validate_claim(evidence: QueueDecisionEvidence, claim: str) -> bool:
    # LEARNER TODO: reject claims that buffering eliminated overload when work
    # was rejected; queueing may redistribute delay, not erase rejection.
    raise NotImplementedError("CAP-69 learner TODO: implement validate_claim")
