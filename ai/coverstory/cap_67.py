"""CAP-67 metric value objects; ranking math stays learner-owned."""
from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class CaseMetrics:
    ranked_ids: tuple[str, ...]
    relevant_ids: frozenset[str]
    k: int
    applicable: bool
    recall_at_k: float | None
    reciprocal_rank: float | None


@dataclass(frozen=True)
class AggregateMetrics:
    answerable_count: int
    mean_recall_at_k: float | None
    mean_reciprocal_rank: float | None


def metrics_at_k(ranked_ids: list[str], relevant_ids: set[str], k: int) -> CaseMetrics:
    # LEARNER TODO: validate unique ranked IDs and k, compute recall@k and RR;
    # empty relevance is non-applicable with null metrics.
    raise NotImplementedError("CAP-67 learner TODO: implement metrics_at_k")


def aggregate(case_metrics: list[CaseMetrics]) -> AggregateMetrics:
    # LEARNER TODO: exclude non-applicable cases and retain an explicit count.
    raise NotImplementedError("CAP-67 learner TODO: implement aggregate")
