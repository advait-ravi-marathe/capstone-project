"""Bounded evidence selection and explanation guard for a single local fixture."""
from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class Clause:
    clause_id: str
    version: str
    owner_id: str
    text: str


@dataclass(frozen=True)
class Answer:
    status: str
    explanation: str
    citations: tuple[str, ...]


def explain(question: str, *, owner_id: str, clauses: tuple[Clause, ...]) -> Answer:
    """Return exact clause text with a citation, or abstain without evidence.

    This intentionally small baseline matches literal question tokens against
    clause text. It is not semantic retrieval and makes no policy decision.
    """
    if not question.strip() or not owner_id.strip():
        raise ValueError("question and owner_id must be non-empty")
    owner_clauses = tuple(c for c in clauses if c.owner_id == owner_id)
    tokens = {token.casefold().strip(".,?!:;") for token in question.split()}
    matches = tuple(c for c in owner_clauses if tokens and len(tokens & {word.casefold().strip(".,?!:;") for word in c.text.split()}) >= 2)
    if len(matches) != 1:
        return Answer("abstain", "Insufficient matching approved evidence.", ())
    clause = matches[0]
    return Answer("cited", clause.text, (f"{clause.clause_id}@{clause.version}",))
