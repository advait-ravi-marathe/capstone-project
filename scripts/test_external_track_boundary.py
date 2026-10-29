"""Protect the current curriculum's separate DSA repository boundary."""

import subprocess
import unittest
from pathlib import Path

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
        self.assertIn("DSA60 + role/AWS bank30 + AI90 = 180 minutes", roadmap)
        self.assertIn("separate DSA plan", first_day)
        self.assertIn("no DSA problems or solutions", roadmap)


if __name__ == "__main__":
    unittest.main()
