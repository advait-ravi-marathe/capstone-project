"""Protect the current curriculum's separate DSA repository boundary."""

import subprocess
import unittest
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]


class ExternalTrackBoundaryTest(unittest.TestCase):
    def test_dsa_problems_and_solutions_are_not_copied_here(self):
        paths = subprocess.check_output(
            ["git", "ls-files"], cwd=ROOT, text=True
        ).splitlines()
        forbidden = ("/dsa/", "dsa-problems", "dsa-solutions", "dsa_review_queue")
        offenders = [p for p in paths if any(token in p.lower() for token in forbidden)]
        self.assertEqual([], offenders)
        self.assertFalse(list((ROOT / "scripts").glob("test_dsa_*.py")))

    def test_active_schedule_keeps_dsa_separate_and_time_bounded(self):
        roadmap = (ROOT / "docs/fde-v2/README.md").read_text(encoding="utf-8")
        first_day = (ROOT / "docs/fde-v2/days/day-01.md").read_text(encoding="utf-8")
        self.assertIn("60 minutes for DSA in its separate repository", roadmap)
        self.assertIn("30 minutes of AWS learning", roadmap)
        self.assertIn("90 minutes for guided AI/Python practice", roadmap)
        self.assertIn("separate DSA repository", first_day)
        self.assertIn("no DSA problems or solutions", roadmap)
        overlay = json.loads((ROOT / "docs/fde-v2/amendments/2026-10-30-beginner-first-15.json").read_text())
        self.assertEqual(len(overlay["days"]), 15)
        for row in overlay["days"]:
            self.assertEqual((row["day"], row["ai_minutes"]["lesson"] + row["ai_minutes"]["worked_example"]
                              + row["ai_minutes"]["practice"] + row["ai_minutes"]["check"]),
                             (row["day"], 90))
        self.assertEqual(overlay["daily_budget_minutes"],
                         {"dsa": 60, "aws_learning": 30, "ai_learning": 90, "total": 180})


if __name__ == "__main__":
    unittest.main()
