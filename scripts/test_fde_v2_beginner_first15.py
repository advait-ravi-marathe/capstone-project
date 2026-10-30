from __future__ import annotations

import json
import subprocess
import sys
import unittest
from pathlib import Path

from ai.fde_v2.beginner_first15 import reference

ROOT = Path(__file__).resolve().parents[1]
OVERLAY = ROOT / "docs/fde-v2/amendments/2026-10-30-beginner-first-15.json"


class BeginnerFirst15Tests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.plan = json.loads(OVERLAY.read_text(encoding="utf-8"))

    def test_active_overlay_has_fifteen_checkable_days_and_preserves_180_minute_cap(self):
        self.assertEqual(len(self.plan["days"]), 15)
        self.assertEqual([row["day"] for row in self.plan["days"]], list(range(1, 16)))
        self.assertEqual(self.plan["daily_budget_minutes"],
                         {"dsa": 60, "aws_learning": 30, "ai_learning": 90, "total": 180})
        for day in self.plan["days"]:
            with self.subTest(day=day["day"]):
                self.assertEqual(sum(day["ai_minutes"].values()), 90)
                self.assertTrue(day["python"].startswith(("https://docs.python.org/", "https://www.sqlite.org/")))
                self.assertTrue(day["aws"]["source"].startswith("https://docs.aws.amazon.com/"))
                self.assertTrue(day["task"]["file"])
                self.assertTrue(day["task"]["function"])
                self.assertTrue(day["task"]["command"])
                self.assertTrue(day["task"]["expected_output"])
                self.assertTrue((ROOT / day["task"]["file"]).is_file())
                card = (ROOT / "docs/fde-v2/days" / f"day-{day['day']:02}.md").read_text(encoding="utf-8")
                self.assertIn(day["task"]["function"], card)
                self.assertIn(day["task"]["command"], card)
                self.assertIn(day["python"], card)
                self.assertIn(day["aws"]["source"], card)
                self.assertIn(day["aws_section"], card)
                for excluded in ("Role interview prompt", "target-role gap", "write a one-page problem brief"):
                    self.assertNotIn(excluded.lower(), card.lower())

    def test_reference_examples_cover_edge_cases_and_stable_search(self):
        rows = [
            {"document_id": "appeals", "title": "Appeals", "text": "Review denied claims", "source": "fixture/appeals"},
            {"document_id": "deductible", "title": "Deductible", "text": "Individual deductible is 500", "source": "fixture/deductible"},
            {"document_id": "therapy", "title": "Therapy", "text": "Referral required", "source": "fixture/therapy"},
        ]
        self.assertEqual(reference.document_title({}), "")
        self.assertEqual(reference.normalize_question("  HELLO  "), "hello")
        self.assertEqual(reference.question_words("The plan 500"), ["the", "plan", "500"])
        self.assertTrue(reference.is_blank_question(" \t"))
        self.assertFalse(reference.is_blank_question("one"))
        self.assertEqual([r["document_id"] for r in reference.matching_records("denied", rows)], ["appeals"])
        self.assertEqual(reference.unique_question_words("a a b"), {"a", "b"})
        self.assertEqual(reference.overlap_score("deductible deductible individual", rows[1]), 2)
        tie = [dict(rows[0], document_id="z"), dict(rows[0], document_id="a")]
        self.assertEqual([r["document_id"] for r in reference.rank_records("appeals", tie, 2)], ["a", "z"])
        self.assertIsNone(reference.best_record("unmentioned phrase", rows))
        self.assertEqual(reference.source_label(rows[0]), "Appeals — fixture/appeals")
        self.assertEqual([r["document_id"] for r in reference.search_records("individual deductible", rows)],
                         ["deductible"])
        self.assertEqual(reference.format_search_results("unmentioned", rows), "No matching documents.")
        self.assertEqual(reference.search_response("  DEDUCTIBLE? ", rows), "Deductible — fixture/deductible")
        self.assertEqual(reference.search_response("  ", rows), "Please enter a question.")
        self.assertEqual(reference.search_records("  ", rows), [])
        with self.assertRaises(ValueError):
            reference.rank_records("appeals", rows, 0)

    def test_day16_chunking_still_waits_for_u011_not_for_a_manual_report(self):
        day15 = (ROOT / "docs/fde-v2/days/day-15.md").read_text(encoding="utf-8")
        day16 = (ROOT / "docs/fde-v2/days/day-16.md").read_text(encoding="utf-8")
        handoff = (ROOT / "docs/fde-v2/amendments/day16-prerequisite-handoff.md").read_text(encoding="utf-8")
        self.assertIn("does **not** satisfy the accepted U011 contract", day15)
        self.assertIn("NOT READY under the beginner-first reset", day16)
        self.assertIn("U011 versioned ingestion must pass its learner acceptance check", handoff)
        self.assertIn("U012", day16)
        self.assertIn("does not satisfy the accepted U011 contract", handoff)
        self.assertNotIn("write a gap report", day16.lower())

    def test_candidate_checks_are_runnable_and_keep_each_unsolved_todo_red(self):
        for day in range(1, 16):
            result = subprocess.run(
                [sys.executable, "-m", "ai.fde_v2.beginner_first15.acceptance", "--day", str(day)],
                cwd=ROOT, text=True, capture_output=True, check=False, timeout=15,
            )
            with self.subTest(day=day):
                self.assertEqual(result.returncode, 1)
                self.assertIn("NOT YET", result.stderr)
                if day == 1:
                    self.assertIn("implement document_title", result.stderr)
                if day == 14:
                    self.assertIn("implement format_search_results", result.stderr)
                if day == 15:
                    self.assertIn("implement search_response", result.stderr)

    def test_supplied_keyword_demo_still_runs_without_learner_todos_or_models(self):
        result = subprocess.run(
            [sys.executable, "-m", "ai.fde_v2.coverstory.day01_baseline"],
            cwd=ROOT, text=True, capture_output=True, check=True, timeout=15,
        )
        self.assertIn("document=northstar-deductible", result.stdout)
        self.assertIn("Instructor-owned keyword baseline only", result.stdout)


if __name__ == "__main__":
    unittest.main()
