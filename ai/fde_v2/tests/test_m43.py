import unittest
from ai.fde_v2.m43 import reference,workflow,learner
def row(which):
 return {"exercise_id":which,"source_license":"MIT fixture","initial_hash":"a"*64,"attempt_date":"2026-10-2"+("9" if which=="A" else "8"),"failure_receipt":"fail-log","root_cause":"adapter mismatch","patch_hash":"b"*64,"regression_receipt":"red-green","blast_radius":"one endpoint","assistance":"docs only","solution_seen_before_attempt":False,"assistant_authored_patch":False,"scores":{k:3 for k in reference.DIMENSIONS},"reviewer_type":"self","critical_failure":False}
class M43Test(unittest.TestCase):
 def test_two_independent_attempts_and_honest_self_handoff(self):
  out=workflow.assess_two_attempts([row("A"),row("B")],{"clean_clone":"self_run","run_receipt":{"command":"make test","empty_workspace_ref":"scratch-empty","environment_hash":"c"*64,"output_ref":"run-out"}},adapter=reference)
  self.assertEqual(out["status"],"EXERCISE_ONLY"); self.assertFalse(out["independent_reproduction_claim"])
  self.assertEqual(out["handoff"]["peer_reproduction"],"NOT_EVIDENCED")
 def test_seen_or_assistant_patch_or_critical_failure_rejected(self):
  for patch in ({"solution_seen_before_attempt":True},{"assistant_authored_patch":True}):
   with self.assertRaises(ValueError): reference.validate_attempt({**row("A"),**patch})
  with self.assertRaises(ValueError): reference.validate_attempt({k:v for k,v in row("A").items() if k!='assistant_authored_patch'})
  with self.assertRaises(ValueError): reference.validate_attempt({**row("A"),'attempt_date':'tomorrow'})
  with self.assertRaises(ValueError): reference.validate_attempt({**row("A"),'patch_hash':'not-a-hash'})
  self.assertEqual(reference.score_attempt({**row("A"),"critical_failure":True})["status"],"REPAIR_REQUIRED")
  with self.assertRaises(ValueError): workflow.assess_two_attempts([row("A"),row("A")],{},adapter=reference)
  with self.assertRaises(ValueError): reference.evaluate_handoff({'clean_clone':'peer_verified','run_receipt':{'command':'test','empty_workspace_ref':'w','environment_hash':'d'*64,'output_ref':'o'}})
  with self.assertRaises(NotImplementedError): learner.validate_attempt(row("A"))
 def test_feature_path_uses_selected_learner_adapter(self):
  seen=[]
  class Spy:
   def __getattr__(self,n):
    f=getattr(reference,n)
    def call(*a,**k): seen.append(n); return f(*a,**k)
    return call
  workflow.assess_two_attempts([row("A"),row("B")],{"clean_clone":"not_run"},adapter=Spy())
  self.assertEqual(seen,["validate_attempt","validate_attempt","score_attempt","score_attempt","evaluate_handoff"])
if __name__=='__main__': unittest.main()
