"""Acceptance path and independent mutation checks for the Day 271–284 lab."""
from __future__ import annotations
import json
import unittest
from pathlib import Path
from unittest.mock import patch
from labs.runtime_sql import learner_analysis, reference_analysis

FIXTURE = Path(__file__).parents[1] / "fixtures" / "observation.json"


def load_fixture():
    return json.loads(FIXTURE.read_text(encoding="utf-8"))


class LearnerAcceptance(unittest.TestCase):
    def test_exact_result_estimate_and_buffer_observations(self):
        observed = learner_analysis.analyze_observation(load_fixture())
        self.assertEqual({
            "row_correct": True, "expected_row_count": 5, "observed_row_count": 5,
            "estimate_rows": 5, "actual_rows": 5.0, "q_error": 1.0,
            "cardinality_state": "comparable", "shared_hit_blocks": 3,
            "shared_read_blocks": 2, "single_run_performance_claim": "not_supported",
        }, observed)

    def test_foreign_or_missing_result_row_is_not_hidden_by_matching_count(self):
        fixture = load_fixture()
        fixture["observed_rows"][0]["owner_id"] = "owner-08"
        observed = learner_analysis.analyze_observation(fixture)
        self.assertFalse(observed["row_correct"])
        self.assertEqual(5, observed["observed_row_count"])

    def test_wrong_plan_estimate_changes_q_error_and_zero_rows_are_explicit(self):
        fixture = load_fixture()
        fixture["explain_json"][0]["Plan"]["Plan Rows"] = 20
        observed = learner_analysis.analyze_observation(fixture)
        self.assertEqual(4.0, observed["q_error"])
        self.assertEqual("not_supported", observed["single_run_performance_claim"])
        fixture["explain_json"][0]["Plan"]["Plan Rows"] = 0
        fixture["explain_json"][0]["Plan"]["Actual Rows"] = 0
        self.assertEqual(1.0, learner_analysis.analyze_observation(fixture)["q_error"])
        fixture["explain_json"][0]["Plan"]["Actual Rows"] = 5
        self.assertEqual("zero_cardinality_mismatch", learner_analysis.analyze_observation(fixture)["cardinality_state"])

    def test_negative_or_malformed_counters_fail_closed(self):
        fixture = load_fixture()
        fixture["explain_json"][0]["Plan"]["Shared Read Blocks"] = -1
        with self.assertRaises(ValueError):
            learner_analysis.analyze_observation(fixture)
        fixture = load_fixture()
        fixture["explain_json"][0]["Plan"].pop("Actual Rows")
        with self.assertRaises(ValueError):
            learner_analysis.analyze_observation(fixture)
        fixture = load_fixture()
        fixture["expected_ids"][0]["revision"] = True
        with self.assertRaises(ValueError):
            learner_analysis.analyze_observation(fixture)
        fixture = load_fixture()
        fixture["explain_json"][0]["Plan"]["Plan Rows"] = float("nan")
        with self.assertRaises(ValueError):
            learner_analysis.analyze_observation(fixture)


class IndependentReferenceAcceptance(unittest.TestCase):
    """The separate reference runs identical assertions; it is no learner credit."""
    def test_reference_passes_actual_acceptance_methods(self):
        for name in ("test_exact_result_estimate_and_buffer_observations",
                     "test_foreign_or_missing_result_row_is_not_hidden_by_matching_count",
                     "test_wrong_plan_estimate_changes_q_error_and_zero_rows_are_explicit",
                     "test_negative_or_malformed_counters_fail_closed"):
            case = LearnerAcceptance(name)
            with patch.object(learner_analysis, "analyze_observation", side_effect=reference_analysis.analyze_observation):
                result = case.run()
            self.assertTrue(result.wasSuccessful(), result.errors + result.failures)

    def test_mutations_on_actual_acceptance_methods_are_rejected(self):
        def wrong_rows(_):
            return dict(reference_analysis.analyze_observation(load_fixture()), row_correct=True)
        def wrong_buffers(obs):
            return dict(reference_analysis.analyze_observation(obs), shared_read_blocks=0)
        def overclaim(obs):
            return dict(reference_analysis.analyze_observation(obs), single_run_performance_claim="fast")
        cases = (
            ("test_foreign_or_missing_result_row_is_not_hidden_by_matching_count", wrong_rows),
            ("test_exact_result_estimate_and_buffer_observations", wrong_buffers),
            ("test_exact_result_estimate_and_buffer_observations", overclaim),
        )
        for name, mutant in cases:
            case = LearnerAcceptance(name)
            with self.subTest(method=name, mutant=mutant.__name__), \
                 patch.object(learner_analysis, "analyze_observation", side_effect=mutant):
                result = case.run()
                self.assertTrue(result.failures, name)


if __name__ == "__main__":
    unittest.main()
