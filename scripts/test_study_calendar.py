"""Regression checks for the stable-unit/calendar-study-day migration."""
import json
import re
import subprocess
import sys
import unittest
from datetime import date
from pathlib import Path
from scripts import generate_study_calendar

ROOT = Path(__file__).resolve().parents[1]
CALENDAR = ROOT / "docs/reboot/calendar/study-calendar.json"
EXPORT = ROOT / "docs/reboot/calendar/story-export-1-305.json"


class StudyCalendarMigrationTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.calendar = json.loads(CALENDAR.read_text())
        cls.export = json.loads(EXPORT.read_text())

    def test_generator_matches_checked_in_crosswalk_and_export(self):
        subprocess.run([sys.executable, "scripts/generate_study_calendar.py", "--check"], cwd=ROOT, check=True)
        subprocess.run([sys.executable, "scripts/generate_gap_assignments.py", "--check"], cwd=ROOT, check=True)

    def test_exact_dates_insertions_and_horizon(self):
        self.assertEqual("2026-10-26", self.calendar["start_date"])
        self.assertEqual((732, 42, 774), (self.calendar["legacy_horizon"], self.calendar["inserted_study_days"], self.calendar["calendar_horizon"]))
        self.assertEqual("2028-12-07", self.calendar["end_date"])
        rows = self.calendar["calendar"]
        self.assertEqual(774, len(rows))
        self.assertEqual("2027-04-15", self.calendar["budget"]["calendar_day_172_date"])
        self.assertEqual("GAP-OPENAPI-01", rows[70]["unit_id"])
        self.assertEqual("GAP-OIDC-09", rows[83]["unit_id"])
        self.assertEqual("GAP-SCHEMA-07", rows[174]["unit_id"])
        self.assertEqual("GAP-AIGOV-07", rows[465]["unit_id"])
        self.assertEqual("GAP-ENTERPRISE-07", rows[563]["unit_id"])
        self.assertEqual("EAI-UNIT-732", rows[773]["unit_id"])
        self.assertEqual("2028-12-07", rows[773]["date"])

    def test_dsa_references_follow_calendar_and_remain_external(self):
        rows = self.calendar["calendar"]
        self.assertEqual((1, "daily_problem_day"), (rows[0]["dsa_calendar_reference"], rows[0]["dsa_reference_kind"]))
        self.assertEqual((150, "daily_problem_day"), (rows[149]["dsa_calendar_reference"], rows[149]["dsa_reference_kind"]))
        self.assertEqual((151, "attempt_relative_review_window_day"), (rows[150]["dsa_calendar_reference"], rows[150]["dsa_reference_kind"]))
        self.assertEqual((171, "attempt_relative_review_window_day"), (rows[170]["dsa_calendar_reference"], rows[170]["dsa_reference_kind"]))
        self.assertIsNone(rows[171]["dsa_calendar_reference"])
        self.assertTrue(self.calendar["dsa_boundary"]["actual_attempt_dates_and_evidence_mutated"] is False)
        self.assertFalse(any(p.name.startswith("dsa") for p in (ROOT / "docs").iterdir()))

    def test_305_story_crosswalk_keeps_ids_and_sprint_history(self):
        stories = self.export["stories"]
        self.assertEqual(305, self.export["story_count"])
        self.assertEqual(list(range(1, 306)), [row["calendar_study_day"] for row in stories])
        existing = [row for row in stories if row["legacy_engineering_unit_number"] is not None]
        self.assertEqual(list(range(1, 285)), [row["legacy_engineering_unit_number"] for row in existing])
        for row in existing:
            unit = row["legacy_engineering_unit_number"]
            expected_day = unit if unit <= 70 else unit + (14 if unit <= 154 else 21 if unit <= 438 else 28 if unit <= 522 else 42)
            self.assertEqual(expected_day, row["calendar_study_day"])
            self.assertEqual(row["calendar_study_day"], row["owner_card"]["calendar_study_day"])
            self.assertEqual(unit, row["owner_card"]["engineering_unit_number"])
            self.assertEqual(row["date"], row["owner_card"]["date"])
            self.assertEqual(row["calendar_sprint"], row["sprint_membership_migration"]["calendar_sprint_number"])
            if 71 <= unit <= 196:
                provenance = json.loads((ROOT / "docs/reboot/calendar/legacy-sprint-provenance.json").read_text())
                self.assertEqual(provenance["unit_sprint_labels"][str(unit)], row["owner_card"]["legacy_sprint_label"])
            if row["calendar_study_day"] <= 150:
                self.assertEqual(row["calendar_study_day"], row["dsa_section"].get("dsa_calendar_day"))
            elif row["calendar_study_day"] <= 171:
                self.assertEqual("attempt_relative_review_window", row["dsa_section"]["mode"])
            else:
                self.assertEqual("none", row["dsa_section"]["mode"])
        sprints = self.calendar["sprint_migration"]
        self.assertEqual((305, 21, 22, 11), (sprints["detailed_calendar_story_count"], sprints["full_calendar_sprints"], sprints["calendar_sprint_count"], sprints["final_partial_sprint_days"]))
        self.assertFalse(self.export["board_writes"])
        self.assertFalse(self.export["new_issue_ids_assigned"])

    def test_legacy_sprint_provenance_is_pinned_to_immutable_preamendment_owner_exports(self):
        provenance = json.loads((ROOT / "docs/reboot/calendar/legacy-sprint-provenance.json").read_text())
        self.assertEqual("4a07a57e707d2d1be68f4263d1cbac1e391886c4", provenance["source_commit"])
        self.assertEqual({str(unit) for unit in range(71, 197)}, set(provenance["unit_sprint_labels"]))
        self.assertEqual({f"Sprint {(unit - 1) // 14 + 1}" for unit in range(71, 197)},
                         set(provenance["unit_sprint_labels"].values()))

    def test_new_insertions_have_executable_contract_owner_command_and_source(self):
        inserted = [row for row in self.export["stories"] if row["unit_id"].startswith("GAP-")]
        self.assertEqual(21, len(inserted))
        for story in inserted:
            self.assertTrue(story["contract_path"])
            self.assertTrue((ROOT / story["contract_path"]).is_file())
            self.assertIsInstance(story["owner_card"], dict)
            self.assertTrue(story["command"].startswith("./mvnw "))
            self.assertIn("section", story["source"])
            self.assertEqual("planned_assignment_not_attempted", story["daily_contract_status"])
            self.assertFalse(story["owner_card"]["learner_completion_awarded"])
            for command in story["command"].split(" && "):
                match = re.search(r"-Dtest=([A-Za-z0-9_,]+)(?:#([A-Za-z0-9_]+))?\s+test", command)
                self.assertIsNotNone(match, f"command has an unrecognized Maven test selector: {command}")
                for selector in match.group(1).split(","):
                    class_file = next((ROOT / "src/test/java").rglob(f"{selector}.java"), None)
                    self.assertIsNotNone(class_file, f"missing test class for owner command: {selector}")
                    if match.group(2):
                        self.assertRegex(class_file.read_text(), rf"\b{re.escape(match.group(2))}\s*\(",
                                         f"missing test method for owner command: {selector}#{match.group(2)}")
            if story["calendar_study_day"] >= 172:
                shared = story["owner_card"]["shared_interview_block"]
                self.assertEqual(45, shared["minutes"])
                self.assertEqual("separate_system_design_question_bank_session", shared["mode"])
                self.assertFalse(shared["inside_engineering_budget"])
            else:
                self.assertNotIn("shared_interview_block", story["owner_card"])
        insertions = json.loads((ROOT / "docs/reboot/calendar/detailed-insertions.json").read_text())["units"]
        self.assertEqual(21, len(insertions))
        self.assertEqual(list(range(71, 85)) + list(range(169, 176)),
                         [unit["calendar_study_day"] for unit in insertions])
        plan = json.loads((ROOT / "docs/reboot/calendar/insertion-plan.json").read_text())
        self.assertEqual(21, len(plan["detailed_inserted_units"]))
        for unit in plan["detailed_inserted_units"]:
            self.assertIn("command", unit)
            self.assertIn("source", unit)
            self.assertIn("acceptance", unit)
            self.assertTrue((ROOT / unit["contract_path"]).is_file())
        later = plan["later_outcome_acceptance"]
        for key in ("GAP-AIGOV", "GAP-IAC", "GAP-ENTERPRISE"):
            self.assertTrue(later[key]["outcomes"])
            self.assertTrue(later[key]["sources"])
            self.assertTrue(all(source["status"] == "official_heading_verified" for source in later[key]["sources"]))

    def test_role_dates_are_evidence_gated_and_dashboard_is_repo_source(self):
        dashboard = json.loads((ROOT / "docs/reboot/fde-alignment.json").read_text())
        milestones = dashboard["calendar_amendment"]["role_review_milestones"]
        self.assertEqual([(175, "2027-04-18"), (459, "2028-01-27"), (550, "2028-04-27"),
                          (648, "2028-08-03"), (718, "2028-10-12"), (774, "2028-12-07"),
                          (None, "2029-10-26")],
                         [(m["calendar_study_day"], m["date"]) for m in milestones])
        self.assertTrue(dashboard["calendar_amendment"]["application_may_begin_earlier"])
        self.assertFalse(dashboard["calendar_amendment"]["calendar_completion_grants_readiness"])
        self.assertEqual("2029-10-26", dashboard["calendar_amendment"]["professional_ownership_review_date"])
        self.assertEqual("published_planning_assignments_not_learner_completion", dashboard["new_batch_status"]["days197_to270"])
        self.assertFalse(dashboard["new_batch_status"]["learner_completion_or_eligibility_granted"])

    def test_days_155_through_171_cross_the_budget_boundary_without_changing_day_172(self):
        manifests = [ROOT / "docs/reboot/manifest.json", *sorted((ROOT / "docs/reboot").glob("continuation-*/manifest.json"))]
        by_unit = {}
        for path in manifests:
            for row in json.loads(path.read_text()).get("days", []):
                if isinstance(row.get("day"), int):
                    by_unit[row["day"]] = row
        self.assertEqual(set(range(155, 172)), set(range(155, 172)) & set(by_unit))
        self.assertEqual("2027-04-15", self.calendar["budget"]["calendar_day_172_date"])
        for unit in range(155, 172):
            row = by_unit[unit]
            expected_day = unit + 21
            self.assertEqual(expected_day, row["calendar_study_day"])
            self.assertEqual(165, row["engineering"]["budget_minutes"])
            self.assertEqual(165, sum(row["engineering"]["allocation_minutes"].values()))
            self.assertEqual(45, row["shared_interview_block"]["minutes"])
            self.assertEqual("separate_system_design_question_bank_session", row["shared_interview_block"]["mode"])
            self.assertEqual(45, row["calendar_transition_task"]["additional_minutes"])
            self.assertFalse(row["calendar_transition_task"]["new_feature_scope"])

    def test_shared_question_ids_match_every_migrated_contract_and_owner_card(self):
        cards = {}
        for path in [ROOT / "docs/reboot/scrum-owner-manifest.json", *sorted((ROOT / "docs/reboot").glob("continuation-*/scrum-owner-manifest.json"))]:
            for card in json.loads(path.read_text()).get("cards", []):
                unit = card.get("engineering_unit_number", card.get("legacy_engineering_unit_number", card.get("day_number")))
                if isinstance(unit, int) and 155 <= unit <= 171:
                    cards[unit] = card
        self.assertEqual(set(range(155, 172)), set(cards))
        for unit, card in cards.items():
            contract = next((path for path in (ROOT / "docs/reboot").glob(f"continuation-*/contracts/day-{unit}.md")), None)
            self.assertIsNotNone(contract)
            expected = re.search(r"question-bank\.md#q(\d+)", contract.read_text(), re.I)
            self.assertIsNotNone(expected)
            active = card.get("shared_interview_block") or {}
            self.assertEqual(f"Q{int(expected.group(1)):02}", active.get("question_id"), f"owner card QID for unit {unit}")
            row = next(item for item in self.export["stories"] if item.get("legacy_engineering_unit_number") == unit)
            self.assertEqual(active.get("question_id"), row["owner_card"].get("shared_interview_block", {}).get("question_id"))

    def test_rendered_contracts_follow_calendar_even_with_anchor_first(self):
        for story in self.export["stories"]:
            unit = story["legacy_engineering_unit_number"]
            contract = story["contract_path"]
            if unit is None or unit < 71 or not contract:
                continue
            text = (ROOT / contract).read_text()
            heading = next((line for line in text.splitlines() if line.startswith("# ")), "")
            self.assertIn(f"Calendar study day {story['calendar_study_day']}", heading, contract)
            self.assertIn(story["date"], heading, contract)
            if 155 <= unit <= 171:
                self.assertIn("165 engineering minutes (25 study, 130 implementation/testing, 10 evidence)", text, contract)
                self.assertIn("## Practice — 130 engineering minutes", text, contract)
                self.assertIn("## Shared 45-minute question-bank block", text, contract)
                self.assertNotIn("120 engineering minutes", text, contract)
                self.assertNotIn("inside the 85-minute practice block", text, contract)

    def test_migration_keeps_historical_sprint_and_title_across_reruns(self):
        sample = {
            "day_number": 150,
            "date": generate_study_calendar.current_date(164),
            "title": "Gate fallback on measured pair evidence",
            "sprint": "Sprint 12",
            "legacy_sprint_label": "Sprint 11",
            "engineering": {"budget_minutes": 120, "allocation_minutes": {
                "concept_or_course": 25, "implementation_and_testing": 85, "evidence": 10
            }},
        }
        first, _ = generate_study_calendar.migrate_assignment_json("fixture.json", sample)
        second, _ = generate_study_calendar.migrate_assignment_json("fixture.json", first)
        self.assertEqual("Sprint 11", first["legacy_sprint_label"])
        self.assertEqual("Sprint 11", second["legacy_sprint_label"])
        self.assertEqual("Gate fallback on measured pair evidence", first["title"])
        self.assertEqual(first["title"], second["title"])


if __name__ == "__main__":
    unittest.main()
