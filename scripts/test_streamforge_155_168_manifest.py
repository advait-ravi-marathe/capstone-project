"""Integrity checks for the unpublished StreamForge Days 155–168 starter plan."""
from __future__ import annotations

import csv
import json
import unittest
from datetime import date, timedelta
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
BATCH = ROOT / "docs/reboot/continuation-155-168"


class StreamForge155168ManifestTest(unittest.TestCase):
    APPROVED_FDE_README_SUFFIX = (
        b"FDE alignment: See [the North Star amendment]"
        b"(../fde-north-star-amendment.md) and [reference-only alignment overlay]"
        b"(../fde-alignment.json). Existing curriculum, prerequisites, budgets, "
        b"DSA history and learner status remain unchanged; this adds purpose and "
        b"evidence boundaries, not completion credit.\n"
    )

    @classmethod
    def _has_exact_readme_extension(cls, actual: bytes, original: bytes) -> bool:
        return actual == original + cls.APPROVED_FDE_README_SUFFIX

    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads((BATCH / "manifest.json").read_text(encoding="utf-8"))
        cls.owner = json.loads((BATCH / "scrum-owner-manifest.json").read_text(encoding="utf-8"))
        cls.plan = json.loads((BATCH / "transfer-plan.json").read_text(encoding="utf-8"))

    def test_fourteen_dates_and_recovery_days_are_bounded(self):
        days = self.manifest["days"]
        self.assertEqual(list(range(155, 169)), [row["day"] for row in days])
        start = date(2027, 3, 27)
        self.assertEqual([start + timedelta(days=i) for i in range(14)], [date.fromisoformat(row["date"]) for row in days])
        self.assertEqual([161, 168], [row["day"] for row in days if row["recovery_day"]])
        self.assertEqual(14, len(self.owner["cards"]))
        self.assertFalse(self.manifest["board_writes"])
        self.assertFalse(self.owner["board_writes"])
        self.assertTrue(all(row["board_ticket_id"] is None and row["status"] == "draft_not_board_published"
                            for row in self.owner["cards"]))

    def test_contract_manifest_owner_and_transfer_plan_selectors_match(self):
        owner_by_day = {row["day_number"]: row for row in self.owner["cards"]}
        plan_by_day = {row["day"]: row for row in self.plan["daily_contracts"]}
        for daily in self.manifest["days"]:
            day = daily["day"]
            contract = (BATCH / daily["contract_file"]).read_text(encoding="utf-8")
            command = daily["engineering"]["acceptance_commands"][0]
            owner = owner_by_day[day]
            self.assertEqual(command, owner["engineering"]["acceptance_commands"][0])
            self.assertEqual(command, plan_by_day[day]["selector"])
            self.assertIn(command, contract)
            self.assertEqual(daily["engineering"], owner["engineering"])
            self.assertEqual(daily["java_dsa"], owner["java_dsa"])

    def test_daily_time_and_dsa_policy_remain_tiered_without_new_anchors(self):
        for daily in self.manifest["days"]:
            self.assertEqual(120, daily["engineering"]["budget_minutes"])
            self.assertEqual({"concept_or_course": 25, "implementation_and_testing": 85, "evidence": 10},
                             daily["engineering"]["allocation_minutes"])
            self.assertEqual(1500, daily["engineering"]["study_total_seconds"])
            self.assertEqual(0, daily["engineering"]["video_seconds"])
            self.assertFalse(daily["java_dsa"]["new_anchor_required"])
            self.assertIn("--on-date \"$(TZ=Asia/Kolkata date +%F)\"", daily["java_dsa"]["queue_command"])
            self.assertEqual({"1_or_2_substantive_exercises": 60, "3_substantive_exercises": 75,
                              "4_substantive_exercises": 90}, daily["java_dsa"]["budget_minutes_by_exercise_count"])
        self.assertEqual(120, self.manifest["engineering_minutes_per_day"])

    def test_catalog_has_only_official_docs_and_no_invented_python_course(self):
        catalog = json.loads((BATCH / "verified-data-foundation-155-168-catalog.json").read_text(encoding="utf-8"))
        self.assertFalse(catalog["exact_python_lessons_verified"] if "exact_python_lessons_verified" in catalog else False)
        self.assertIn("No exact Python", catalog["preferred_course_gap"])
        self.assertTrue(all(url.startswith(("https://docs.python.org/3.12/", "https://scikit-learn.org/"))
                            for url in [x["url"] for x in catalog["official_sources"]]))
        self.assertEqual("Python 3.12.14", self.manifest["python_runtime_observed"])
        self.assertEqual([], self.manifest["third_party_python_dependencies"])
        self.assertIn("https://docs.python.org/3.12/tutorial/errors.html#handling-exceptions",
                      [row["url"] for row in catalog["official_sources"]])

    def test_day155_preserves_timestamp_strings_and_day167_is_prior_only(self):
        day155 = next(row for row in self.manifest["days"] if row["day"] == 155)
        day167 = next(row for row in self.manifest["days"] if row["day"] == 167)
        self.assertIn("without parsing UTC strings", day155["engineering"]["scope"])
        self.assertEqual("prior-only-integration-verification", day167["engineering"]["learner_seam"])
        contract155 = (BATCH / day155["contract_file"]).read_text(encoding="utf-8")
        contract167 = (BATCH / day167["contract_file"]).read_text(encoding="utf-8")
        self.assertIn("Handling Exceptions", contract155)
        self.assertIn("readiness bridge", contract155)
        command = "python3 -m ai.streamforge.readiness_bridge"
        self.assertEqual(command, day155["engineering"]["readiness_command"])
        self.assertIn(command, day155["engineering"]["acceptance_commands"])
        owner = json.loads((BATCH / "scrum-owner-manifest.json").read_text(encoding="utf-8"))
        owner155 = next(card for card in owner["cards"] if card["day_number"] == 155)
        self.assertEqual(command, owner155["engineering"]["readiness_command"])
        self.assertIn(command, owner155["engineering"]["acceptance_commands"])
        plan = json.loads((BATCH / "transfer-plan.json").read_text(encoding="utf-8"))
        plan155 = next(row for row in plan["daily_contracts"] if row["day"] == 155)
        self.assertEqual(command, plan155["readiness_command"])
        self.assertIn("25-minute study block", plan155["readiness_gate"])
        self.assertIn("no new learner function", contract167)

    def test_four_kib_field_limit_is_utf8_bytes_not_characters(self):
        self.assertEqual(4096, self.manifest["fixture"]["max_field_bytes"])
        self.assertNotIn("max_field_chars", self.manifest["fixture"])
        original_readme = (BATCH / "streamforge-days-155-168-transfer-plan.md").read_bytes()
        updated_readme = (BATCH / "README.md").read_bytes()
        self.assertTrue(self._has_exact_readme_extension(updated_readme, original_readme))
        self.assertFalse(self._has_exact_readme_extension(
            updated_readme, original_readme + b"\nmodified original content"))
        self.assertFalse(self._has_exact_readme_extension(
            updated_readme + b"unexpected suffix", original_readme))
        self.assertEqual((BATCH / "transfer-plan.json").read_bytes(),
                         (BATCH / "streamforge-days-155-168-transfer-plan.json").read_bytes())

    def test_fixture_is_small_synthetic_and_has_temporal_boundary_examples(self):
        fixture = ROOT / "ai/streamforge/fixtures/basic"
        manifest = json.loads((fixture / "manifest.json").read_text(encoding="utf-8"))
        with (fixture / manifest["catalog_file"]).open(newline="", encoding="utf-8") as stream:
            catalog = list(csv.DictReader(stream))
        with (fixture / manifest["events_file"]).open(newline="", encoding="utf-8") as stream:
            events = list(csv.DictReader(stream))
        self.assertEqual("synthetic_hand_authored", manifest["source_kind"])
        self.assertEqual(30, len(catalog))
        self.assertEqual(200, len(events))
        self.assertIn(manifest["cutoff_utc"], [event["occurred_at_utc"] for event in events])
        self.assertTrue(any(event["occurred_at_utc"] >= manifest["window_end_utc"] for event in events))
        self.assertTrue(all(int(row["duration_s"]) in range(1, 7201) for row in catalog))
        self.assertTrue(all(0 <= int(row["watch_seconds"]) <= int(catalog[int(row["media_id"][-3:])-1]["duration_s"])
                            for row in events))

    def test_streamforge_scope_excludes_prior_cap44_and_cloud_work(self):
        readme = (BATCH / "README.md").read_text(encoding="utf-8").lower()
        self.assertIn("not a recommender", readme)
        self.assertIn("no aws/cloud", readme)
        self.assertIn("cap44", readme)
        self.assertIn("no arbitrary paths or urls", readme)
        self.assertEqual("5dc558eeda441177dc4aac87725d0a18417c5abd", self.manifest["base_commit"])


if __name__ == "__main__":
    unittest.main()
