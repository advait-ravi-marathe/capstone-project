from __future__ import annotations

import json
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


if __name__ == "__main__":
    unittest.main()
