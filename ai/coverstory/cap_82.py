"""CAP-82 typed ranker comparison seam; evaluation logic is learner-owned."""
from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class EvaluationCase:
    case_id: str
    answerable: bool
    relevant_source_ids: frozenset[str]
    retrieved_source_ids: tuple[str, ...]
    query_owner: str
    source_owner_by_id: tuple[tuple[str, str], ...]


@dataclass(frozen=True)
class EvalRun:
    dataset_sha256: str
    corpus_sha256: str
    k: int
    cases: tuple[EvaluationCase, ...]
    latency_samples_ms: tuple[float, ...]


@dataclass(frozen=True)
class ComparisonReport:
    status: str
    reason: str | None
    baseline_mean_reciprocal_rank: float | None
    candidate_mean_reciprocal_rank: float | None
    delta_mean_reciprocal_rank: float | None
    baseline_mean_recall_at_k: float | None
    candidate_mean_recall_at_k: float | None
    delta_mean_recall_at_k: float | None
    baseline_latency_ms: float | None
    candidate_latency_ms: float | None
    changed_case_ids: tuple[str, ...]
    disqualified_case_ids: tuple[str, ...]


def compare_runs(baseline: EvalRun, candidate: EvalRun) -> ComparisonReport:
    # LEARNER TODO: require identical evaluation identity; disqualify any private
    # cross-owner hit before reporting metrics; retain regressions and latencies.
    raise NotImplementedError("CAP-82 learner TODO: implement compare_runs")
