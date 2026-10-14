"""Instructor-owned CAP-41 types, tokenizer and fixture checks."""
import json
import unittest
from pathlib import Path

from ai.retrieval.coverstory_health import Hit, PublicWording, tokenize
from ai.tests.coverstory_cap41_contract import assert_matches, expected_hits


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

    def test_expected_result_oracle_accepts_correct_witness_and_rejects_faults(self):
        fixture = json.loads(FIXTURE.read_text())
        case = fixture["cases"]["normal"]
        expected = [Hit(**row) for row in case["expected"]]
        self.assertEqual(
            [("w-health-A", "v1", "waiting period", 2), ("w-health-B", "v1", "waiting", 1)],
            [(hit.source_id, hit.version, hit.excerpt, hit.score) for hit in expected],
        )
        self.assertEqual([], fixture["cases"]["no_match"]["expected"])

        def assert_contract(actual, contract_case=case):
            return assert_matches(contract_case, actual)

        correct = expected_hits(case)
        self.assertEqual(expected, correct)
        assert_contract(correct)  # non-vacuous positive witness from the supplied documents
        with self.assertRaises(AssertionError): assert_contract(list(reversed(correct)))  # ranking-order defect
        stale_citation = [Hit("w-health-A", "latest", "SYNTHETIC", "waiting period", 2), expected[1]]
        with self.assertRaises(AssertionError): assert_contract(stale_citation)  # invented latest version must not pass

    def test_tie_mixed_score_and_truncation_cases_use_reference_and_mutants(self):
        fixture = json.loads(FIXTURE.read_text())
        for name in ("equal_score_ties", "mixed_scores_truncated"):
            case = fixture["cases"][name]
            expected = [Hit(**row) for row in case["expected"]]
            correct = expected_hits(case)
            self.assertEqual(expected, correct)
            assert_contract = lambda actual: self.assertEqual(correct, actual)
            assert_contract(correct)
            with self.assertRaises(AssertionError): assert_contract(list(reversed(correct)))


if __name__ == "__main__":
    unittest.main()
