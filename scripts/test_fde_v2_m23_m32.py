"""Regression for conditional milestone schedules, daily cards and evidence wording."""
from __future__ import annotations
import json, unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]

class ConditionalM23M32ManifestTest(unittest.TestCase):
    def check_schedule(self, key, first, substantive, buffer, visibility, unallocated):
        raw=json.loads((ROOT/f'ai/fde_v2/{key}/draft_schedule.json').read_text())
        rows=raw['days']
        self.assertEqual([r['day'] for r in rows],list(range(first,first+21)))
        self.assertTrue(all(r['dsa_minutes']==60 and r['role_bank_minutes']==30 and r['ai_minutes']==90 for r in rows))
        self.assertTrue(all(r['substantive_minutes']+r['buffer_minutes']+r['visibility_minutes']+r['unallocated_minutes']==90 for r in rows))
        self.assertEqual(sum(r['substantive_minutes'] for r in rows),substantive)
        self.assertEqual(sum(r['buffer_minutes'] for r in rows),buffer)
        self.assertEqual(sum(r['visibility_minutes'] for r in rows),visibility)
        self.assertEqual(sum(r['unallocated_minutes'] for r in rows),unallocated)
        stage_totals={}
        for row in rows:
            card=ROOT/f'docs/fde-v2/days/day-{row["day"]:02}.md'
            self.assertTrue(card.is_file(),f'missing {card}')
            text=card.read_text()
            self.assertIn('Status: Not started',text)
            self.assertRegex(text,r'180-minute (daily )?budget')
            for seg in row['segments']:
                if seg['stage']!='buffer': stage_totals[seg['stage']]=stage_totals.get(seg['stage'],0)+seg['minutes']
        return stage_totals

    def test_three_schedules_and_daily_cards_are_budget_exact(self):
        m23=self.check_schedule('m23',82,1440,300,90,60)
        self.assertEqual(sorted(m23.values()),[90]+[150]*9)
        m31=self.check_schedule('m31',103,1500,240,90,60)
        self.assertEqual(set(m31.values()),{150})
        m32=self.check_schedule('m32',123,1470,240,90,90)
        self.assertEqual(sorted(m32.values()),[120]+[150]*9)

    def test_boundaries_keep_accepted_caps_and_distinguish_draft_from_completion(self):
        contract=json.loads((ROOT/'docs/fde-v2/accepted/later-milestone-contracts.json').read_text())
        by_id={x['id']:x for x in contract['units']}
        self.assertEqual(by_id['AIFDE-V2-M2.3']['buffer_hours'],5)
        self.assertEqual(by_id['AIFDE-V2-M3.1']['buffer_hours'],4)
        self.assertEqual(by_id['AIFDE-V2-M3.2']['buffer_hours'],4)
        guide=(ROOT/'docs/fde-v2/proposals/m23-m32-conditional-guide.md').read_text()
        self.assertIn('**Status:** These are draft study/build plans',guide)
        self.assertIn('trusted human UI',guide)
        self.assertIn('AWS account/service/cost approval is absent',guide)
        self.assertIn('SDK2.0.0 or later',guide)

    def test_day82_boundary_resolves_m22_buffer_before_m23_start(self):
        m22=json.loads((ROOT/'ai/fde_v2/m22/draft_schedule.json').read_text())
        m23=json.loads((ROOT/'ai/fde_v2/m23/draft_schedule.json').read_text())
        r22=next(r for r in m22['days'] if r['day']==82)
        r23=next(r for r in m23['days'] if r['day']==82)
        self.assertEqual((r22['m22_buffer_minutes'],r22['unallocated_minutes']),(60,30))
        self.assertEqual((r23['substantive_minutes'],r23['unallocated_minutes']),(30,60))
        card=(ROOT/'docs/fde-v2/days/day-82.md').read_text()
        self.assertIn('If M2.2 is still blocked, use that buffer for M2.2 and carry M2.3-S01 forward',card)
        self.assertIn('the other 60 minutes of AI time stays unallocated',card)

if __name__=='__main__': unittest.main()
