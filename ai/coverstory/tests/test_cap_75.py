"""CAP-75 learner acceptance contract; failures at TODO are expected initially."""
from __future__ import annotations
import json
import unittest
from pathlib import Path

from ai.coverstory.cap_75 import classify_provider_failure

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-75/synthetic-v1.json"


class CAP75AcceptanceTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.data = json.loads(FIXTURE.read_text(encoding="utf-8"))

    def test_malformed_json_is_non_retryable_and_points_to_batch_line(self):
        case = self.data["cases"][0]
        self.assertEqual(case["expected"], classify_provider_failure(case["run"]))

    def test_database_timeout_replays_or_looks_up_same_batch_reference(self):
        case = self.data["cases"][1]
        result = classify_provider_failure(case["run"])
        self.assertEqual(case["expected"], result)
        self.assertEqual("batch:b2", result["evidence_ids"][0])
        self.assertIn("same batch/reference", case["recovery"])

    def test_same_event_id_with_different_hash_is_idempotency_conflict(self):
        case = self.data["cases"][2]
        self.assertEqual(case["expected"], classify_provider_failure(case["run"]))

    def test_unknown_failure_evidence_never_exposes_exception_details(self):
        case = self.data["cases"][3]
        result = classify_provider_failure(case["run"])
        self.assertEqual(case["expected"], result)
        rendered = repr(result)
        for forbidden in case["forbidden_output_substrings"]:
            self.assertNotIn(forbidden, rendered)


if __name__ == "__main__":
    unittest.main()
