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
                         'OpenAI','Stripe','four focused blocks','October 2029','Now–April 2027',
                         'May–October 2027','November 2027–October 2028','November 2028–October 2029',
                         'not promised promotions','rather than inserting sessions into its accepted milestone calendar',
                         'substantive production-ML training begins only after the learner reaches the core handoff'):
            # The phrase check intentionally ignores case; it protects route coverage and honest gates.
            self.assertIn(required.lower(),amendment.lower())
        # All four extension blocks contribute only to the accepted model/data-depth KR.
        for block in ('PML1','PML2','PML3','PML4'):
            self.assertIn(f"| {block} ",amendment)
        for row in self.schedule['days']:
            self.assertEqual(row['objective_links'],[{'objective':'O4','key_result':'KR-4.2'}])
        self.assertIn('Status: Not started',(ROOT/'docs/fde-v2/days/day-271.md').read_text())
        self.assertIn('not_started',self.schedule['days'][0]['status'])

    def test_representative_cards_bind_resource_task_interface_and_acceptance(self):
        expected={
            283:('CS229 logistic regression section','ai/fde_v2/ml_depth/learner.py','logistic_loss and train_logistic','StatisticsAndClassicalMLTests'),
            305:('Optimizing Model Parameters','ai/fde_v2/ml_depth/learner.py','train_torch_model','RealTorchTrainingTests'),
            331:('Google Recommendation Systems: collaborative filtering summary','ai/fde_v2/ml_depth/learner.py','sample_negative_pairs','LearnedRankingTests'),
            358:('Microsoft Research online experiments: validity checks','ai/fde_v2/ml_depth/learner.py','srm_check','ExperimentAndLifecycleTests'),
        }
        for day,(resource,module,functions,selector) in expected.items():
            page=(ROOT/f'docs/fde-v2/days/day-{day}.md').read_text()
            self.assertIn(resource,page)
            self.assertIn(f'`{module}::{functions}`',page)
            row=next(value for value in self.schedule['days'] if value['day']==day)
            self.assertIn(row['acceptance_selector'],page)
            self.assertIn(row['acceptance_command'],page)
            self.assertIn('python3 -m ai.fde_v2.ml_depth.examples',page)
        schedule=self.schedule['days']
        for block_start in (271,299,327,355):
            first=next(row for row in schedule if row['day']==block_start)
            self.assertEqual(first['minutes'],{'reading':15,'worked_example':10,'learner_build_test':55,'evidence_log':10})
        focused={row['day']:row for row in schedule if row['day_kind']=='focused'}
        for day,function,task in ((282,'regression_loss_profile','squared, absolute and Huber'),
                                  (300,'make_torch_loaders','actual PyTorch Dataset/DataLoader'),
                                  (339,'cold_start_fallback','popularity fallback'),
                                  (374,'reconcile_delayed_labels','event time')):
            row=focused[day]; page=(ROOT/f'docs/fde-v2/days/day-{day}.md').read_text()
            self.assertEqual(row['learner_interface']['functions'],function)
            self.assertIn('learner_acceptance.MLDepthLearnerAcceptance',row['acceptance_selector'])
            self.assertIn('ML_DEPTH_CANDIDATE=ai.fde_v2.ml_depth.learner',row['acceptance_command'])
            self.assertIn(task.lower(),row['task'].lower())
            self.assertIn(row['acceptance_command'],page)
        self.assertIn('Datasets & DataLoaders (`Dataset` and `DataLoader` sections)',focused[300]['reading'])
        self.assertIn('Feast documentation',focused[374]['reading'])

    def test_all_96_focused_sessions_have_complete_validated_matrix(self):
        governance=json.loads((ROOT/'docs/fde-v2/governance/north-star-and-okrs.json').read_text())
        valid={(objective['id'],kr['id']) for objective in governance['objectives']
               for kr in objective['key_results']}
        focused=[row for row in self.schedule['days'] if row['day_kind']=='focused']
        self.assertEqual(len(focused),96)
        self.assertEqual(len({row['title'] for row in focused}),96)
        self.assertEqual(len({row['task'] for row in focused}),96)
        self.assertEqual(len({row['learner_artifact'] for row in focused}),96)
        for row in focused:
            with self.subTest(day=row['day']):
                self.assertTrue(row['reading'].strip())
                self.assertTrue(row['task'].strip())
                self.assertTrue(row['prerequisite'].strip())
                self.assertTrue(row['learner_artifact'].startswith('learning-artifacts/production-ml/'))
                self.assertTrue(row['learner_interface']['functions'].strip())
                if row['learner_interface']['path'].endswith('attempt-record.schema.json'):
                    self.assertTrue((ROOT/row['learner_interface']['path']).is_file())
                self.assertTrue(row['acceptance_selector'].startswith(('ai.fde_v2.ml_depth.test_ml_depth.',
                                                                       'ai.fde_v2.ml_depth.learner_acceptance.',
                                                                       'manual-evidence-review/')))
                rubric=row['evidence_rubric']
                expected_primary=(row['code_session_detail'][1] if row.get('code_session_detail') else row['task'])
                self.assertEqual(rubric['primary_check'],expected_primary)
                self.assertTrue(rubric['evidence_check'].strip())
                self.assertTrue(rubric['reasoning_check'].strip())
                self.assertEqual(rubric['review_type'],'learner self-check with assistant feedback; not independent assessment')
                for link in row['objective_links']:
                    self.assertIn((link['objective'],link['key_result']),valid)
                page=(ROOT/f"docs/fde-v2/days/day-{row['day']}.md").read_text()
                for token in (row['task'],row['prerequisite'],row['learner_artifact'],row['acceptance_selector']):
                    self.assertIn(token,page)
                self.assertIn(row['evidence_rubric']['primary_check'],page)
                self.assertIn(row['evidence_rubric']['reasoning_check'],page)
                if row.get('code_session_detail'):
                    self.assertIn('**Code check:**',page)
                    self.assertIn(row['code_session_detail'][1],page)
                    self.assertNotEqual(rubric['primary_check'],row['task'])
                    if row['learner_interface']['path'].startswith('ai/'):
                        self.assertTrue((ROOT/row['learner_interface']['path']).is_file(),row['learner_interface']['path'])
                    for selector in re.findall(r'`(test_[A-Za-z0-9_]+)`',row['code_session_detail'][1]):
                        self.assertIn(f'def {selector}(',
                                      (ROOT/'ai/fde_v2/ml_depth/learner_acceptance.py').read_text())
                if row['acceptance_selector'].startswith('manual-evidence-review/'):
                    self.assertIn('runs instructor mechanics only',page)
                    self.assertIn('manual, task-specific evidence rubric',page)
                    self.assertIn(f"`{row['learner_interface']['path']}::{row['learner_interface']['functions']}`",page)
                    if row['learner_interface']['path']=='learning-artifact':
                        self.assertIn(f"day {row['day']} evidence reviewed against its specific rubric",
                                      row['learner_interface']['functions'])
                else:
                    self.assertIn('learner_acceptance.MLDepthLearnerAcceptance',row['acceptance_selector'])
                if 'M5.2 accepted guide' in row['reading']:
                    self.assertIn('../proposals/m52-conditional-guide.md',page)
                else:
                    for resource in row['reading'].split('; '):
                        visible=resource
                        if resource.startswith('PyTorch'):
                            visible=resource.removeprefix('PyTorch ').split(':',1)[-1].strip().split(' with ',1)[0].split(' (`',1)[0]
                        self.assertIn(visible,page)
        for row in self.schedule['days']:
            for link in row['objective_links']:
                self.assertIn((link['objective'],link['key_result']),valid)

    def test_core_protected_retrieval_works_with_supplied_plumbing_without_learner_code(self):
        from ai.fde_v2.tests.test_m22_integration import M22LocalIntegration
        case=M22LocalIntegration(methodName='test_default_application_authenticates_signed_fixture_and_no_learner_code_is_required')
        case.setUp()
        try:
            case.test_default_application_authenticates_signed_fixture_and_no_learner_code_is_required()
            case.test_failed_physical_purge_keeps_tombstone_and_denies_stale_qdrant_hits()
        finally:
            case.tearDown()

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
        from ai.fde_v2.tests.test_m22_integration import M22LocalIntegration
        with self.assertRaises(NotImplementedError):
            application_auth_learner.verify_identity('token',None,None,
                expected_issuer='issuer',expected_audience='audience',now=0)
        case=M22LocalIntegration(methodName='test_default_application_authenticates_signed_fixture_and_no_learner_code_is_required')
        case.setUp()
        try:
            case.test_default_application_authenticates_signed_fixture_and_no_learner_code_is_required()
        finally:
            case.tearDown()

    def test_assistant_plumbing_inventory_covers_every_active_workflow(self):
        inventory=json.loads((ROOT/'docs/fde-v2/application-plumbing-inventory.json').read_text())
        entries=inventory['entries']
        self.assertEqual(len(entries),14)
        self.assertEqual(len({entry['scope'] for entry in entries}),14)
        for entry in entries:
            self.assertTrue((ROOT/entry['workflow']).is_file(),entry['workflow'])
            self.assertTrue((ROOT/entry['support']).is_file(),entry['support'])
            for test in entry['tests']:
                test_path=test if '/' in test else test.replace('.', '/')+'.py'
                self.assertTrue((ROOT/test_path).is_file(),test)
            self.assertIn('learner_owned',entry)
        self.assertIn('No required ML task depends',inventory['invariant'])
        policy=(ROOT/'docs/fde-v2/amendments/2026-10-29-application-code-responsibility-overlay.md').read_text()
        self.assertIn('application-plumbing-inventory.json',policy)
        active_guides=(ROOT/'docs/fde-v2/proposals/m21-detailed-days-42-62.md',
                       ROOT/'docs/fde-v2/proposals/m22-detailed-days-62-82.md',
                       ROOT/'docs/fde-v2/proposals/m23-m32-conditional-guide.md',
                       ROOT/'docs/fde-v2/proposals/m33-m42-conditional-guide.md')
        for guide in active_guides:
            text=guide.read_text().lower()
            self.assertNotIn('default application adapter remains the learner todo',text)
            self.assertNotIn('ordinary server defaults to the unsolved learner adapters',text)
        self.assertIn('default application workflow is runnable without learner fe/be code',
                      (ROOT/'docs/fde-v2/proposals/m23-m32-conditional-guide.md').read_text().lower())

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
            'backend_coding_debugging':2,'backend_sql_query_plan':1,'backend_concurrency_debugging':1,
            'frontend_coding_debugging':2,'frontend_render_performance':1,'aws':3,
            'system_design_customer_sa':2,'behavioral_customer_story':1,'integrated_mini_mock':1})
        self.assertEqual(rotation['coding_drill_minutes'],{
            'understand_prompt':3,'solve':17,'run_tests_review':7,'feedback_log':3})
        self.assertFalse(rotation['capstone_code_edit_allowed'])
        self.assertIn('40-row React list',slots[8]['exercise'])
        self.assertEqual(sum(rotation['coding_drill_minutes'].values()),30)
        self.assertEqual(rotation['baseline_aws_days_1_30'],[5,6,12,13,19,20,26,27])

    def test_role_bank_has_concrete_artifacts_tests_and_honest_rubric(self):
        guide=ROOT/'docs/fde-v2/interview-drills/README.md'
        self.assertTrue(guide.is_file())
        text=guide.read_text()
        self.assertEqual(len(re.findall(r'^\|\s*\d+\s*\|',text,re.M)),14)
        for artifact in ('SpringServiceBoundaryDrill.java','starter.ts','RequestBudgetDrill.java',
                         'IdempotencyDrill.java','ai/fde_v2/m22/workflow.py','ai/fde_v2/m42/reference.py',
                         'ai/fde_v2/m23/providers.py','m53'):
            self.assertIn(artifact,text)
        for command in ('ai.fde_v2.tests.test_m22_integration',
                        'ai.fde_v2.tests.test_m42','ai.fde_v2.tests.test_m23_draft',
                        'ai.fde_v2.tests.test_m53','VITE_DRILL_IMPLEMENTATION=starter npx vitest run'):
            self.assertIn(command,text)
        self.assertIn('0–4 total',text)
        self.assertIn('scratch',text.lower())
        self.assertIn('SQL/query-plan debugging',text)
        self.assertIn('concurrency debugging',text)
        self.assertIn('forces both duplicate requests past the idempotency lookup',text)


if __name__=='__main__': unittest.main()
