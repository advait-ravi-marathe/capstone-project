"""Ensure the capstone has no duplicate algorithm-practice implementation or tracker."""
import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DSA_REPO = "https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14"


class ExternalTrackBoundaryTest(unittest.TestCase):
    def test_implementation_tests_and_tracker_files_are_external(self):
        self.assertFalse((ROOT / "src/main/java/assignments/dsa").exists())
        self.assertFalse((ROOT / "src/test/java/assignments/dsa").exists())
        self.assertFalse((ROOT / "docs/reboot/dsa").exists())
        self.assertFalse((ROOT / "docs/reboot/continuation-31-44/dsa").exists())
        self.assertFalse((ROOT / "scripts/dsa-review-queue.py").exists())
        self.assertFalse(list((ROOT / "scripts").glob("test_dsa_*.py")))
        self.assertFalse(list((ROOT / "docs/reboot").rglob("*dsa*.json")))

    def test_planning_exports_only_link_to_separate_repository(self):
        files = list((ROOT / "docs/reboot").rglob("manifest.json"))
        files += list((ROOT / "docs/reboot").rglob("scrum-owner-manifest.json"))
        files += [ROOT / "docs/reboot/continuation-155-168/transfer-plan.json",
                  ROOT / "docs/reboot/continuation-155-168/streamforge-days-155-168-transfer-plan.json",
                  ROOT / "docs/reboot/continuation-183-196/verified-content-features-183-196-guidance.json"]
        self.assertGreaterEqual(len(files), 25)
        for path in files:
            data = json.loads(path.read_text(encoding="utf-8"))
            self.assertEqual(DSA_REPO, data["separate_learning_repository"]["algorithm_practice"], path)
            serialized = json.dumps(data).lower()
            self.assertNotIn("java_dsa", serialized, path)
            self.assertNotIn("dsa-review-queue", serialized, path)
            self.assertNotIn("docs/reboot/dsa/", serialized, path)

    def test_daily_contracts_point_outward_without_local_schedule(self):
        contracts = list((ROOT / "docs/reboot").rglob("contracts/day-*.md"))
        self.assertGreaterEqual(len(contracts), 190)
        for path in contracts:
            text = path.read_text(encoding="utf-8")
            self.assertIn(DSA_REPO, text, path)
            self.assertNotRegex(text, r"(?:java_dsa|dsa-review-queue|docs/reboot/dsa/)")

    def test_ci_keeps_engineering_and_streamforge_checks(self):
        workflow = (ROOT / ".github/workflows/coverstory-starter.yml").read_text(encoding="utf-8")
        self.assertIn("Verify engineering roadmap manifests and curriculum", workflow)
        self.assertIn("Verify StreamForge local fixture infrastructure", workflow)
        self.assertNotIn("test_dsa_", workflow)
        self.assertNotIn("Verify DSA", workflow)


if __name__ == "__main__":
    unittest.main()
