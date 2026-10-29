"""Contracts, exact daily budgets and visible evidence gates for conditional M4.3–M5.3."""
from __future__ import annotations
import json,re,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def load_schedule(key): return json.loads((ROOT/f'ai/fde_v2/{key}/draft_schedule.json').read_text())
class LaterMilestoneScheduleTest(unittest.TestCase):
 def test_day_index_is_complete_and_marks_missing_pages(self):
  index=(ROOT/'docs/fde-v2/days/README.md').read_text()
  rows=re.findall(r'^\| (\d+) \| (.*?) \|$',index,re.M)
  self.assertEqual([int(n) for n,_ in rows],list(range(1,271)))
  for n,label in rows:
   if '](' in label:
    target=label.split('](',1)[1].rstrip(')')
    self.assertTrue((ROOT/'docs/fde-v2/days'/target).is_file(),f'day {n} index link is broken')
   else:
    self.assertRegex(label,r'No per-day page|No daily page authored')
 def check(self,key,days,sub,buffer,visibility,external=0):
  d=load_schedule(key); rows=d['days']; self.assertEqual([r['day'] for r in rows],days)
  for r in rows:
   self.assertEqual((r['dsa_minutes'],r['role_bank_minutes'],r['ai_minutes']),(60,30,90))
   self.assertEqual(r['substantive_minutes']+r['buffer_minutes']+r['visibility_minutes']+r['unallocated_minutes']+r.get('external_milestone_minutes',0),90)
   page=ROOT/f'docs/fde-v2/days/day-{r["day"]}.md'
   self.assertTrue(page.is_file(),str(page)); self.assertIn('Status: Not started',page.read_text())
  self.assertEqual(sum(r['substantive_minutes'] for r in rows),sub)
  self.assertEqual(sum(r['buffer_minutes'] for r in rows),buffer)
  self.assertEqual(sum(r['visibility_minutes'] for r in rows),visibility)
  self.assertEqual(sum(r.get('external_milestone_minutes',0) for r in rows),external)
  self.assertEqual(d['status'],'conditional_design_draft_not_active')
  return d
 def test_four_exact_budget_schedules_and_boundary_shares(self):
  m43=self.check('m43',list(range(204,225)),1440,300,90,30)
  self.assertEqual((m43['expected_substantive_ai_minutes'],m43['listed_substantive_ai_minutes'],m43['substantive_schedule_delta_minutes']),(1440,1440,0))
  m51=self.check('m51',list(range(224,231)),300,240,0,90)
  self.assertEqual(m51['days'][0]['external_milestone_minutes'],60)
  self.assertEqual(m51['days'][-1]['external_milestone_minutes'],30)
  self.check('m52',list(range(230,251)),1500,240,90,60)
  self.check('m53',list(range(251,271)),1410,300,90)
 def test_accepted_contracts_remain_unchanged_and_guides_preserve_blockers(self):
  contracts=json.loads((ROOT/'docs/fde-v2/accepted/later-milestone-contracts.json').read_text())
  units={u['id']:u for u in contracts['units']}
  for key in ('M4.3','M5.1','M5.2','M5.3'):
   u=units['AIFDE-V2-'+key]
   self.assertEqual(u['learner_status'],'not_started')
   self.assertIsNone(u['executable_commands'])
   slug={'M4.3':'m43','M5.1':'m51','M5.2':'m52','M5.3':'m53'}[key]
   guide=(ROOT/f'docs/fde-v2/proposals/{slug}-conditional-guide.md').read_text().lower()
   self.assertIn('not started',guide)
   self.assertIn('not inferred',guide)
  self.assertIn('two unfamiliar-repository',units['AIFDE-V2-M4.3']['deliverables'][0].lower())
  self.assertIn('20 hours',units['AIFDE-V2-M5.1']['deliverables'][2].lower())
  self.assertIn('held-out results',units['AIFDE-V2-M5.2']['acceptance_criteria'][0].lower())
  self.assertIn('public posting',units['AIFDE-V2-M5.3']['acceptance_criteria'][2].lower())
 def test_cosine_is_manual_and_certification_or_user_outcomes_stay_blocked(self):
  text=(ROOT/'ai/fde_v2/m52/reference.py').read_text()
  self.assertIn('dot=sum(',text); self.assertIn('sqrt(',text); self.assertNotIn('cosine_similarity(',text.split('def cosine_similarity',1)[1].split('def popularity',1)[0].replace('def cosine_similarity',''))
  m53=(ROOT/'ai/fde_v2/m53/reference.py').read_text()
  self.assertIn('exam_booked":False',m53); self.assertIn('applications_sent":False',m53); self.assertIn('public_posting":False',m53)
  m52guide=(ROOT/'docs/fde-v2/proposals/m52-conditional-guide.md').read_text().lower()
  self.assertIn('dot product and vector norms in its own code',m52guide)
if __name__=='__main__': unittest.main()
