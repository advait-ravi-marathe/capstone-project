import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MOCK_DAYS = {
    45: ('2026-12-09', 'Q03', 'PolicyRenewalAcceptanceTest#day45AnnualTermUsesExclusiveAnniversaryAndThirtyDayWindow'),
    49: ('2026-12-13', 'Q07', 'PolicyRenewalControllerAcceptanceTest#day49OwnerComesOnlyFromPrincipalAndUnauthenticatedDoesNotRead+day49MockMvcPassesPrincipalRatherThanForgedQueryOrHeaderToTheReadStore'),
    52: ('2026-12-16', 'Q14', 'PolicyRenewalPreviewPanel.test.tsx'),
    56: ('2026-12-20', 'Q12', 'RenewalPreviewTransportInfrastructureTest#day56PreviewReadIsStableAcrossRestartAndDoesNotWrite'),
}
SPRINT4_DAYS = set(range(43, 57))
NON_MOCK_DAYS = SPRINT4_DAYS - set(MOCK_DAYS)


class Sprint4InterviewCurriculumTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads((ROOT / 'docs/reboot/continuation-45-56/manifest.json').read_text())
        cls.owner = json.loads((ROOT / 'docs/reboot/continuation-45-56/scrum-owner-manifest.json').read_text())
        cls.curriculum = json.loads((ROOT / 'docs/reboot/continuation-45-56/fde45-56-v2-curriculum.json').read_text())
        cls.days = {x['day']: x for x in cls.manifest['days']}
        cls.cards = {x['day_number']: x for x in cls.owner['cards']}
        cls.curriculum_days = {x['day']: x for x in cls.curriculum['days']}
        cls.bank = (ROOT / 'docs/reboot/interview/question-bank.md').read_text()
        cls.scope = (ROOT / 'docs/reboot/interview/sprint4-session-scope.md').read_text()

    def test_four_mocks_fit_two_per_week_without_changing_daily_budget(self):
        self.assertEqual(set(MOCK_DAYS), {45, 49, 52, 56})
        for day, (date, qid, selector) in MOCK_DAYS.items():
            with self.subTest(day=day):
                d = self.days[day]
                assignment = d['interview_practice']
                self.assertEqual(d['date'], date)
                self.assertEqual(assignment, self.cards[day]['interview_practice'])
                self.assertEqual(assignment, self.curriculum_days[day]['interview_practice'])
                self.assertEqual(assignment['question_id'], qid)
                self.assertEqual(assignment['duration_minutes'], 45)
                self.assertEqual(assignment['practice_total_minutes'], 85)
                self.assertEqual(assignment['mock_minutes'] + sum(assignment['eai_slice_minutes'].values()), 85)
                self.assertEqual(assignment['eai_slice_minutes'], {'case_setup': 5, 'implementation_or_observation': 25, 'selector_and_blocker_record': 10})
                self.assertEqual([p['minutes'] for p in assignment['mock_phases']], [5, 5, 15, 10, 10])
                self.assertTrue(assignment['proposal_before_hints_reference_or_ai'])
                self.assertEqual(d['engineering']['budget_minutes'], 120)
                self.assertEqual(d['engineering']['allocation_minutes'], {'implementation_and_testing': 85, 'concept_or_course': 25, 'evidence': 10})
                self.assertEqual(assignment['practice_minutes_available'], 85)
                self.assertIn(selector.split('#')[-1].split('+')[0].split('.')[0], assignment['implementation_scope'] + ' ' + json.dumps(d['engineering']))
                self.assertTrue(assignment['deferred_work_id'])
                self.assertTrue(assignment['deferred_work'])
                self.assertIn('45 minutes', (ROOT / f'docs/reboot/continuation-45-56/contracts/day-{day}.md').read_text())
        week_one = [d for d in MOCK_DAYS if '2026-12-09' <= MOCK_DAYS[d][0] <= '2026-12-13']
        week_two = [d for d in MOCK_DAYS if '2026-12-14' <= MOCK_DAYS[d][0] <= '2026-12-20']
        self.assertEqual(len(week_one), 2)
        self.assertEqual(len(week_two), 2)

    def test_every_sprint4_day_has_a_synchronized_task_first_assignment(self):
        early_manifest = json.loads((ROOT / 'docs/reboot/continuation-31-44/manifest.json').read_text())
        early_owner = json.loads((ROOT / 'docs/reboot/continuation-31-44/scrum-owner-manifest.json').read_text())
        early_curriculum = json.loads((ROOT / 'docs/reboot/continuation-31-44/fde31-44-v2-curriculum.json').read_text())
        early = {x['day']: x for x in early_manifest['days']}
        early_cards = {x['day_number']: x for x in early_owner['cards']}
        early_curriculum_days = {x['day']: x for x in early_curriculum['days']}
        curriculum_days = {**early_curriculum_days, **self.curriculum_days}
        canonical_days = {**early, **self.days}
        owner_days = {**early_cards, **self.cards}

        self.assertIn('Sprint 3 ends at Day 42', self.scope)
        self.assertIn('first two Sprint 4 days', self.scope)
        self.assertNotIn('Days 43–44 remain the existing Sprint 3', self.scope)
        self.assertEqual(set(canonical_days) & SPRINT4_DAYS, SPRINT4_DAYS)
        for day in sorted(SPRINT4_DAYS):
            with self.subTest(day=day):
                canonical = canonical_days[day]
                card = owner_days[day]
                curriculum = curriculum_days[day]
                assignment = canonical['interview_practice']
                self.assertEqual(assignment, card['interview_practice'])
                self.assertEqual(assignment, curriculum['interview_practice'])
                self.assertEqual(assignment['proposal_scope'], 'system_design_interview_question_only' if day in MOCK_DAYS else 'none')
                self.assertEqual(assignment['capstone_architecture'], 'assistant_supplied')
                self.assertEqual(assignment['practice_minutes_available'], 85)
                self.assertIn(f'#day-{day}', assignment['assistant_architecture_note'])
                commands = assignment.get('acceptance_commands', canonical['engineering']['acceptance_commands'])
                self.assertTrue(commands)
                self.assertEqual(commands, canonical['engineering']['acceptance_commands'])
                self.assertEqual(canonical['engineering']['budget_minutes'], 120)
                self.assertEqual(canonical['engineering']['allocation_minutes'], {
                    'implementation_and_testing': 85, 'concept_or_course': 25, 'evidence': 10
                })
                self.assertEqual(sum(curriculum['practice_segment_minutes']), 85)
                contract_path = ROOT / canonical['contract_file']
                contract = contract_path.read_text()
                self.assertIn('Assistant-provided capstone architecture', contract)
                if day in MOCK_DAYS:
                    self.assertEqual(assignment['mode'], '45_minute_mock')
                    self.assertEqual(assignment['duration_minutes'], 45)
                    self.assertTrue(assignment['proposal_before_hints_reference_or_ai'])
                    self.assertIn('For this interview question only', contract)
                else:
                    self.assertEqual(assignment['mode'], 'assistant_supplied_capstone_architecture')
                    self.assertEqual(assignment['duration_minutes'], 0)
                    self.assertFalse(assignment['proposal_before_hints_reference_or_ai'])
                    self.assertIn('full 85-minute practice allocation', assignment['implementation_scope'])
                    self.assertNotRegex(contract, r'^## (?:Learner-first|Interview-first) .*proposal')
                    self.assertNotIn('no post-hoc architectural justification is required', contract)
                for command in commands:
                    self.assertIn(command, contract)
                for source in assignment['supporting_sources']:
                    if source.startswith('http'):
                        continue
                    path, _, anchor = source.partition('#')
                    target = ROOT / path
                    self.assertTrue(target.is_file(), source)
                    if anchor:
                        self.assertIn(f'id="{anchor}"', target.read_text(), source)
                for command in commands:
                    if command.startswith('npm '):
                        self.assertTrue((ROOT / 'frontend/src/__tests__/Day43PolicyServicingPanel.test.tsx').is_file() if day == 43 else (ROOT / 'frontend/src/__tests__/PolicyRenewalPreviewPanel.test.tsx').is_file())
                    if '-Dtest=' in command:
                        target = command.split('-Dtest=', 1)[1].split()[0]
                        test_class = None
                        for selector in target.split('+'):
                            if '#' in selector:
                                test_class, method = selector.split('#', 1)
                            else:
                                self.assertIsNotNone(test_class, command)
                                method = selector
                            matches = list((ROOT / 'src/test/java').rglob(test_class.split('.')[-1] + '.java'))
                            self.assertEqual(len(matches), 1, f'day {day}: {test_class}')
                            self.assertRegex(matches[0].read_text(), rf'\b{re.escape(method)}\s*\(')

    def test_day56_restart_wording_requires_seed_once_then_same_database(self):
        contract = (ROOT / 'docs/reboot/continuation-45-56/contracts/day-56.md').read_text()
        self.assertNotIn('unseeded database', contract)
        self.assertIn('same PostgreSQL instance, seeded once before the first context and not reseeded between contexts', contract)
        for record in (self.days[56], self.cards[56], self.curriculum_days[56]):
            assignment = record['interview_practice']
            self.assertNotIn('unseeded', assignment['deferred_work'].lower())
            self.assertIn('seeded once before the first context', assignment['deferred_work'])

    def test_question_pattern_and_contract_links_resolve(self):
        for day, (date, qid, _) in MOCK_DAYS.items():
            item = self.days[day]['interview_practice']
            self.assertIn(f'id="{qid.lower()}"', self.bank)
            self.assertIn(item['question_title'], self.bank)
            for link in [item['question_href'], *item['pattern_refs']]:
                path, anchor = link.split('#', 1)
                self.assertTrue((ROOT / path).is_file(), link)
                self.assertIn(f'id="{anchor}"', (ROOT / path).read_text(), link)
            contract = (ROOT / f'docs/reboot/continuation-45-56/contracts/day-{day}.md').read_text()
            self.assertIn(f'#{qid.lower()}', contract)
            self.assertIn('before hints, reference answers, or AI', contract)
            self.assertIn(item['deferred_work_id'], contract)
            for command in self.days[day]['engineering']['acceptance_commands']:
                if command.startswith('npm '):
                    self.assertIn('test:learner', json.loads((ROOT / 'frontend/package.json').read_text())['scripts'])
                    self.assertTrue((ROOT / 'frontend/src/__tests__/PolicyRenewalPreviewPanel.test.tsx').is_file())
                if '-Dtest=' in command:
                    target = command.split('-Dtest=', 1)[1].split()[0]
                    class_name = None
                    for entry in target.split('+'):
                        if '#' in entry:
                            class_name, method = entry.split('#', 1)
                        else:
                            self.assertIsNotNone(class_name, f'day {day}: missing selector class in {command}')
                            method = entry
                        matches = list((ROOT / 'src/test/java').rglob(class_name.split('.')[-1] + '.java'))
                        self.assertEqual(len(matches), 1, f'day {day}: test class {class_name}')
                        self.assertRegex(matches[0].read_text(), rf'\b{re.escape(method)}\s*\(')

    def test_mock_contract_allocations_replace_old_full_implementation_splits(self):
        old_splits = ('10 / 55 / 20', '15 / 40 / 20', '10 / 50 / 25', '15 / 40 / 20')
        for day in MOCK_DAYS:
            contract = (ROOT / f'docs/reboot/continuation-45-56/contracts/day-{day}.md').read_text()
            block = re.search(r'\*\*Practice allocation \(85 minutes\):\*\*([^\n]+)', contract)
            self.assertIsNotNone(block, day)
            self.assertIn('45 minutes', block.group(1))
            self.assertIn('40 minutes', block.group(1))
            self.assertIn('5 minutes', block.group(1))
            self.assertIn('25 minutes', block.group(1))
            self.assertIn('10 minutes', block.group(1))
            for split in old_splits:
                self.assertNotIn(split, block.group(1), f'day {day}: contradictory allocation {split}')
            self.assertIn('Original acceptance remains required', block.group(1))

    def test_sprint3_day42_integration_queue_remains_open(self):
        d42 = next(x for x in json.loads((ROOT / 'docs/reboot/continuation-31-44/manifest.json').read_text())['days'] if x['day'] == 42)
        item = d42['interview_practice']
        self.assertEqual(item['additional_deferred_work_id'], 'S3-D42-2')
        self.assertIn('cannot close S3-D42-2', item['additional_deferred_work'])
        day45 = (ROOT / 'docs/reboot/continuation-45-56/contracts/day-45.md').read_text()
        self.assertIn('S3-D42-2 remains open', day45)
        self.assertIn('does not claim or close it', self.scope)
        self.assertIn('mark a day complete', self.scope)

    def test_sprint4_dates_and_day_budgets_remain_intact(self):
        self.assertEqual(self.manifest['effective_start'], '2026-12-09')
        self.assertEqual(self.manifest['effective_end'], '2026-12-20')
        for day in range(45, 57):
            d = self.days[day]
            self.assertEqual(d['engineering']['budget_minutes'], 120)
            self.assertEqual(d['engineering']['allocation_minutes'], {'implementation_and_testing': 85, 'concept_or_course': 25, 'evidence': 10})
        self.assertEqual(self.days[56]['learner_status'], 'not_attempted')
        self.assertEqual(self.days[56]['integration_status'], 'blocked')


if __name__ == '__main__':
    unittest.main()
