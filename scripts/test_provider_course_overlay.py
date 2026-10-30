"""Guard the optional course overlay against schedule/credential drift."""

import json
import unittest
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
                    sum(slot["minutes"] for slot in course["study_slots"] if slot["part"].startswith("course study")),
                    course["catalog_estimate_minutes"],
                )
                self.assertEqual(
                    sum(slot["minutes"] for slot in course["study_slots"] if slot["part"].startswith("assessment")),
                    course["assessment_minutes"],
                )
                self.assertTrue(all(slot["minutes"] <= max_study_slot[course_id] for slot in course["study_slots"]))

                schedule = read_json(schedule_path)
                rows = {row["day"]: row for row in schedule["days"]}
                for slot in course["study_slots"]:
                    row = rows[slot["day"]]
                    self.assertEqual(row[ai_key], 90)
                    self.assertEqual(row.get("dsa", row.get("dsa_minutes")), 60)
                    self.assertEqual(row.get("role_aws", row.get("role_bank_minutes")), 30)
                    if "segments" in row and slot["stage"] != "M2.1":
                        self.assertIn(slot["stage"], {segment["stage"] for segment in row["segments"]})

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
        self.assertNotIn("Anthropic Academy", guide + readme)


if __name__ == "__main__":
    unittest.main()
