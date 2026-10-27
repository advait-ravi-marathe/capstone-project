"""Learner acceptance contract; expected red until the Day 1 TODO is done."""

import unittest

from ai.model_building.learner import train_one_logistic_step
from ai.model_building.day01_example import loss


class Day01LearnerAcceptance(unittest.TestCase):
    def test_one_step_moves_toward_independent_labels_without_mutating_inputs(self):
        rows = [([1.0, 0.0], 1), ([0.0, 1.0], 0)]
        original = [0.0, 0.0, 0.0]
        updated = train_one_logistic_step(original, rows, 0.2)
        self.assertEqual([0.0, 0.05, -0.05], updated)
        self.assertEqual([0.0, 0.0, 0.0], original)
        self.assertLess(loss(updated, rows), loss(original, rows))

    def test_rejects_empty_batch_bad_dimensions_bad_labels_and_nonpositive_rate(self):
        invalid = [
            ([], [], 0.2),
            ([0.0, 0.0], [([1.0], 1)], 0.2),
            ([0.0, 0.0], [([1.0], 2)], 0.2),
            ([0.0, 0.0], [([1.0], 1)], 0.0),
        ]
        for weights, rows, rate in invalid:
            with self.subTest(weights=weights, rows=rows, rate=rate):
                with self.assertRaises(ValueError):
                    train_one_logistic_step(weights, rows, rate)


if __name__ == "__main__":
    unittest.main()
