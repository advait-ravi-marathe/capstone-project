"""Candidate checks; intentionally fail while learner TODOs remain unsolved."""
import importlib,os,unittest
from ai.fde_v2.m43 import workflow
from ai.fde_v2.tests.test_m43 import row
C=importlib.import_module(os.environ.get('M43_CANDIDATE','ai.fde_v2.m43.learner'))
class M43LearnerAcceptance(unittest.TestCase):
 def test_two_originals_keep_ownership_and_unverified_handoff(self):
  result=workflow.assess_two_attempts([row('A'),row('B')],{'clean_clone':'not_run'},adapter=C)
  self.assertEqual(len(result['scores']),2)
  self.assertFalse(result['independent_reproduction_claim'])
  self.assertNotEqual(result['handoff']['peer_reproduction'],'PASS')
if __name__=='__main__': unittest.main()
