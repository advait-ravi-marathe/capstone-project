"""Independent instructor witness for the saved CAP-67 cases; no learner call."""
from __future__ import annotations
import hashlib
import json
import unittest
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-67/synthetic-v1.json"


def reference(ranked: list[str], relevant: set[str], k: int) -> tuple[bool, float | None, float | None]:
    if k < 1 or len(ranked) != len(set(ranked)) or any(not item for item in ranked):
        raise ValueError("invalid rank list or k")
    if not relevant:
        return False, None, None
    top = ranked[:k]
    hits = set(top) & relevant
    rr = next((1.0 / rank for rank, item in enumerate(ranked, 1) if item in relevant), 0.0)
    return True, len(hits) / len(relevant), rr


def sorted_order_defect(ranked: list[str], relevant: set[str], k: int):
    # Wrongly sorts identifiers, changing observed rank positions.
    return reference(sorted(ranked)[:k], relevant, k)


class CAP67InstructorInfrastructureTest(unittest.TestCase):
    def test_fixture_identity_and_expected_examples_are_pinned(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.assertEqual("CAP-67-synthetic-v1", data["fixture_id"])
        self.assertTrue(data["synthetic"])
        self.assertEqual(5, len(data["cases"]))
        self.assertEqual((0.5, 0.5), tuple(reference(["C", "A", "B"], {"A", "B"}, 2)[1:]))
        self.assertFalse(reference(["A"], set(), 2)[0])
        with self.assertRaises(ValueError): reference(["A", "A"], {"A"}, 2)

    def test_positive_and_negative_witness_discriminate_rank_and_denominator(self):
        ranked, relevant, k = ["C", "A", "B"], {"A", "B"}, 2
        correct = reference(ranked, relevant, k)
        defective = sorted_order_defect(ranked, relevant, k)
        self.assertEqual((True, 0.5, 0.5), correct)
        self.assertNotEqual(correct, defective)
        # A wrong denominator that includes irrelevant retrieved items is caught.
        self.assertNotEqual(0.5, len(set(["A"]) & {"A", "B"}) / len(set(["A"])))

    def test_aggregate_excludes_non_applicable_instead_of_treating_as_zero(self):
        answerable = [(1.0, 1.0), (0.0, 0.5)]
        recall = sum(x[0] for x in answerable) / len(answerable)
        rr = sum(x[1] for x in answerable) / len(answerable)
        defective_recall = sum((1.0, 0.0, 0.0)) / 3
        self.assertEqual((0.5, 0.75), (recall, rr))
        self.assertNotEqual(recall, defective_recall)


if __name__ == "__main__": unittest.main()
