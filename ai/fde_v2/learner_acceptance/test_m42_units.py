"""M4.2 learner acceptance selectors; no AWS or sustained-availability proof."""
import importlib, os, unittest, tempfile
from ai.fde_v2.m42 import reference
from ai.fde_v2.m42 import workflow
C=importlib.import_module(os.environ.get('M42_CANDIDATE','ai.fde_v2.m42.learner'))

CHANGE={"requirement":"bounded change","old_behavior":"old","new_behavior":"new","risk":"consumer impact",
 "rollback_target":"known-good","compatibility":"backward-compatible additive","scope":"minimal","regression_tests":["test_contract"]}
EVENTS=[{"stage":s,"evidence_ref":s,"time_kind":"observed","observed_status":reference.SUCCESS_OUTCOMES[s]} for s in reference.INCIDENT_STAGES]

class M42Acceptance(unittest.TestCase):
    def test_observation_rows_preserve_failures_and_cost_unknown(self):
        out=C.summarize_observations([{"request_id":"a","status":"success","elapsed_ms":3},
          {"request_id":"b","status":"timeout","elapsed_ms":10,"timeout_penalty_ms":60}])
        self.assertEqual(out["request_count"],2); self.assertEqual(out["timeout_count"],1); self.assertIsNone(out["cost"])
    def test_gate_needs_actual_test_run_and_full_recovery_order(self):
        out=C.evaluate_change_gate(CHANGE,{"status":"PASS","source_hash":"d"*64,"test_run_id":"run"},
             {"classification":"local_drill","events":EVENTS})
        self.assertFalse(out["production_reliability_claim"])
        with self.assertRaises((ValueError,AssertionError)): C.summarize_observations([])
    def test_local_runtime_operations_are_driven_by_selected_candidate(self):
        with tempfile.TemporaryDirectory() as td:
            out=workflow.run_local_exercise(td,adapter=C)
            self.assertEqual(out["change"]["before"]["returncode"],1)
            self.assertEqual(out["change"]["after"]["returncode"],0)
            self.assertEqual(out["migration"]["status"],"PASS")
            self.assertEqual(out["rollback"]["status"],"PASS")
            self.assertEqual(out["incident"]["probe_returncodes"],[1,0])
            self.assertTrue(out["retention"]["physically_removed"])
            self.assertFalse(out["learner_completion"])

if __name__=='__main__': unittest.main()
