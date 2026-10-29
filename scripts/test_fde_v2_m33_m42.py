"""Regression for conditional M3.3–M4.2 budgets, daily cards, and blocked evidence gates."""
from __future__ import annotations
import json, unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]

class ConditionalM33M42ManifestTest(unittest.TestCase):
    def check_schedule(self,key,first,substantive,buffer,visibility,unallocated):
        path=ROOT/f'ai/fde_v2/{key}/draft_schedule.json'
        raw=json.loads(path.read_text()); rows=raw['days']
        self.assertEqual(raw['status'],'conditional_design_draft_not_active')
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

    def test_daily_schedules_have_exact_caps_and_stage_totals(self):
        m33=self.check_schedule('m33',143,1440,300,90,60)
        self.assertEqual(sorted(m33.values()),[90]+[150]*9)
        m41=self.check_schedule('m41',163,1500,240,90,60)
        self.assertEqual(set(m41.values()),{150})
        m42=self.check_schedule('m42',184,1500,240,90,60)
        self.assertEqual(set(m42.values()),{150})

    def test_accepted_contracts_remain_unchanged_and_guides_keep_hard_gates(self):
        contracts=json.loads((ROOT/'docs/fde-v2/accepted/later-milestone-contracts.json').read_text())
        ids={u['id'] for u in contracts['units']}
        self.assertTrue({'AIFDE-V2-M3.3','AIFDE-V2-M4.1','AIFDE-V2-M4.2'}.issubset(ids))
        guide=(ROOT/'docs/fde-v2/proposals/m33-m42-conditional-guide.md').read_text()
        for required in ('Not activated','UNVERIFIED_SCORE','trusted actual evaluator integration','BLOCKED',
                         'exactly-once delivery','production reliability'):
            self.assertIn(required.lower(),guide.lower())

    def test_boundary_days_carry_unresolved_work_without_exceeding_daily_cap(self):
        m33=json.loads((ROOT/'ai/fde_v2/m33/draft_schedule.json').read_text())
        m41=json.loads((ROOT/'ai/fde_v2/m41/draft_schedule.json').read_text())
        day163_old=next(r for r in m33['days'] if r['day']==163)
        day163_new=next(r for r in m41['days'] if r['day']==163)
        self.assertEqual((day163_old['buffer_minutes'],day163_old['unallocated_minutes']),(60,30))
        self.assertEqual((day163_new['substantive_minutes'],day163_new['unallocated_minutes']),(30,60))
        card=(ROOT/'docs/fde-v2/days/day-163.md').read_text().lower()
        self.assertIn('if m3.3 still needs its reserved buffer',card)
        self.assertIn('carry the 30 m4.1 minutes forward',card)
        self.assertIn('ai: 90 minutes total',card)

if __name__=='__main__': unittest.main()
