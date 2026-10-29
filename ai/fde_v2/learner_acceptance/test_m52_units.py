"""Candidate checks; require learner-written vector math and ranker path."""
import importlib,os,unittest
from ai.fde_v2.m52 import workflow
from ai.fde_v2.tests.test_m52 import EVENTS
candidate=importlib.import_module(os.environ.get('M52_CANDIDATE','ai.fde_v2.m52.learner'))
class M52LearnerAcceptance(unittest.TestCase):
 def test_cosine_math_and_served_ranker_are_integrated(self):
  self.assertAlmostEqual(candidate.cosine_similarity([1,0],[1,1]),2**-.5)
  output=workflow.predict(EVENTS,'u0',['i1','i2','i9'],adapter=candidate)
  self.assertTrue(output['ranked'])
  self.assertFalse(output['online_uplift_claim'])
 def test_learner_ndcg_seam(self):
  self.assertAlmostEqual(candidate.ndcg_at_k([3,2,1],3),1.0)
  # A relevant item ranked second has DCG 1/log2(3), while ideal DCG is 1.
  # This independently scored non-perfect case rejects a constant-one mutant.
  expected=1.0/1.584962500721156
  observed=candidate.ndcg_at_k([0,1],2)
  self.assertAlmostEqual(observed,expected,places=10)
  self.assertNotAlmostEqual(observed,1.0,places=6)
if __name__=='__main__': unittest.main()
