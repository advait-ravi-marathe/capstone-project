"""Governance parity checks for the conditional M2.1 design proposal."""
from __future__ import annotations

import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class M21DraftGovernance(unittest.TestCase):
    def setUp(self):
        self.contracts = json.loads((ROOT / "docs/fde-v2/accepted/later-milestone-contracts.json").read_text())
        self.m21 = next(unit for unit in self.contracts["units"] if unit["id"] == "AIFDE-V2-M2.1")
        self.schedule = json.loads((ROOT / "ai/fde_v2/m21/draft_schedule.json").read_text())
        self.guide = (ROOT / "docs/fde-v2/proposals/m21-detailed-days-42-62.md").read_text()

    def test_draft_remains_conditional_on_accepted_p1_prerequisite(self):
        self.assertEqual(self.m21["required_capability_prerequisites"],
                         ["P1: actual generation/embeddings/vector retrieval and learner ownership"])
        self.assertEqual(self.schedule["status"], "DRAFT_CONDITIONAL_NOT_ACTIVE")
        self.assertIn("BLOCKED", self.guide)
        self.assertIn("not an active learner assignment", self.guide)

    def test_proposal_covers_each_accepted_deliverable_and_acceptance(self):
        required_terms = (
            "versioned", "upsert", "deletion", "provenance", "keyword", "vector",
            "100", "development", "held-out", "answerability", "adversarial",
            "unchanged baseline", "proposed change", "regression", "useful-workflow",
            "freeze", "release evaluation", "failure",
        )
        lower = self.guide.lower()
        for term in required_terms:
            with self.subTest(term=term):
                self.assertIn(term.lower(), lower)

    def test_exact_ai_hour_envelope_and_day_indices(self):
        days = self.schedule["days"]
        self.assertEqual([row["day"] for row in days], list(range(42, 63)))
        self.assertEqual(sum(row["m21_substantive"] for row in days), 25 * 60)
        self.assertEqual(sum(row["m21_buffer"] for row in days), 4 * 60)
        self.assertEqual(sum(row["visibility"] for row in days), 90)
        self.assertEqual(sum(row["unallocated"] for row in days), 60)
        self.assertEqual(self.m21["ai_work_hours_excluding_weekly_visibility"], 29.0)
        self.assertEqual(self.m21["substantive_ai_hours"], 25.0)
        for row in days:
            with self.subTest(day=row["day"]):
                self.assertEqual((row["dsa"], row["role_aws"], row["ai"]), (60, 30, 90))
                self.assertEqual(row["m21_substantive"] + row["m21_buffer"] +
                                 row["visibility"] + row["unallocated"], row["ai"])

    def test_guide_has_no_unresolved_local_resource_paths(self):
        self.assertIn("../../../ai/fde_v2/m21/draft_schedule.json", self.guide)
        for relative in ("../../../ai/fde_v2/m21/learner.py",
                         "../../../ai/fde_v2/m21/reference.py",
                         "../../../ai/fde_v2/tests/test_m21_draft.py",
                         "../../../ai/fde_v2/learner_acceptance/m21_acceptance.py"):
            self.assertTrue((ROOT / "docs/fde-v2/proposals" / relative).resolve().exists(), relative)


if __name__ == "__main__":
    unittest.main()
