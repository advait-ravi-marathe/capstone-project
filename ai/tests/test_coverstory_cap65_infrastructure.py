from __future__ import annotations

import json
import unittest
from pathlib import Path

from ai.coverstory.cap_65 import (
    AccessControl,
    AccessDenied,
    Candidate,
    Principal,
    RetrievalQuery,
    SourceDocument,
    retrieve_for_principal,
)

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-65/synthetic-v1.json"


class ScoreWitness:
    def __init__(self, documents: tuple[SourceDocument, ...]):
        self.documents = documents
        self.calls: list[tuple[Candidate, ...]] = []

    def score(self, query: str, candidates: list[Candidate], k: int):
        self.calls.append(tuple(candidates))
        return [type("WitnessHit", (), {"source_id": c.source_id, "text": c.text, "score": 1.0})()
                for c in candidates[:k]]


def load_data():
    data = json.loads(FIXTURE.read_text(encoding="utf-8"))
    principals = {key: Principal(row["subject"], frozenset(row["roles"]))
                  for key, row in data["principals"].items()}
    docs = []
    for row in data["documents"]:
        acl_data = row.get("acl")
        acl = None if acl_data is None else AccessControl(
            acl_data["owner_id"], frozenset(acl_data["assigned_support_ids"])
        )
        docs.append(SourceDocument(row["source_id"], row["visibility"], row.get("public_text"),
                                   row.get("applicant_text"), row.get("support_excerpt"), acl))
    return data, principals, tuple(docs)


def instructor_reference(principal, query: RetrievalQuery, registry: ScoreWitness, k: int):
    """Independent setup oracle: authorize and minimize text before score()."""
    if query.owner_id is not None and (principal is None or query.owner_id != principal.subject):
        raise AccessDenied("owner override")
    if query.scope not in {"public", "private", "all"} or k < 0:
        raise ValueError("invalid retrieval request")
    if query.scope == "private" and (principal is None or not principal.authenticated):
        raise AccessDenied("authentication required")
    private_requested = query.scope == "private"
    if private_requested and principal and "catalog_editor" in principal.roles:
        raise AccessDenied("role has no private access")

    allowed: list[Candidate] = []
    for document in registry.documents:
        if document.visibility == "public":
            if query.scope in {"public", "all"} and document.public_text:
                allowed.append(Candidate(document.source_id, document.public_text))
            continue
        if query.scope == "public":
            continue
        if principal is None or not principal.authenticated or "catalog_editor" in principal.roles:
            continue
        # Missing ACL means no owner or assignment grant.
        acl = document.acl
        if acl is None:
            continue
        if acl.owner_id is not None and principal.subject == acl.owner_id and "applicant" in principal.roles:
            if document.applicant_text:
                allowed.append(Candidate(document.source_id, document.applicant_text))
        elif principal.subject in acl.assigned_support_ids and "support" in principal.roles:
            if document.support_excerpt:
                allowed.append(Candidate(document.source_id, document.support_excerpt))
    if private_requested and not allowed:
        raise AccessDenied("no explicitly authorized private sources")
    if not allowed:
        return []
    # Only the authorized, role-specific text crosses this point.
    return registry.score(query.text, allowed, k)


class CAP65InfrastructureTest(unittest.TestCase):
    def setUp(self) -> None:
        self.fixture, self.principals, self.documents = load_data()

    def test_fixture_and_typed_seam_are_present(self) -> None:
        self.assertTrue(self.fixture["synthetic"])
        self.assertEqual([f"CAP-65-CS-0{i}" for i in range(1, 5)],
                         [case["id"] for case in self.fixture["cases"]])
        self.assertEqual("source_id", next(iter(SourceDocument.__dataclass_fields__)))
        self.assertTrue(callable(retrieve_for_principal))
        with self.assertRaisesRegex(NotImplementedError, "CAP-65 learner TODO"):
            retrieve_for_principal(self.principals["visitor"], RetrievalQuery("x"), ScoreWitness(()), 1)

    def test_public_wording_and_applicant_owner_scope_witness(self) -> None:
        registry = ScoreWitness(self.documents)
        hits = instructor_reference(self.principals["visitor"], RetrievalQuery("waiting", scope="public"), registry, 5)
        self.assertEqual(["public-wording-1"], [hit.source_id for hit in hits])
        self.assertEqual(["public-wording-1"], [c.source_id for c in registry.calls[0]])

        registry = ScoreWitness(self.documents)
        hits = instructor_reference(self.principals["applicant_a"], RetrievalQuery("callback", scope="private"), registry, 5)
        self.assertEqual(["case-A-docA"], [hit.source_id for hit in hits])
        self.assertEqual(["case-A-docA"], [c.source_id for c in registry.calls[0]])
        a = next(row for row in self.documents if row.source_id == "case-A-docA")
        b = next(row for row in self.documents if row.source_id == "case-B-docB")
        self.assertEqual(a.applicant_text, b.applicant_text)  # equal-content leakage witness

    def test_support_assignment_receives_only_minimal_excerpt(self) -> None:
        registry = ScoreWitness(self.documents)
        hits = instructor_reference(self.principals["support_s"], RetrievalQuery("callback", scope="private"), registry, 5)
        self.assertEqual(["case-A-docA", "support-only-A"], [hit.source_id for hit in hits])
        self.assertEqual(["Callback requested.", "Please call after 4 pm."], [c.text for c in registry.calls[0]])
        self.assertNotIn("Synthetic case: applicant requested a callback.", [c.text for c in registry.calls[0]])
        self.assertNotIn("case-B-docB", [c.source_id for c in registry.calls[0]])

    def test_editor_missing_acl_override_and_anonymous_denials_precede_scoring(self) -> None:
        no_acl = tuple(d for d in self.documents if d.source_id == "case-A-docA-no-acl")
        denied_requests = (
            (self.principals["catalog_editor"], RetrievalQuery("private", scope="private"),
             tuple(d for d in self.documents if d.visibility == "private")),
            (self.principals["applicant_a_no_acl"], RetrievalQuery("private", scope="private"), no_acl),
            (self.principals["applicant_a"], RetrievalQuery("private", owner_id="applicant-B", scope="private"), no_acl),
            (self.principals["visitor"], RetrievalQuery("private", scope="private"), no_acl),
            (None, RetrievalQuery("private", scope="private"), no_acl),
        )
        for principal, query, docs in denied_requests:
            registry = ScoreWitness(docs)
            with self.subTest(principal=principal, query=query), self.assertRaises(AccessDenied):
                instructor_reference(principal, query, registry, 3)
            self.assertEqual([], registry.calls)

    def test_post_score_redaction_mutant_is_detectable(self) -> None:
        """A global-search-then-filter mutant leaks at the score boundary."""
        registry = ScoreWitness(self.documents)
        all_private = [Candidate(d.source_id, d.applicant_text or "") for d in registry.documents
                       if d.visibility == "private"]
        registry.score("callback", all_private, 10)  # deliberate defective ordering
        scored_ids = {candidate.source_id for candidate in registry.calls[0]}
        self.assertIn("case-B-docB", scored_ids)
        self.assertNotEqual({"case-A-docA"}, scored_ids)


if __name__ == "__main__":
    unittest.main()
