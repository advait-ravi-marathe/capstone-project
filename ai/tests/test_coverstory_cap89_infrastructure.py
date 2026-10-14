from __future__ import annotations

import json
import math
import subprocess
import sys
import unittest
from pathlib import Path

from ai.coverstory.cap_89 import Baseline, GatePolicy, GateResult, gate

FIXTURE = Path(__file__).resolve().parents[1] / "fixtures/coverstory/CAP-89/synthetic-v1.json"


def reference_gate(report: dict, baseline: dict, policy: dict) -> dict:
    """Independent fixture oracle. Deliberately does not call the learner seam."""
    required = policy["required_cases"]
    completed = report.get("completed_cases")
    rr = report.get("mean_rr")
    base = baseline.get("mean_rr")
    if (not isinstance(completed, list) or not isinstance(rr, (int, float)) or isinstance(rr, bool)
            or not isinstance(base, (int, float)) or isinstance(base, bool)
            or not math.isfinite(float(rr)) or not math.isfinite(float(base))
            or any(case not in completed for case in required)):
        return {"kind": "INVALID", "exit_code": 2}
    if report.get("authorization_violations") != 0 or report.get("unsupported_answers") != 0:
        return {"kind": "FAIL", "exit_code": 1}
    if rr < base - policy["allowed_regression"]:
        return {"kind": "FAIL", "exit_code": 1}
    return {"kind": "PASS", "exit_code": 0}


class CAP89InfrastructureTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))
        cls.policy = cls.fixture["policy"]
        cls.baseline = cls.fixture["baseline"]

    def test_fixture_is_synthetic_and_independent_oracle_satisfies_saved_cases(self):
        self.assertEqual("CAP-89-synthetic-v1", self.fixture["fixture_id"])
        self.assertTrue(self.fixture["synthetic"])
        self.assertFalse(self.fixture["actual_sources_used"])
        for case in self.fixture["cases"]:
            self.assertEqual(case["expected"], reference_gate(case["report"], self.baseline, self.policy), case["id"])

    def test_exact_boundary_passes_but_779_fails(self):
        cases = {c["id"]: c for c in self.fixture["cases"]}
        self.assertEqual(0.78, cases["boundary-pass"]["report"]["mean_rr"])
        self.assertEqual({"kind": "PASS", "exit_code": 0}, reference_gate(cases["boundary-pass"]["report"], self.baseline, self.policy))
        self.assertEqual({"kind": "FAIL", "exit_code": 1}, reference_gate(cases["ranking-regression"]["report"], self.baseline, self.policy))

    def test_safety_cannot_be_offset_by_perfect_ranking(self):
        case = next(c for c in self.fixture["cases"] if c["id"] == "cross-owner-citation")
        self.assertEqual(1.0, case["report"]["mean_rr"])
        self.assertEqual({"kind": "FAIL", "exit_code": 1}, reference_gate(case["report"], self.baseline, self.policy))

    def test_missing_case_and_nan_are_invalid(self):
        for case in self.fixture["cases"]:
            if case["id"] in {"missing-required-case", "nan-metric"}:
                self.assertEqual({"kind": "INVALID", "exit_code": 2}, reference_gate(case["report"], self.baseline, self.policy))

    def test_known_failing_subprocess_exit_is_preserved_by_caller(self):
        # A local caller must propagate the child status; there is deliberately
        # no repository CI workflow integration claimed by this harness.
        child = subprocess.run([sys.executable, "-c", "raise SystemExit(1)"], check=False)
        observed_exit = child.returncode
        self.assertEqual(1, observed_exit)

    def test_learner_seam_is_present_and_explicitly_unimplemented(self):
        case = self.fixture["cases"][0]
        with self.assertRaisesRegex(NotImplementedError, "CAP-89 learner TODO"):
            gate(case["report"], Baseline(**self.baseline), GatePolicy(**self.policy))


if __name__ == "__main__":
    unittest.main()
