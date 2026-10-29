"""Governance checks for the conditional M2.2 draft and executable mechanics."""
from __future__ import annotations

import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class M22DraftContract(unittest.TestCase):
    def test_draft_remains_conditional_and_does_not_rewrite_accepted_contract(self):
        contracts = json.loads((ROOT / "docs/fde-v2/accepted/later-milestone-contracts.json").read_text())
        m22 = next(row for row in contracts["units"] if row["id"] == "AIFDE-V2-M2.2")
        self.assertEqual(m22["implementation_status"], "planned_milestone_contract_only")
        self.assertEqual(m22["learner_status"], "not_started")
        self.assertIn("Actual authentication", m22["deliverables"][0])
        self.assertIn("Cross-user/tenant access", m22["acceptance_criteria"][0])
        proposal = (ROOT / "docs/fde-v2/proposals/m22-detailed-days-62-82.md").read_text()
        self.assertIn("Status: proposed scaffold and conditional teaching design only", proposal)
        self.assertIn("does not activate M2.2", proposal)
        self.assertIn("no learner days are complete", proposal.lower())
        self.assertIn("does not repeat the initial audit", proposal)
        self.assertIn("No credentials, model requests, cloud setup", proposal)

    def test_proposal_links_and_draft_schedule_are_present(self):
        proposal = ROOT / "docs/fde-v2/proposals/m22-detailed-days-62-82.md"
        text = proposal.read_text()
        schedule = json.loads((ROOT / "ai/fde_v2/m22/draft_schedule.json").read_text())
        self.assertEqual(schedule["status"], "conditional_design_draft_not_active")
        self.assertEqual(schedule["milestone"], "AIFDE-V2-M2.2")
        self.assertEqual([row["day"] for row in schedule["days"]], list(range(62, 83)))
        for target in re.findall(r"\[[^\]]+\]\(([^)]+)\)", text):
            if target.startswith(("https://", "http://", "mailto:", "#")):
                continue
            filepart = target.split("#", 1)[0]
            if filepart:
                self.assertTrue((proposal.parent / filepart).resolve().exists(), target)

    def test_later_daily_pages_exist_but_remain_conditional(self):
        days = ROOT / "docs/fde-v2/days"
        for day in range(42, 82):
            page = days / f"day-{day}.md"
            self.assertTrue(page.exists(), f"missing conditional day card {day}")
            text = page.read_text()
            self.assertIn("Conditional", text)
            self.assertIn("Not started", text)
        day82 = days / "day-82.md"  # conditional M2.3 planning card, not M2.2 activation
        if day82.exists():
            text = day82.read_text()
            self.assertIn("Conditional draft only", text)
            self.assertNotIn("M2.2-S", text)
        self.assertIn("NotImplementedError", (ROOT / "ai/fde_v2/m22/learner.py").read_text())


if __name__ == "__main__":
    unittest.main()
