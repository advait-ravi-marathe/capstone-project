"""Run the early mathematical acceptance checks without third-party packages.

Invoke as: python3 -S -m ai.model_building.tests.stdlib_days_02_14
"""

from contextlib import ExitStack
import sys
import unittest
from unittest.mock import patch

from ai.model_building import learner
from ai.model_building.tests import reference_witness as ref
from ai.model_building.learner_acceptance.test_days_02_30 import Day02To30Acceptance


def main():
    functions = (
        "sigmoid", "mean_binary_cross_entropy", "dot", "central_difference",
        "train_one_logistic_step", "deterministic_split", "find_target_leakage",
        "fit_threshold_classifier", "predict_threshold_classifier", "fit_standardizer",
        "majority_prediction", "nearest_centroid_predictions", "confusion_matrix",
        "precision_recall", "threshold_predictions", "metrics_by_slice",
        "compare_predictions", "score_rows",
    )
    selected = [name for number in range(2, 15)
                for name in dir(Day02To30Acceptance)
                if name.startswith(f"test_day_{number:02d}_")]
    with ExitStack() as stack:
        for name in functions:
            stack.enter_context(patch.object(learner, name, getattr(ref, name)))
        suite = unittest.TestSuite(Day02To30Acceptance(name) for name in selected)
        result = unittest.TestResult()
        suite.run(result)
    if not result.wasSuccessful():
        raise AssertionError(result.failures + result.errors)
    if "numpy" in sys.modules:
        raise AssertionError("NumPy was imported by the Day 2–14 path")
    print(f"Days 2–14: {result.testsRun} witness-backed checks passed; NumPy was not imported")


if __name__ == "__main__":
    main()
