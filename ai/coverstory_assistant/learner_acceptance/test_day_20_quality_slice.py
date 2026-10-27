import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class Day20QualitySliceTests(unittest.TestCase):
    def test_quality_labels_keep_all_independent_failure_slices(self):
        path = ROOT / "learner_artifacts/day-20-quality-labels.jsonl"
        rows = [json.loads(line) for line in path.read_text().splitlines() if line.strip()]
        self.assertEqual(
            [
                ("clean-excerpt", "usable"),
                ("ocr-confusable", "degraded"),
                ("stale-version", "stale"),
                ("hostile-instruction", "untrusted_instruction"),
            ],
            [(row["case_id"], row["quality_label"]) for row in rows],
        )
        self.assertEqual(4, len({row["case_id"] for row in rows}))
        self.assertTrue(all(row.get("source_fixture") == "day-20-inputs.jsonl" for row in rows))
