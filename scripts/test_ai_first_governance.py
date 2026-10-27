import hashlib
import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class AiFirstGovernanceTests(unittest.TestCase):
    def test_active_entrypoints_and_legacy_archive_are_unambiguous(self):
        root_readme = (ROOT / "README.md").read_text()
        reboot_readme = (ROOT / "docs/reboot/README.md").read_text()
        self.assertIn("docs/model-first-roadmap/README.md", root_readme)
        self.assertIn("Day 1 is", root_readme)
        self.assertTrue(reboot_readme.startswith("> **Historical sequence notice"))
        self.assertIn("../model-first-roadmap/README.md", reboot_readme)
        self.assertTrue((ROOT / "docs/ai-first-roadmap/README.md").read_text().startswith("# Preserved API-first candidate (superseded)"))
        snapshots = {
            "docs/ai-first-roadmap/archive/root-README-8d27f57.md": "e568f75663faf48391070b3969901e5251b8da2dcff350a7a3aed85730640b78",
            "docs/ai-first-roadmap/archive/reboot-README-8d27f57.md": "3206d0dd2939371b9e6d5900cadfdc77d2831dcfee3fbbd0e0166526d4ff0fb6",
            "docs/ai-first-roadmap/archive/fde-alignment-8d27f57.json": "fa7713347b9f8d314cf0c08ba2c87afc7aacad5b2175612cadaf12fd48ca748d",
        }
        for rel, digest in snapshots.items():
            self.assertEqual(digest, hashlib.sha256((ROOT / rel).read_bytes()).hexdigest(), rel)

    def test_governing_sources_point_to_model_first_without_erasing_legacy_definitions(self):
        manifest = json.loads((ROOT / "docs/reboot/manifest.json").read_text())
        alignment = json.loads((ROOT / "docs/reboot/fde-alignment.json").read_text())
        active = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())
        self.assertEqual("superseded_historical_definition_preserved", manifest["historical_sequence_status"])
        self.assertEqual("docs/model-first-roadmap/manifest.json", manifest["active_replacement_manifest"])
        self.assertEqual(30, len(manifest["days"]))
        self.assertEqual("docs/model-first-roadmap/manifest.json", alignment["active_learning_sequence"]["path"])
        self.assertEqual("2026-10-27", alignment["active_learning_sequence"]["start_date"])
        self.assertEqual("local_review_draft", active["status"])
        self.assertEqual("not_started", active["detailed_days"][0]["status"])
        self.assertFalse(any(day["learner_completion"] for day in active["detailed_days"]))
        self.assertEqual(548, active["horizon_days"])
        self.assertEqual("2028-04-26", active["end_date"])
        self.assertEqual(1378.75, active["budget"]["engineering_hours"])
        self.assertEqual(282.75, active["budget"]["shared_hours"])
        self.assertEqual(0.2, active["budget"]["reserve_fraction_inside_engineering"])
        self.assertEqual(625.25, active["feasibility"]["nine_month_engineering_hours_by_day_274"])
        self.assertIn("one strong end-to-end coverstory", active["feasibility"]["nine_month_target"].lower())
        self.assertEqual(8, len(active["phases"]))
        self.assertEqual("docs/model-first-roadmap/legacy-issue-migration.json", active["legacy_issue_migration"])
        self.assertEqual("c2caee396002bb48b2a34d407545ecd96ecdb1ab", active["dsa_source"]["commit"])
        self.assertIn("Kafka", active["application_comprehension_and_scalability_lane"]["topics_by_project_need"][2]["topics"])
        self.assertEqual(1378.75, alignment["active_learning_sequence"]["capacity"]["engineering_hours"])
        calendar = alignment["calendar_amendment"]
        self.assertEqual(548, calendar["learning_horizon_day"])
        self.assertEqual("2028-04-26", calendar["learning_horizon_date"])
        self.assertEqual("2029-10-26", calendar["professional_ownership_review_date"])
        self.assertEqual(7, len(calendar["role_review_milestones"]))
        self.assertEqual(774, alignment["superseded_calendar_amendment_774_day_plan"]["original_learning_horizon_day"])
        self.assertEqual(active["milestone_reviews"], calendar["role_review_milestones"])
        self.assertEqual("superseded_prior_outcome_plan_preserved; not the active learning sequence",
                         alignment["new_batch_status"]["days271_to774"])

    def test_new_local_markdown_links_resolve(self):
        paths = [ROOT / "README.md", ROOT / "docs/ai-first-roadmap/README.md", ROOT / "docs/ai-first-roadmap/role-alignment.md", ROOT / "docs/model-first-roadmap/README.md"]
        paths.extend(sorted((ROOT / "docs/model-first-roadmap/contracts").glob("*.md")))
        for source in paths:
            text = source.read_text()
            for target in re.findall(r"\[[^\]]+\]\(([^)]+)\)", text):
                if target.startswith(("https://", "http://", "repo://", "#")):
                    continue
                local = (source.parent / target.split("#", 1)[0]).resolve()
                self.assertTrue(local.exists(), f"{source.relative_to(ROOT)} -> {target}")

    def test_role_benchmarks_are_limited_and_geo_status_is_explicit(self):
        text = (ROOT / "docs/ai-first-roadmap/role-alignment.md").read_text()
        expected = ["OpenAI", "Anthropic", "Google / DeepMind", "Microsoft AI", "Meta AI",
                    "NVIDIA", "AWS / Amazon", "Databricks", "Snowflake"]
        rows = [line for line in text.splitlines() if line.startswith("| ") and not line.startswith("| ---")]
        self.assertEqual(expected, [line.split("|")[1].strip() for line in rows[1:]])
        self.assertIn("global role signals", text)
        self.assertIn("India eligibility", text)
        self.assertIn("explicitly closed", text)
        self.assertIn("FDE and customer-deployment roles are a later", text)
        self.assertIn("Research Scientist is not the default target", text)
