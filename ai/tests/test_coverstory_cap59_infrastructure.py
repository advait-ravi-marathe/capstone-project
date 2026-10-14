from __future__ import annotations

import json
import hashlib
import unittest
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-59/synthetic-v1.json"


class CAP59InfrastructureTest(unittest.TestCase):
    def test_citation_fixture_contains_exact_source_version_and_slice(self) -> None:
        fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))
        row = fixture["valid_citation"]
        source = next(item for item in fixture["registry"] if (item["source_id"], item["version"]) == (row["source_id"], row["version"]))
        self.assertEqual(row["excerpt"], source["text"][row["start"]:row["end"]])
        self.assertEqual(hashlib.sha256(source["text"].encode()).hexdigest(), row["content_sha256"])
        self.assertEqual(4, len(fixture["cases"]))


if __name__ == "__main__": unittest.main()
