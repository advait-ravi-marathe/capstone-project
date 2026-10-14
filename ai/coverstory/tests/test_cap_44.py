from __future__ import annotations

import json
import unittest
from pathlib import Path

from ai.coverstory.cap_44 import SourceVersion, rank_wording

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-44/synthetic-v1.json"


def load_fixture() -> dict:
    return json.loads(FIXTURE.read_text(encoding="utf-8"))


def sources_from(fixture: dict) -> list[SourceVersion]:
    return [SourceVersion(
        visibility=row["visibility"], allowed_fixture_principals=frozenset(row["allowed_fixture_principals"]),
        category=row["category"], scenario=row["scenario"], source_id=row["source_id"],
        version=row["version"], text=row["text"],
    ) for row in fixture["sources"]]


class CAP44AcceptanceTest(unittest.TestCase):
    def test_public_wording_is_ranked_without_login_and_ties_are_stable(self) -> None:
        fixture = load_fixture()
        hits = rank_wording(fixture["query"], "Visitor", sources_from(fixture), 3)
        self.assertEqual([("w1", "v1", 2), ("w2", "v1", 1), ("w3", "v2", 1)],
                         [(hit.source_id, hit.version, hit.score) for hit in hits])
        self.assertEqual("waiting period", hits[0].excerpt)

    def test_restricted_draft_never_changes_public_results_for_other_principals(self) -> None:
        fixture = load_fixture(); sources = sources_from(fixture)
        public = [(hit.source_id, hit.version, hit.score) for hit in rank_wording(fixture["query"], "Visitor", sources, 5)]
        other = [(hit.source_id, hit.version, hit.score) for hit in rank_wording(fixture["query"], "catalog-editor-F", sources, 5)]
        self.assertEqual(public, other)
        self.assertNotIn("draft-E", [item[0] for item in other])

    def test_source_order_and_nonmatching_scenario_do_not_change_ranking(self) -> None:
        fixture = load_fixture(); sources = list(reversed(sources_from(fixture)))
        hits = rank_wording(fixture["query"], "Visitor", sources, 3)
        self.assertEqual([("w1", 2), ("w2", 1), ("w3", 1)], [(hit.source_id, hit.score) for hit in hits])
        self.assertNotIn("motor-A", [hit.source_id for hit in hits])

    def test_duplicate_provenance_or_missing_version_is_rejected(self) -> None:
        fixture = load_fixture(); sources = sources_from(fixture)
        sources.append(sources[1])
        with self.assertRaises(ValueError): rank_wording(fixture["query"], "Visitor", sources, 3)
        sources = [source for source in sources_from(fixture) if source.source_id != "w1"]
        sources.append(SourceVersion("PUBLIC_PUBLISHED", frozenset(), "health", "waiting-period", "w1", None, "waiting period"))
        with self.assertRaises(ValueError): rank_wording(fixture["query"], "Visitor", sources, 3)


if __name__ == "__main__":
    unittest.main()
