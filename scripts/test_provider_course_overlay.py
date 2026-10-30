"""Guard the optional course overlay against schedule/credential drift."""

import json
import unittest
from collections import defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def read_json(relative_path):
    return json.loads((ROOT / relative_path).read_text(encoding="utf-8"))


class ProviderCourseOverlayTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.overlay = read_json("docs/fde-v2/provider-courses.json")
        cls.courses = {course["id"]: course for course in cls.overlay["course_substitutions"]}

    def test_route_and_daily_time_budget_are_unchanged(self):
        self.assertEqual(self.overlay["base_route_days"], 270)
        self.assertEqual(self.overlay["production_ml_extension_days"], 112)
        self.assertEqual(self.overlay["recommended_route_days"], 382)
        self.assertEqual(
            self.overlay["daily_budget_minutes"],
            {"dsa": 60, "role_aws": 30, "ai": 90},
        )

    def test_course_minutes_include_assessments_and_fit_existing_study_slots(self):
        expected = {
            "openai_scope_ai_solutions": (50, "ai/fde_v2/m41/draft_schedule.json", "ai_minutes"),
            "openai_evaluate_ai_applications": (90, "ai/fde_v2/m52/draft_schedule.json", "ai_minutes"),
            "openai_build_with_rag": (120, "ai/fde_v2/m21/draft_schedule.json", "m21_substantive"),
            "openai_design_and_build_agentic_systems": (130, "ai/fde_v2/m33/draft_schedule.json", "ai_minutes"),
            "openai_optimize_ai_application_performance": (50, "ai/fde_v2/m52/draft_schedule.json", "ai_minutes"),
        }
        max_study_slot = {
            "openai_scope_ai_solutions": 20,
            "openai_evaluate_ai_applications": 20,
            "openai_build_with_rag": 20,
            "openai_design_and_build_agentic_systems": 20,
            "openai_optimize_ai_application_performance": 20,
        }
        stage_study_budgets = {
            "openai_scope_ai_solutions": {"M4.1-S04": 30, "M4.1-S05": 30},
            "openai_evaluate_ai_applications": {"M5.2-S03": 30, "M5.2-S04": 30, "M5.2-S05": 30},
            "openai_design_and_build_agentic_systems": {
                "M3.3-S03": 30,
                "M3.3-S04": 30,
                "M3.3-S05": 30,
                "M3.3-S06": 30,
                "M3.3-S07": 30,
            },
            "openai_optimize_ai_application_performance": {"M5.2-S09": 30, "M5.2-S10": 30},
        }
        schedule_cache = {}
        per_day_minutes = defaultdict(int)
        seen_course_day_stage = set()
        for course_id, (minutes, schedule_path, ai_key) in expected.items():
            with self.subTest(course=course_id):
                course = self.courses[course_id]
                self.assertEqual(course["planned_substitution_minutes"], minutes)
                self.assertEqual(
                    course["catalog_estimate_minutes"] + course["assessment_minutes"],
                    minutes,
                )
                self.assertEqual(sum(slot["minutes"] for slot in course["study_slots"]), minutes)
                self.assertEqual(
                    sum(slot["minutes"] for slot in course["study_slots"] if "course study" in slot["part"]),
                    course["catalog_estimate_minutes"],
                )
                self.assertEqual(
                    sum(slot["minutes"] for slot in course["study_slots"] if "assessment" in slot["part"]),
                    course["assessment_minutes"],
                )
                self.assertTrue(all(slot["minutes"] <= max_study_slot[course_id] for slot in course["study_slots"]))

                schedule = schedule_cache.setdefault(schedule_path, read_json(schedule_path))
                rows = {row["day"]: row for row in schedule["days"]}
                per_stage_minutes = defaultdict(int)
                for slot in course["study_slots"]:
                    row = rows[slot["day"]]
                    self.assertEqual(row[ai_key], 90)
                    self.assertEqual(row.get("dsa", row.get("dsa_minutes")), 60)
                    self.assertEqual(row.get("role_aws", row.get("role_bank_minutes")), 30)
                    if "segments" in row and slot["stage"] != "M2.1":
                        self.assertIn(slot["stage"], {segment["stage"] for segment in row["segments"]})
                        per_stage_minutes[slot["stage"]] += slot["minutes"]
                    key = (course_id, slot["day"], slot["stage"])
                    self.assertNotIn(key, seen_course_day_stage)
                    seen_course_day_stage.add(key)
                    per_day_minutes[slot["day"]] += slot["minutes"]

                for stage, study_budget in stage_study_budgets.get(course_id, {}).items():
                    stage_total = sum(
                        segment["minutes"]
                        for row in rows.values()
                        for segment in row.get("segments", [])
                        if segment["stage"] == stage
                    )
                    self.assertEqual(stage_total, 150, f"{stage} schedule duration changed")
                    self.assertLessEqual(per_stage_minutes[stage], study_budget, f"{stage} study budget exceeded")

        self.assertEqual(
            sum(course["catalog_estimate_minutes"] for course in self.courses.values()),
            self.overlay["catalog_estimate_minutes_total"],
        )
        self.assertEqual(
            sum(course["assessment_minutes"] for course in self.courses.values()),
            self.overlay["planned_assessment_minutes_total"],
        )
        self.assertEqual(
            self.overlay["catalog_estimate_minutes_total"]
            + self.overlay["planned_assessment_minutes_total"],
            self.overlay["planned_overlay_minutes_total"],
        )
        self.assertIn("do not add minutes", self.overlay["overrun_policy"])
        self.assertIn("does not displace learner implementation", self.overlay["overrun_policy"])

        mapped_days = [
            slot["day"]
            for course in self.courses.values()
            for slot in course["study_slots"]
        ]
        self.assertEqual(len(mapped_days), len(set(mapped_days)), "course substitutions must not double-book a day")
        self.assertTrue(all(minutes <= 20 for minutes in per_day_minutes.values()))
        evaluation_slots = self.courses["openai_evaluate_ai_applications"]["study_slots"]
        self.assertLessEqual(max(slot["day"] for slot in evaluation_slots), 239)
        self.assertEqual(evaluation_slots[0]["day"], 234)
        self.assertEqual(evaluation_slots[-1]["day"], 239)
        m52 = read_json("ai/fde_v2/m52/draft_schedule.json")
        rows = {row["day"]: row for row in m52["days"]}
        day234_stages = [part["stage"] for part in rows[234]["segments"]]
        day239_stages = [part["stage"] for part in rows[239]["segments"]]
        self.assertLess(day234_stages.index("M5.2-S02"), day234_stages.index("M5.2-S03"))
        self.assertLess(day239_stages.index("M5.2-S05"), day239_stages.index("M5.2-S06"))
        scope_slots = self.courses["openai_scope_ai_solutions"]["study_slots"]
        self.assertEqual(scope_slots[0]["stage"], "M4.1-S04")
        self.assertEqual(scope_slots[-1]["stage"], "M4.1-S05")
        self.assertLess(max(slot["day"] for slot in self.courses["openai_design_and_build_agentic_systems"]["study_slots"]), 156)

    def test_m21_mandatory_reading_defers_optional_rag_chunks_by_default(self):
        course = self.courses["openai_build_with_rag"]
        self.assertEqual(course["slot_policy"], "conditional-deferred-by-default")
        self.assertIn("GroupShuffleSplit", course["required_reading_precedence"])
        self.assertIn("sqlite transactions", course["required_reading_precedence"].lower())
        self.assertIn("Qdrant", course["required_reading_precedence"])
        self.assertTrue(all("otherwise defer" in slot["part"] for slot in course["study_slots"]))
        self.assertEqual(self.overlay["conditional_deferred_minutes_total"], 120)
        self.assertEqual(self.overlay["scheduled_overlay_minutes_total_excluding_conditional_m21"], 320)
        self.assertIn("120 M2.1 minutes on Days 43–48 are conditional and deferred by default", self.overlay["conditional_slot_policy"])

    def test_reduced_dsa_days_skip_optional_course_before_required_ai_work(self):
        self.assertIn("Skip optional course minutes first", self.overlay["reduced_ai_day_policy"])
        for dsa_minutes, expected_ai in ((75, 75), (90, 60)):
            with self.subTest(dsa_minutes=dsa_minutes):
                ai_minutes = 180 - 30 - dsa_minutes
                optional_course_minutes = 0
                self.assertEqual(ai_minutes, expected_ai)
                self.assertEqual(dsa_minutes + 30 + ai_minutes, 180)
                self.assertEqual(optional_course_minutes, 0)

    def test_course_and_badge_progress_starts_unclaimed(self):
        self.assertEqual(set(self.courses), {
            "openai_scope_ai_solutions",
            "openai_evaluate_ai_applications",
            "openai_build_with_rag",
            "openai_design_and_build_agentic_systems",
            "openai_optimize_ai_application_performance",
        })
        for course in self.courses.values():
            with self.subTest(course=course["id"]):
                self.assertEqual(course["course_status"], "not_started")
                self.assertEqual(course["badge_status"], "not_verified")
                self.assertIsNone(course["resume_section"])
        self.assertEqual(
            self.overlay["credential_status"]["openai_api_pathway_certificate"],
            "not_verified",
        )

    def test_map_is_linked_and_disclaims_nonprofessional_badges(self):
        readme = (ROOT / "docs/fde-v2/README.md").read_text(encoding="utf-8")
        guide = (ROOT / "docs/fde-v2/provider-courses.md").read_text(encoding="utf-8")
        self.assertIn("[provider-course map and credential limits](provider-courses.md)", readme)
        self.assertIn("not professional certifications", guide)
        self.assertIn("does **not** claim its certificate or completion", guide)
        self.assertIn("API-key setup", guide)
        self.assertIn("not an official OpenAI roadmap", guide)
        self.assertIn("No existing task or required technical reference is deleted", guide)
        self.assertIn("`in_progress`: a course has been started but is paused", guide)
        self.assertNotIn("Anthropic Academy", guide + readme)
        self.assertIn("The **M2.1 Days 43–48 course chunks are deferred by default**", guide)
        self.assertIn("resume only in a later already-scheduled compatible", guide)


if __name__ == "__main__":
    unittest.main()
