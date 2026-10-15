"""CAP-49 synthetic evidence-failure classification contract."""
from __future__ import annotations

from dataclasses import dataclass
from enum import Enum


class FailureCategory(str, Enum):
    PASS = "PASS"
    FALSE_ABSTENTION = "FALSE_ABSTENTION"
    MISSING_CITATION = "MISSING_CITATION"
    WRONG_SOURCE = "WRONG_SOURCE"
    WRONG_VERSION = "WRONG_VERSION"
    UNSUPPORTED_ANSWER = "UNSUPPORTED_ANSWER"


@dataclass(frozen=True)
class EvidenceCase:
    question_id: str
    answerable: bool
    expected_source_id: str | None
    expected_version: str | None


@dataclass(frozen=True)
class Citation:
    source_id: str
    version: str


@dataclass(frozen=True)
class EvidenceAnswer:
    abstained: bool
    citations: tuple[Citation, ...]


def classify_failure(case: EvidenceCase, answer: EvidenceAnswer) -> FailureCategory:
    # LEARNER TODO: preserve the supplied precedence for abstention, citation
    # presence, source identity, and source version.
    raise NotImplementedError("CAP-49 learner TODO: implement failure classification")
