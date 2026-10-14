"""CAP-61 abstention data contract; answer selection remains learner-owned."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Literal


@dataclass(frozen=True)
class EvidenceHit:
    score: float
    authorized: bool
    applicable: bool
    text: str
    citation: tuple[str, str]


@dataclass(frozen=True)
class AnswerDecision:
    decision: Literal["ANSWER", "ABSTAIN"]
    text: str | None = None
    citation: tuple[str, str] | None = None


def decide_answer(authorized_hits: list[EvidenceHit], threshold: float) -> AnswerDecision:
    # LEARNER TODO: validate finite [0,1] scores/threshold, require authorized
    # applicable evidence meeting the inclusive threshold, else abstain.
    raise NotImplementedError("CAP-61 learner TODO: implement decide_answer")
