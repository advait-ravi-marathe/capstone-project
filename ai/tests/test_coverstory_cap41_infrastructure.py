"""Instructor-owned CAP-41 types, tokenizer and fixture checks."""
import json
from pathlib import Path
import unittest

from ai.retrieval.coverstory_health import Hit, PublicWording, tokenize


ROOT = Path(__file__).resolve().parents[2]
FIXTURE = ROOT / "ai/fixtures/coverstory/CAP-41/synthetic-v1.json"


class CoverStoryCap41InfrastructureTests(unittest.TestCase):
    def test_instructor_types_and_whitespace_tokenizer(self):
        self.assertEqual({"waiting", "period"}, tokenize("Waiting   period"))
        self.assertIsNone(PublicWording("w-health-A", None, "SYNTHETIC", "waiting").version)
        self.assertEqual("excerpt", Hit("w-health-A", "v1", "SYNTHETIC", "excerpt", 1).excerpt)

    def test_fixture_is_explicitly_synthetic_and_citation_complete(self):
        fixture = json.loads(FIXTURE.read_text())
        self.assertEqual("CAP-41", fixture["contract"])
        self.assertEqual("SYNTHETIC", fixture["classification"])
        normal = fixture["cases"]["normal"]
        self.assertEqual(["w-health-A", "w-health-B"], [d["source_id"] for d in normal["documents"]])
        self.assertTrue(all(d["version"] and d["evidence_status"] == "SYNTHETIC" for d in normal["documents"]))


if __name__ == "__main__":
    unittest.main()
