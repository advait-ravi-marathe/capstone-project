import json
import subprocess
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class AiFirstMigrationTests(unittest.TestCase):
    def test_305_ids_are_accounted_once_without_completion_credit(self):
        migration = json.loads((ROOT / "docs/ai-first-roadmap/legacy-issue-migration.json").read_text())
        active = migration["active_mappings"]
        superseded = migration["superseded_records"]
        all_records = active + superseded
        ids = [row["existing_issue_id"] for row in all_records]
        self.assertEqual((30, 275, 305), (len(active), len(superseded), migration["total_legacy_issue_count"]))
        self.assertEqual(305, len(set(ids)))
        self.assertEqual(305, len([issue for issue in ids if issue.startswith("EAI-")]))
        self.assertTrue(all(row["completion_credit"] is False for row in all_records))
        self.assertTrue(all(row["legacy_definition_sha256"] and row["legacy_definition_immutable"] for row in active))
        self.assertTrue(all(row["disposition"] == "superseded_not_deleted" for row in superseded))
        self.assertFalse(migration["board_writes"])
        self.assertFalse(migration["dsa_repository_writes"])
        self.assertEqual("a92877e4ade6033eeea31f4dd774808300cfea6219321846f88f6b0706da119b", migration["legacy_source_sha256"])

    def test_native_gap_issue_ids_285_through_305_are_explicitly_disposed(self):
        migration = json.loads((ROOT / "docs/ai-first-roadmap/legacy-issue-migration.json").read_text())
        superseded = {row["existing_issue_id"]: row for row in migration["superseded_records"]}
        for issue_number in range(285, 306):
            row = superseded[f"EAI-{issue_number}"]
            self.assertTrue(row["legacy_title"])
            self.assertRegex(row["legacy_definition_sha256"], r"^[0-9a-f]{64}$")

    def test_active_cards_reference_dsa_days_without_copying_content(self):
        migration = json.loads((ROOT / "docs/ai-first-roadmap/legacy-issue-migration.json").read_text())
        for day, row in enumerate(migration["active_mappings"], 1):
            self.assertEqual(day, row["dsa_pointer"]["dsa_calendar_day"])
            self.assertEqual("https://github.com/advait-ravi-marathe/dsa-prep", row["dsa_pointer"]["repository"])
            self.assertEqual("c2caee396002bb48b2a34d407545ecd96ecdb1ab", row["dsa_pointer"]["source_commit"])
            self.assertFalse(row["dsa_pointer"]["copied_content"])
            self.assertFalse(row["dsa_pointer"]["solutions_copied"])

    def test_migration_generator_is_current(self):
        result = subprocess.run(["python3", "scripts/generate_ai_first_migration.py", "--check"], cwd=ROOT, capture_output=True, text=True)
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
