"""Prove public learner acceptance tests pass correct test-only witnesses and reject mutants."""

import unittest
from contextlib import ExitStack
from unittest.mock import patch

from ai.model_building import learner
from ai.model_building.tests import reference_witness as ref
from ai.model_building.learner_acceptance import test_day_01, test_days_02_30


PATCHES = {
    "sigmoid": ref.sigmoid,
    "mean_binary_cross_entropy": ref.mean_binary_cross_entropy,
    "dot": ref.dot,
    "central_difference": ref.central_difference,
    "train_one_logistic_step": ref.train_one_logistic_step,
    "deterministic_split": ref.deterministic_split,
    "find_target_leakage": ref.find_target_leakage,
    "fit_threshold_classifier": ref.fit_threshold_classifier,
    "predict_threshold_classifier": ref.predict_threshold_classifier,
    "fit_standardizer": ref.fit_standardizer,
    "majority_prediction": ref.majority_prediction,
    "nearest_centroid_predictions": ref.nearest_centroid_predictions,
    "confusion_matrix": ref.confusion_matrix,
    "precision_recall": ref.precision_recall,
    "threshold_predictions": ref.threshold_predictions,
    "metrics_by_slice": ref.metrics_by_slice,
    "compare_predictions": ref.compare_predictions,
    "matrix_shape": ref.matrix_shape,
    "linear_scores": ref.linear_scores,
    "batch_gradient": ref.batch_gradient,
    "finite_sigmoid": ref.finite_sigmoid,
    "train_curve": ref.train_curve,
    "experiment_record": ref.experiment_record,
    "sklearn_pipeline_baseline": ref.sklearn_pipeline_baseline,
    "fit_id_guard": ref.fit_id_guard,
    "fold_evaluation_summary": ref.fold_evaluation_summary,
    "deterministic_folds": ref.deterministic_folds,
    "calibration_bins": ref.calibration_bins,
    "build_document_fixture": ref.build_document_fixture,
    "prediction_error_ids": ref.prediction_error_ids,
    "report_model_limit": ref.report_model_limit,
    "serialize_linear_model": ref.serialize_linear_model,
    "load_linear_model": ref.load_linear_model,
    "fit_document_type_centroids": ref.fit_document_type_centroids,
    "predict_document_type": ref.predict_document_type,
    "serialize_document_type_model": ref.serialize_document_type_model,
    "load_document_type_model": ref.load_document_type_model,
    "score_rows": ref.score_rows,
}


class AcceptanceWitnessTests(unittest.TestCase):
    def test_full_day_1_to_30_acceptance_suite_passes_correct_test_only_witnesses(self):
        with ExitStack() as stack:
            for name, candidate in PATCHES.items():
                stack.enter_context(patch.object(learner, name, candidate))
            stack.enter_context(patch.object(test_day_01, "train_one_logistic_step", ref.train_one_logistic_step))
            suite = unittest.defaultTestLoader.loadTestsFromTestCase(test_day_01.Day01LearnerAcceptance)
            suite.addTests(unittest.defaultTestLoader.loadTestsFromTestCase(test_days_02_30.Day02To30Acceptance))
            result = unittest.TestResult()
            suite.run(result)
        self.assertTrue(result.wasSuccessful(), f"reference candidate errors={result.errors}, failures={result.failures}")

    def test_day_1_acceptance_rejects_no_update_wrong_sign_no_average_and_bad_one_feature_mutants(self):
        rows = [([1.0, 0.0], 1), ([0.0, 1.0], 0)]
        correct = ref.train_one_logistic_step
        mutants = {
            "no-update": lambda weights, examples, rate: list(weights),
            "wrong-sign": lambda weights, examples, rate: [0.0, -0.05, 0.05],
            "no-batch-averaging": lambda weights, examples, rate: [0.0, 0.1, -0.1],
            "rejects-valid-bias-plus-one-feature": lambda weights, examples, rate: (_ for _ in ()).throw(ValueError()) if len(weights) == 2 else correct(weights, examples, rate),
        }
        for name, mutant in mutants.items():
            with self.subTest(mutant=name), patch.object(test_day_01, "train_one_logistic_step", mutant):
                case = test_day_01.Day01LearnerAcceptance("test_one_step_moves_toward_independent_labels_without_mutating_inputs")
                with self.assertRaises((AssertionError, ValueError)):
                    case.test_one_step_moves_toward_independent_labels_without_mutating_inputs()
        # Confirm the scalar single-feature fixture itself is valid under the reference.
        self.assertEqual([0.1, 0.1], correct([0.0, 0.0], [([1.0], 1)], 0.2))

    def test_day_17_gradient_oracle_and_axis_sign_average_mutants(self):
        rows, labels, weights = [[1.0, 0.0], [0.0, 1.0]], [1, 0], [0, 0, 0]
        expected = [0.0, -0.25, 0.25]
        self.assertEqual(expected, ref.batch_gradient(rows, labels, weights).tolist())
        mutants = [
            lambda *_: [0.0, 0.25, -0.25],       # reversed sign
            lambda *_: [0.0, -0.5, 0.5],          # sum instead of mean
            lambda *_: [-0.25, 0.0, 0.25],        # mixed-up bias/feature axis
        ]
        for mutant in mutants:
            with patch.object(learner, "batch_gradient", mutant):
                case = test_days_02_30.Day02To30Acceptance("test_day_17_gradient_keeps_bias_and_feature_axes")
                with self.assertRaises(AssertionError):
                    case.test_day_17_gradient_keeps_bias_and_feature_axes()

    def test_named_review_mutants_are_rejected_by_their_learner_acceptance_paths(self):
        cases = [
            ("fit_standardizer", lambda *args: ([3, 14], [2, 5], [[-1, -1], [1, 1]], [[1, 1]]), "test_day_09_standardizer_uses_training_statistics"),
            ("nearest_centroid_predictions", lambda *args: [1, 0], "test_day_10_nearest_centroid_is_fit_on_training_rows"),
            ("threshold_predictions", lambda probabilities, threshold: [1, 1, 0, 0], "test_day_12_two_thresholds_show_precision_recall_tradeoff"),
            ("fold_evaluation_summary", lambda *args: {"fold_names": [], "fold_accuracies": [], "mean_cv_accuracy": 0, "final_test_accuracy": 0, "final_test_count": 0}, "test_day_22_retains_each_fold_score_and_one_final_test_result"),
            ("compare_predictions", lambda *args: {}, "test_day_25_error_ids_retain_order_and_ignore_correct_rows"),
            ("fit_document_type_centroids", lambda *args: {"classes": [], "feature_names": [], "training_ids": [], "centroids": []}, "test_day_30_train_save_reload_and_evaluate_document_type_model"),
        ]
        for name, mutant, test_name in cases:
            with self.subTest(function=name), ExitStack() as stack:
                for helper_name, candidate in PATCHES.items():
                    stack.enter_context(patch.object(learner, helper_name, candidate))
                stack.enter_context(patch.object(learner, name, mutant))
                case = test_days_02_30.Day02To30Acceptance(test_name)
                with self.assertRaises((AssertionError, ValueError, TypeError)):
                    getattr(case, test_name)()

    def test_each_day_02_through_30_rejects_a_substantive_mutant(self):
        cases = [
            ("sigmoid", lambda *_: 0.5, "test_day_02_sigmoid"),
            ("mean_binary_cross_entropy", lambda *_: 0.0, "test_day_03_binary_cross_entropy"),
            ("dot", lambda *_: 0.0, "test_day_04_dot_product"),
            ("central_difference", lambda *_: 0.0, "test_day_05_finite_difference"),
            ("train_one_logistic_step", lambda weights, *_: list(weights), "test_day_06_batch_step_matches_independent_fixed_contract"),
            ("deterministic_split", lambda records, *_: ([r["id"] for r in records], [r["id"] for r in records], []), "test_day_07_split_is_disjoint_and_ordered"),
            ("find_target_leakage", lambda *_: [], "test_day_08_reports_all_forbidden_features"),
            ("fit_threshold_classifier", lambda *_: {"threshold": -1.0, "training_count": 4}, "test_day_08_reports_all_forbidden_features"),
            ("predict_threshold_classifier", lambda _model, values: [1 for _ in values], "test_day_08_reports_all_forbidden_features"),
            ("fit_standardizer", lambda *_: ([0, 0], [1, 1], [[0, 0], [0, 0]], [[0, 0]]), "test_day_09_standardizer_uses_training_statistics"),
            ("majority_prediction", lambda *_: 0, "test_day_10_majority_tie_breaks_to_zero"),
            ("nearest_centroid_predictions", lambda *_: [1, 0], "test_day_10_nearest_centroid_is_fit_on_training_rows"),
            ("confusion_matrix", lambda *_: {"tp": 0, "tn": 0, "fp": 0, "fn": 0}, "test_day_11_confusion_matrix_has_exact_counts"),
            ("precision_recall", lambda *_: {"precision": 1.0, "recall": 1.0}, "test_day_12_undefined_metrics_remain_none"),
            ("threshold_predictions", lambda *_: [0, 0, 0, 0], "test_day_12_two_thresholds_show_precision_recall_tradeoff"),
            ("metrics_by_slice", lambda *_args, **_kwargs: {}, "test_day_13_slice_denominators_preserve_rows"),
            ("compare_predictions", lambda *_: {}, "test_day_14_comparison_reports_source_ordered_error_ids"),
            ("matrix_shape", lambda *_: (None, None), "test_day_15_rectangular_matrix_shape"),
            ("linear_scores", lambda *_: [0.0, 0.0], "test_day_16_batched_scores_match_scalar_oracle"),
            ("batch_gradient", lambda *_: [0.0, 0.0, 0.0], "test_day_17_gradient_keeps_bias_and_feature_axes"),
            ("finite_sigmoid", lambda *_: 0.5, "test_day_18_extreme_sigmoid_is_finite"),
            ("train_curve", lambda *_: [], "test_day_19_curve_retains_every_rate_and_checkpoint"),
            ("experiment_record", lambda *_: {}, "test_day_20_experiment_record_preserves_seed_and_ordered_metadata"),
            ("sklearn_pipeline_baseline", lambda *_: {}, "test_day_21_baseline_against_same_heldout_labels"),
            ("deterministic_folds", lambda *_: [["a", "b", "c"]], "test_day_22_retains_each_fold_score_and_one_final_test_result"),
            ("fold_evaluation_summary", lambda *_: {}, "test_day_22_retains_each_fold_score_and_one_final_test_result"),
            ("calibration_bins", lambda *_: [], "test_day_23_calibration_bins_match_fixed_counts"),
            ("build_document_fixture", lambda: [], "test_day_24_document_fixture_is_fixed_and_split_safe"),
            ("compare_predictions", lambda *_: {}, "test_day_25_error_ids_retain_order_and_ignore_correct_rows"),
            ("prediction_error_ids", lambda *_: [], "test_day_26_supplied_route_reading_is_paired_with_model_error_analysis"),
            ("prediction_error_ids", lambda *_: [], "test_day_27_seeded_failure_analysis_uses_observed_prediction_errors"),
            ("report_model_limit", lambda *_: {"score": 0.0}, "test_day_28_report_does_not_promote_score_to_decision"),
            ("load_linear_model", lambda *_: {"feature_names": [], "weights": []}, "test_day_29_roundtrip_model_preserves_feature_order_and_values"),
            ("fit_document_type_centroids", lambda *_: {}, "test_day_30_train_save_reload_and_evaluate_document_type_model"),
        ]
        for function_name, mutant, test_name in cases:
            with self.subTest(day=test_name, function=function_name), ExitStack() as stack:
                for helper_name, candidate in PATCHES.items():
                    stack.enter_context(patch.object(learner, helper_name, candidate))
                stack.enter_context(patch.object(learner, function_name, mutant))
                case = test_days_02_30.Day02To30Acceptance(test_name)
                with self.assertRaises((AssertionError, ValueError, TypeError, AttributeError, KeyError)):
                    getattr(case, test_name)()


if __name__ == "__main__":
    unittest.main()
