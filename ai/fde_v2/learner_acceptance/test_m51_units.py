"""Candidate checks; intentionally fail while learner TODOs remain unsolved."""
import importlib,os,unittest
from datetime import date,timedelta
candidate=importlib.import_module(os.environ.get('M51_CANDIDATE','ai.fde_v2.m51.learner'))
from ai.fde_v2.tests.test_m51 import attempt
class M51LearnerAcceptance(unittest.TestCase):
 def test_threshold_and_repair_are_implemented(self):
  report=candidate.score_gate([attempt('system_design',1,'2026-10-29'),attempt('system_design',2,'2026-10-30')])
  self.assertEqual(report['status'],'REHEARSAL_THRESHOLD_MET')
  self.assertFalse(report['professional_tenure_inferred'])
 def test_formal_assessment_excludes_aws(self):
  start=date(2026,1,1)
  rows=[{'actual_date':(start+timedelta(days=i)).isoformat(),'lane':'role_bank','kind':'formal_interview_assessment','minutes':30} for i in range(40)]+[{'actual_date':'2026-03-01','lane':'role_bank','kind':'AWS','minutes':30}]
  self.assertEqual(candidate.validate_formal_assessment_ledger(rows)['non_dsa_formal_minutes'],1200)
if __name__=='__main__': unittest.main()
