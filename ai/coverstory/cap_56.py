"""CAP-56 metric record types; evaluator remains learner-owned."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Callable


@dataclass(frozen=True)
class EvaluationCase:
    case_id: str
    query: str
    answerable: bool
    relevant_source_ids: frozenset[str]
    authorized_source_ids: frozenset[str]


@dataclass(frozen=True)
class CaseMetric:
    case_id: str
    hit_at_k: int | None
    retrieved_source_ids: tuple[str, ...]


@dataclass(frozen=True)
class BaselineReport:
    k: int
    answerable_count: int
    unanswerable_count: int
    macro_hit_at_k: float | None
    cases: tuple[CaseMetric, ...]
    corpus_sha256: str
    config_sha256: str


class AuthorizationViolation(ValueError):
    pass


def evaluate_retriever(cases: list[EvaluationCase], retriever: Callable[[str, int], list[str]], k: int,
                       corpus_sha256: str, config_sha256: str) -> BaselineReport:
    # LEARNER TODO: validate inputs, record per-case hit@k, fail closed on
    # unauthorized results, and macro-average answerable cases only.
    raise NotImplementedError("CAP-56 learner TODO: implement evaluate_retriever")
