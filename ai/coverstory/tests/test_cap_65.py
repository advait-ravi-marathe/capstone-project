from __future__ import annotations

import json
import unittest
from pathlib import Path

from ai.coverstory.cap_65 import (
    AccessControl,
    AccessDenied,
    Candidate,
    Hit,
    Principal,
    RetrievalQuery,
    SourceDocument,
    retrieve_for_principal,
)

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-65/synthetic-v1.json"


class ScoringRegistry:
    """Instructor supplied scorer seam with an inspectable call boundary."""

    def __init__(self, documents: tuple[SourceDocument, ...]):
        self.documents = documents
        self.scored: list[tuple[Candidate, ...]] = []

    def score(self, query: str, candidates: list[Candidate], k: int) -> list[Hit]:
        self.scored.append(tuple(candidates))
        return [Hit(candidate.source_id, candidate.text, 1.0) for candidate in candidates[:k]]


def load_fixture() -> tuple[dict, dict[str, Principal], tuple[SourceDocument, ...]]:
    data = json.loads(FIXTURE.read_text(encoding="utf-8"))
    principals = {
        name: Principal(row["subject"], frozenset(row["roles"]))
        for name, row in data["principals"].items()
    }
    documents = []
    for row in data["documents"]:
        raw_acl = row.get("acl")
        acl = None if raw_acl is None else AccessControl(
            raw_acl["owner_id"], frozenset(raw_acl["assigned_support_ids"])
        )
        documents.append(SourceDocument(
            source_id=row["source_id"],
            visibility=row["visibility"],
            public_text=row.get("public_text"),
            applicant_text=row.get("applicant_text"),
            support_excerpt=row.get("support_excerpt"),
            acl=acl,
        ))
    return data, principals, tuple(documents)


class CAP65AcceptanceTest(unittest.TestCase):
    def setUp(self) -> None:
        self.fixture, self.principals, self.documents = load_fixture()
        self.registry = ScoringRegistry(self.documents)

    def test_visitor_can_retrieve_only_public_wording(self) -> None:
        public_only = tuple(row for row in self.documents if row.visibility == "public")
        registry = ScoringRegistry(public_only)
        hits = retrieve_for_principal(self.principals["visitor"], RetrievalQuery("waiting period", scope="public"), registry, 5)
        self.assertEqual(["public-wording-1"], [hit.source_id for hit in hits])
        self.assertEqual([["public-wording-1"]], [[c.source_id for c in call] for call in registry.scored])

    def test_applicant_gets_own_case_even_when_other_case_text_is_identical(self) -> None:
        hits = retrieve_for_principal(self.principals["applicant_a"], RetrievalQuery("callback", scope="private"), self.registry, 10)
        self.assertEqual(["case-A-docA"], [hit.source_id for hit in hits])
        self.assertEqual(["Synthetic case: applicant requested a callback."], [hit.text for hit in hits])
        self.assertEqual(["case-A-docA"], [candidate.source_id for candidate in self.registry.scored[0]])

    def test_support_gets_assigned_case_minimal_excerpts_only(self) -> None:
        hits = retrieve_for_principal(self.principals["support_s"], RetrievalQuery("callback", scope="private"), self.registry, 10)
        self.assertEqual(["case-A-docA", "support-only-A"], [hit.source_id for hit in hits])
        self.assertEqual(["Callback requested.", "Please call after 4 pm."], [hit.text for hit in hits])
        self.assertEqual([hit.text for hit in hits], [candidate.text for candidate in self.registry.scored[0]])
        self.assertNotIn("Synthetic case: applicant requested a callback.", [c.text for c in self.registry.scored[0]])

    def test_catalog_editor_and_missing_acl_are_denied_before_scoring(self) -> None:
        private_only = tuple(row for row in self.documents if row.visibility == "private")
        for principal in (self.principals["catalog_editor"], self.principals["applicant_a_no_acl"]):
            registry = ScoringRegistry(private_only)
            with self.subTest(principal=principal), self.assertRaises(AccessDenied):
                retrieve_for_principal(principal, RetrievalQuery("restricted", scope="private"), registry, 5)
            self.assertEqual([], registry.scored)

    def test_owner_override_and_unauthenticated_private_access_fail_before_scoring(self) -> None:
        for principal, query in (
            (self.principals["applicant_a"], RetrievalQuery("callback", owner_id="applicant-B")),
            (self.principals["visitor"], RetrievalQuery("private case", scope="private")),
            (None, RetrievalQuery("private case", scope="private")),
        ):
            registry = ScoringRegistry(tuple(row for row in self.documents if row.visibility == "private"))
            with self.subTest(principal=principal, query=query), self.assertRaises(AccessDenied):
                retrieve_for_principal(principal, query, registry, 5)
            self.assertEqual([], registry.scored)

    def test_fixture_keeps_saved_case_ids_and_synthetic_notice(self) -> None:
        self.assertTrue(self.fixture["synthetic"])
        self.assertEqual([f"CAP-65-CS-0{i}" for i in range(1, 5)], [row["id"] for row in self.fixture["cases"]])
        self.assertIn("No consumer", self.fixture["data_notice"])


if __name__ == "__main__":
    unittest.main()
