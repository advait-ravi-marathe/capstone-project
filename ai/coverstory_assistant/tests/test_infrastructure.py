import json
import re
import subprocess
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]


class AiFirstInfrastructureTests(unittest.TestCase):
    def test_day_index_calendar_budget_and_contract_files_are_complete(self):
        manifest = json.loads((ROOT / "docs/ai-first-roadmap/manifest.json").read_text())
        days = manifest["detailed_days"]
        self.assertEqual(30, len(days))
        self.assertEqual(list(range(1, 31)), [day["day"] for day in days])
        self.assertEqual("2026-10-27", days[0]["date"])
        self.assertEqual("2026-11-25", days[-1]["date"])
        self.assertTrue(all(day["minutes"] == 120 for day in days))
        self.assertEqual(875.5, manifest["budget"]["engineering_hours"])
        self.assertEqual(145.5, manifest["budget"]["shared_hours"])
        for day in days:
            contract = ROOT / "docs/ai-first-roadmap/contracts" / f"day-{day['day']:02d}.md"
            self.assertTrue(contract.is_file(), str(contract))
            self.assertTrue(day["prerequisite"].strip())
            self.assertTrue(day["negative_case"].strip())
            self.assertTrue(day["evidence"].strip())
            self.assertEqual(f"evidence/ai-first/day-{day['day']:02d}.json", day["evidence_artifact"])
            self.assertTrue(day["source"]["title"].strip())
            self.assertTrue(day["acceptance_command"].startswith("python3 -m unittest "))
            target = day["acceptance_command"].split("python3 -m unittest ", 1)[1].split()[0]
            parts = target.split(".")
            module = ROOT.joinpath(*parts).with_suffix(".py")
            if not module.is_file():
                module = ROOT.joinpath(*parts[:-1]).with_suffix(".py")
            self.assertTrue(module.is_file(), f"missing acceptance module for day {day['day']}: {target}")
        self.assertEqual(["scripts/package-local-demo.sh", "scripts/local-demo.sh"], days[-1]["demo_commands"])
        self.assertTrue((ROOT / days[-1]["demo_commands"][0]).is_file())
        self.assertTrue((ROOT / days[-1]["demo_commands"][1]).is_file())

    def test_no_frontend_backend_learner_implementation_days_26_to_28(self):
        manifest = json.loads((ROOT / "docs/ai-first-roadmap/manifest.json").read_text())
        days = manifest["detailed_days"][25:28]
        self.assertTrue(all(day["category"] == "comprehension_only" for day in days))
        self.assertTrue(all(day["learner_owned_file"].startswith("None;") for day in days))
        for path in (
            "src/main/java/assignments/local/LocalCoverStoryApplication.java",
            "frontend/src/local/LocalCoverStoryApp.tsx",
        ):
            self.assertTrue((ROOT / path).is_file())

    def test_generated_contracts_are_current(self):
        result = subprocess.run(
            ["python3", "scripts/generate_ai_first_contracts.py", "--check"],
            cwd=ROOT,
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        day29 = (ROOT / "docs/ai-first-roadmap/contracts/day-29.md").read_text()
        day30 = (ROOT / "docs/ai-first-roadmap/contracts/day-30.md").read_text()
        self.assertIn("run_offline_pipeline", day29)
        self.assertIn("foreign-tenant claim", day29)
        self.assertIn("scripts/local-demo.sh", day30)
        self.assertIn("no provider request", day30)

    def test_active_project_selection_preserves_three_flagships_and_shared_eval(self):
        manifest = json.loads((ROOT / "docs/ai-first-roadmap/manifest.json").read_text())
        self.assertEqual(
            ["coverstory-ai", "streamforge-ai", "developer-workflow-assistant", "shared-integration-closeout"],
            [project["id"] for project in manifest["projects"]],
        )
        self.assertFalse(any(project["id"] == "evaluation-workbench" for project in manifest["projects"]))
