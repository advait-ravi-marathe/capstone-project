"""Regression checks for the stable-unit/calendar-study-day migration."""
import json
import subprocess
import sys
import unittest
from datetime import date
from pathlib import Path

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


if __name__ == "__main__":
    unittest.main()
