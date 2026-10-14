"""CAP-44 typed fixture boundary; ranking remains learner-owned."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Literal


Visibility = Literal["PUBLIC_PUBLISHED", "RESTRICTED_EDITOR_DRAFT"]


@dataclass(frozen=True)
class SourceVersion:
    visibility: Visibility
    allowed_fixture_principals: frozenset[str]
    category: str
    scenario: str
    source_id: str
    version: str | None
    text: str

    def visible_to(self, principal: str | None) -> bool:
        """Instructor fixture adapter; it is not a production authorization policy."""
        if self.visibility == "PUBLIC_PUBLISHED":
            return True
        return principal is not None and principal in self.allowed_fixture_principals


@dataclass(frozen=True)
class RankedHit:
    source_id: str
    version: str
    score: int
    excerpt: str


def rank_wording(query: str, fixture_principal: str | None, sources: list[SourceVersion], k: int) -> list[RankedHit]:
    # LEARNER TODO: validate provenance, apply fixture visibility and scenario,
    # tokenize [a-z0-9]+, score distinct overlap, then sort deterministically.
    raise NotImplementedError("CAP-44 learner TODO: implement rank_wording")
