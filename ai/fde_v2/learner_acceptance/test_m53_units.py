"""Candidate checks; learner claims must remain evidence-linked and action-free."""
import importlib,os,unittest
from ai.fde_v2.tests.test_m53 import EV
candidate=importlib.import_module(os.environ.get('M53_CANDIDATE','ai.fde_v2.m53.learner'))
class M53LearnerAcceptance(unittest.TestCase):
 def test_claim_and_certification_seams_keep_unknowns(self):
  claims=[{'claim':'local test fixed','level':'local','evidence_refs':['test']}]
  report=candidate.validate_claims(claims,EV)
  self.assertFalse(report['professional_tenure_inferred'])
  cert=candidate.decide_certification({'decision':'defer','evidence_refs':[]})
  self.assertFalse(cert['exam_booked']); self.assertFalse(cert['payment_authorized'])
if __name__=='__main__': unittest.main()
