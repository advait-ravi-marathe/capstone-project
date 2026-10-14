from __future__ import annotations

import json
import re
import unittest
from pathlib import Path

from ai.coverstory.cap_44 import SourceVersion

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-44/synthetic-v1.json"


class CAP44InfrastructureTest(unittest.TestCase):
    def test_fixture_loads_typed_public_and_restricted_scope_controls(self) -> None:
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.assertEqual("CAP-44-synthetic-v1", data["fixture_id"])
        self.assertEqual(4, len(data["cases"]))
        restricted = next(row for row in data["sources"] if row["visibility"] == "RESTRICTED_EDITOR_DRAFT")
        source = SourceVersion(restricted["visibility"], frozenset(restricted["allowed_fixture_principals"]),
                               restricted["category"], restricted["scenario"], restricted["source_id"],
                               restricted["version"], restricted["text"])
        self.assertTrue(source.visible_to("catalog-editor-E"))
        self.assertFalse(source.visible_to("Visitor"))

    def test_independent_reference_catches_visibility_category_and_order_mutants(self) -> None:
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        expected = [tuple(row) for row in data["expected_public_order"]]
        sources = data["sources"]

        def reference(principal: str | None, rows: list[dict], *, ignore_visibility: bool = False,
                      ignore_category: bool = False, k: int = 10):
            query = set(re.findall(r"[a-z0-9]+", data["query"].lower()))
            scored = []
            for row in rows:
                typed = SourceVersion(row["visibility"], frozenset(row["allowed_fixture_principals"]),
                                      row["category"], row["scenario"], row["source_id"],
                                      row["version"], row["text"])
                if (not ignore_visibility and not typed.visible_to(principal)) or (not ignore_category and row["category"] != "health") or row["scenario"] != "waiting-period":
                    continue
                score = len(query & set(re.findall(r"[a-z0-9]+", row["text"].lower())))
                if score:
                    scored.append((row["source_id"], row["version"], score))
            return sorted(scored, key=lambda hit: (-hit[2], hit[0], hit[1]))[:k]

        self.assertEqual(expected, reference("Visitor", sources))
        self.assertEqual(expected, reference("Visitor", list(reversed(sources))), "source order must not affect ties")
        self.assertNotEqual(expected, reference("Visitor", sources, ignore_visibility=True),
                            "visibility bypass mutant must be caught")
        self.assertNotEqual(expected, reference("Visitor", sources, ignore_category=True),
                            "category-filter mutant must be caught")
        self.assertEqual(expected[:2], reference("Visitor", sources, k=2), "k truncation must preserve the leading pair")
        repeated = set(re.findall(r"[a-z0-9]+", "waiting waiting period"))
        self.assertEqual(2, len(repeated), "distinct-token scoring must not count duplicate query tokens")


if __name__ == "__main__":
    unittest.main()
