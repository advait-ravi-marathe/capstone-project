from __future__ import annotations

import json
import os
import subprocess
import sys
import unittest
from unittest import mock
from pathlib import Path
from datetime import date, timedelta

from ai.fde_v2.beginner_first15 import reference
from ai.fde_v2.beginner_first15 import acceptance, learner as candidate

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
                self.assertIn(day["planned_date"], card)
                self.assertNotIn("Find Task", card)
                if day["day"] == 6:
                    self.assertIn('"document_id": "appeals"', card)
                    self.assertIn('one matching record, with `document_id="appeals"`', card)
                self.assertTrue(day["aws"]["check"])
                self.assertEqual(day["planned_date"],
                                 (date(2026, 10, 29) + timedelta(days=day["day"] - 1)).isoformat())
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

    def test_reference_adapter_passes_all_fifteen_selectors_without_claiming_learner_evidence(self):
        env = dict(os.environ)
        env["BEGINNER_FIRST15_CANDIDATE"] = "ai.fde_v2.beginner_first15.reference"
        env["FDE_V2_CANDIDATE"] = "ai.fde_v2.coverstory.reference"
        for day in range(1, 16):
            result = subprocess.run(
                [sys.executable, "-m", "ai.fde_v2.beginner_first15.acceptance", "--day", str(day)],
                cwd=ROOT, text=True, capture_output=True, check=False, timeout=15, env=env,
            )
            with self.subTest(day=day):
                self.assertEqual(result.returncode, 0, result.stderr)
                self.assertIn("REFERENCE MECHANICS PASS", result.stdout)
                self.assertIn("not learner evidence", result.stdout)

    def test_day12_supplied_constructor_runs_only_after_learner_validation(self):
        validator = lambda record: reference.document_record_valid(record)
        with mock.patch.object(candidate, "document_record_valid", side_effect=validator):
            self.assertIn("PASS", acceptance.check(12))
        with mock.patch.object(candidate, "document_record_valid", return_value=True):
            with self.assertRaises((AssertionError, KeyError, ValueError)):
                acceptance.check(12)

    def test_meaningful_wrong_implementations_fail_and_day1_ignores_future_todos(self):
        records = [
            {"document_id": "z", "title": "Red", "text": "red", "source": "z"},
            {"document_id": "a", "title": "Red", "text": "red", "source": "a"},
            {"document_id": "top", "title": "Red blue", "text": "red blue", "source": "top"},
            {"document_id": "none", "title": "Green", "text": "green", "source": "none"},
        ]
        with mock.patch.object(candidate, "rank_records", return_value=records[:2]):
            with self.assertRaises(AssertionError):
                acceptance.check(9)
        with mock.patch.object(candidate, "format_search_results", return_value="Appeals — synthetic/appeals"):
            with self.assertRaises(AssertionError):
                acceptance.check(14)
        with mock.patch.object(candidate, "search_response", return_value="Deductible — a source"):
            with self.assertRaises(AssertionError):
                acceptance.check(15)
        with mock.patch.object(candidate, "document_title", side_effect=lambda record: record.get("title", "")), \
             mock.patch.object(candidate, "normalize_question", side_effect=AssertionError("future TODO called")):
            self.assertIn("PASS", acceptance.check(1))

    def test_supplied_keyword_demo_still_runs_without_learner_todos_or_models(self):
        result = subprocess.run(
            [sys.executable, "-m", "ai.fde_v2.coverstory.day01_baseline"],
            cwd=ROOT, text=True, capture_output=True, check=True, timeout=15,
        )
        self.assertIn("document=northstar-deductible", result.stdout)
        self.assertIn("Instructor-owned keyword baseline only", result.stdout)


if __name__ == "__main__":
    unittest.main()
