import unittest
import tempfile
from pathlib import Path
from ai.fde_v2.m43 import reference,workflow,learner
def row(which):
 return {"exercise_id":which,"source_license":"MIT fixture","initial_hash":("a" if which=="A" else "c")*64,"attempt_date":"2026-10-2"+("9" if which=="A" else "8"),"failure_receipt":"fail-log","root_cause":("adapter mismatch" if which=="A" else "incorrect boundary condition"),"patch_hash":("b" if which=="A" else "d")*64,"regression_receipt":"red-green","blast_radius":"one endpoint","assistance":"docs only","solution_seen_before_attempt":False,"assistant_authored_patch":False,"scores":{k:3 for k in reference.DIMENSIONS},"reviewer_type":"self","critical_failure":False}
class M43Test(unittest.TestCase):
 def test_real_fixture_factory_supplies_distinct_red_green_and_clean_copy_exercises(self):
  with tempfile.TemporaryDirectory() as td:
   out=workflow.run_instructor_fixture_suite(td)
   self.assertEqual(out["status"],"INSTRUCTOR_FIXTURES_VERIFIED")
   self.assertEqual(out["manifest"]["classification"],"instructor_fixture_only")
   self.assertNotEqual(out["manifest"]["exercises"]["A"]["initial_source_sha256"],
                       out["manifest"]["exercises"]["B"]["initial_source_sha256"])
   self.assertNotEqual(out["manifest"]["exercises"]["A"]["root_defect"],
                       out["manifest"]["exercises"]["B"]["root_defect"])
   for exercise in ("A","B"):
    fixture=out["controls"]["exercises"][exercise]
    self.assertNotEqual(fixture["red"]["returncode"],0)
    self.assertEqual(fixture["green_reference_copy"]["returncode"],0)
    self.assertTrue(fixture["learner_source_modified"] is False)
    self.assertNotEqual(out["clean_copy"][exercise]["returncode"],0)
    self.assertEqual(out["clean_copy"][exercise]["classification"],
                     "clean_copy_fixture_not_peer_handoff")
   worked=out["controls"]["worked_example"]
   self.assertTrue(worked["unrelated_to_exercises"])
   self.assertNotEqual(worked["red"]["returncode"],0)
   self.assertEqual(worked["green_reference_copy"]["returncode"],0)
   self.assertFalse(out["learner_completion"])
   self.assertEqual(list((Path(td)/"exercises").iterdir()).__len__(),2)
 def test_attempt_test_runner_observes_changed_source_but_does_not_make_attempt_claim(self):
  with tempfile.TemporaryDirectory() as td:
   manifest=__import__("ai.fde_v2.m43.exercises",fromlist=["materialize_exercises"]).materialize_exercises(td)
   exercise=Path(td)/"exercises"/"A"/"exercise.py"
   first=workflow.run_attempt_tests(td,"A",expected_initial_sha256=manifest["exercises"]["A"]["initial_source_sha256"])
   self.assertNotEqual(first["returncode"],0)
   exercise.write_text("def total(values):\n    return sum(values)\n")
   after=workflow.run_attempt_tests(td,"A",expected_initial_sha256=manifest["exercises"]["A"]["initial_source_sha256"])
   self.assertEqual(after["returncode"],0)
   self.assertTrue(after["source_changed_from_initial"])
   self.assertEqual(after["classification"],"attempt_test_receipt_requires_learner_provenance")
 def test_seen_or_assistant_patch_or_critical_failure_rejected(self):
  for patch in ({"solution_seen_before_attempt":True},{"assistant_authored_patch":True}):
   with self.assertRaises(ValueError): reference.validate_attempt({**row("A"),**patch})
  with self.assertRaises(ValueError): reference.validate_attempt({k:v for k,v in row("A").items() if k!='assistant_authored_patch'})
  with self.assertRaises(ValueError): reference.validate_attempt({**row("A"),'attempt_date':'tomorrow'})
  with self.assertRaises(ValueError): reference.validate_attempt({**row("A"),'patch_hash':'not-a-hash'})
  self.assertEqual(reference.score_attempt({**row("A"),"critical_failure":True})["status"],"REPAIR_REQUIRED")
  with self.assertRaises(ValueError): workflow.assess_two_attempts([row("A"),row("A")],{},adapter=reference)
  with self.assertRaises(ValueError):
   workflow.assess_two_attempts([row("A"),{**row("B"),"initial_hash":row("A")["initial_hash"],
      "patch_hash":row("A")["patch_hash"],"root_cause":row("A")["root_cause"]}],{},adapter=reference)
  with self.assertRaises(ValueError): reference.evaluate_handoff({'clean_clone':'peer_verified','run_receipt':{'command':'test','empty_workspace_ref':'w','environment_hash':'d'*64,'output_ref':'o'}})
  with self.assertRaises(NotImplementedError): learner.validate_attempt(row("A"))
 def test_default_assessment_plumbing_runs_without_app_code_while_attempts_stay_unverified(self):
  out=workflow.assess_two_attempts([row("A"),row("B")],{"clean_clone":"not_run"})
  self.assertEqual(out["status"],"EXERCISE_ONLY")
  self.assertFalse(out["independent_reproduction_claim"])
  with self.assertRaises(NotImplementedError):
   workflow.assess_two_attempts([row("A"),row("B")],{"clean_clone":"not_run"},adapter=learner)
if __name__=='__main__': unittest.main()
