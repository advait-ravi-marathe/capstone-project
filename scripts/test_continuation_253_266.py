"""Instructor-only manifest checks and independent acceptance witness for Days 253–266."""
from __future__ import annotations
import importlib.util,json,subprocess,sys,unittest
from dataclasses import replace
from datetime import date,timedelta
from pathlib import Path
from unittest.mock import patch
ROOT=Path(__file__).resolve().parents[1]; OUT=ROOT/'docs/reboot/continuation-253-266'; GEN=ROOT/'scripts/generate_continuation_253_266.py'
EXPECTED={'project_implementation_tests':600,'ai_workflow_implementation_evaluation':900,'certification_labs_questions':110,'ai_concept_source_study':210,'certification_domain_study':150,'core_systems_security_cloud_study':130,'evidence':210}
def load(name,path):
 spec=importlib.util.spec_from_file_location(name,path); m=importlib.util.module_from_spec(spec);sys.modules[name]=m;spec.loader.exec_module(m);return m
acceptance=load('enterprise_acceptance',ROOT/'ai/grounded/learner_acceptance/test_enterprise_contract_253.py'); c=acceptance.contract

def witness(a,d,r,*,now,skip_schema=False,skip_verified=False,skip_issuer=False,skip_audience=False,skip_time=False,skip_tenant=False,skip_scope=False,skip_group=False,skip_active=False,skip_revision=False,skip_action=False,skip_id=False,leak_denials=False,adapter_open=False,wrong_excerpt=False,ignore_max_ttl=False,always_allow=False):
 def deny(reason,doc=d):
  if leak_denials:return c.Decision(False,reason,doc.document_id,'secret-source@secret-rev',doc.excerpt)
  return c.Decision(False,reason)
 if always_allow:return c.Decision(True,'allowed',d.document_id,f'{d.document_id}@{d.revision}',d.excerpt)
 if a.adapter_status=='unavailable':
  if adapter_open:return c.Decision(True,'allowed',d.document_id,f'{d.document_id}@{d.revision}',d.excerpt)
  return deny('adapter_unavailable')
 if ((not a.adapter_verified or a.adapter_status!='ok') and not skip_verified) or (a.issuer!=c.EXPECTED_ISSUER and not skip_issuer):return deny('identity_untrusted')
 shape=(isinstance(a.subject,str) and bool(a.subject.strip()) and isinstance(a.groups,tuple) and all(isinstance(x,str) for x in a.groups) and isinstance(a.scopes,tuple) and all(isinstance(x,str) for x in a.scopes) and isinstance(a.issued_at,int) and not isinstance(a.issued_at,bool) and isinstance(a.expires_at,int) and not isinstance(a.expires_at,bool))
 if not shape and not skip_schema:return deny('claim_invalid')
 if (a.audience!=c.EXPECTED_AUDIENCE and not skip_audience) or (not skip_time and (a.issued_at>now or now>=a.expires_at or a.expires_at<=a.issued_at or (not ignore_max_ttl and a.expires_at-a.issued_at>c.MAX_ASSERTION_SECONDS))):return deny('claim_invalid')
 if (a.tenant!=c.EXPECTED_TENANT or d.tenant!=a.tenant) and not skip_tenant:return deny('tenant_mismatch')
 if r.action!='document.read' and not skip_action:return deny('action_denied')
 if r.document_id!=d.document_id and not skip_id:return deny('document_not_found')
 if not d.active and not skip_active:return deny('document_inactive')
 if r.revision!=d.revision and not skip_revision:return deny('revision_mismatch')
 if c.REQUIRED_SCOPE not in a.scopes and not skip_scope:return deny('scope_denied')
 if not set(a.groups).intersection(d.allowed_groups) and not skip_group:return deny('permission_denied')
 return c.Decision(True,'allowed',d.document_id,f'{d.document_id}@{d.revision}',('wrong synthetic excerpt' if wrong_excerpt else d.excerpt))

def run_with(method,mut=None):
 fn=lambda *args,now,**kwargs:witness(*args,now=now,**(mut or {}))
 with patch.object(c,'evaluate_access',side_effect=fn): method()

class ManifestTests(unittest.TestCase):
 def test_exports_and_all_daily_contracts_are_synchronized(self):
  names=['manifest.json','days-253-266-transfer-plan.json','scrum-owner-manifest.json','verified-sources.json']
  before={n:(OUT/n).read_bytes() for n in names};subprocess.run([sys.executable,str(GEN)],cwd=ROOT,check=True,capture_output=True)
  self.assertEqual(before,{n:(OUT/n).read_bytes() for n in names})
  src=json.loads((OUT/'plan-source.json').read_text()); man=json.loads((OUT/'manifest.json').read_text());own=json.loads((OUT/'scrum-owner-manifest.json').read_text());tr=json.loads((OUT/'days-253-266-transfer-plan.json').read_text())
  self.assertEqual(list(range(253,267)),[x['day'] for x in man['days']]);self.assertEqual(EXPECTED,man['engineering_allocation']);self.assertEqual(EXPECTED,tr['budget']['window_workstreams']);self.assertFalse(own['board_writes']);self.assertEqual(14,own['story_count'])
  for i,(task,row,card) in enumerate(zip(src['tasks'],man['days'],own['cards'])):
   self.assertEqual((date(2027,7,3)+timedelta(days=i)).isoformat(),row['date']);self.assertEqual(task['date'],row['date']) if 'date' in task else None
   self.assertEqual(165,sum(row['workstream_minutes'].values()));self.assertEqual(45,row['shared_interview_block']['minutes']);self.assertEqual('unobserved_not_implied_by_scaffold',row['learner_status']);self.assertFalse(row['engineering_architecture']['capstone_proposal_required']);self.assertEqual('separate_interview_question_bank_only',row['engineering_architecture']['system_design_scope'])
   self.assertEqual(set(EXPECTED),set(row['workstream_minutes']));self.assertEqual(row['workstream_minutes'],card['engineering']['allocation'])
   contract=(OUT/f'contracts/day-{row["day"]}.md').read_text();self.assertIn(f'id="day-{row["day"]}"',contract);self.assertIn('Assistant-supplied architecture',contract)
   for target in __import__('re').findall(r'\]\(([^)]+)\)',contract):
    if target.startswith(('http://','https://')):continue
    dest,_,anchor=target.partition('#');path=(OUT/'contracts'/dest).resolve();self.assertTrue(path.exists(),target)
    if anchor:self.assertIn(f'id="{anchor}"',path.read_text())
  self.assertIn('DVA-C02',(OUT/'README.md').read_text());self.assertIn('Recheck', (OUT/'README.md').read_text())

class AcceptanceWitnessTests(unittest.TestCase):
 def test_all_daily_acceptance_selectors_run_against_test_only_reference(self):
  for d,tail in [(253,'schema'),(254,'trusted_adapter'),(255,'claim_window'),(256,'tenant_isolation'),(257,'permission_intersection'),(258,'document_lifecycle'),(259,'safe_denials'),(260,'bounded_action'),(261,'adapter_failure'),(262,'exact_render'),(263,'time_boundaries'),(264,'independent_matrix'),(265,'request_isolation'),(266,'reproducible_handoff')]:
   method=getattr(acceptance.EnterpriseContractAcceptance(f'test_day{d}_{tail}'),f'test_day{d}_{tail}')
   with self.subTest(day=d):run_with(method)
 def test_actual_paths_reject_security_and_evaluation_mutations(self):
  cases=[(253,'schema',{'skip_schema':True}),(254,'trusted_adapter',{'skip_verified':True,'skip_issuer':True}),
   (255,'claim_window',{'skip_audience':True,'skip_time':True}),(256,'tenant_isolation',{'skip_tenant':True}),
   (257,'permission_intersection',{'skip_scope':True,'skip_group':True}),(258,'document_lifecycle',{'skip_active':True,'skip_revision':True}),
   (259,'safe_denials',{'leak_denials':True}),(260,'bounded_action',{'skip_action':True,'skip_id':True}),
   (261,'adapter_failure',{'adapter_open':True}),(262,'exact_render',{'wrong_excerpt':True}),
   (263,'time_boundaries',{'ignore_max_ttl':True}),(264,'independent_matrix',{'always_allow':True}),
   (265,'request_isolation',{'skip_tenant':True}),(266,'reproducible_handoff',{'always_allow':True,'leak_denials':True})]
  for d,tail,mut in cases:
   method=getattr(acceptance.EnterpriseContractAcceptance(f'test_day{d}_{tail}'),f'test_day{d}_{tail}')
   with self.subTest(day=d),self.assertRaises(AssertionError):run_with(method,mut)

if __name__=='__main__':unittest.main()
