"""Versioned ML-depth schedule, source-immutability and navigation checks."""
from __future__ import annotations
import hashlib
import json
import re
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]


class ProductionMLDepthScheduleTest(unittest.TestCase):
    def setUp(self):
        self.schedule=json.loads((ROOT/'ai/fde_v2/ml_depth/draft_schedule.json').read_text())

    def test_versioned_schedule_covers_112_days_with_exact_daily_budget(self):
        rows=self.schedule['days']
        self.assertEqual([row['day'] for row in rows],list(range(271,383)))
        self.assertEqual((self.schedule['focused_sessions'],self.schedule['buffer_sessions']),(96,16))
        self.assertEqual((sum(row['substantive_minutes'] for row in rows),sum(row['buffer_minutes'] for row in rows)),(8640,1440))
        for row in rows:
            self.assertEqual((row['dsa_minutes'],row['role_bank_minutes'],row['ai_minutes']),(60,30,90))
            self.assertEqual(row['substantive_minutes']+row['buffer_minutes'],90)
            self.assertEqual(row['status'],'not_started')
            self.assertTrue((ROOT/f"docs/fde-v2/days/day-{row['day']}.md").is_file())
        for block,start in zip(('PML1','PML2','PML3','PML4'),(271,299,327,355)):
            selected=[row for row in rows if row['block']==block]
            self.assertEqual([row['day'] for row in selected],list(range(start,start+28)))
            self.assertEqual(sum(row['day_kind']=='focused' for row in selected),24)
            self.assertEqual(sum(row['day_kind']=='buffer' for row in selected),4)

    def test_accepted_original_sources_and_core_remain_unchanged(self):
        expected={
          'docs/fde-v2/accepted/later-milestone-contracts.json':'97e5aba0f3ff30c46bcb5de3c654660fa85c8510359752fcf1f7d9c2a6bb7cd0',
          'docs/fde-v2/accepted/repaired-roadmap-architecture.md':'68d99d68de68acbd4c6957f2c9deefa601442aa29cbabfc52101711134177649'}
        for path,digest in expected.items(): self.assertEqual(hashlib.sha256((ROOT/path).read_bytes()).hexdigest(),digest)
        self.assertEqual(self.schedule['base_core_days'],[1,270])
        self.assertEqual(self.schedule['recommended_dual_skill_route_days'],[1,382])
        contracts=json.loads((ROOT/'docs/fde-v2/accepted/later-milestone-contracts.json').read_text())
        self.assertEqual(len(contracts['units']),12)

    def test_all_extension_links_work_and_evidence_is_not_promoted(self):
        index=ROOT/'docs/fde-v2/ml-depth/README.md'
        rows=re.findall(r'^\| (\d+) \| .*? \| \[Day \d+\]\((.*?)\) \|$',index.read_text(),re.M)
        self.assertEqual([int(day) for day,_ in rows],list(range(271,383)))
        for _,target in rows: self.assertTrue((index.parent/target).resolve().is_file(),target)
        amendment=(ROOT/'docs/fde-v2/amendments/2026-10-29-production-ml-depth-amendment.md').read_text()
        for required in ('382','PyTorch','boosted-tree','negative sampling','sample-ratio mismatch','hiring readiness',
                         'OpenAI','Stripe','four focused blocks'):
            # The phrase check intentionally ignores case; it protects route coverage and honest gates.
            self.assertIn(required.lower(),amendment.lower())
        self.assertIn('Status: Not started',(ROOT/'docs/fde-v2/days/day-271.md').read_text())
        self.assertIn('not_started',self.schedule['days'][0]['status'])

    def test_learner_workflows_fail_at_todos_without_reference_fallback(self):
        from ai.fde_v2.ml_depth import learner,workflow
        with self.assertRaises(NotImplementedError):
            workflow.compare_classical([[-1.0],[1.0]],[0,1],[[-.5],[.5]],[0,1],adapter=learner)
        self.assertIn('raise NotImplementedError',(ROOT/'ai/fde_v2/ml_depth/learner.py').read_text())

    def test_recommended_route_applies_no_application_code_overlay(self):
        overlay=ROOT/'docs/fde-v2/amendments/2026-10-29-application-code-responsibility-overlay.md'
        self.assertTrue(overlay.is_file())
        policy=overlay.read_text().lower()
        for phrase in ('does **not** write, edit, or implement those capstone application components',
                       'assistant supplies and maintains', 'day22', '`explain query plan`',
                       'do not demonstrate independent capstone application delivery', 'model/loss/optimizer/training code',
                       'existing 30-minute role-bank rotation', 'short interview-focused fe/be coding/debugging drills'):
            self.assertIn(phrase,policy)
        for page in (ROOT/'docs/fde-v2/README.md',ROOT/'docs/fde-v2/days/README.md',
                     ROOT/'docs/fde-v2/ml-depth/README.md'):
            self.assertIn('application-code-responsibility-overlay.md',page.read_text())
        for day in range(271,383):
            card=(ROOT/f'docs/fde-v2/days/day-{day}.md').read_text().lower()
            self.assertIn('application-code responsibility overlay',card)
            self.assertIn('14-day rotation',card)
        day22=(ROOT/'docs/fde-v2/days/day-22.md').read_text().lower()
        self.assertIn('js/ts implementation interview practice',day22)
        self.assertIn('overlay',policy)
        role_bank=(ROOT/'docs/fde-v2/role-bank.md').read_text().lower()
        self.assertIn('existing 30-minute bank',role_bank)
        self.assertIn('fde_v2_query_plan_lab.py',policy)
        north_star=(ROOT/'docs/fde-v2/amendments/2026-10-29-production-ml-depth-amendment.md').read_text().lower()
        for objective in ('customer workflow','diagnose','customer outcomes','passed fde release gate from synthetic data'):
            self.assertIn(objective,north_star)
        for row in self.schedule['days']:
            task=row['task'].lower()
            for forbidden in ('write an api','implement an api','build a frontend','implement a backend',
                              'write a route','implement a route','write sql','implement a migration',
                              'implement deployment','write a component'):
                self.assertNotIn(forbidden,task)

    def test_historical_application_todo_does_not_block_ml_reference_workflow(self):
        from ai.fde_v2.m22 import learner as application_auth_learner
        from ai.fde_v2.ml_depth import reference,workflow
        with self.assertRaises(NotImplementedError):
            application_auth_learner.verify_identity('token',None,None,
                expected_issuer='issuer',expected_audience='audience',now=0)
        result=workflow.compare_classical([[-2.0],[-1.0],[1.0],[2.0]],[0,0,1,1],
            [[-1.5],[1.5]],[0,1],adapter=reference,epochs=20)
        self.assertEqual(result['fit_partition'],'train')
        self.assertEqual(result['adapter_module'],'ai.fde_v2.ml_depth.reference')

    def test_role_rotation_fits_existing_bank_and_targets_fde_north_star(self):
        rotation=json.loads((ROOT/'docs/fde-v2/role-bank-rotation.json').read_text())
        self.assertEqual(rotation['cycle_days'],14)
        self.assertEqual(rotation['minutes_per_slot'],30)
        self.assertEqual(rotation['daily_minutes_unchanged'],180)
        slots=rotation['slots']
        self.assertEqual([slot['day_in_cycle'] for slot in slots],list(range(1,15)))
        from collections import Counter
        counts=Counter(slot['focus'] for slot in slots)
        self.assertEqual(counts,{
            'backend_coding_debugging':4,'frontend_coding_debugging':3,'aws':3,
            'system_design_customer_sa':2,'behavioral_customer_story':1,'integrated_mini_mock':1})
        self.assertEqual(rotation['coding_drill_minutes'],{
            'understand_prompt':3,'solve':17,'run_tests_review':7,'feedback_log':3})
        self.assertFalse(rotation['capstone_code_edit_allowed'])
        self.assertEqual(sum(rotation['coding_drill_minutes'].values()),30)
        self.assertEqual(rotation['baseline_aws_days_1_30'],[5,6,12,13,19,20,26,27])


if __name__=='__main__': unittest.main()
