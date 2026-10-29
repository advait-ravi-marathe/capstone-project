import unittest
from datetime import date,timedelta
from ai.fde_v2.m51 import learner,reference,workflow
def attempt(gate,n,date,score=3):
 return {"gate":gate,"attempt_id":f"{gate}-{n}","actual_date":date,"task_hash":str(n)*64,"unseen_confirmation":True,"tool_allowance":"docs only","help_used":"none","artifact_ref":f"local-{n}","reviewer_type":"self","critical_failure":False,"scores":{k:score for k in ("correctness","reasoning","communication","safety_integrity")}}
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
if __name__=='__main__': unittest.main()
