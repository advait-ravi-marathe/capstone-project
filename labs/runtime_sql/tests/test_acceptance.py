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

    def test_each_composite_key_component_and_order_are_enforced(self):
        mutations = (
            ("owner_id", "owner-08"),
            ("policy_id", "policy-9999"),
            ("revision", 999),
        )
        for key, value in mutations:
            with self.subTest(key=key):
                fixture = load_fixture()
                fixture["observed_rows"][0][key] = value
                observed = learner_analysis.analyze_observation(fixture)
                self.assertFalse(observed["row_correct"])
                self.assertEqual(5, observed["observed_row_count"])
        fixture = load_fixture()
        fixture["observed_rows"].reverse()
        self.assertFalse(learner_analysis.analyze_observation(fixture)["row_correct"])

    def test_missing_and_extra_rows_are_detected(self):
        fixture = load_fixture()
        fixture["observed_rows"].pop()
        observed = learner_analysis.analyze_observation(fixture)
        self.assertFalse(observed["row_correct"])
        self.assertEqual(4, observed["observed_row_count"])
        fixture = load_fixture()
        fixture["observed_rows"].append({"owner_id": "owner-08", "policy_id": "policy-0012", "revision": 4})
        observed = learner_analysis.analyze_observation(fixture)
        self.assertFalse(observed["row_correct"])
        self.assertEqual(6, observed["observed_row_count"])
        # A same-count omission/substitution defeats length-only comparisons.
        fixture = load_fixture()
        fixture["observed_rows"].pop(0)
        fixture["observed_rows"].append({"owner_id": "owner-08", "policy_id": "policy-0012", "revision": 4})
        observed = learner_analysis.analyze_observation(fixture)
        self.assertFalse(observed["row_correct"])
        self.assertEqual(5, observed["observed_row_count"])
        fixture = load_fixture()
        fixture["observed_rows"][0]["policy_id"] = "policy-9999"
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
                     "test_each_composite_key_component_and_order_are_enforced",
                     "test_missing_and_extra_rows_are_detected",
                     "test_wrong_plan_estimate_changes_q_error_and_zero_rows_are_explicit",
                     "test_negative_or_malformed_counters_fail_closed"):
            case = LearnerAcceptance(name)
            with patch.object(learner_analysis, "analyze_observation", side_effect=reference_analysis.analyze_observation):
                result = case.run()
            self.assertTrue(result.wasSuccessful(), result.errors + result.failures)

    def test_mutations_on_actual_acceptance_methods_are_rejected(self):
        def compare_owner_only(obs):
            out = reference_analysis.analyze_observation(obs)
            out["row_correct"] = all(a["owner_id"] == e["owner_id"] for a, e in zip(obs["observed_rows"], obs["expected_ids"]))
            return out
        def ignore_order(obs):
            out = reference_analysis.analyze_observation(obs)
            out["row_correct"] = sorted(obs["observed_rows"], key=lambda x: x["revision"], reverse=True) == obs["expected_ids"]
            return out
        def compare_count_only(obs):
            out = reference_analysis.analyze_observation(obs)
            out["row_correct"] = len(obs["observed_rows"]) == len(obs["expected_ids"])
            return out
        def wrong_buffers(obs):
            return dict(reference_analysis.analyze_observation(obs), shared_read_blocks=0)
        def overclaim(obs):
            return dict(reference_analysis.analyze_observation(obs), single_run_performance_claim="fast")
        cases = (
            ("test_each_composite_key_component_and_order_are_enforced", compare_owner_only),
            ("test_each_composite_key_component_and_order_are_enforced", ignore_order),
            ("test_each_composite_key_component_and_order_are_enforced", compare_count_only),
            ("test_missing_and_extra_rows_are_detected", compare_count_only),
            ("test_missing_and_extra_rows_are_detected", compare_owner_only),
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
