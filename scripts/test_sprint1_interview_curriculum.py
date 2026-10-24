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
        numbers = [int(x) for x in re.findall(r"^(\d+)\. \*\*Q\d{2} —", questions, re.M)]
        ids = [int(x) for x in re.findall(r"^\d+\. \*\*Q(\d{2}) —", questions, re.M)]
        self.assertEqual(list(range(1, 37)), numbers)
        self.assertEqual(list(range(1, 37)), ids)
        self.assertEqual(12, len(re.findall(r"^\|\s*\d+\s*\|[^\n]*\| Q\d{2} \|", sessions, re.M)))
        self.assertEqual(36, len(re.findall(r'<a id="q\d{2}"></a>', questions)))
        design_guide = (ROOT / "docs/reboot/system-design-practice.md").read_text()
        self.assertIn("Before hints, reference answers, worked solutions, or AI answers", design_guide)

    def test_question_and_pattern_anchors_resolve(self):
        questions = (ROOT / "docs/reboot/interview/question-bank.md").read_text()
        patterns = (ROOT / "docs/reboot/interview/pattern-library.md").read_text()
        question_anchors = set(re.findall(r'<a id="([a-z0-9-]+)"></a>', questions))
        pattern_anchors = set(re.findall(r'<a id="([a-z0-9-]+)"></a>', patterns))
        for href in re.findall(r"\]\(([^)]+)\)", questions):
            if href.startswith("pattern-library.md#"):
                self.assertIn(href.split("#", 1)[1], pattern_anchors, href)
            elif href.startswith("question-bank.md#"):
                self.assertIn(href.split("#", 1)[1], question_anchors, href)
        for prompt_id in ("q03", "q05", "q08", "q13"):
            self.assertIn(prompt_id, question_anchors)

    def test_mock_format_fits_existing_engineering_block(self):
        mock = (ROOT / "docs/reboot/interview/mock-rubric.md").read_text()
        deep = (ROOT / "docs/reboot/interview/deep-practice.md").read_text()
        template = (ROOT / "docs/reboot/interview/design-proposal-template.md").read_text()
        design = (ROOT / "docs/reboot/system-design-practice.md").read_text()
        manifest = json.loads((ROOT / "docs/reboot/manifest.json").read_text())
        budget = manifest["daily_budget_minutes"]
        self.assertEqual(120, budget["engineering"])
        self.assertEqual(85, budget["engineering_build_test"])
        self.assertEqual(25, budget["concept_course"])
        self.assertEqual(10, budget["evidence"])
        self.assertIn("45 minutes inside the existing 85-minute practice allocation", mock)
        self.assertRegex(mock, r"5/5/15/10/10")
        phase_labels = ["Requirements", "Capacity and constraints",
                        "Initial architecture and data model",
                        "Bottlenecks, reliability, scaling",
                        "Trade-offs, failures, follow-ups"]
        for document in (mock, deep, template, design):
            normalized = re.sub(r"\s+", " ", document.lower())
            for label in phase_labels:
                self.assertIn(label.lower(), normalized)

    def test_first_sprint_mock_days_and_dates(self):
        guide = (ROOT / "docs/reboot/system-design-practice.md").read_text()
        manifest = json.loads((ROOT / "docs/reboot/manifest.json").read_text())
        rows = {x["day"]: x for x in manifest["days"]}
        for day, expected_date, prompt in [
            (3, "2026-10-26", "Q08"),
            (7, "2026-10-30", "Q03"),
            (10, "2026-11-02", "Q13"),
            (14, "2026-11-06", "Q05"),
        ]:
            self.assertEqual(expected_date, rows[day]["date"])
            self.assertRegex(guide, rf"\|\s*{day}\s*\| {expected_date} \|.*\| {prompt} \|")
        deep = (ROOT / "docs/reboot/interview/deep-practice.md").read_text()
        for day, expected_date, prompt in [
            (3, "2026-10-26", "Q08"),
            (7, "2026-10-30", "Q03"),
            (10, "2026-11-02", "Q13"),
            (14, "2026-11-06", "Q05"),
            (17, "2026-11-09", "Q01"),
            (21, "2026-11-13", "Q07"),
            (24, "2026-11-16", "Q10"),
            (28, "2026-11-20", "Q14"),
            (31, "2026-11-23", "Q18"),
            (35, "2026-11-27", "Q16"),
            (38, "2026-11-30", "Q28"),
            (42, "2026-12-04", "Q30"),
        ]:
            expected = date(2026, 10, 24) + timedelta(days=day - 1)
            self.assertEqual(expected_date, expected.isoformat())
            self.assertRegex(deep, rf"\|\s*\d+\s*\| {day} · {expected_date} \| {prompt} \|")

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

    def test_java_calendar_assertions_and_generator_follow_reshifted_schedule(self):
        java_expectations = {
            "src/test/java/assignments/reboot/Day30ContractTest.java": ["2026-10-24", "2026-11-22"],
            "src/test/java/assignments/reboot/ContinuationManifestInfrastructureTest.java": ["2026-11-23"],
            "src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java": ["2026-12-19"],
            "src/test/java/assignments/renewal/RenewalPreviewManifestInfrastructureTest.java": ["2026-12-07"],
            "src/test/java/assignments/renewal/Day71To84InfrastructureTest.java": ["2027-01-02"],
            "src/test/java/assignments/renewal/Day85To98InfrastructureTest.java": ["2027-01-16"],
            "src/test/java/assignments/delivery/Day99To112InfrastructureTest.java": ["2027-01-30"],
            "src/test/java/assignments/ops/Day141To154InfrastructureTest.java": ["LocalDate.of(2027,3,13)"],
        }
        for relative, dates in java_expectations.items():
            source = (ROOT / relative).read_text()
            for expected in dates:
                self.assertIn(expected, source, f"{relative} should assert {expected}")
        generator = (ROOT / "scripts/create_days141_154_docs.py").read_text()
        self.assertIn("dt=date(2027,3,13)+timedelta(days=i)", generator)
        self.assertIn("'start_date':'2027-03-13','end_date':'2027-03-26'", generator)
        self.assertIn("'dates':'2027-03-13..2027-03-26'", generator)
        self.assertIn("Dates: 2027-03-13–2027-03-26", generator)

    def test_first_sprint_story_ids_and_course_bridges_remain_present(self):
        owner = json.loads((ROOT / "docs/reboot/scrum-owner-manifest.json").read_text())
        canonical = json.loads((ROOT / "docs/reboot/manifest.json").read_text())
        courses = json.loads((ROOT / "docs/reboot/course-learning-blocks.json").read_text())
        cards = owner["cards"][:14]
        self.assertEqual([f"CS-DAY{i:02}" for i in range(1, 15)], [x["id"] for x in cards])
        self.assertEqual([f"2026-10-{day:02}" for day in range(24, 32)] + [
            "2026-11-01", "2026-11-02", "2026-11-03", "2026-11-04", "2026-11-05", "2026-11-06"
        ], [x["date"] for x in cards])
        self.assertEqual(list(range(1, 31)), [x["day"] for x in courses["days"]])
        self.assertTrue(all(x.get("verified_lessons_line") for x in courses["days"][:14]))
        dedicated = {3: "Q08", 7: "Q03", 10: "Q13", 14: "Q05"}
        for i, (card, day) in enumerate(zip(cards, canonical["days"][:14]), start=1):
            assignment = card["interview_practice"]
            self.assertEqual(assignment, day["interview_practice"])
            self.assertEqual(85, assignment["practice_minutes_available"])
            self.assertTrue(assignment["proposal_before_hints_reference_or_ai"])
            self.assertEqual(
                [x.get("source_item_id") for x in card["course_learning_lessons"]],
                [x.get("source_item_id") for x in assignment["course_bridges"]],
            )
            if i in dedicated:
                self.assertEqual("45_minute_mock", assignment["mode"])
                self.assertEqual(dedicated[i], assignment["question_id"])
                self.assertIsNotNone(assignment["deferred_work_id"])
                self.assertIn("40 minutes", assignment["implementation_scope"])
                self.assertEqual([
                    {"minutes": 5, "activity": "Requirements"},
                    {"minutes": 5, "activity": "Capacity and constraints"},
                    {"minutes": 15, "activity": "Initial architecture and data model"},
                    {"minutes": 10, "activity": "Bottlenecks, reliability, scaling"},
                    {"minutes": 10, "activity": "Trade-offs, failures, follow-ups"},
                ], assignment["mock_phases"])
            else:
                self.assertEqual("proposal_before_reference", assignment["mode"])
                self.assertIsNone(assignment.get("question_id"))
            self.assertFalse(assignment["acceptance"].endswith("complete."))

    def test_sprint2_assignments_sync_contracts_course_bridges_and_story_exports(self):
        canonical = json.loads((ROOT / "docs/reboot/manifest.json").read_text())
        owner = json.loads((ROOT / "docs/reboot/scrum-owner-manifest.json").read_text())
        courses = json.loads((ROOT / "docs/reboot/course-learning-blocks.json").read_text())
        cards = {card["day_number"]: card for card in owner["cards"]}
        deep = (ROOT / "docs/reboot/interview/deep-practice.md").read_text()
        scope = (ROOT / "docs/reboot/interview/sprint2-session-scope.md").read_text()
        expected_mocks = {17: ("2026-11-09", "Q01"), 21: ("2026-11-13", "Q07"),
                          24: ("2026-11-16", "Q10"), 28: ("2026-11-20", "Q14")}
        for day in range(15, 29):
            item = canonical["days"][day - 1]
            card = cards[day]
            assignment = item["interview_practice"]
            self.assertEqual(assignment, card["interview_practice"])
            self.assertEqual(85, assignment["practice_minutes_available"])
            self.assertTrue(assignment["proposal_before_hints_reference_or_ai"])
            expected_bridges = [{key: lesson.get(key) for key in (
                "title", "section_title", "duration_seconds", "source_item_id", "course_id")}
                for lesson in courses["days"][day - 1].get("lesson_details", [])]
            self.assertEqual(expected_bridges, assignment["course_bridges"], f"Day {day} course bridge")
            contract = (ROOT / f"docs/reboot/contracts/day-{day:02d}.md").read_text()
            self.assertIn("Interview-first", contract)
            self.assertNotIn("Interview transfer (inside the current evidence block)", contract)
            if day in expected_mocks:
                planned_date, question = expected_mocks[day]
                self.assertEqual(planned_date, item["date"])
                self.assertEqual("45_minute_mock", assignment["mode"])
                self.assertEqual(question, assignment["question_id"])
                self.assertTrue(assignment["deferred_work_id"].startswith("S2-D"))
                self.assertIn("40 minutes", assignment["implementation_scope"])
                self.assertEqual(85, assignment["duration_minutes"] + 40)
                self.assertEqual([5, 5, 15, 10, 10], [p["minutes"] for p in assignment["mock_phases"]])
                self.assertIn(assignment["deferred_work_id"], scope)
                self.assertRegex(deep, rf"\|\s*\d+\s*\| {day} · {planned_date} \| {question} \|")
            else:
                self.assertEqual("proposal_before_reference", assignment["mode"])
                self.assertEqual(5, assignment["duration_minutes"])
                self.assertIsNone(assignment["deferred_work_id"])
        self.assertNotIn("65 build/test + 20 mock", scope)
        self.assertIn("named 40-minute EAI slice", scope)

    def test_mock_scope_has_explicit_eai_slices_and_deferred_acceptance(self):
        scope = (ROOT / "docs/reboot/interview/session-scope.md").read_text()
        for marker in ("S1-D03-1", "S1-D07-1", "S1-D10-1", "S1-D14-1"):
            self.assertIn(marker, scope)
        self.assertIn("40-minute EAI slice", scope)
        self.assertIn("complete acceptance selector passes", scope)

    def test_rubric_is_weighted_to_100_across_requested_dimensions(self):
        rubric = (ROOT / "docs/reboot/interview/mock-rubric.md").read_text()
        weights = [int(x) for x in re.findall(r"^\| [^|]+ \| (\d+) \|", rubric, re.M)]
        self.assertEqual([15, 20, 20, 10, 15, 10, 10], weights)
        self.assertEqual(100, sum(weights))
        for dimension in ("Requirements and constraints", "Architecture and boundaries",
                          "Data consistency and correctness", "Scalability and capacity",
                          "Reliability and operations", "Trade-offs and alternatives",
                          "Communication and verification"):
            self.assertIn(dimension, rubric)


if __name__ == "__main__":
    unittest.main()
