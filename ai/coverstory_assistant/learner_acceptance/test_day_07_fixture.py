import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class Day07FixtureTests(unittest.TestCase):
    def test_learner_labels_match_independent_fixed_cases_and_have_unique_ids(self):
        path = ROOT / "learner_artifacts/day-07-labels.jsonl"
        rows = [json.loads(line) for line in path.read_text().splitlines() if line.strip()]
        self.assertEqual(
            [
                {"case_id": "hail-excluded", "expected_label": "cited"},
                {"case_id": "missing-flood-clause", "expected_label": "abstain"},
                {"case_id": "conflicting-water-terms", "expected_label": "clarify"},
            ],
            [{"case_id": row["case_id"], "expected_label": row["expected_label"]} for row in rows],
        )
        self.assertEqual(3, len({row["case_id"] for row in rows}))
        self.assertTrue(all(row.get("source_fixture") == "day-07-inputs.jsonl" for row in rows))
