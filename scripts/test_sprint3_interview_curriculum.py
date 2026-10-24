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
            self.assertIn('#'+item['question_id'].lower(), text)
            self.assertIn(item['question_title'], text)
            for selector in re.findall(r'`([^`]*\./mvnw[^`]*)`', item['implementation_scope'] + ' ' + text):
                class_and_method = selector.split('-Dtest=', 1)[-1].split()[0]
                if '#' in class_and_method:
                    class_name, method = class_and_method.split('#', 1)
                    java_path = ROOT / 'src/test/java' / Path(*class_name.split('.')).with_suffix('.java')
                    if java_path.exists():
                        self.assertRegex(java_path.read_text(), rf'\b{re.escape(method)}\s*\(')

    def test_other_days_keep_a_five_minute_prework_prompt_and_original_acceptance(self):
        mock_days = {31, 35, 38, 42}
        for day in set(range(29, 43)) - mock_days:
            canonical, item = self.assignment(day)
            self.assertEqual(item['mode'], '5_minute_task_proposal')
            self.assertEqual(item['duration_minutes'], 5)
            self.assertTrue(item['proposal_before_hints_reference_or_ai'])
            self.assertIn('remaining 80 minutes', item['implementation_scope'])
            self.assertEqual(5 + 80, item['practice_minutes_available'])
            if day <= 30:
                path = ROOT / f'docs/reboot/contracts/day-{day}.md'
            else:
                path = ROOT / f'docs/reboot/continuation-31-44/contracts/day-{day}.md'
            text = path.read_text()
            self.assertIn('Interview-first task proposal', text)
            self.assertIn('Before code, hints, worked answers, or AI', text)
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
