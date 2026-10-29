import unittest
import hashlib
from datetime import date,timedelta
from ai.fde_v2.m51 import learner,reference,workflow
def attempt(gate,n,date,score=3):
 content_hash=hashlib.sha256(f"exercise-content:{gate}:{n}".encode()).hexdigest()
 return {"gate":gate,"task_type":gate,"attempt_id":f"{gate}-{n}","actual_date":date,"task_content_sha256":content_hash,"task_hash":hashlib.sha256(f"{gate}:{content_hash}".encode()).hexdigest(),"unseen_confirmation":True,"tool_allowance":"docs only","help_used":"none","artifact_ref":f"local-{gate}-{n}","reviewer_type":"self","critical_failure":False,"scores":{k:score for k in ("correctness","reasoning","communication","safety_integrity")}}
class M51Test(unittest.TestCase):
 def test_two_separate_attempts_and_bounded_remediation(self):
  r=reference.score_gate([attempt("system_design",1,"2026-10-29"),attempt("system_design",2,"2026-10-30")])
  self.assertEqual(r["status"],"REHEARSAL_THRESHOLD_MET"); self.assertFalse(r["professional_tenure_inferred"])
  low=reference.score_gate([attempt("coding_dsa",1,"2026-10-31"),attempt("coding_dsa",2,"2026-11-01",2)])
  self.assertEqual(reference.remediation([low])["next_action"],"bounded_repair")
  rows=[{"actual_date":(date(2026,1,1)+timedelta(days=i)).isoformat(),"lane":"role_bank","kind":"formal_interview_assessment","minutes":30} for i in range(40)]+[{"actual_date":"2026-03-01","lane":"role_bank","kind":"AWS","minutes":30}]
  self.assertEqual(reference.validate_formal_assessment_ledger(rows)["non_dsa_formal_minutes"],1200)
  with self.assertRaises(ValueError): reference.validate_formal_assessment_ledger([{"actual_date":"2026-01-01","lane":"role_bank","kind":"formal_interview_assessment","minutes":60}])
 def test_same_day_seen_or_critical_failure_not_pass(self):
  with self.assertRaises(ValueError): reference.score_gate([attempt("coding_dsa",1,"2026-11-02"),attempt("coding_dsa",2,"2026-11-02")])
  with self.assertRaises(ValueError): reference.validate_attempt({**attempt("coding_dsa",1,"2026-11-03"),"unseen_confirmation":False})
  self.assertEqual(reference.score_gate([attempt("coding_dsa",1,"2026-11-03"),{**attempt("coding_dsa",2,"2026-11-04"),"critical_failure":True}])["status"],"REPAIR_REQUIRED")
  with self.assertRaises(NotImplementedError): learner.score_gate([])
  with self.assertRaises(NotImplementedError): learner.validate_formal_assessment_ledger([])
 def test_workflow_calls_learner_interface(self):
  calls=[]
  class Spy:
   def score_gate(self,a): calls.append("score_gate"); return reference.score_gate(a)
   def remediation(self,g): calls.append("remediation"); return reference.remediation(g)
   def validate_formal_assessment_ledger(self,e): calls.append("ledger"); return reference.validate_formal_assessment_ledger(e)
  workflow.assess({"system_design":[attempt("system_design",1,"2026-10-28"),attempt("system_design",2,"2026-10-29")]},adapter=Spy())
  self.assertEqual(calls,["score_gate","remediation","ledger"])
 def test_workflow_rejects_cross_gate_replay_relabel_and_duplicate_task(self):
  pair=[attempt("coding_dsa",1,"2026-10-28"),attempt("coding_dsa",2,"2026-10-29")]
  reused={gate:pair for gate in reference.GATES}
  with self.assertRaises(ValueError): workflow.assess(reused,adapter=reference)
  relabeled={"system_design":[{**a,"gate":"system_design","task_type":"system_design"} for a in pair]}
  with self.assertRaises(ValueError): workflow.assess(relabeled,adapter=reference)
  duplicate=[attempt("system_design",1,"2026-10-28"),attempt("system_design",2,"2026-10-29")]
  duplicate[1]["task_hash"]=duplicate[0]["task_hash"]
  with self.assertRaises(ValueError): workflow.assess({"system_design":duplicate},adapter=reference)
  malformed=[attempt("system_design",1,"2026-10-28"),attempt("system_design",2,"2026-10-29")]
  malformed[0]["task_hash"]="c"*63
  with self.assertRaises(ValueError): workflow.assess({"system_design":malformed},adapter=reference)
  # Relabeling identical source tasks and recomputing gate-bound digests must not
  # make one underlying exercise look like six unseen gate-specific tasks.
  relabeled={}
  for gate_index,gate in enumerate(sorted(reference.GATES)):
   rows=[]
   for n in (1,2):
    content_hash=hashlib.sha256(f"shared-original-exercise-{n}".encode()).hexdigest()
    row=attempt(gate,n,f"2026-11-{gate_index*2+n:02d}")
    row["task_content_sha256"]=content_hash
    row["task_hash"]=hashlib.sha256(f"{gate}:{content_hash}".encode()).hexdigest()
    rows.append(row)
   relabeled[gate]=rows
  with self.assertRaises(ValueError): workflow.assess(relabeled,adapter=reference)
 def test_workflow_rejects_adapter_that_inflates_scores_or_discards_attempt_outcomes(self):
  pair=[attempt("system_design",1,"2026-10-28",2),attempt("system_design",2,"2026-10-29",2)]
  class Inflated:
   @staticmethod
   def score_gate(rows):
    return {"gate":"system_design","attempts":[{"attempt_id":x["attempt_id"],"scores":x["scores"],
      "reviewer_type":x["reviewer_type"],"passed":True} for x in rows],
      "status":"REHEARSAL_THRESHOLD_MET"}
   remediation=staticmethod(reference.remediation)
   validate_formal_assessment_ledger=staticmethod(reference.validate_formal_assessment_ledger)
  with self.assertRaises(ValueError): workflow.assess({"system_design":pair},adapter=Inflated)
if __name__=='__main__': unittest.main()
