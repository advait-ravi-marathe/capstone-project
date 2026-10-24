import datetime
import json
import pathlib
import re
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
BASE = ROOT / "docs/reboot/continuation-113-126"
EXPECTED_LESSONS = {
    115: ("Step 12 - Creating a custom Spring JDBC RowMapper", "Journey From Spring  JDBC to JPA", "7907420", 298),
    116: ("Step 11 - Implementing insert and update Spring JDBC Update Methods", "Journey From Spring  JDBC to JPA", "7907416", 519),
    120: ("Step 63 - Implementing Transaction Management - 3 Things to Decide", "Transaction Management", "7907620", 278),
    122: ("Step 62 - Choosing between Isolation Levels", "Transaction Management", "7907618", 243),
    124: ("Step 06 - Execute the findAll method using CommandLineRunner", "Journey From Spring  JDBC to JPA", "7907404", 345),
}

class ContinuationManifestTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads((BASE / "manifest.json").read_text())
        cls.export = json.loads((BASE / "scrum-owner-manifest.json").read_text())

    def test_day_date_budget_and_export_identity(self):
        self.assertEqual(list(range(113, 127)), [d["day"] for d in self.manifest["days"]])
        self.assertEqual(14, self.export["story_count"])
        self.assertEqual(14, len(self.export["cards"]))
        for offset, (day, card) in enumerate(zip(self.manifest["days"], self.export["cards"])):
            self.assertEqual(113 + offset, day["day"])
            self.assertEqual((datetime.date(2027, 2, 13) + datetime.timedelta(days=offset)).isoformat(), day["date"])
            self.assertEqual(day["day"], card["day_number"])
            self.assertEqual(day["date"], card["date"])
            self.assertEqual(day["title"], card["title"])
            self.assertEqual(day["engineering"], card["engineering"])
            self.assertEqual(120, day["engineering"]["budget_minutes"])
            self.assertEqual({"implementation_and_testing": 85, "concept_or_course": 25, "evidence": 10}, day["engineering"]["allocation_minutes"])
            study = day["engineering"]["study_seconds"]
            self.assertEqual(1500, study["total"])
            self.assertEqual(1500, study["video"] + study["worked_reference"] + study["teachback"])
            contract = ROOT / day["contract_file"]
            self.assertTrue(contract.is_file(), contract)
            text = contract.read_text()
            self.assertIn(day["title"], text)
            for command in day["engineering"]["acceptance_commands"]:
                self.assertIn(command, text)
            self.assertTrue((ROOT / day["engineering"]["evidence_reference"]).is_file())

    def test_exact_verified_course_records_only_on_matched_days(self):
        found = {}
        for day in self.manifest["days"]:
            lessons = day["engineering"]["concept_course"]["lessons"]
            if lessons:
                found[day["day"]] = lessons[0]
                self.assertEqual(1500, day["engineering"]["study_seconds"]["total"])
                course = day["engineering"]["concept_course"]["courses"][0]
                self.assertEqual("1341538", course["course_id"])
                self.assertEqual("https://www.udemy.com/course/hibernate-jpa-tutorial-for-beginners-in-100-steps/", course["url"])
                self.assertEqual("unconfirmed_reference_only", course["access_status"])
                md = (ROOT / day["contract_file"]).read_text()
                self.assertIn(lessons[0]["title"], md)
                self.assertIn(lessons[0]["section_title"], md)
        self.assertEqual(set(EXPECTED_LESSONS), set(found))
        for day, expected in EXPECTED_LESSONS.items():
            lesson = found[day]
            self.assertEqual(expected, (lesson["title"], lesson["section_title"], lesson["source_item_id"], lesson["duration_seconds"]))
            minutes, seconds = divmod(lesson["duration_seconds"], 60)
            self.assertEqual(f"{minutes}:{seconds:02d}", lesson["duration"])

    def test_recovery_days_only_run_prior_work_and_no_unresolved_commands(self):
        for day in (119, 126):
            text = (ROOT / f"docs/reboot/continuation-113-126/contracts/day-{day}.md").read_text()
            self.assertIn("Recovery/remediation only", text)
        for day in self.manifest["days"]:
            for command in day["engineering"]["acceptance_commands"]:
                self.assertNotIn("TODO", command)
                if command.startswith("(cd frontend &&"):
                    self.assertEqual("(cd frontend && npm ci && npm run build && npm run build:local)", command)
                else:
                    self.assertIn("./mvnw", command)

if __name__ == "__main__":
    unittest.main()
