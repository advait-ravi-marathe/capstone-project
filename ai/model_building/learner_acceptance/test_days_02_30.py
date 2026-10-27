"""Fixed independent learner contracts for early model exercises.

Each listed test is expected to remain red at its explicit learner TODO until
the corresponding task is attempted. Expected values are independent fixtures.
"""

import math
import unittest

import numpy as np

from ai.model_building import learner


class Day02To30Acceptance(unittest.TestCase):
    def test_day_02_sigmoid(self):
        self.assertEqual(0.5, learner.sigmoid(0.0))
        self.assertAlmostEqual(0.8807970779778823, learner.sigmoid(2.0), places=14)
        with self.assertRaises(ValueError):
            learner.sigmoid(float("nan"))

    def test_day_03_binary_cross_entropy(self):
        self.assertAlmostEqual((-math.log(0.8) - math.log(0.75)) / 2,
                               learner.mean_binary_cross_entropy([0.8, 0.25], [1, 0]), places=14)
        for probabilities, labels in (([], []), ([0.8], []), ([1.1], [1]), ([0.2], [2])):
            with self.subTest(probabilities=probabilities, labels=labels):
                with self.assertRaises(ValueError):
                    learner.mean_binary_cross_entropy(probabilities, labels)

    def test_day_04_dot_product(self):
        self.assertEqual(5.5, learner.dot([0.5, 2.0], [3.0, 2.0]))
        with self.assertRaises(ValueError):
            learner.dot([1.0], [1.0, 2.0])

    def test_day_05_finite_difference(self):
        self.assertAlmostEqual(6.0, learner.central_difference(lambda x: x * x, 3.0), places=7)
        with self.assertRaises(ValueError):
            learner.central_difference(lambda x: x * x, 3.0, 0.0)

    def test_day_06_batch_step_matches_independent_fixed_contract(self):
        result = learner.train_one_logistic_step([0.0, 0.0, 0.0], [([1.0, 0.0], 1), ([0.0, 1.0], 0)], 0.2)
        self.assertEqual([0.0, 0.05, -0.05], result)

    def test_day_07_split_is_disjoint_and_ordered(self):
        records = [{"id": f"r{i}"} for i in range(6)]
        self.assertEqual((["r0", "r1", "r2"], ["r3"], ["r4", "r5"]),
                         learner.deterministic_split(records, 3, 1))
        with self.assertRaises(ValueError):
            learner.deterministic_split(records + [records[0]], 3, 1)

    def test_day_08_reports_all_forbidden_features(self):
        self.assertEqual(["answer", "claim_outcome"],
                         learner.find_target_leakage(["term_count", "answer", "claim_outcome"], ["answer", "claim_outcome"]))
        self.assertEqual([], learner.find_target_leakage(["term_count", "section_count"], ["answer"]))

    def test_day_09_standardizer_uses_training_statistics(self):
        means, scales, transformed = learner.fit_standardizer([[1.0, 10.0], [3.0, 14.0]])
        self.assertEqual([2.0, 12.0], means)
        self.assertEqual([1.0, 2.0], scales)
        self.assertEqual([[-1.0, -1.0], [1.0, 1.0]], transformed)
        with self.assertRaises(ValueError):
            learner.fit_standardizer([[1.0, 2.0], [3.0]])

    def test_day_10_majority_tie_breaks_to_zero(self):
        self.assertEqual(0, learner.majority_prediction([1, 0]))
        self.assertEqual(1, learner.majority_prediction([1, 1, 0]))
        with self.assertRaises(ValueError):
            learner.majority_prediction([])

    def test_day_11_confusion_matrix_has_exact_counts(self):
        self.assertEqual({"tp": 1, "tn": 1, "fp": 1, "fn": 1},
                         learner.confusion_matrix([1, 0, 1, 0], [1, 0, 0, 1]))
        with self.assertRaises(ValueError):
            learner.confusion_matrix([1], [1, 0])

    def test_day_12_undefined_metrics_remain_none(self):
        self.assertEqual({"precision": None, "recall": 0.0},
                         learner.precision_recall({"tp": 0, "tn": 2, "fp": 0, "fn": 1}))
        with self.assertRaises(ValueError):
            learner.precision_recall({"tp": -1, "tn": 2, "fp": 0, "fn": 1})

    def test_day_13_slice_denominators_preserve_rows(self):
        result = learner.metrics_by_slice([1, 0, 1], [1, 1, 0], ["a", "a", "b"])
        self.assertEqual({"a": {"count": 2, "tp": 1, "tn": 0, "fp": 1, "fn": 0},
                          "b": {"count": 1, "tp": 0, "tn": 0, "fp": 0, "fn": 1}}, result)
        with self.assertRaises(ValueError):
            learner.metrics_by_slice([1], [1], [])

    def test_day_14_comparison_reports_source_ordered_error_ids(self):
        result = learner.compare_predictions(["r0", "r1", "r2"], [1, 0, 1], {"baseline": [0, 0, 1]})
        self.assertEqual(["r0"], result["baseline"]["error_ids"])
        self.assertEqual(2 / 3, result["baseline"]["accuracy"])
        with self.assertRaises(ValueError):
            learner.compare_predictions(["r0"], [1, 0], {"bad": [1, 0]})

    def test_day_15_rectangular_matrix_shape(self):
        array = learner.matrix_shape([[1, 2, 3], [4, 5, 6]])
        self.assertIsInstance(array, np.ndarray)
        self.assertEqual((2, 3), array.shape)
        with self.assertRaises(ValueError):
            learner.matrix_shape([[1, 2], [3]])

    def test_day_16_batched_scores_match_scalar_oracle(self):
        scores = learner.linear_scores([[1, 0], [0, 2]], [1.0, 2.0], 0.5)
        self.assertIsInstance(scores, np.ndarray)
        np.testing.assert_allclose([1.5, 4.5], scores, rtol=0, atol=1e-12)
        with self.assertRaises(ValueError):
            learner.linear_scores([[1, 2]], [1], 0.0)

    def test_day_17_gradient_keeps_bias_and_feature_axes(self):
        gradient = learner.batch_gradient([[1.0, 0.0], [0.0, 1.0]], [1, 0], [0, 0, 0])
        np.testing.assert_allclose([-0.5, -0.5, 0.5], gradient, rtol=0, atol=1e-12)
        with self.assertRaises(ValueError):
            learner.batch_gradient([[1.0]], [1, 0], [0.0, 0.0])

    def test_day_18_extreme_sigmoid_is_finite(self):
        self.assertTrue(math.isfinite(learner.finite_sigmoid(-1000.0)))
        self.assertEqual(1.0, learner.finite_sigmoid(1000.0))
        self.assertGreaterEqual(learner.finite_sigmoid(-1000.0), 0.0)
        self.assertGreater(learner.finite_sigmoid(2.0), learner.finite_sigmoid(0.0))

    def test_day_19_curve_retains_every_rate_and_checkpoint(self):
        curve = learner.train_curve(0.0, [0.1, 0.5, 1.1], 4)
        self.assertEqual([0.1, 0.5, 1.1], [row["learning_rate"] for row in curve])
        self.assertEqual(5, len(curve[0]["losses"]))
        self.assertLess(curve[0]["losses"][-1], curve[0]["losses"][0])
        self.assertGreater(curve[2]["losses"][-1], curve[2]["losses"][0])
        with self.assertRaises(ValueError):
            learner.train_curve(0.0, [0.0], 4)

    def test_day_20_experiment_record_preserves_seed_and_ordered_metadata(self):
        expected = {"seed": 41, "split_ids": ["a", "b"], "feature_names": ["x", "y"], "predictions": [1, 0]}
        self.assertEqual(expected, learner.experiment_record(41, ["a", "b"], ["x", "y"], [1, 0]))
        self.assertNotEqual(expected, learner.experiment_record(42, ["a", "b"], ["x", "y"], [1, 0]))

    def test_day_21_baseline_against_same_heldout_labels(self):
        report = learner.sklearn_pipeline_baseline(
            [[0.0], [1.0], [9.0], [10.0]], [0, 0, 1, 1], [0, 1, 2], [3]
        )
        self.assertEqual([0, 1, 2], report["fit_indices"])
        self.assertEqual([3], report["test_indices"])
        self.assertEqual([1], report["predictions"])
        self.assertEqual(1, report["correct_count"])
        with self.assertRaises(ValueError):
            learner.sklearn_pipeline_baseline([[0.0], [1.0], [9.0], [10.0]], [0, 0, 1, 1], [0, 1, 3], [3])

    def test_day_22_preprocessing_fit_must_exclude_evaluation_ids(self):
        self.assertTrue(learner.fit_id_guard(["a", "b"], ["a", "b"], ["c"]))
        self.assertFalse(learner.fit_id_guard(["a", "b"], ["a", "c"], ["c"]))
        self.assertFalse(learner.fit_id_guard(["a", "b"], ["a", "b"], ["b"]))

    def test_day_23_calibration_bins_match_fixed_counts(self):
        self.assertEqual([{"count": 2, "mean_confidence": 0.25, "accuracy": 0.5},
                          {"count": 2, "mean_confidence": 0.75, "accuracy": 0.5}],
                         learner.calibration_bins([0.1, 0.4, 0.6, 0.9], [0, 1, 0, 1], [0, 0.5, 1]))
        with self.assertRaises(ValueError):
            learner.calibration_bins([1.2], [1], [0, 0.5, 1])

    def test_day_24_document_fixture_is_fixed_and_split_safe(self):
        rows = learner.build_document_fixture()
        self.assertEqual(12, len(rows))
        self.assertEqual(12, len({row["id"] for row in rows}))
        self.assertEqual({"train", "validation", "test"}, {row["split"] for row in rows})
        self.assertEqual([f"doc-{index:02d}" for index in range(1, 13)], [row["id"] for row in rows])
        self.assertEqual(["schedule", "exclusion", "definition"] * 4, [row["label"] for row in rows])
        self.assertEqual(["train"] * 6 + ["validation"] * 3 + ["test"] * 3, [row["split"] for row in rows])
        self.assertEqual([
            "annual premium is due", "excluded loss wording", "insured person definition",
            "coverage period schedule", "waiting period exclusion", "deductible definition",
            "renewal date schedule", "not covered exclusion", "claim notice definition",
            "named premises schedule", "policy limitation exclusion", "policy term definition",
        ], [row["text"] for row in rows])

    def test_day_25_error_ids_retain_order_and_ignore_correct_rows(self):
        self.assertEqual(["d2", "d4"], learner.prediction_error_ids(["d1", "d2", "d3", "d4"], [1, 1, 0, 0], [1, 0, 0, 1]))
        with self.assertRaises(ValueError):
            learner.prediction_error_ids(["d1"], [0, 1], [0, 1])

    def test_day_26_supplied_route_reading_is_paired_with_model_error_analysis(self):
        # The separate source trace is a human comprehension artifact; this
        # fixed model slice is the executable part of the day's acceptance.
        self.assertEqual(["bad"], learner.prediction_error_ids(["ok", "bad"], [1, 0], [1, 1]))

    def test_day_27_seeded_failure_analysis_uses_observed_prediction_errors(self):
        self.assertEqual(["r2"], learner.prediction_error_ids(["r1", "r2"], [0, 1], [0, 0]))

    def test_day_28_report_does_not_promote_score_to_decision(self):
        self.assertEqual({"score": 0.92, "predicted_label": 1, "downstream_decision": "not_assessed"},
                         learner.report_model_limit(0.92, 1, "not_assessed"))
        with self.assertRaises(ValueError):
            learner.report_model_limit(0.92, 1, "approve_claim")

    def test_day_29_roundtrip_model_preserves_feature_order_and_values(self):
        serialized = learner.serialize_linear_model([0.1, 0.2, -0.3], ["a", "b"])
        loaded = learner.load_linear_model(serialized)
        self.assertEqual(["a", "b"], loaded["feature_names"])
        self.assertEqual([0.1, 0.2, -0.3], loaded["weights"])
        with self.assertRaises(ValueError):
            learner.load_linear_model('{"weights":[NaN],"feature_names":["x"]}')

    def test_day_30_model_scores_fixed_rows_in_declared_order(self):
        self.assertEqual([0.0, -0.2], learner.score_rows([0.1, 0.2, -0.3], [[1, 1], [0, 1]]))
        with self.assertRaises(ValueError):
            learner.score_rows([0.1, 0.2], [[1, 1]])


if __name__ == "__main__":
    unittest.main()
