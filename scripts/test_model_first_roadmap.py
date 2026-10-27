import json
import re
import subprocess
import sys
import unittest
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class ModelFirstRoadmapTests(unittest.TestCase):
    def test_calendar_budget_phase_arithmetic_and_gate_positions(self):
        manifest = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())
        start = date.fromisoformat(manifest["start_date"])
        self.assertEqual(date(2026, 10, 27), start)
        self.assertEqual(date(2028, 4, 26), start + timedelta(days=547))
        self.assertEqual("2028-04-26", manifest["end_date"])
        self.assertEqual(548, manifest["horizon_days"])
        budget = manifest["budget"]
        self.assertEqual(1378.75, budget["engineering_hours"])
        self.assertEqual(282.75, budget["shared_hours"])
        self.assertEqual(120, budget["days_1_to_171"]["engineering_minutes"])
        self.assertEqual(165, budget["days_172_to_548"]["engineering_minutes"])
        self.assertEqual(45, budget["days_172_to_548"]["shared_minutes"])
        phases = manifest["phases"]
        for first, second in zip(phases, phases[1:]):
            self.assertEqual(first["days"][1] + 1, second["days"][0])
        self.assertEqual([1, 61, 151, 231, 301, 366, 451, 521], [p["days"][0] for p in phases])
        self.assertEqual(["2026-10-27", "2026-12-25"], phases[0]["dates"])
        self.assertEqual(["2026-10-27", "2028-04-26"], [manifest["start_date"], manifest["end_date"]])
        self.assertEqual(["2027-05-25", "2027-10-26"], manifest["project_gates"][0]["target_dates"])
        milestone_dates = {item["study_day"]: item["date"] for item in manifest["milestone_reviews"] if item["study_day"] is not None}
        self.assertEqual({150:"2027-03-25",274:"2027-07-27",365:"2027-10-26",450:"2028-01-19",520:"2028-03-29",548:"2028-04-26"}, milestone_dates)
        self.assertEqual("2029-10-26", manifest["milestone_reviews"][-1]["date"])
        self.assertEqual(625.25, manifest["feasibility"]["nine_month_engineering_hours_by_day_274"])
        self.assertIn("one strong end-to-end coverstory", manifest["feasibility"]["nine_month_target"].lower())
        self.assertIn("no-go", phases[3]["exit"])
        self.assertFalse(any(day["learner_completion"] for day in manifest["detailed_days"]))

    def test_thirty_contracts_have_prerequisite_sources_commands_negative_controls_and_dates(self):
        manifest = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())
        days = manifest["detailed_days"]
        self.assertEqual(30, len(days))
        for index, day in enumerate(days, 1):
            self.assertEqual(index, day["day"])
            self.assertEqual(date(2026, 10, 27) + timedelta(days=index - 1), date.fromisoformat(day["date"]))
            for key in ("prerequisite", "source_title_or_path", "source", "acceptance_command", "acceptance", "negative_control", "evidence", "evidence_category"):
                self.assertTrue(day[key], (index, key))
            contract = ROOT / f"docs/model-first-roadmap/contracts/day-{index:02d}.md"
            self.assertTrue(contract.exists())
            self.assertIn(day["acceptance_command"].split()[-1].split(".")[-1], contract.read_text())
            if index > 1:
                completed = subprocess.run(day["acceptance_command"].split(), cwd=ROOT, capture_output=True, text=True)
                self.assertNotEqual(0, completed.returncode, f"Day {index} learner TODO should remain red")
                self.assertIn("NotImplementedError", completed.stderr)
        self.assertIn("logistic-regression", days[0]["learner_scope"].lower())
        self.assertNotIn("API call", days[0]["learner_scope"])
        self.assertTrue(all("code" in days[i]["negative_control"].lower() for i in (25, 26, 27)))

    def test_day1_learner_acceptance_is_red_only_at_explicit_todo(self):
        day = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())["detailed_days"][0]
        result = subprocess.run(day["acceptance_command"].split(), cwd=ROOT, capture_output=True, text=True)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("NotImplementedError: Day 1", result.stderr)

    def test_migration_is_305_unique_records_with_only_first30_active(self):
        migration = json.loads((ROOT / "docs/model-first-roadmap/legacy-issue-migration.json").read_text())
        self.assertEqual(30, migration["active_issue_count"])
        self.assertEqual(275, migration["superseded_issue_count"])
        self.assertEqual(305, migration["total_legacy_issue_count"])
        active_ids = [row["existing_issue_id"] for row in migration["active_mappings"]]
        superseded_ids = [row["existing_issue_id"] for row in migration["superseded_records"]]
        self.assertEqual([f"EAI-{n}" for n in range(211, 241)], active_ids)
        self.assertEqual(305, len(set(active_ids + superseded_ids)))
        self.assertEqual(30, len(set(active_ids)))
        for row in migration["active_mappings"]:
            self.assertTrue(row["legacy_definition_immutable"])
            self.assertEqual("Open", row["legacy_status_preserved"])
            self.assertFalse(row["learner_completion_credit"])
            self.assertEqual(64, len(row["legacy_definition_sha256"]))
        self.assertFalse(migration["board_writes"])
        self.assertEqual(0, migration["completion_count"])

    def test_dsa_external_pointer_and_model_only_scope(self):
        manifest = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())
        self.assertEqual("c2caee396002bb48b2a34d407545ecd96ecdb1ab", manifest["dsa_source"]["commit"])
        text = (ROOT / "docs/model-first-roadmap/README.md").read_text()
        self.assertIn("learner ui/controller/auth implementation", text.lower())
        self.assertIn("reads/traces that code", text)
        self.assertIn("Redis", text)
        self.assertIn("Kafka", text)
        self.assertIn("MLOps", text)
        self.assertIn("global or production scale", text)

    def test_read_only_application_answers_are_anchored_to_supplied_code(self):
        ui = (ROOT / "frontend/src/ai/AiFirstCoverStoryApp.tsx").read_text()
        api = (ROOT / "src/main/java/assignments/local/LocalAiAssistantController.java").read_text()
        fixture = (ROOT / "ai/coverstory_assistant/server.py").read_text()
        self.assertIn("/api/ai/policy-assistant", ui)
        self.assertIn("JSON.stringify({ question })", ui)
        self.assertIn("authentication.getName()", api)
        self.assertIn('"INVALID_QUESTION"', api)
        self.assertIn("HttpStatus.SERVICE_UNAVAILABLE", api)
        self.assertIn('"AI_PROVIDER_UNAVAILABLE"', api)
        self.assertIn('"offline_synthetic_fixture"', fixture)
        self.assertIn('"providerRequestMade": False', fixture)
        manifest = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())
        self.assertTrue(all(manifest["detailed_days"][day]["comprehension_questions"] for day in (25, 26, 27)))

    def test_generation_checks_and_local_markdown_links(self):
        for command in ([sys.executable, "scripts/generate_model_first_roadmap.py", "--check"],
                        [sys.executable, "scripts/generate_model_first_migration.py", "--check"]):
            subprocess.run(command, cwd=ROOT, check=True, capture_output=True, text=True)
        paths = [ROOT / "README.md", ROOT / "docs/model-first-roadmap/README.md"]
        paths.extend(sorted((ROOT / "docs/model-first-roadmap/contracts").glob("*.md")))
        for source in paths:
            for target in re.findall(r"\[[^\]]+\]\(([^)]+)\)", source.read_text()):
                if target.startswith(("https://", "http://", "#")):
                    continue
                resolved = (source.parent / target.split("#", 1)[0]).resolve()
                self.assertTrue(resolved.exists(), f"{source.relative_to(ROOT)} -> {target}")


if __name__ == "__main__":
    unittest.main()
