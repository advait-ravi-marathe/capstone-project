"""CAP-86 source-grounded onboarding answer policy seam.

Inputs and types are supplied for the exercise. The learner owns only the
bounded decision policy; this module performs no retrieval, networking, writes,
or application mutation.
"""
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import Literal


@dataclass(frozen=True)
class SourceEvidence:
    source_id: str
    owner_id: str
    version: str
    excerpt: str
    content_sha256: str
    observed_at: datetime
    score: float
    source_label: str = "SYNTHETIC"


@dataclass(frozen=True)
class Citation:
    source_id: str
    version: str
    content_sha256: str


@dataclass(frozen=True)
class AnswerDecision:
    status: Literal["ANSWER", "ABSTAIN"]
    explanation: str
    citations: tuple[Citation, ...]
    manual_path: bool
    writes: int = 0


def decide_onboarding_answer(
    principal_id: str,
    hits: tuple[SourceEvidence, ...],
    now: datetime,
    score_threshold: float = 0.7,
) -> AnswerDecision:
    """Allow only authorized, fresh, digest-valid, sufficiently scored source.

    LEARNER TODO: apply the safety gates in order: authorization, freshness,
    digest, threshold, then answer policy. Every rejected or absent source must
    abstain with no citations and a manual-help path. Do not write a draft.
    """
    raise NotImplementedError("CAP-86 learner TODO: implement source answer policy")
