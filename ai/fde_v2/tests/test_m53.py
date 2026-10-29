import unittest
from ai.fde_v2.m53 import learner,reference,workflow
EV={"test":{"sha256":"a"*64,"actual_contributor":"learner","environment":"local","status":"verified","source_kind":"attempt","level":"local"}}
class M53Test(unittest.TestCase):
 def test_claim_requires_hashed_actual_evidence_and_explicit_level(self):
  good=[{"claim":"local retrieval regression fixed","level":"local","evidence_refs":["test"]}]
  self.assertEqual(reference.validate_claims(good,EV)['status'],'SCHEMA_COMPLETE_UNVERIFIED')
  self.assertEqual(reference.validate_claims([{**good[0],"evidence_refs":[]}],EV)['status'],'INCOMPLETE')
  self.assertEqual(reference.validate_claims([{**good[0],"evidence_refs":["missing"]}],EV)['status'],'INCOMPLETE')
  with self.assertRaises(ValueError): reference.validate_claims([{**good[0],"level":"credential"}],EV)
 def test_role_requirements_are_sourced_and_no_application_sent(self):
  role={"role_id":"sample","official_source":"source-ref","observed_date":"2026-10-29","requirements":[{"requirement":"Python","category":"technical","evidence_ref":"test"},{"requirement":"years","category":"experience"},{"requirement":"region","category":"location"},{"requirement":"authorization","category":"work_authorization"},{"requirement":"travel","category":"travel"},{"requirement":"customer production","category":"customer_production","evidence_ref":"missing"}]}
  r=reference.audit_role_fit([role],EV)
  self.assertEqual(r['roles'][0]['supported_requirements'],['Python']); self.assertEqual(r['roles'][0]['gaps_or_unknowns'],['years','region','authorization','travel','customer production']); self.assertFalse(r['applications_sent'])
  with self.assertRaises(ValueError): reference.audit_role_fit([{**role,"official_source":""}],EV)
 def test_cert_and_readiness_never_claim_pass_from_scaffold(self):
  c=reference.decide_certification({"decision":"defer","evidence_refs":[]})
  self.assertFalse(c['exam_booked']); self.assertFalse(c['payment_authorized'])
  r=reference.build_partial_readiness({"status":"INCOMPLETE"},{"status":"EVIDENCE_MAP_ONLY"},{"model":"BLOCKED","cloud":"NOT_RUN"})
  self.assertEqual(r['status'],'PARTIAL_READINESS_GAPS_REMAIN'); self.assertFalse(r['universal_fde_ready_claim'])
  self.assertEqual(workflow.review([],{},[],{"decision":"defer"},{"model":"BLOCKED"},adapter=reference)['external_actions'],False)
  with self.assertRaises(NotImplementedError): learner.validate_claims([],{})
if __name__=='__main__': unittest.main()
