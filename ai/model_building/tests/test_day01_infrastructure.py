import json
import subprocess
import sys
import unittest

from ai.model_building.day01_example import loss, reference_step


class Day01InstructorInfrastructureTests(unittest.TestCase):
    def test_worked_example_is_deterministic_and_warns_about_toy_data(self):
        run = subprocess.run(
            [sys.executable, "-m", "ai.model_building.day01_example"],
            check=True, capture_output=True, text=True,
        )
        report = json.loads(run.stdout)
        self.assertEqual(["mentions_waiting_period", "mentions_exclusion"], report["features"])
        self.assertEqual([1, 0], report["labels"])
        self.assertEqual([0.0, 0.05, -0.05], report["weights_after_one_cpu_step"])
        self.assertLess(report["mean_loss_after"], report["mean_loss_before"])
        self.assertIn("not a useful", report["warning"])

    def test_reference_expected_result_is_independent_and_rejects_two_mutants(self):
        rows = [([1.0, 0.0], 1), ([0.0, 1.0], 0)]
        initial = [0.0, 0.0, 0.0]
        expected = [0.0, 0.05, -0.05]
        observed = reference_step(initial, rows, 0.2)
        self.assertEqual(expected, observed)
        self.assertLess(loss(observed, rows), loss(initial, rows))
        # Negative controls model a missing update and a reversed gradient.
        no_update = initial.copy()
        reversed_gradient = [0.0, -0.05, 0.05]
        self.assertNotEqual(expected, no_update)
        self.assertNotEqual(expected, reversed_gradient)
        self.assertGreater(loss(reversed_gradient, rows), loss(initial, rows))


if __name__ == "__main__":
    unittest.main()
