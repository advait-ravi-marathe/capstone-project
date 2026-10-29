"""M4.2 mechanics use complete local receipts and keep live cloud blocked."""
import unittest, tempfile
import sqlite3
from ai.fde_v2.m42 import learner, reference, workflow

CHANGE={"requirement":"add stable source version","old_behavior":"source omitted","new_behavior":"source_version returned",
        "risk":"legacy clients may ignore field","rollback_target":"previous commit","compatibility":"additive response field",
        "scope":"minimal","regression_tests":["test_contract_source_version"]}
EVENTS=[{"stage":s,"evidence_ref":f"local-{s}","time_kind":"observed","observed_status":reference.SUCCESS_OUTCOMES[s]}
        for s in reference.INCIDENT_STAGES]
INCIDENT={"classification":"local_drill","events":EVENTS}

class M42Mechanics(unittest.TestCase):
    def test_observations_keep_errors_and_timeout_penalty_in_denominator(self):
        rows=[{"request_id":"1","status":"success","elapsed_ms":10},
              {"request_id":"2","status":"error","elapsed_ms":20},
              {"request_id":"3","status":"timeout","elapsed_ms":30,"timeout_penalty_ms":100}]
        got=reference.summarize_observations(rows)
        self.assertEqual((got["request_count"],got["error_count"],got["timeout_count"]),(3,1,1))
        self.assertEqual(got["availability_fraction"],1/3)
        self.assertEqual(got["all_request_p95_ms"],100)
        self.assertIsNone(got["cost"])
        with self.assertRaises(ValueError): reference.summarize_observations(rows[:2]+[rows[0]])

    def test_change_and_recovery_require_test_receipt_and_never_claim_production(self):
        obs=[{"request_id":"r1","status":"success","elapsed_ms":10}]
        summary={"status":"PASS","source_hash":"a"*64,"test_run_id":"run-local"}
        out=workflow.review_change(CHANGE,obs,INCIDENT,summary,adapter=reference)
        self.assertEqual(out["gate"]["status"],"EVIDENCE_REQUIRES_REVIEW")
        self.assertFalse(out["gate"]["production_reliability_claim"])
        self.assertEqual(out["gate"]["live_cloud_status"],"BLOCKED")
        blocked=reference.evaluate_change_gate(CHANGE,{},INCIDENT)
        self.assertEqual(blocked["status"],"BLOCKED")

    def test_incident_order_and_estimated_time_need_provenance(self):
        bad={**INCIDENT,"events":[EVENTS[1],EVENTS[0],*EVENTS[2:]]}
        self.assertEqual(reference.validate_incident(bad)["status"],"INCOMPLETE")
        empty=[dict(x) for x in EVENTS]; empty[0]={**EVENTS[0],"observed_status":" "}
        with self.assertRaises(ValueError): reference.validate_incident({"classification":"local_drill","events":empty})
        failed=[dict(x) for x in EVENTS]; failed[3]={**EVENTS[3],"observed_status":"not_recovered"}
        self.assertEqual(reference.validate_incident({"classification":"local_drill","events":failed})["status"],"INCOMPLETE")
        estimated=[dict(x) for x in EVENTS]; estimated[0]={**EVENTS[0],"time_kind":"estimated"}
        with self.assertRaises(ValueError): reference.validate_incident({"classification":"local_drill","events":estimated})
        with self.assertRaises(AssertionError): self.assertEqual(reference.validate_incident(INCIDENT)["cloud_incident_evidence"],"PASS")
        with self.assertRaises(NotImplementedError): learner.summarize_observations([])

    def test_feature_workflow_routes_each_contract_through_selected_learner_adapter(self):
        calls=[]
        class Tracking:
            def __getattr__(self,name):
                function=getattr(reference,name)
                def invoke(*args,**kwargs):
                    calls.append(name)
                    return function(*args,**kwargs)
                return invoke
        workflow.review_change(CHANGE,[{"request_id":"q","status":"success","elapsed_ms":4}],INCIDENT,
            {"status":"PASS","source_hash":"e"*64,"test_run_id":"local-run"},adapter=Tracking())
        self.assertEqual(calls,["validate_change_contract","summarize_observations","validate_incident","evaluate_change_gate"])

    def test_local_exercise_runs_red_green_migration_incident_and_retention(self):
        with tempfile.TemporaryDirectory() as td:
            out=workflow.run_local_exercise(td,adapter=reference)
            self.assertEqual(out["change"]["before"]["returncode"],1)
            self.assertEqual(out["change"]["after"]["returncode"],0)
            self.assertEqual(out["migration"]["status"],"PASS")
            self.assertEqual(out["rollback"]["status"],"PASS")
            self.assertEqual(out["incident"]["probe_returncodes"],[2,0])
            self.assertEqual([e["stage"] for e in out["incident"]["events"]],
                             ["detection","diagnosis","mitigation","recovery","prevention"])
            self.assertTrue(out["retention"]["physically_removed"])
            self.assertTrue(out["local_mechanics_pass"])
            self.assertEqual(out["live_cloud_status"],"BLOCKED")
            self.assertFalse(out["learner_completion"])

    def test_local_exercise_does_not_replace_unimplemented_learner_patch(self):
        with tempfile.TemporaryDirectory() as td:
            with self.assertRaises(NotImplementedError): workflow.run_local_exercise(td)

    def test_rollback_restores_all_varied_rows_and_schema_from_snapshot(self):
        with tempfile.TemporaryDirectory() as td:
            db_path=td+"/varied.sqlite3"
            db=sqlite3.connect(db_path)
            db.execute("CREATE TABLE docs(doc_id TEXT PRIMARY KEY, body TEXT NOT NULL)")
            db.executemany("INSERT INTO docs VALUES(?,?)",[("alpha","one"),("beta","two"),("fixture-doc","synthetic")])
            db.execute("CREATE INDEX docs_body_idx ON docs(body)")
            db.commit()
            db.close()
            migrated=reference.migrate_fixture_database(db_path)
            self.assertEqual(migrated["row_count"],3)
            db=sqlite3.connect(db_path)
            db.execute("INSERT INTO docs(doc_id,body) VALUES('post-migration','discard-on-rollback')")
            db.commit(); db.close()
            restored=reference.rollback_fixture_database(db_path)
            self.assertTrue(restored["data_preserved"])
            db=sqlite3.connect(db_path)
            self.assertEqual(db.execute("SELECT doc_id,body FROM docs ORDER BY doc_id").fetchall(),
                             [("alpha","one"),("beta","two"),("fixture-doc","synthetic")])
            self.assertIsNone(db.execute("SELECT 1 FROM sqlite_master WHERE name='retention_state'").fetchone())
            self.assertIsNotNone(db.execute("SELECT 1 FROM sqlite_master WHERE name='docs_body_idx'").fetchone())
            db.close()

    def test_incident_prevention_is_an_executed_guard_regression(self):
        with tempfile.TemporaryDirectory() as td:
            incident=reference.run_fixture_incident(td)
            self.assertEqual(incident["status"],"PASS")
            self.assertTrue(incident["diagnosis_observed"])
            self.assertTrue(incident["prevention_regression_executed"])
            self.assertTrue(incident["all_stages_observed"])
            self.assertEqual(incident["prevention_test_returncode"],0)
            self.assertTrue(all(len(event["evidence_ref"])==64 for event in incident["events"]))
            with self.assertRaises(PermissionError): reference.run_fixture_incident(td)

    def test_local_lab_rejects_nonempty_or_symlink_scope(self):
        with tempfile.TemporaryDirectory() as td, tempfile.TemporaryDirectory() as other:
            from pathlib import Path
            Path(td,"keep.txt").write_text("safe")
            with self.assertRaises(PermissionError): workflow.run_local_exercise(td,adapter=reference)
            link=Path(other,"alias")
            link.symlink_to(td,target_is_directory=True)
            with self.assertRaises(PermissionError): workflow.run_local_exercise(str(link),adapter=reference)

if __name__=='__main__': unittest.main()
