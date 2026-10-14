"""CAP-47 fixture types for a tiny citation/abstention evaluation contract."""
from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class Citation:
    source_id: str
    version: str


@dataclass(frozen=True)
class EvalCase:
    case_id: str
    answerable: bool
    allowed_citations: frozenset[tuple[str, str]]


@dataclass(frozen=True)
class Answer:
    abstained: bool
    citations: tuple[Citation, ...]


@dataclass(frozen=True)
class EvalReport:
    total_cases: int
    supported_answers: int
    correct_abstentions: int
    unsupported: int


def evaluate(cases: list[EvalCase], answers: dict[str, Answer]) -> EvalReport:
    # LEARNER TODO: require one answer per case, validate each source/version
    # against that case's allowed set, and count supported/abstained/unsupported.
    raise NotImplementedError("CAP-47 learner TODO: implement citation evaluation")
