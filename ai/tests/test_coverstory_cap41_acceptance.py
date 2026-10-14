"""CAP-41 learner acceptance contract; failures are expected until retrieve is implemented."""
import json
from pathlib import Path
import unittest

from ai.retrieval.coverstory_health import Hit, PublicWording, retrieve


ROOT = Path(__file__).resolve().parents[2]
FIXTURE = json.loads((ROOT / "ai/fixtures/coverstory/CAP-41/synthetic-v1.json").read_text())


def documents(rows):
    return [PublicWording(**row) for row in rows]


def hits(rows):
    return [Hit(**row) for row in rows]


class CoverStoryCap41AcceptanceTests(unittest.TestCase):
    def test_returns_ranked_exact_synthetic_citations(self):
        case = FIXTURE["cases"]["normal"]
        self.assertEqual(hits(case["expected"]), retrieve(case["query"], documents(case["documents"]), case["k"]))

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


if __name__ == "__main__":
    unittest.main()
