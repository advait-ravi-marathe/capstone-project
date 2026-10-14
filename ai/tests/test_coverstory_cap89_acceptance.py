"""Saved learner assertions for CAP-89; expected to stop at the TODO today."""
from __future__ import annotations

import json
import unittest
from pathlib import Path

from ai.coverstory.cap_89 import Baseline, GatePolicy, gate

FIXTURE = Path(__file__).resolve().parents[1] / "fixtures/coverstory/CAP-89/synthetic-v1.json"


class CAP89AcceptanceTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))

    def run_case(self, case_id):
        case = next(c for c in self.fixture["cases"] if c["id"] == case_id)
        return gate(case["report"], Baseline(**self.fixture["baseline"]), GatePolicy(**self.fixture["policy"]))

    def test_boundary_is_inclusive(self):
        self.assertEqual(0, self.run_case("boundary-pass").exit_code)

    def test_regression_below_boundary_fails(self):
        self.assertEqual(1, self.run_case("ranking-regression").exit_code)

    def test_cross_owner_safety_violation_fails_even_with_rr_one(self):
        self.assertEqual(1, self.run_case("cross-owner-citation").exit_code)

    def test_missing_case_is_invalid(self):
        self.assertEqual(2, self.run_case("missing-required-case").exit_code)

    def test_nan_is_invalid(self):
        self.assertEqual(2, self.run_case("nan-metric").exit_code)


if __name__ == "__main__":
    unittest.main()
