"""Offline evaluator for fixed expected outcomes; it never calls a model/API."""
from __future__ import annotations
from dataclasses import dataclass
from .evidence import Answer, Clause, explain

@dataclass(frozen=True)
class Case:
    question: str
    owner_id: str
    expected_status: str
    expected_citations: tuple[str, ...]

CASES = (
    Case("What renewal account requirement applies?", "owner-a", "cited", ("synthetic-renewal-01@v1",)),
    Case("What private account condition applies?", "owner-a", "abstain", ()),
    Case("What claims must include receipt?", "owner-a", "cited", ("synthetic-claim-02@v1",)),
)

def evaluate(cases: tuple[Case, ...], clauses: tuple[Clause, ...]) -> dict[str, int]:
    counts = {"total": len(cases), "cited": 0, "abstained": 0, "exact_match": 0, "citation_mismatch": 0}
    for case in cases:
        result = explain(case.question, owner_id=case.owner_id, clauses=clauses)
        counts["cited" if result.status == "cited" else "abstained"] += 1
        exact = result.status == case.expected_status and result.citations == case.expected_citations
        counts["exact_match"] += int(exact)
        counts["citation_mismatch"] += int(result.status == "cited" and result.citations != case.expected_citations)
    return counts
