from __future__ import annotations
import json
import unittest
from pathlib import Path
from ai.coverstory.cap_67 import AggregateMetrics, CaseMetrics, aggregate, metrics_at_k

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-67/synthetic-v1.json"


class CAP67AcceptanceTest(unittest.TestCase):
    def test_recall_and_first_relevant_rank_at_k(self):
        row = metrics_at_k(["C", "A", "B"], {"A", "B"}, 2)
        self.assertEqual((0.5, 0.5), (row.recall_at_k, row.reciprocal_rank))

    def test_no_relevant_hit_is_zero(self):
        row = metrics_at_k([], {"A"}, 3)
        self.assertEqual((0.0, 0.0), (row.recall_at_k, row.reciprocal_rank))

    def test_unanswerable_case_is_not_applicable(self):
        row = metrics_at_k(["A"], set(), 2)
        self.assertFalse(row.applicable)
        self.assertIsNone(row.recall_at_k)
        self.assertIsNone(row.reciprocal_rank)

    def test_duplicate_ranked_id_is_invalid(self):
        with self.assertRaises(ValueError):
            metrics_at_k(["A", "A"], {"A"}, 2)

    def test_macro_denominator_excludes_non_applicable(self):
        data = json.loads(FIXTURE.read_text())
        rows = [CaseMetrics(tuple(), frozenset(), 2, True, 1.0, 1.0),
                CaseMetrics(tuple(), frozenset(), 2, True, 0.0, 0.5),
                CaseMetrics(tuple(), frozenset(), 2, False, None, None)]
        result = aggregate(rows)
        expected = data["cases"][-1]["expected"]
        self.assertEqual(expected["answerable_count"], result.answerable_count)
        self.assertEqual(expected["mean_recall_at_k"], result.mean_recall_at_k)
        self.assertEqual(expected["mean_reciprocal_rank"], result.mean_reciprocal_rank)


if __name__ == "__main__": unittest.main()
