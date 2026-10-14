import json
import unittest
from pathlib import Path

from scripts.coverstory_local_gate import GateInput, evaluate


ROOT = Path(__file__).resolve().parents[2]
FIXTURES = ROOT / "fixtures/coverstory/CAP-96"


def report(name):
    return json.loads((FIXTURES / "reports" / name).read_text(encoding="utf-8"))


class Cap96GateInfrastructureTests(unittest.TestCase):
    def test_all_pass_with_explicit_reports_only_qualifies_locally(self):
        result = evaluate(GateInput(0, report("smoke-pass.json"), report("learner-pass.json"), report("oracle-valid.json")))
        self.assertEqual("PASS", result["decision"])
        self.assertTrue(result["package_eligible_locally"])
        self.assertFalse(result["publication_claim"])

    def test_learner_failure_is_not_hidden_by_green_smoke(self):
        result = evaluate(GateInput(1, report("smoke-pass.json"), report("learner-fail.json"), report("oracle-valid.json")))
        self.assertEqual("FAIL", result["decision"])
        self.assertTrue(result["smoke_healthy"])
        self.assertFalse(result["package_eligible_locally"])

    def test_zero_exit_without_learner_report_is_incomplete(self):
        result = evaluate(GateInput(0, report("smoke-pass.json"), None, report("oracle-valid.json")))
        self.assertEqual("INCOMPLETE", result["decision"])

    def test_permanent_todo_501_invalidates_smoke_harness(self):
        result = evaluate(GateInput(1, report("smoke-todo-501.json"), report("learner-pass.json"), report("oracle-valid.json"), todo_sentinel=True))
        self.assertEqual("HARNESS_INVALID", result["decision"])

    def test_known_defect_is_rejected_with_retained_independent_oracle(self):
        result = evaluate(GateInput(0, report("smoke-pass.json"), report("learner-pass.json"), report("oracle-valid.json"), known_defect=True))
        self.assertEqual("FAIL", result["decision"])
        self.assertTrue(result["oracle_evidence_retained"])

    def test_unproven_known_defect_rejection_invalidates_oracle(self):
        result = evaluate(GateInput(0, report("smoke-pass.json"), report("learner-pass.json"), report("oracle-invalid.json"), known_defect=True))
        self.assertEqual("HARNESS_INVALID", result["decision"])
        self.assertTrue(result["oracle_evidence_retained"])


if __name__ == "__main__":
    unittest.main()
