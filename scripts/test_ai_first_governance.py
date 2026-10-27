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
        self.assertIn("docs/ai-first-roadmap/README.md", root_readme)
        self.assertIn("historical artifacts", root_readme)
        self.assertTrue(reboot_readme.startswith("> **Historical sequence notice"))
        self.assertIn("../ai-first-roadmap/README.md", reboot_readme)
        snapshots = {
            "docs/ai-first-roadmap/archive/root-README-8d27f57.md": "e568f75663faf48391070b3969901e5251b8da2dcff350a7a3aed85730640b78",
            "docs/ai-first-roadmap/archive/reboot-README-8d27f57.md": "3206d0dd2939371b9e6d5900cadfdc77d2831dcfee3fbbd0e0166526d4ff0fb6",
            "docs/ai-first-roadmap/archive/fde-alignment-8d27f57.json": "fa7713347b9f8d314cf0c08ba2c87afc7aacad5b2175612cadaf12fd48ca748d",
        }
        for rel, digest in snapshots.items():
            self.assertEqual(digest, hashlib.sha256((ROOT / rel).read_bytes()).hexdigest(), rel)

    def test_governing_sources_point_to_ai_first_without_erasing_legacy_definitions(self):
        manifest = json.loads((ROOT / "docs/reboot/manifest.json").read_text())
        alignment = json.loads((ROOT / "docs/reboot/fde-alignment.json").read_text())
        active = json.loads((ROOT / "docs/ai-first-roadmap/manifest.json").read_text())
        self.assertEqual("superseded_historical_definition_preserved", manifest["historical_sequence_status"])
        self.assertEqual("docs/ai-first-roadmap/manifest.json", manifest["active_replacement_manifest"])
        self.assertEqual(30, len(manifest["days"]))
        self.assertEqual("docs/ai-first-roadmap/manifest.json", alignment["active_learning_sequence"]["path"])
        self.assertEqual("2026-10-27", alignment["active_learning_sequence"]["start_date"])
        self.assertEqual("2027-07-27", alignment["active_learning_sequence"]["nine_month_checkpoint"]["date"])
        self.assertEqual("local_review_draft", active["status"])
        self.assertEqual("not_started", active["detailed_days"][0]["status"])
        self.assertFalse(any(day["learner_completion"] for day in active["detailed_days"]))
        self.assertEqual(["coverstory-ai", "streamforge-ai", "developer-workflow-assistant", "shared-integration-closeout"],
                         [project["id"] for project in active["projects"]])
        self.assertEqual(365, active["horizon_days"])
        self.assertEqual(875.5, active["budget"]["engineering_hours"])
        capacity = active["capacity_policy"]
        phases = active["projects"]
        self.assertEqual(0.2, capacity["reserve_fraction"])
        self.assertAlmostEqual(875.5, sum(phase["engineering_hours"] for phase in phases))
        self.assertAlmostEqual(175.1, sum(phase["reserved_hours"] for phase in phases))
        self.assertAlmostEqual(700.4, sum(phase["planned_build_hours"] for phase in phases))
        self.assertEqual([1, 150, 151, 255, 256, 340, 341, 365],
                         [bound for phase in phases for bound in phase["days"]])
        self.assertEqual(["2026-10-27", "2027-03-25", "2027-03-26", "2027-07-08",
                          "2027-07-09", "2027-10-01", "2027-10-02", "2027-10-26"],
                         [bound for phase in phases for bound in phase["dates"]])
        self.assertEqual(2027, int(active["nine_month_checkpoint"]["date"][:4]))
        self.assertEqual(274, active["nine_month_checkpoint"]["study_day"])
        self.assertEqual(2, len(active["projects"][:2]))
        self.assertIn("early/in progress", active["nine_month_checkpoint"]["expected_phase_position"])
        architecture = active["assistant_selected_architecture"]
        self.assertIn("assistant selects and explains", architecture["learner_design_work"])
        self.assertIn("synthetic session/CSRF", architecture["session_and_api"])
        self.assertIn("loopback", architecture["ai_application"])

        alignment_capacity = alignment["active_learning_sequence"]["capacity"]
        self.assertEqual(capacity["reserve_total_hours"], alignment_capacity["reserved_hours"])
        self.assertEqual(capacity["planned_build_total_hours"], alignment_capacity["planned_build_hours"])
        self.assertEqual([phase["days"] for phase in phases],
                         [phase["days"] for phase in alignment_capacity["phase_envelopes"]])

    def test_new_local_markdown_links_resolve(self):
        paths = [ROOT / "README.md", ROOT / "docs/ai-first-roadmap/README.md", ROOT / "docs/ai-first-roadmap/role-alignment.md"]
        paths.extend(sorted((ROOT / "docs/ai-first-roadmap/contracts").glob("*.md")))
        for source in paths:
            text = source.read_text()
            for target in re.findall(r"\[[^\]]+\]\(([^)]+)\)", text):
                if target.startswith(("https://", "http://", "repo://", "#")):
                    continue
                local = (source.parent / target.split("#", 1)[0]).resolve()
                self.assertTrue(local.exists(), f"{source.relative_to(ROOT)} -> {target}")
