"""Green instructor-owned contract checks; they do not implement learner seams."""
import unittest
import subprocess
import sys
from pathlib import Path
from fractions import Fraction

from ai.streamforge.evaluation_io import (BASELINE_IDS, K, PROTOCOL_ID,
                                         fixed_protocol, id_order_reference,
                                         prediction_fingerprint,
                                         validate_popularity_state,
                                         validate_ranked_list, validate_evaluation,
                                         validate_comparison)
from ai.streamforge.evaluation_models import (BaselineComparison, BaselineEvaluation,
                                              PopularityState, RankedList)


class EvaluationInfrastructureTest(unittest.TestCase):
    def test_evaluation_cli_keeps_named_todo_and_never_emits_partial_success(self):
        root = Path(__file__).resolve().parents[3]
        result = subprocess.run([sys.executable, "-m", "ai.streamforge.evaluate", "--fixture", "basic"],
                                cwd=root, capture_output=True, timeout=10, check=False)
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(b"", result.stdout)
        self.assertIn(b"Day 179 learner TODO", result.stderr)

    def test_frozen_protocol_and_fixed_id_reference(self):
        protocol = fixed_protocol("2027-01-20T00:00:00Z")
        self.assertEqual("streamforge-cutoff-popularity-v1", PROTOCOL_ID)
        self.assertEqual(3, K)
        self.assertEqual(("popularity_count", "id_order_reference"), BASELINE_IDS)
        self.assertEqual(3, protocol["precision_denominator"])
        self.assertIn("all distinct positive", protocol["recall_targets"])
        self.assertEqual(("a", "b", "c"), id_order_reference(("c", "b", "a", "d")))
        self.assertEqual(("a",), id_order_reference(("a",)))

    def test_result_boundaries_reject_bad_scores_versions_and_recommendation_shapes(self):
        candidates = ("a", "b", "c")
        state = PopularityState({"a": 0, "b": 2, "c": 1}, False)
        validate_popularity_state(state, candidates)
        for bad in (PopularityState({"a": 0, "b": True, "c": 1}, False),
                    PopularityState({"a": 0, "b": -1, "c": 1}, False),
                    PopularityState({"a": 0, "b": 2}, False),
                    PopularityState({"a": 0, "b": 0, "c": 0}, False)):
            with self.subTest(bad=bad), self.assertRaises(ValueError):
                validate_popularity_state(bad, candidates)
        ranked = RankedList(("b", "c", "a"), ("b", "c", "a"), "popularity_ranked")
        validate_ranked_list(ranked, candidates)
        for bad in (RankedList(candidates, ("a", "a"), "popularity_ranked"),
                    RankedList(candidates, ("a", "b", "c", "d"), "popularity_ranked"),
                    RankedList(candidates, ("not-candidate",), "popularity_ranked"),
                    RankedList(candidates, ("a",), "unknown")):
            with self.subTest(bad=bad), self.assertRaises(ValueError):
                validate_ranked_list(bad, candidates)

    def test_prediction_fingerprint_is_content_addressed_from_declared_inputs_only(self):
        protocol = fixed_protocol("2027-01-20T00:00:00Z")
        state = PopularityState({"a": 0, "b": 2, "c": 1}, False)
        ranked = RankedList(("b", "c", "a"), ("b", "c", "a"), "popularity_ranked")
        first = prediction_fingerprint(protocol, ranked.candidate_ids, state, ranked)
        # Holdout/full-source fields are intentionally not accepted by this helper.
        self.assertEqual(first, prediction_fingerprint(protocol, ranked.candidate_ids, state, ranked))
        self.assertEqual(64, len(first))
        with self.assertRaises(ValueError):
            prediction_fingerprint({**protocol, "protocol_id": "unknown"}, ranked.candidate_ids, state, ranked)

    def test_evaluation_and_comparison_boundaries_enforce_cohort_and_exact_deltas(self):
        candidates = ("a", "b", "c")
        ranked = RankedList(candidates, candidates, "popularity_ranked")
        good = BaselineEvaluation(ranked, Fraction(1, 3), Fraction(1, 2), 2, 1, 1,
                                  2, 0, 1, 0, 3, 3, "available")
        validate_evaluation(good, candidates)
        reference_ranked = RankedList(candidates, ("a", "b", "c"), "id_order_no_positive_training_signal")
        reference = BaselineEvaluation(reference_ranked, Fraction(0, 1), Fraction(0, 1), 2, 1, 1,
                                       2, 0, 1, 0, 3, 3, "available")
        validate_comparison(BaselineComparison(good, reference, Fraction(1, 3), Fraction(1, 2), "available"),
                            candidates)
        with self.assertRaises(ValueError):
            validate_evaluation(BaselineEvaluation(ranked, Fraction(1, 3), Fraction(1, 2), 2, 1, 0,
                                                    2, 0, 1, 0, 3, 3, "available"), candidates)
        with self.assertRaises(ValueError):
            validate_evaluation(BaselineEvaluation(ranked, None, None, 2, 1, 1,
                                                    2, 0, 1, 0, 3, 3, "available"), candidates)
        # Null scores remain valid for genuinely non-evaluable cohorts.
        validate_evaluation(BaselineEvaluation(ranked, None, None, 1, 0, 1,
                                                0, 0, 1, 0, 3, 3, "no_positive_holdout"), candidates)
        with self.assertRaises(ValueError):
            validate_comparison(BaselineComparison(good, reference, Fraction(0), Fraction(1, 2), "available"),
                                candidates)


if __name__ == "__main__":
    unittest.main()
