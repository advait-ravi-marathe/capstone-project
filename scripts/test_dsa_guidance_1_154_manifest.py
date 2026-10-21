import datetime as dt
import json
import pathlib
import re
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
POLICY = "docs/reboot/dsa/guided-transfer-policy.md"
TIERS = {"1_or_2_substantive_exercises": 60, "3_substantive_exercises": 75, "4_substantive_exercises": 90}


class DsaGuidanceAcrossDaysManifestTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest_paths = [ROOT / "docs/reboot/manifest.json"] + sorted((ROOT / "docs/reboot").glob("continuation-*/manifest.json"))
        cls.days = {}
        cls.manifests = []
        for path in cls.manifest_paths:
            data = json.loads(path.read_text())
            cls.manifests.append((path, data))
            for day in data.get("days", []):
                cls.days[day["day"]] = (path, day)

    def test_all_154_days_have_guidance_and_unchanged_engineering_export(self):
        self.assertEqual(list(range(1, 155)), sorted(self.days))
        for number, (path, day) in self.days.items():
            if number <= 30:
                contract = ROOT / day["contract_file"]
                text = contract.read_text()
                self.assertIn("## DSA study (workload-tiered; actual attempts only)", text)
                self.assertIn("guided-transfer-policy.md", text)
                self.assertEqual({1: 121, 2: 1, 3: 383}.get(number), day["dsa_guided_encounter_problem_id"])
                owner = json.loads((ROOT / "docs/reboot/scrum-owner-manifest.json").read_text())
                card = next(c for c in owner["cards"] if c["day_number"] == number)
                self.assertEqual(POLICY, card["dsa_guidance_reference"])
                self.assertEqual(TIERS, card["java_dsa"]["budget_minutes_by_exercise_count"])
            else:
                export = json.loads((path.parent / "scrum-owner-manifest.json").read_text())
                card = next(c for c in export["cards"] if c["day_number"] == number)
                self.assertEqual(day["java_dsa"], card["java_dsa"], f"DSA contract/export mismatch on Day {number}")
                self.assertEqual(TIERS, day["java_dsa"]["budget_minutes_by_exercise_count"])
                self.assertEqual(120, day["engineering"]["budget_minutes"])
                self.assertEqual({"implementation_and_testing": 85, "concept_or_course": 25, "evidence": 10},
                                 day["engineering"]["allocation_minutes"])
                contract = ROOT / day["contract_file"]
                text = contract.read_text()
                self.assertIn("## DSA study (workload-tiered; actual attempts only)", text)
                self.assertIn("guided-transfer-policy.md", text)
                self.assertIn(day["java_dsa"]["queue_command"], text)
                self.assertEqual({"implementation_and_testing": 85, "concept_or_course": 25, "evidence": 10},
                                 day["engineering"]["allocation_minutes"])

    def test_first_three_guided_sequence_and_recovery_cutoff(self):
        self.assertEqual([121, 1, 383], [self.days[n][1]["dsa_current_guided_sequence_problem_id"] for n in (1, 2, 3)])
        registry = json.loads((ROOT / "docs/reboot/dsa/canonical-anchors.json").read_text())
        self.assertEqual([1, 3, 5], [a["original_anchor_day"] for a in registry["anchors"][:3]])
        for n in (133, 140, 147, 154):
            self.assertIn("recovery only", self.days[n][1]["dsa_current_guidance"])
            self.assertIn("**Recovery only:**", (ROOT / self.days[n][1]["contract_file"]).read_text())
        for n in (151, 152, 153, 154):
            text = (ROOT / self.days[n][1]["contract_file"]).read_text()
            self.assertNotIn("**Guided first encounter:**", text)
            self.assertTrue("Revision-only period" in text or "Recovery only" in text)

    def test_registry_catalog_and_synthetic_capacity_artifact(self):
        registry = json.loads((ROOT / "docs/reboot/dsa/canonical-anchors.json").read_text())
        catalog = json.loads((ROOT / "docs/reboot/dsa/guided-transfer-catalog-2026-10-21.json").read_text())
        self.assertEqual(31, len(registry["anchors"]))
        self.assertEqual(31, len({a["anchor_id"] for a in registry["anchors"]}))
        self.assertTrue(all(a["attempt_status"] == "unknown_until_actual_event" for a in registry["anchors"]))
        # Catalog order follows canonical source order; candidates remain a partial seed.
        self.assertEqual([a["problem_id"] for a in registry["anchors"]],
                         [e["canonical_id"] for e in catalog["entries"]])
        ids = [c["id"] for e in catalog["entries"] for c in e["candidates"]]
        self.assertEqual(43, len(ids))
        self.assertEqual(43, len(set(ids)))
        self.assertTrue(all(c["unseen_status"] == "unknown_requires_learner_history_check"
                            for e in catalog["entries"] for c in e["candidates"]))
        simulation = json.loads((ROOT / "docs/reboot/dsa/tiered-capacity-simulation-2026-10-21.json").read_text())
        self.assertEqual("SYNTHETIC_CAPACITY_ILLUSTRATION_NOT_LEARNER_EVENTS", simulation["status"])
        self.assertEqual(154, len(simulation["rows"]))
        self.assertEqual({"60": 25, "75": 17, "90": 10}, simulation["summary"]["active_days_by_tier"])


if __name__ == "__main__":
    unittest.main()
