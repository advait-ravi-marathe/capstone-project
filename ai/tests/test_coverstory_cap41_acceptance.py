"""CAP-41 learner acceptance contract; failures are expected until retrieve is implemented."""
import json
from pathlib import Path
import unittest

from ai.retrieval.coverstory_health import Hit, PublicWording, retrieve
from ai.tests.coverstory_cap41_contract import assert_matches


ROOT = Path(__file__).resolve().parents[2]
FIXTURE = json.loads((ROOT / "ai/fixtures/coverstory/CAP-41/synthetic-v1.json").read_text())


def documents(rows):
    return [PublicWording(**row) for row in rows]


def hits(rows):
    return [Hit(**row) for row in rows]


class CoverStoryCap41AcceptanceTests(unittest.TestCase):
    def test_returns_ranked_exact_synthetic_citations(self):
        for name in ("normal", "equal_score_ties", "mixed_scores_truncated"):
            case = FIXTURE["cases"][name]
            with self.subTest(case=name):
                actual = retrieve(case["query"], documents(case["documents"]), case["k"])
                assert_matches(case, actual)
                self.assertEqual(hits(case["expected"]), actual)

    def test_no_match_and_blank_query_return_no_hits(self):
        case = FIXTURE["cases"]["no_match"]
        rows = documents(case["documents"])
        self.assertEqual([], retrieve(case["query"], rows, case["k"]))
        self.assertEqual([], retrieve("   ", rows, 1))

    def test_k_is_limited_to_one_through_three(self):
        rows = documents(FIXTURE["cases"]["normal"]["documents"])
        for invalid_k in (0, 4):
            with self.subTest(k=invalid_k), self.assertRaises(ValueError):
                retrieve("waiting period", rows, invalid_k)

    def test_missing_source_version_is_rejected_before_partial_results(self):
        rows = documents(FIXTURE["cases"]["normal"]["documents"])
        rows.append(PublicWording("w-health-missing-version", None, "SYNTHETIC", "waiting"))
        with self.assertRaises(ValueError):
            retrieve("waiting", rows, 2)

    def test_reversing_documents_does_not_change_order(self):
        case = FIXTURE["cases"]["normal"]
        rows = documents(case["documents"])
        self.assertEqual(
            retrieve(case["query"], rows, case["k"]),
            retrieve(case["query"], list(reversed(rows)), case["k"]),
        )

    def test_equal_score_tie_order_and_k_truncation_are_explicit(self):
        tied = FIXTURE["cases"]["equal_score_ties"]
        actual = retrieve(tied["query"], documents(tied["documents"]), tied["k"])
        self.assertEqual(["w-tie-A", "w-tie-Z", "w-mixed"], [row.source_id for row in actual])
        self.assertEqual([2, 2, 1], [row.score for row in actual])
        truncated = FIXTURE["cases"]["mixed_scores_truncated"]
        actual = retrieve(truncated["query"], documents(truncated["documents"]), truncated["k"])
        self.assertEqual(2, len(actual))
        self.assertEqual(["w-high", "w-low"], [row.source_id for row in actual])


if __name__ == "__main__":
    unittest.main()
