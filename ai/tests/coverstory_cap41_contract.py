"""Shared public CAP-41 contract evaluator for learner and instructor tests."""
from ai.retrieval.coverstory_health import Hit


def expected_hits(case):
    query_tokens = set(case["query"].lower().split())
    rows = []
    for document in case["documents"]:
        score = len(query_tokens & set(document["text"].lower().split()))
        if score:
            rows.append(Hit(document["source_id"], document["version"], document["evidence_status"], document["text"], score))
    return sorted(rows, key=lambda hit: (-hit.score, hit.source_id))[:case["k"]]


def assert_matches(case, actual):
    expected = expected_hits(case)
    if expected != actual:
        raise AssertionError(f"CAP-41 mismatch: expected {expected!r}; got {actual!r}")
    return expected
