"""CAP-44 instructor types and tokenization; ranking is learner work."""

from dataclasses import dataclass
import re


@dataclass(frozen=True)
class Runbook:
    source_id: str
    version: str
    allowed_owners: list[str]
    text: str


@dataclass(frozen=True)
class RankedHit:
    source_id: str
    version: str
    score: int
    excerpt: str


def tokenize(text: str) -> set[str]:
    """Return distinct lowercase ASCII alphanumeric tokens."""
    return set(re.findall(r"[a-z0-9]+", text.lower()))


def score_runbook(query_tokens: set[str], text: str) -> int:
    """Instructor scoring primitive; tests may spy on calls for denied rows."""
    return len(query_tokens & tokenize(text))


def rank_runbooks(
    query: str, owner_id: str, runbooks: list[Runbook], k: int
) -> list[RankedHit]:
    """Rank eligible runbooks. TODO: implement CAP-44's deterministic contract."""
    raise NotImplementedError("CAP-44 learner TODO: implement deterministic ranking")
