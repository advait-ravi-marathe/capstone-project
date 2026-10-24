import json
import re
import unittest
from datetime import date, timedelta
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


class Sprint1InterviewCurriculumTest(unittest.TestCase):
    def test_question_and_deep_practice_counts_are_explicit(self):
        questions = (ROOT / "docs/reboot/interview/question-bank.md").read_text()
        sessions = (ROOT / "docs/reboot/interview/deep-practice.md").read_text()
        self.assertEqual(36, len(re.findall(r"^\d+\. \*\*Q\d{2} —", questions, re.M)))
        self.assertEqual(12, len(re.findall(r"^\|\s*\d+\s*\|[^\n]*\| Q\d{2} \|", sessions, re.M)))
        design_guide = (ROOT / "docs/reboot/system-design-practice.md").read_text()
        self.assertIn("Before hints, reference answers, worked solutions, or AI answers", design_guide)

    def test_mock_format_fits_existing_engineering_block(self):
        mock = (ROOT / "docs/reboot/interview/mock-rubric.md").read_text()
        manifest = json.loads((ROOT / "docs/reboot/manifest.json").read_text())
        budget = manifest["daily_budget_minutes"]
        self.assertEqual(120, budget["engineering"])
        self.assertEqual(85, budget["engineering_build_test"])
        self.assertEqual(25, budget["concept_course"])
        self.assertEqual(10, budget["evidence"])
        self.assertIn("45 minutes inside the existing 85-minute practice allocation", mock)
        self.assertRegex(mock, r"5/5/15/10/10")

    def test_first_sprint_mock_days_and_dates(self):
        guide = (ROOT / "docs/reboot/system-design-practice.md").read_text()
        manifest = json.loads((ROOT / "docs/reboot/manifest.json").read_text())
        rows = {x["day"]: x for x in manifest["days"]}
        for day, expected_date, prompt in [
            (3, "2026-10-26", "Q08"),
            (7, "2026-10-30", "Q03"),
            (10, "2026-11-02", "Q13"),
            (14, "2026-11-06", "Q31"),
        ]:
            self.assertEqual(expected_date, rows[day]["date"])
            self.assertRegex(guide, rf"\|\s*{day}\s*\| {expected_date} \|.*\| {prompt} \|")

    def test_all_manifest_calendar_dates_shift_consistently(self):
        manifests = [ROOT / "docs/reboot/manifest.json"]
        manifests.extend(sorted((ROOT / "docs/reboot").glob("continuation-*/manifest.json")))
        days = {}
        for path in manifests:
            content = json.loads(path.read_text())
            for item in content.get("days", []):
                days[int(item["day"])] = item["date"]
        self.assertEqual(set(range(1, 197)), set(days))
        start = date(2026, 10, 24)
        for day, planned_date in days.items():
            self.assertEqual(start + timedelta(days=day - 1), date.fromisoformat(planned_date), day)

    def test_day_one_forecast_reviews_follow_the_new_planned_encounter(self):
        manifest = json.loads((ROOT / "docs/reboot/manifest.json").read_text())
        day_one = manifest["days"][0]
        self.assertEqual("2026-10-24", day_one["dsa_guided_encounter_date_forecast"])
        self.assertEqual(
            ["2026-10-27", "2026-10-31", "2026-11-14"],
            day_one["dsa_review_schedule"]["review_due_dates_if_attempted_on_plan"],
        )
        self.assertIsNone(day_one["dsa_review_schedule"]["first_attempt_date"])

    def test_first_sprint_story_ids_and_course_bridges_remain_present(self):
        owner = json.loads((ROOT / "docs/reboot/scrum-owner-manifest.json").read_text())
        courses = json.loads((ROOT / "docs/reboot/course-learning-blocks.json").read_text())
        cards = owner["cards"][:14]
        self.assertEqual([f"CS-DAY{i:02}" for i in range(1, 15)], [x["id"] for x in cards])
        self.assertEqual([f"2026-10-{day:02}" for day in range(24, 32)] + [
            "2026-11-01", "2026-11-02", "2026-11-03", "2026-11-04", "2026-11-05", "2026-11-06"
        ], [x["date"] for x in cards])
        self.assertEqual(list(range(1, 31)), [x["day"] for x in courses["days"]])
        self.assertTrue(all(x.get("verified_lessons_line") for x in courses["days"][:14]))


if __name__ == "__main__":
    unittest.main()
