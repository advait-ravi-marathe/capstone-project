import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class Sprint3InterviewCurriculumTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root_manifest = json.loads((ROOT / 'docs/reboot/manifest.json').read_text())
        cls.root_owner = json.loads((ROOT / 'docs/reboot/scrum-owner-manifest.json').read_text())
        cls.cont_manifest = json.loads((ROOT / 'docs/reboot/continuation-31-44/manifest.json').read_text())
        cls.cont_owner = json.loads((ROOT / 'docs/reboot/continuation-31-44/scrum-owner-manifest.json').read_text())
        cls.root_days = {d['day']: d for d in cls.root_manifest['days']}
        cls.root_cards = {d['day_number']: d for d in cls.root_owner['cards']}
        cls.cont_days = {d['day']: d for d in cls.cont_manifest['days']}
        cls.cont_cards = {d['day_number']: d for d in cls.cont_owner['cards']}
        cls.scope = (ROOT / 'docs/reboot/interview/sprint3-session-scope.md').read_text()
        cls.deep = (ROOT / 'docs/reboot/interview/deep-practice.md').read_text()

    def assignment(self, day):
        if day <= 30:
            canonical, owner = self.root_days[day], self.root_cards[day]
        else:
            canonical, owner = self.cont_days[day], self.cont_cards[day]
        self.assertEqual(canonical['interview_practice'], owner['interview_practice'])
        return canonical, canonical['interview_practice']

    def test_four_mocks_are_two_per_week_with_exact_phase_budget(self):
        expected = {
            31: ('2026-11-23', 'Q05', 'S3-D31-1'),
            35: ('2026-11-27', 'Q13', 'S3-D35-1'),
            38: ('2026-11-30', 'Q16', 'S3-D38-1'),
            42: ('2026-12-04', 'Q11', 'S3-D42-1'),
        }
        for day, (date, question, queue) in expected.items():
            canonical, item = self.assignment(day)
            self.assertEqual(canonical['date'], date)
            self.assertEqual(item['mode'], '45_minute_mock')
            self.assertTrue(item['proposal_before_hints_reference_or_ai'])
            self.assertEqual(item['question_id'], question)
            self.assertEqual(item['duration_minutes'], 45)
            self.assertEqual(item['practice_minutes_available'], 85)
            self.assertEqual(item['mock_minutes'], 45)
            self.assertEqual(item['eai_slice_minutes'], {'case_setup': 5, 'implementation': 25, 'selector_and_blocker_record': 10})
            self.assertEqual(item['practice_total_minutes'], 85)
            self.assertEqual(item['mock_minutes'] + sum(item['eai_slice_minutes'].values()), 85)
            self.assertEqual(sum(p['minutes'] for p in item['mock_phases']), 45)
            self.assertEqual([p['minutes'] for p in item['mock_phases']], [5, 5, 15, 10, 10])
            self.assertEqual(45 + 40, item['practice_minutes_available'])
            self.assertEqual(item['deferred_work_id'], queue)
            self.assertTrue(item['deferred_work'])
            self.assertEqual(item['course_bridges'], canonical['course_learning']['lessons'])
            self.assertEqual(item['course_catalog'], canonical['course_learning']['courses'])
            for resource in canonical['course_learning'].get('resources', []):
                self.assertIn(resource['url'], item['supporting_sources'])
            for ref in item['pattern_refs']:
                doc, anchor = ref.split('#', 1)
                self.assertTrue((ROOT / doc).is_file(), ref)
                self.assertIn(f'id="{anchor}"', (ROOT / doc).read_text(), ref)
            qdoc, qanchor = item['question_href'].split('#', 1)
            self.assertTrue((ROOT / qdoc).is_file())
            self.assertIn(f'id="{qanchor}"', (ROOT / qdoc).read_text())
            self.assertIn('40 minutes', item['implementation_scope'])
            self.assertIn('S3-', item['deferred_work_id'])
            contract = ROOT / (f'docs/reboot/contracts/day-{day}.md' if day <= 30 else f'docs/reboot/continuation-31-44/contracts/day-{day}.md')
            text = contract.read_text()
            self.assertIn('Interview-first system-design mock', text)
            self.assertIn('before hints, reference answers, or AI', text)
            self.assertIn(item['deferred_work_id'], text)
            allocation = re.search(r'\*\*Practice allocation \(85 minutes\):\*\*([^\n]+)', text)
            self.assertIsNotNone(allocation, f'day {day} practice allocation')
            self.assertIn('45 minutes', allocation.group(1))
            self.assertIn('40 minutes', allocation.group(1))
            for superseded in ('15 / 55 / 15', '10 / 55 / 20', '10 / 60 / 15'):
                self.assertNotIn(superseded, allocation.group(1), f'day {day} contradictory active allocation')
            self.assertIn('#'+item['question_id'].lower(), text)
            self.assertIn(item['question_title'], text)
            for selector in re.findall(r'`([^`]*\./mvnw[^`]*)`', item['implementation_scope'] + ' ' + text):
                class_and_method = selector.split('-Dtest=', 1)[-1].split()[0]
                if '#' in class_and_method:
                    class_name, method = class_and_method.split('#', 1)
                    java_path = ROOT / 'src/test/java' / Path(*class_name.split('.')).with_suffix('.java')
                    if java_path.exists():
                        self.assertRegex(java_path.read_text(), rf'\b{re.escape(method)}\s*\(')

    def test_other_days_use_supplied_capstone_architecture_and_original_acceptance(self):
        mock_days = {31, 35, 38, 42}
        for day in set(range(29, 43)) - mock_days:
            canonical, item = self.assignment(day)
            self.assertEqual(item['mode'], 'assistant_supplied_capstone_architecture')
            self.assertFalse(item['proposal_before_hints_reference_or_ai'])
            self.assertEqual(item['proposal_scope'], 'none')
            self.assertEqual(item['capstone_architecture'], 'assistant_supplied')
            self.assertIn(f'#day-{day}', item['assistant_architecture_note'])
            self.assertEqual(85, item['practice_minutes_available'])
            self.assertIn('full 85-minute practice allocation', item['implementation_scope'])
            path = ROOT / (f'docs/reboot/contracts/day-{day}.md' if day <= 30 else f'docs/reboot/continuation-31-44/contracts/day-{day}.md')
            text = path.read_text()
            self.assertNotRegex(text, r'^## (?:Learner-first|Interview-first) .*proposal')
            self.assertIn('Assistant-provided capstone architecture', text)
            self.assertIn('acceptance', text.lower())

    def test_deep_practice_sessions_nine_to_twelve_match_exported_sprint3_prompts(self):
        for number, day, question, selector in [
            (9, 31, 'Q05', 'day31RejectsForeignUnselectedStaleOrIneligibleQuoteWithoutIssue'),
            (10, 35, 'Q13', 'day35AtomicPostgresWriteHasOnePolicyAndOneReceipt'),
            (11, 38, 'Q16', 'day38EndorsementAppendsAndPreservesPriorHistoryInEffectiveOrder'),
            (12, 42, 'Q11', 'day42CancelledPolicyRejectsWritesButRemainsReadable'),
        ]:
            self.assertRegex(self.deep, rf'\| {number} \| {day} · [^|]+ \| {question} \|')
            self.assertIn(selector, self.deep)
        self.assertIn('sprint3-session-scope.md', self.deep)
        self.assertIn('two in each seven-day practice', self.scope)

    def test_day42_queue_does_not_overclaim_pure_selector_as_integration(self):
        canonical, item = self.assignment(42)
        owner = self.cont_cards[42]['interview_practice']
        self.assertEqual(item['additional_deferred_work_id'], 'S3-D42-2')
        self.assertEqual(item, owner)
        for payload in (item['deferred_work'], item['additional_deferred_work']):
            self.assertTrue(payload)
        self.assertIn('does not prove actual endorsement/reschedule writes', item['deferred_work'])
        self.assertIn('cannot close S3-D42-2', item['additional_deferred_work'])
        self.assertIn('OPEN; prerequisite-gated', (ROOT / 'docs/reboot/continuation-31-44/contracts/day-42.md').read_text())
        self.assertIn('synthetic owner read only', (ROOT / 'docs/reboot/interview/sprint3-session-scope.md').read_text())
        source = (ROOT / 'src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java').read_text()
        selector = source.split('void day42CancelledPolicyRejectsWritesButRemainsReadable()', 1)[1].split('\n    }', 1)[0]
        self.assertIn('permitsServicingWrite(cancelled)', selector)
        self.assertIn('readForOwner(store', selector)
        self.assertNotIn('DataSource', selector)

    def test_course_bridges_are_existing_metadata_and_dsa_stays_external(self):
        for day in [31, 35, 38, 42]:
            canonical, assignment = self.assignment(day)
            lessons = canonical['course_learning']['lessons']
            self.assertEqual(assignment['course_bridges'], lessons)
        self.assertIn('access unconfirmed', self.scope)
        self.assertIn('dsa-prep', self.cont_manifest['separate_learning_repository']['algorithm_practice'])
        self.assertNotIn('DSA assignment', self.scope)


if __name__ == '__main__':
    unittest.main()
