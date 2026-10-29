"""M3.3 acceptance selector. Use M33_CANDIDATE=reference for instructor mechanics."""
import importlib, os, tempfile, unittest
from pathlib import Path
from ai.fde_v2.m31 import reference as m31_reference
from ai.fde_v2.m33 import workflow
C=importlib.import_module(os.environ.get('M33_CANDIDATE','ai.fde_v2.m33.learner'))

class M33Acceptance(unittest.TestCase):
    def test_group_split_and_case_outcome_use_real_tool_trace(self):
        cases=[{"case_id":"d1","root_group":"d"},{"case_id":"h1","root_group":"h"}]
        self.assertEqual(C.split_by_group(cases,{"d"})["heldout_case_ids"],["h1"])
        with tempfile.TemporaryDirectory() as td:
            Path(td,"actual.txt").write_text("changed request contract")
            case={"case_id":"read","kind":"read_contains","expected_text":"contract",
                  "plan":{"steps":[{"tool":"read_file","arguments":{"path":"actual.txt"}}]}}
            row=workflow.run_task_suite(td,[case],tool_adapter=m31_reference,evaluator=C)[0]
            self.assertTrue(row["success"])

    def test_release_rejects_missing_rows_unsafe_action_and_unfrozen_version(self):
        ids=[f"h{i}" for i in range(12)]
        rows=[{"case_id":cid,"success":i<10,"authorization_safe":True} for i,cid in enumerate(ids)]
        cases=[{"case_id":f"d{i}","root_group":f"dev-{i}"} for i in range(12)]
        cases += [{"case_id":f"h{i}","root_group":f"held-{i}"} for i in range(12)]
        split=C.split_by_group(cases,{f"dev-{i}" for i in range(12)})
        base={"minimum_successes":10,"version_hash":"c"*64,"frozen":True,"manifest_case_count":24,
              "heldout_case_count":12,"split_receipt":split}
        self.assertEqual(C.evaluate_release(ids,rows,base)["status"],"BLOCKED")
        unsafe=[{**r,"authorization_safe":False} if r["case_id"]=="h0" else r for r in rows]
        self.assertEqual(C.evaluate_release(ids,unsafe,base)["status"],"FAIL")
        self.assertEqual(C.evaluate_release(ids[:11],rows[:11],base)["status"],"INVALID")

if __name__=='__main__': unittest.main()
