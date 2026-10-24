import json
import re
import unittest
from datetime import date, timedelta
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "docs/reboot/continuation-57-70/manifest.json"
OWNER = ROOT / "docs/reboot/continuation-57-70/scrum-owner-manifest.json"
CURRICULUM = ROOT / "docs/reboot/continuation-57-70/fde57-70-v2-curriculum.json"
PHASES = [
    ("Requirements", 5),
    ("Capacity and constraints", 5),
    ("Initial architecture and data model", 15),
    ("Bottlenecks, reliability, scaling", 10),
    ("Trade-offs, failures, follow-ups", 10),
]
MOCK_DAYS = {58, 60, 66, 69}


class Sprint5InterviewCurriculumTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads(MANIFEST.read_text())
        cls.owner = json.loads(OWNER.read_text())
        cls.curriculum = json.loads(CURRICULUM.read_text())
        cls.days = {row["day"]: row for row in cls.manifest["days"]}
        cls.cards = {row["day_number"]: row for row in cls.owner["cards"]}
        cls.assignments = {row["day"]: row for row in cls.curriculum["assignments"]}

    def test_all_days_have_synchronized_assignments(self):
        self.assertEqual(set(range(57, 71)), set(self.days))
        self.assertEqual(set(range(57, 71)), set(self.cards))
        self.assertEqual(set(range(57, 71)), set(self.assignments))
        for day in range(57, 71):
            assignment = self.assignments[day]
            self.assertEqual(assignment, self.days[day]["engineering"]["interview_assignment"])
            self.assertEqual(assignment, self.cards[day]["engineering"]["interview_assignment"])
            self.assertEqual(self.days[day]["date"], assignment["date"])
            self.assertEqual(self.days[day]["title"], assignment["title"])
            self.assertTrue(assignment["proposal_before_hints_reference_or_ai"])
            self.assertTrue(assignment["deferred_work_id"].startswith("S5-D"))

    def test_mock_cadence_phases_and_practice_budget_are_exact(self):
        self.assertEqual(MOCK_DAYS, set(self.curriculum["mock_days"]))
        self.assertEqual([58, 60], [d for d in sorted(MOCK_DAYS) if d <= 63])
        self.assertEqual([66, 69], [d for d in sorted(MOCK_DAYS) if d >= 64])
        for day in MOCK_DAYS:
            assignment = self.assignments[day]
            self.assertEqual("45_minute_mock", assignment["mode"])
            self.assertEqual(PHASES, [(p["phase"], p["minutes"]) for p in assignment["mock_phases"]])
            self.assertEqual(45, assignment["mock_minutes"])
            self.assertEqual(40, sum(assignment["eai_slice_minutes"].values()))
            self.assertEqual(85, assignment["practice_total_minutes"])
            self.assertEqual(45, assignment["duration_minutes"])
            self.assertTrue(assignment["question_href"].endswith("#" + assignment["question_id"].lower()))

    def test_non_mock_proposals_replace_five_minutes_inside_practice(self):
        for day, assignment in self.assignments.items():
            if day in MOCK_DAYS:
                continue
            self.assertEqual("5_minute_task_proposal", assignment["mode"])
            self.assertEqual(5, assignment["proposal_minutes"])
            self.assertEqual(80, assignment["existing_acceptance_minutes"])
            self.assertEqual(85, assignment["practice_total_minutes"])
            self.assertIn("before", assignment["proposal_prompt"].lower())
        d70 = self.assignments[70]
        self.assertIn("up to 50-minute first-blocker recovery", d70["scope_boundary"])

    def test_course_bridges_are_exact_saved_metadata_and_not_marked_watched(self):
        self.assertFalse(self.manifest.get("videos_watched", False))
        for day, assignment in self.assignments.items():
            saved = self.days[day]["course_learning"]
            self.assertEqual(saved.get("courses", []), assignment["course_catalog"])
            self.assertEqual(saved.get("lessons", []), assignment["course_bridges"])
            self.assertEqual(saved.get("resources", []), assignment["supporting_sources"])
            concept = self.days[day]["engineering"].get("concept_course", {})
            self.assertEqual(saved.get("courses", []), concept.get("courses", []))
            self.assertEqual(saved.get("lessons", []), concept.get("lessons", []))
            self.assertEqual(saved.get("resources", []), concept.get("resources", []))
            self.assertFalse(saved.get("videos_watched", False))

    def test_acceptance_commands_resolve_to_current_files_and_selectors(self):
        for day, assignment in self.assignments.items():
            for command in assignment["acceptance_commands"]:
                for java_class, method in re.findall(r"-Dtest=([A-Za-z0-9_]+)#([A-Za-z0-9_]+)", command):
                    matches = list((ROOT / "src/test/java").rglob(java_class + ".java"))
                    self.assertEqual(1, len(matches), f"Day {day}: missing/ambiguous {java_class}")
                    source = matches[0].read_text()
                    self.assertRegex(source, rf"\b(?:void|\w+\s+\w+)\s+{re.escape(method)}\s*\(",
                                     f"Day {day}: missing method {method}")
                if "npm --prefix frontend run " in command:
                    script = command.rsplit(" run ", 1)[1]
                    package = json.loads((ROOT / "frontend/package.json").read_text())
                    self.assertIn(script, package["scripts"], f"Day {day}: missing npm script {script}")
                if command.startswith("scripts/"):
                    self.assertTrue((ROOT / command.split()[0]).is_file(), f"Day {day}: missing script {command}")

    def test_question_and_pattern_anchors_exist(self):
        question_bank = (ROOT / "docs/reboot/interview/question-bank.md").read_text()
        patterns = (ROOT / "docs/reboot/interview/pattern-library.md").read_text()
        json_dir = CURRICULUM.parent
        for assignment in self.assignments.values():
            if assignment["mode"] != "45_minute_mock":
                continue
            qid = assignment["question_id"].lower()
            self.assertIn(f'<a id="{qid}"></a>', question_bank)
            question_path = assignment["question_href"].split("#", 1)[0]
            self.assertTrue((json_dir / question_path).resolve().is_file(), question_path)
            for ref in assignment["pattern_refs"]:
                anchor = ref.rsplit("#", 1)[1]
                self.assertIn(f'<a id="{anchor}"></a>', patterns)
                pattern_path = ref.split("#", 1)[0]
                self.assertTrue((json_dir / pattern_path).resolve().is_file(), pattern_path)

    def test_contracts_and_scope_cover_each_assignment_without_extra_hours(self):
        scope = (ROOT / "docs/reboot/interview/sprint5-session-scope.md").read_text()
        self.assertIn("19 December 2026–1 January 2027", scope)
        self.assertIn("45-minute interview mocks", scope)
        for day, assignment in self.assignments.items():
            contract = (ROOT / self.days[day]["contract_file"]).read_text()
            self.assertIn(assignment["deferred_work_id"], contract)
            if assignment.get("open_acceptance_gaps"):
                self.assertIn("## Selector limits and open evidence", contract)
                for gap in assignment["open_acceptance_gaps"]:
                    self.assertIn(gap, contract)
            if day in MOCK_DAYS:
                self.assertIn("Interview design mock (45 minutes", contract)
                self.assertIn("before opening hints, worked references, or AI", contract)
                self.assertIn("100-point rubric", contract)
            else:
                self.assertRegex(contract, r"## Learner-first (?:task|recovery) proposal \(5 minutes")
                self.assertTrue("before opening" in contract.lower() or "before reading" in contract.lower())
            for command in assignment["acceptance_commands"]:
                if "-Dtest=" in command:
                    method = command.split("#", 1)[1].split()[0]
                    self.assertIn(method, contract)

    def test_original_dates_and_engineering_budgets_are_preserved(self):
        start = date.fromisoformat(self.manifest["effective_start"])
        self.assertEqual(date(2026, 12, 19), start)
        self.assertEqual(date(2027, 1, 1), date.fromisoformat(self.manifest["effective_end"]))
        for day in range(57, 71):
            expected = (start + timedelta(days=day - 57)).isoformat()
            self.assertEqual(expected, self.days[day]["date"])
            budget = self.days[day]["engineering"]["allocation_minutes"]
            self.assertEqual({"implementation_and_testing": 85, "concept_or_course": 25, "evidence": 10}, budget)
            self.assertEqual("not_attempted", self.days[day]["learner_status"])
        self.assertEqual("not_attempted", self.days[57]["learner_status"])
        self.assertTrue(self.curriculum["status"].startswith("prospective_curriculum"))

    def test_day59_and_other_known_selector_limits_are_explicit(self):
        d59 = self.assignments[59]["acceptance"]
        self.assertIn("V001–V005", d59)
        self.assertIn("not the Day59 target-3 acceptance", d59)
        self.assertIn("does not alone prove", self.assignments[60]["acceptance"])
        self.assertIn("inspects source shape", self.assignments[67]["acceptance"])
        self.assertIn("does not establish", self.assignments[65]["acceptance"])

    def test_dsa_and_later_runtime_are_not_added_as_dependencies(self):
        text = json.dumps(self.curriculum).lower()
        self.assertNotIn("ai prerequisite", text)
        self.assertNotIn("streamforge implementation", text)
        self.assertIn("dsa-prep", self.manifest["separate_learning_repository"]["algorithm_practice"])


if __name__ == "__main__":
    unittest.main()
