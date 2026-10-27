"""Fixed independent learner contracts for early model exercises.

Each listed test is expected to remain red at its explicit learner TODO until
the corresponding task is attempted. Expected values are independent fixtures.
"""

import math
import json
import subprocess
import sys
import unittest


from ai.model_building import learner


class Day02To30Acceptance(unittest.TestCase):
    def test_day_02_sigmoid(self):
        self.assertEqual(0.5, learner.sigmoid(0.0))
        self.assertAlmostEqual(0.8807970779778823, learner.sigmoid(2.0), places=14)
        self.assertAlmostEqual(0.11920292202211755, learner.sigmoid(-2.0), places=14)
        for invalid in (float("nan"), float("inf"), float("-inf")):
            with self.subTest(value=invalid), self.assertRaises(ValueError):
                learner.sigmoid(invalid)

    def test_day_03_binary_cross_entropy(self):
        high_confidence = learner.mean_binary_cross_entropy([0.8, 0.25], [1, 0])
        lower_confidence = learner.mean_binary_cross_entropy([0.6, 0.4], [1, 0])
        self.assertAlmostEqual((-math.log(0.8) - math.log(0.75)) / 2, high_confidence, places=14)
        self.assertLess(high_confidence, lower_confidence)
        for probabilities, labels in (([], []), ([0.8], []), ([1.1], [1]), ([0.2], [2]),
                                      ([0.0], [0]), ([1.0], [1])):
            with self.subTest(probabilities=probabilities, labels=labels):
                with self.assertRaises(ValueError):
                    learner.mean_binary_cross_entropy(probabilities, labels)

    def test_day_04_dot_product(self):
        self.assertEqual(5.5, learner.dot([0.5, 2.0], [3.0, 2.0]))
        self.assertEqual(-1.25, learner.dot([-1.0, 2.0, 0.5], [1.0, 0.25, -1.5]))
        with self.assertRaises(ValueError):
            learner.dot([1.0], [1.0, 2.0])

    def test_day_05_finite_difference(self):
        self.assertAlmostEqual(6.0, learner.central_difference(lambda x: x * x, 3.0), places=7)
        # Binary cross-entropy derivative for one x=2,y=1 example at w=.3:
        # p=sigmoid(.6), dL/dw=(p-y)*x. This checks a model parameter, not only x^2.
        probability = 1 / (1 + math.exp(-0.6))
        analytic = (probability - 1) * 2.0
        loss_at = lambda weight: -math.log(1 / (1 + math.exp(-2 * weight)))
        self.assertAlmostEqual(analytic, learner.central_difference(loss_at, 0.3), delta=1e-8)
        # A two-parameter model makes parameter-index mistakes observable.
        parameters, features = [0.4, -0.2], [2.0, 3.0]
        for index in (0, 1):
            def selected_loss(value):
                changed = parameters[:]
                changed[index] = value
                score = sum(w * x for w, x in zip(changed, features))
                return math.log1p(math.exp(-score))
            p = 1 / (1 + math.exp(-sum(w * x for w, x in zip(parameters, features))))
            self.assertAlmostEqual((p - 1) * features[index],
                                   learner.central_difference(selected_loss, parameters[index]), delta=1e-8)
        with self.assertRaises(ValueError):
            learner.central_difference(lambda x: x * x, 3.0, 0.0)

    def test_day_06_batch_step_matches_independent_fixed_contract(self):
        result = learner.train_one_logistic_step([0.0, 0.0, 0.0], [([1.0, 0.0], 1), ([0.0, 1.0], 0)], 0.2)
        self.assertEqual([0.0, 0.05, -0.05], result)
        varied = learner.train_one_logistic_step(
            [0.2, -0.1, 0.3], [([2.0, 1.0], 0), ([1.0, -1.0], 1), ([0.5, 0.5], 1)], 0.05
        )
        for actual, expected in zip(varied, [0.20668248272815268, -0.10643787157861116, 0.28480837045783386]):
            self.assertAlmostEqual(expected, actual, places=14)
        gradient = learner.mean_batch_gradient_python(
            [[2.0, 1.0], [1.0, -1.0], [0.5, 0.5]], [0, 1, 1], [0.2, -0.1, 0.3]
        )
        for actual, expected in zip(gradient, [-0.13364965456305328, 0.12875743157222325, 0.30383259084332215]):
            self.assertAlmostEqual(expected, actual, places=12)

    def test_day_07_split_is_disjoint_and_ordered(self):
        records = [{"id": f"r{i}", "label": label} for i, label in enumerate([0, 0, 1, 0, 1, 1])]
        self.assertEqual((["r0", "r1", "r2"], ["r3"], ["r4", "r5"]),
                         learner.deterministic_split(records, 3, 1))
        train_ids, validation_ids, test_ids = learner.deterministic_split(records, 3, 1)
        by_id = {row["id"]: row["label"] for row in records}
        class_counts = lambda ids: {label: sum(by_id[rid] == label for rid in ids) for label in (0, 1)}
        self.assertEqual({0: 2, 1: 1}, class_counts(train_ids))
        self.assertEqual({0: 1, 1: 0}, class_counts(validation_ids))
        self.assertEqual({0: 0, 1: 2}, class_counts(test_ids))
        with self.assertRaises(ValueError):
            learner.deterministic_split(records + [records[0]], 3, 1)

    def test_day_08_reports_all_forbidden_features(self):
        self.assertEqual(["answer", "claim_outcome"],
                         learner.find_target_leakage(["term_count", "answer", "claim_outcome"], ["answer", "claim_outcome"]))
        self.assertEqual([], learner.find_target_leakage(["term_count", "section_count"], ["answer"]))
        training_labels = [0, 0, 1, 1]
        clean_train = [0.0, 1.0, 2.0, 3.0]
        target_copy_train = training_labels[:]
        clean_model = learner.fit_threshold_classifier(clean_train, training_labels)
        leak_model = learner.fit_threshold_classifier(target_copy_train, training_labels)
        heldout_labels = [0, 1]
        clean_predictions = learner.predict_threshold_classifier(clean_model, [3.0, 1.0])
        leaky_predictions = learner.predict_threshold_classifier(leak_model, heldout_labels[:])
        comparison = {}
        for name, candidate in (("target_copy", leaky_predictions), ("clean_feature", clean_predictions)):
            errors = [record_id for record_id, expected, actual in zip(("heldout-a", "heldout-b"), heldout_labels, candidate)
                      if expected != actual]
            comparison[name] = {"accuracy": (len(heldout_labels) - len(errors)) / len(heldout_labels),
                                "error_ids": errors}
        self.assertEqual(1.0, comparison["target_copy"]["accuracy"])
        self.assertEqual(0.0, comparison["clean_feature"]["accuracy"])
        self.assertEqual([], comparison["target_copy"]["error_ids"])
        self.assertEqual(["heldout-a", "heldout-b"], comparison["clean_feature"]["error_ids"])
        self.assertEqual(["target_copy"], learner.find_target_leakage(["word_count", "target_copy"], ["target_copy"]))

    def test_day_09_standardizer_uses_training_statistics(self):
        train = [[1.0, 10.0], [3.0, 14.0]]
        heldout = [[5.0, 20.0]]
        means, scales, train_transformed, heldout_transformed = learner.fit_standardizer(train, heldout)
        self.assertEqual([2.0, 12.0], means)
        self.assertEqual([1.0, 2.0], scales)
        self.assertEqual([[-1.0, -1.0], [1.0, 1.0]], train_transformed)
        self.assertEqual([[3.0, 4.0]], heldout_transformed)
        self.assertEqual([[5.0, 20.0]], heldout)
        with self.assertRaises(ValueError):
            learner.fit_standardizer(train, [[5.0]])

    def test_day_10_majority_tie_breaks_to_zero(self):
        self.assertEqual(0, learner.majority_prediction([1, 0]))
        self.assertEqual(1, learner.majority_prediction([1, 1, 0]))
        with self.assertRaises(ValueError):
            learner.majority_prediction([])

    def test_day_10_nearest_centroid_is_fit_on_training_rows(self):
        training_rows = [[0.0, 0.0], [2.0, 0.0], [0.0, 2.0]]
        training_labels = [0, 0, 1]
        heldout_rows, heldout_labels = [[1.5, 0.0], [0.0, 1.5]], [0, 1]
        result = learner.nearest_centroid_predictions(training_rows, training_labels, heldout_rows)
        self.assertEqual([0, 1], result)
        majority_predictions = [learner.majority_prediction(training_labels)] * len(heldout_labels)
        self.assertEqual(1.0, sum(a == b for a, b in zip(heldout_labels, result)) / len(result))
        self.assertEqual(0.5, sum(a == b for a, b in zip(heldout_labels, majority_predictions)) / len(result))
        with self.assertRaises(ValueError):
            learner.nearest_centroid_predictions([[0.0]], [0], [[0.0, 1.0]])

    def test_day_11_confusion_matrix_has_exact_counts(self):
        self.assertEqual({"tp": 1, "tn": 1, "fp": 1, "fn": 2},
                         learner.confusion_matrix([1, 1, 0, 0, 1], [1, 0, 1, 0, 0]))
        with self.assertRaises(ValueError):
            learner.confusion_matrix([1], [1, 0])

    def test_day_12_undefined_metrics_remain_none(self):
        self.assertEqual({"precision": None, "recall": 0.0},
                         learner.precision_recall({"tp": 0, "tn": 2, "fp": 0, "fn": 1}))
        with self.assertRaises(ValueError):
            learner.precision_recall({"tp": -1, "tn": 2, "fp": 0, "fn": 1})

    def test_day_12_two_thresholds_show_precision_recall_tradeoff(self):
        probabilities = [0.9, 0.7, 0.4, 0.2]
        labels = [1, 0, 1, 0]
        low = learner.threshold_predictions(probabilities, 0.5)
        high = learner.threshold_predictions(probabilities, 0.75)
        self.assertEqual([1, 1, 0, 0], low)
        self.assertEqual([1, 0, 0, 0], high)
        self.assertEqual({"tp": 1, "tn": 1, "fp": 1, "fn": 1}, learner.confusion_matrix(labels, low))
        self.assertEqual({"tp": 1, "tn": 2, "fp": 0, "fn": 1}, learner.confusion_matrix(labels, high))
        self.assertEqual({"precision": 0.5, "recall": 0.5}, learner.precision_recall(learner.confusion_matrix(labels, low)))
        self.assertEqual({"precision": 1.0, "recall": 0.5}, learner.precision_recall(learner.confusion_matrix(labels, high)))

    def test_day_13_slice_denominators_preserve_rows(self):
        result = learner.metrics_by_slice([1, 0, 1], [1, 1, 0], ["a", "a", "b"])
        self.assertEqual({"a": {"count": 2, "tp": 1, "tn": 0, "fp": 1, "fn": 0,
                                 "observed_positive_fraction": 1.0, "precision": 0.5, "recall": 1.0},
                          "b": {"count": 1, "tp": 0, "tn": 0, "fp": 0, "fn": 1,
                                 "observed_positive_fraction": 0.0, "precision": None, "recall": 0.0},
                          "unknown": {"count": 0, "tp": 0, "tn": 0, "fp": 0, "fn": 0,
                                      "observed_positive_fraction": None, "precision": None, "recall": None}},
                         learner.metrics_by_slice([1, 0, 1], [1, 1, 0], ["a", "a", "b"], expected_slices=["a", "b", "unknown"]))
        with self.assertRaises(ValueError):
            learner.metrics_by_slice([1], [1], [])

    def test_day_14_comparison_reports_source_ordered_error_ids(self):
        result = learner.compare_predictions(["r0", "r1", "r2"], [1, 0, 1], {"baseline": [0, 0, 1]})
        self.assertEqual(["r0"], result["baseline"]["error_ids"])
        self.assertEqual(2 / 3, result["baseline"]["accuracy"])
        with self.assertRaises(ValueError):
            learner.compare_predictions(["r0"], [1, 0], {"bad": [1, 0]})
        train_ids = ["t1", "t2", "t3", "t4"]
        test_ids = ["e1", "e2"]
        train_rows, train_labels = [[0], [1], [2], [3]], [0, 0, 1, 1]
        test_rows, test_labels = [[0.5], [2.5]], [0, 1]
        self.assertFalse(set(train_ids) & set(test_ids))
        self.assertEqual(4, len(train_rows))
        self.assertEqual(2, len(test_rows))
        weights = learner.train_one_logistic_step([0.0, 0.0], list(zip(train_rows, train_labels)), 0.1)
        # Keep this before Day 15: use the already-taught scalar bias + dot
        # arithmetic, not the later NumPy batch-score helper.
        linear = [int(weights[0] + sum(w * x for w, x in zip(weights[1:], row)) >= 0.5)
                  for row in test_rows]
        centroid = learner.nearest_centroid_predictions(train_rows, train_labels, test_rows)
        majority = [learner.majority_prediction(train_labels)] * len(test_ids)
        report = learner.compare_predictions(test_ids, test_labels,
                                             {"majority": majority, "centroid": centroid, "linear": linear})
        self.assertEqual({"majority": {"accuracy": 0.5, "error_ids": ["e2"]},
                          "centroid": {"accuracy": 1.0, "error_ids": []},
                          "linear": {"accuracy": 0.5, "error_ids": ["e2"]}}, report)
        self.assertFalse(set(train_ids) & set(test_ids))

    def test_day_15_rectangular_matrix_shape(self):
        import numpy as np
        array, labels = learner.matrix_shape([[1, 2, 3], [4, 5, 6]], [0, 1])
        self.assertIsInstance(array, np.ndarray)
        self.assertEqual((2, 3), array.shape)
        np.testing.assert_array_equal([[1, 2, 3], [4, 5, 6]], array)
        self.assertEqual(np.dtype("int64"), array.dtype)
        self.assertEqual((2,), labels.shape)
        np.testing.assert_array_equal([0, 1], labels)
        with self.assertRaises(ValueError):
            learner.matrix_shape([[1, 2], [3]], [0, 1])

    def test_day_16_batched_scores_match_scalar_oracle(self):
        import numpy as np
        scores = learner.linear_scores([[1, 0, 2], [0, 1, 1]], [1.0, 2.0, 3.0], 0.5)
        self.assertIsInstance(scores, np.ndarray)
        np.testing.assert_allclose([7.5, 5.5], scores, rtol=0, atol=1e-12)
        with self.assertRaises(ValueError):
            learner.linear_scores([[1, 2, 3]], [1, 2], 0.0)

    def test_day_17_gradient_keeps_bias_and_feature_axes(self):
        import numpy as np
        gradient = learner.batch_gradient([[1.0, 0.0], [0.0, 1.0]], [1, 0], [0, 0, 0])
        # At zero weights p=.5. Row gradients are [-.5,-.5,0] and [.5,0,.5];
        # their mean is [0,-.25,.25] in bias, feature-0, feature-1 order.
        np.testing.assert_allclose([0.0, -0.25, 0.25], gradient, rtol=0, atol=1e-12)
        with self.assertRaises(ValueError):
            learner.batch_gradient([[1.0]], [1, 0], [0.0, 0.0])
        rows = [[1.0, 0.0], [0.0, 1.0], [2.0, 1.0]]
        labels = [1, 0, 1]
        gradient = learner.batch_gradient(rows, labels, [0.2, -0.1, 0.3])
        np.testing.assert_allclose([-0.09270632, -0.44204526, 0.06563395], gradient, rtol=0, atol=1e-8)

    def test_day_18_extreme_sigmoid_is_finite(self):
        self.assertAlmostEqual(0.11920292202211755, learner.finite_sigmoid(-2.0), places=14)
        self.assertEqual(0.5, learner.finite_sigmoid(0.0))
        self.assertAlmostEqual(0.8807970779778823, learner.finite_sigmoid(2.0), places=14)
        self.assertTrue(math.isfinite(learner.finite_sigmoid(-1000.0)))
        self.assertEqual(1.0, learner.finite_sigmoid(1000.0))
        self.assertGreaterEqual(learner.finite_sigmoid(-1000.0), 0.0)
        self.assertGreater(learner.finite_sigmoid(2.0), learner.finite_sigmoid(0.0))
        for invalid in (float("nan"), float("inf"), float("-inf")):
            with self.subTest(value=invalid), self.assertRaises(ValueError):
                learner.finite_sigmoid(invalid)

    def test_day_19_curve_retains_every_rate_and_checkpoint(self):
        curve = learner.train_curve(0.0, [0.1, 0.5, 2.0], 4)
        self.assertEqual([0.1, 0.5, 2.0], [row["learning_rate"] for row in curve])
        import numpy as np
        np.testing.assert_allclose([4.0, 2.56, 1.6384, 1.048576, 0.67108864], curve[0]["losses"], rtol=0, atol=1e-12)
        np.testing.assert_allclose([4.0, 0.0, 0.0, 0.0, 0.0], curve[1]["losses"], rtol=0, atol=1e-12)
        np.testing.assert_allclose([4.0, 36.0, 324.0, 2916.0, 26244.0], curve[2]["losses"], rtol=0, atol=1e-12)
        self.assertEqual(5, len(curve[0]["losses"]))
        self.assertLess(curve[0]["losses"][-1], curve[0]["losses"][0])
        self.assertGreater(curve[2]["losses"][-1], curve[2]["losses"][0])
        with self.assertRaises(ValueError):
            learner.train_curve(0.0, [0.0], 4)

    def test_day_20_experiment_record_preserves_seed_and_ordered_metadata(self):
        examples = [([1.0, 0.0], 1), ([0.0, 1.0], 0), ([1.0, 1.0], 1)]
        ids = ["train-a", "train-b", "train-c"]
        weights_a = learner.train_one_logistic_step([0.0, 0.0, 0.0], examples, 0.1)
        weights_b = learner.train_one_logistic_step([0.0, 0.0, 0.0], examples, 0.1)
        self.assertEqual([0.016666666666666666, 0.03333333333333333, 0.0], weights_a)
        self.assertEqual(weights_a, weights_b)
        predictions = [int(weights_a[0] + sum(w*x for w,x in zip(weights_a[1:], row)) >= 0.0)
                       for row in [[1.0, 0.0], [0.0, 1.0]]]
        record = learner.experiment_record(41, ids, ["schedule_count", "exclusion_count"], predictions)
        self.assertEqual({"seed": 41, "split_ids": ids, "feature_names": ["schedule_count", "exclusion_count"], "predictions": [1, 1]}, record)
        self.assertNotEqual(record, learner.experiment_record(42, ids, ["schedule_count", "exclusion_count"], predictions))
        # A separate process independently repeats the fixed CPU gradient step.
        script = "import json,math; x=[([1.,0.],1),([0.,1.],0),([1.,1.],1)]; w=[0.,0.,0.]; g=[0.,0.,0.];\nfor row,y in x:\n p=1/(1+math.exp(-sum(a*b for a,b in zip(w,[1.,*row])))); e=p-y\n for i,v in enumerate([1.,*row]): g[i]+=e*v/len(x)\nw=[a-.1*b for a,b in zip(w,g)]; p=[int(w[0]+w[1]*1+w[2]*0>=0),int(w[0]+w[1]*0+w[2]*1>=0)]; print(json.dumps({'seed':41,'split_ids':['train-a','train-b','train-c'],'feature_names':['schedule_count','exclusion_count'],'predictions':p},separators=(',',':')))"
        child = subprocess.run([sys.executable, "-c", script], check=True, capture_output=True, text=True)
        self.assertEqual(json.dumps(record, separators=(",", ":")), child.stdout.strip())

    def test_day_21_baseline_against_same_heldout_labels(self):
        import numpy as np
        report = learner.sklearn_pipeline_baseline(
            [[0.0], [1.0], [9.0], [10.0]], [0, 0, 1, 1], [0, 1, 2], [3]
        )
        self.assertEqual([0, 1, 2], report["fit_indices"])
        self.assertEqual([3], report["test_indices"])
        self.assertEqual([1], report["predictions"])
        self.assertEqual(1, report["correct_count"])
        self.assertEqual("Pipeline", type(report["pipeline"]).__name__)
        np.testing.assert_allclose([10 / 3], report["pipeline"].named_steps["standardscaler"].mean_)
        np.testing.assert_allclose([4.0276819911981905], report["pipeline"].named_steps["standardscaler"].scale_, rtol=0, atol=1e-12)
        with self.assertRaises(ValueError):
            learner.sklearn_pipeline_baseline([[0.0], [1.0], [9.0], [10.0]], [0, 0, 1, 1], [0, 1, 3], [3])

    def test_day_22_preprocessing_fit_must_exclude_evaluation_ids(self):
        self.assertTrue(learner.fit_id_guard(["a", "b"], ["a", "b"], ["c"]))
        self.assertFalse(learner.fit_id_guard(["a", "b"], ["a", "c"], ["c"]))
        self.assertFalse(learner.fit_id_guard(["a", "b"], ["a", "b"], ["b"]))

    def test_day_22_retains_each_fold_score_and_one_final_test_result(self):
        ids = ["a", "b", "c", "d", "e", "f"]
        folds = learner.deterministic_folds(ids, 3)
        self.assertEqual([["a", "d"], ["b", "e"], ["c", "f"]], folds)
        self.assertEqual(set(ids), {rid for fold in folds for rid in fold})
        final_test_ids = ["g", "h"]
        self.assertFalse(set(ids) & set(final_test_ids))
        dev_labels = {"a": 1, "b": 0, "c": 1, "d": 0, "e": 1, "f": 0}
        dev_predictions = {"a": 1, "b": 0, "c": 1, "d": 1, "e": 0, "f": 0}
        fold_labels = [[dev_labels[rid] for rid in fold] for fold in folds]
        fold_predictions = [[dev_predictions[rid] for rid in fold] for fold in folds]
        final_ids, final_labels, final_predictions = ["g", "h"], [1, 0], [1, 1]
        self.assertFalse(set(ids) & set(final_ids))
        result = learner.fold_evaluation_summary(
            ["fold-a", "fold-b", "fold-c"], fold_labels, fold_predictions, final_labels, final_predictions,
        )
        self.assertEqual({
            "fold_names": ["fold-a", "fold-b", "fold-c"],
            "fold_accuracies": [0.5, 0.5, 1.0],
            "mean_cv_accuracy": 2 / 3,
            "final_test_accuracy": 1 / 2,
            "final_test_count": 2,
        }, result)
        with self.assertRaises(ValueError):
            learner.fold_evaluation_summary(["fold-a"], [[1]], [[1, 0]], [1], [1])
        with self.assertRaises(ValueError):
            learner.deterministic_folds(["a", "a", "b"], 2)

    def test_day_23_calibration_bins_match_fixed_counts(self):
        self.assertEqual([{"count": 2, "mean_confidence": 0.25, "observed_positive_fraction": 0.5},
                          {"count": 2, "mean_confidence": 0.75, "observed_positive_fraction": 0.5}],
                         learner.calibration_bins([0.1, 0.4, 0.6, 0.9], [0, 1, 0, 1], [0, 0.5, 1]))
        self.assertEqual([
            {"count": 1, "mean_confidence": 0.0, "observed_positive_fraction": 0.0},
            {"count": 0, "mean_confidence": None, "observed_positive_fraction": None},
            {"count": 2, "mean_confidence": 0.875, "observed_positive_fraction": 1.0},
        ], learner.calibration_bins([0.0, 0.75, 1.0], [0, 1, 1], [0, 0.25, 0.75, 1]))
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
        result = learner.compare_predictions(
            ["a", "b", "c", "d"], [1, 0, 1, 0],
            {"majority": [1, 1, 1, 1], "lexical": [1, 0, 0, 0], "linear": [0, 0, 1, 1]},
        )
        self.assertEqual({
            "majority": {"accuracy": 0.5, "error_ids": ["b", "d"]},
            "lexical": {"accuracy": 0.75, "error_ids": ["c"]},
            "linear": {"accuracy": 0.5, "error_ids": ["a", "d"]},
        }, result)
        # Exercise three actual training-only baselines on one held-out set,
        # then verify complete independent confusion tables.
        train_rows, train_labels = [[0.0], [1.0], [2.0], [3.0]], [0, 0, 1, 1]
        heldout_ids, heldout_rows, heldout_labels = ["h1", "h2", "h3", "h4"], [[0.0], [1.0], [2.0], [3.0]], [0, 1, 1, 0]
        centroid = learner.nearest_centroid_predictions(train_rows, train_labels, heldout_rows)
        majority = [learner.majority_prediction(train_labels)] * len(heldout_ids)
        # Fixed lexical rule predicts class 1 at feature >= 2.
        lexical = [int(row[0] >= 2) for row in heldout_rows]
        linear_weights = learner.train_one_logistic_step([0.0, 0.0], list(zip(train_rows, train_labels)), 0.1)
        linear = [int(linear_weights[0] + sum(w*x for w,x in zip(linear_weights[1:], row)) >= 0.5)
                  for row in heldout_rows]
        table = learner.compare_predictions(heldout_ids, heldout_labels,
                                            {"majority": majority, "lexical": lexical, "linear": linear, "centroid": centroid})
        self.assertEqual({
            "majority": {"accuracy": 0.5, "error_ids": ["h2", "h3"]},
            "lexical": {"accuracy": 0.5, "error_ids": ["h2", "h4"]},
            "linear": {"accuracy": 0.5, "error_ids": ["h2", "h3"]},
            "centroid": {"accuracy": 0.5, "error_ids": ["h2", "h4"]},
        }, table)
        self.assertEqual({"tp": 0, "tn": 2, "fp": 0, "fn": 2}, learner.confusion_matrix(heldout_labels, majority))
        self.assertEqual({"tp": 1, "tn": 1, "fp": 1, "fn": 1}, learner.confusion_matrix(heldout_labels, lexical))
        self.assertEqual({"tp": 0, "tn": 2, "fp": 0, "fn": 2}, learner.confusion_matrix(heldout_labels, linear))
        self.assertEqual({"tp": 1, "tn": 1, "fp": 1, "fn": 1}, learner.confusion_matrix(heldout_labels, centroid))
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
        import numpy as np
        serialized = learner.serialize_linear_model([0.1, 0.2, -0.3], ["a", "b"])
        loaded = learner.load_linear_model(serialized)
        self.assertEqual(["a", "b"], loaded["feature_names"])
        self.assertEqual([0.1, 0.2, -0.3], loaded["weights"])
        before = learner.score_rows([0.1, 0.2, -0.3], [[1, 1], [0, 1]])
        after = learner.score_rows(loaded["weights"], [[1, 1], [0, 1]])
        np.testing.assert_allclose([0.0, -0.2], before, rtol=0, atol=1e-12)
        np.testing.assert_allclose(before, after, rtol=0, atol=1e-12)
        with self.assertRaises(ValueError):
            learner.load_linear_model('{"weights":[NaN],"feature_names":["x"]}')
        with self.assertRaises(ValueError):
            learner.score_rows(loaded["weights"], [[1.0]])

    def test_day_30_train_save_reload_and_evaluate_document_type_model(self):
        train_rows = [[2, 0, 0], [1, 0, 0], [0, 2, 0], [0, 1, 0], [0, 0, 2], [0, 0, 1]]
        train_labels = ["schedule", "schedule", "exclusion", "exclusion", "definition", "definition"]
        train_ids = [f"train-{i}" for i in range(1, 7)]
        feature_names = ["schedule_count", "exclusion_count", "definition_count"]
        model = learner.fit_document_type_centroids(train_rows, train_labels, train_ids, feature_names)
        serialized = learner.serialize_document_type_model(model)
        loaded = learner.load_document_type_model(serialized)
        heldout_rows = [[1.4, 0.1, 0.0], [0.1, 1.4, 0.0], [0.0, 0.1, 1.4]]
        heldout_labels = ["schedule", "exclusion", "definition"]
        predictions = learner.predict_document_type(loaded, heldout_rows)
        predictions_before = learner.predict_document_type(model, heldout_rows)
        self.assertEqual(feature_names, loaded["feature_names"])
        self.assertEqual(train_ids, loaded["training_ids"])
        self.assertEqual(["schedule", "exclusion", "definition"], loaded["classes"])
        import numpy as np
        np.testing.assert_allclose([[1.5, 0.0, 0.0], [0.0, 1.5, 0.0], [0.0, 0.0, 1.5]], loaded["centroids"], rtol=0, atol=1e-12)
        self.assertEqual(heldout_labels, predictions)
        self.assertEqual(predictions_before, predictions)
        self.assertEqual([], learner.prediction_error_ids(["test-1", "test-2", "test-3"], heldout_labels, predictions))
        self.assertAlmostEqual(1.0, sum(a == b for a, b in zip(heldout_labels, predictions)) / len(heldout_labels))
        with self.assertRaises(ValueError):
            learner.fit_document_type_centroids(train_rows, train_labels[:-1], train_ids, feature_names)
        with self.assertRaises(ValueError):
            learner.predict_document_type(loaded, [[1.0, 2.0]])
        with self.assertRaises(ValueError):
            learner.predict_document_type(loaded, [[float("nan"), 0.0, 1.0]])
        with self.assertRaises(ValueError):
            learner.load_document_type_model('{"classes": ["schedule"], "centroids": [[NaN]]}')


if __name__ == "__main__":
    unittest.main()
