import json
import shlex
import unittest
from datetime import date, timedelta
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
BATCH = ROOT / "docs/reboot/continuation-169-182"


class StreamForge169182ManifestTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads((BATCH / "manifest.json").read_text(encoding="utf-8"))
        cls.owner = json.loads((BATCH / "scrum-owner-manifest.json").read_text(encoding="utf-8"))

    def test_exact_source_branch_range_dates_and_contract_files(self):
        self.assertEqual("bb4cc3e5df7336c74a3f086b24510d325ea871e1", self.manifest["base_commit"])
        self.assertEqual("learning/streamforge-days-169-182", self.manifest["branch"])
        self.assertEqual(14, len(self.manifest["days"]))
        self.assertEqual(list(range(169, 183)), [day["day"] for day in self.manifest["days"]])
        first = date.fromisoformat(self.manifest["start_date"])
        for offset, day in enumerate(self.manifest["days"]):
            self.assertEqual(first + timedelta(days=offset), date.fromisoformat(day["date"]))
            self.assertTrue((BATCH / day["contract_file"]).is_file())
            self.assertEqual("starter_draft_not_board_published", day["status"])
            self.assertFalse(day["interview_or_dsa"]["new_guided_anchors_allowed"])
            self.assertIn("--on-date \"$(TZ=Asia/Kolkata date +%F)\"", day["interview_or_dsa"]["queue_command"])
            self.assertIn(f"--roadmap-day {day['day']}", day["interview_or_dsa"]["queue_command"])
            prose = (BATCH / day["contract_file"]).read_text(encoding="utf-8")
            self.assertIn(day["title"], prose)
            self.assertIn("actual local study dates", prose)
            if day["day"] >= 172:
                self.assertIn("165 engineering minutes", prose)
                self.assertIn("45 minutes total", prose)
                self.assertIn("not additive", prose)
            else:
                self.assertIn("120 engineering minutes", prose)
                self.assertIn("60/75/90 minutes", prose)

    def test_budget_transition_is_explicit_at_day_172_including_recoveries(self):
        for day in self.manifest["days"]:
            engineering = day["engineering"]
            allocation = engineering["allocation_minutes"]
            self.assertEqual(engineering["budget_minutes"], sum(allocation.values()))
            self.assertEqual(10 if day["day"] <= 171 else 15, allocation["evidence"])
            if day["day"] <= 171:
                self.assertEqual(120, engineering["budget_minutes"])
                self.assertEqual({"1_or_2": 60, "3": 75, "4": 90},
                                 day["interview_or_dsa"]["budget_minutes_by_substantive_exercise_count"])
                self.assertEqual([180, 195, 210], day["interview_or_dsa"]["total_daily_minutes_by_dsa_tier"])
            else:
                block = day["interview_or_dsa"]
                self.assertEqual(165, engineering["budget_minutes"])
                self.assertEqual(45, block["budget_minutes"])
                self.assertEqual(210, block["total_daily_minutes"])
                self.assertEqual(1, block["maximum_substantive_coding_exercises"])
                self.assertEqual({"one_substantive_timebox": 30, "recall_or_explain_back": 10,
                                  "evidence_and_next_due": 5}, block["allocation_minutes"])
        self.assertEqual([175, 182], [d["day"] for d in self.manifest["days"] if d["recovery_day"]])

    def test_owner_export_matches_daily_manifest_without_claiming_learner_completion(self):
        self.assertFalse(self.manifest["board_writes"])
        self.assertFalse(self.owner["board_writes"])
        cards = self.owner["cards"]
        self.assertEqual(14, len(cards))
        for source, card in zip(self.manifest["days"], cards):
            self.assertEqual(source["day"], card["day_number"])
            self.assertEqual(source["date"], card["date"])
            self.assertEqual(source["title"], card["title"])
            self.assertEqual(source["engineering"]["budget_minutes"], card["engineering"]["minutes"])
            self.assertEqual(source["interview_or_dsa"], card["interview_or_remaining_reviews"])
            self.assertEqual("unobserved_not_implied_by_scaffold", card["learner_status"])
            self.assertEqual("not_recorded", card["evidence_status"])
            self.assertIsNone(card["board_ticket_id"])

    def test_total_engineering_allocation_matches_fourteen_day_plan(self):
        self.assertEqual(2175, sum(day["engineering"]["budget_minutes"] for day in self.manifest["days"]))
        self.assertEqual(495, sum(day["engineering"]["budget_minutes"] - 120
                                  for day in self.manifest["days"]))
        for key in self.manifest["source_references"]:
            self.assertTrue(self.manifest["source_references"][key]["url"].startswith("https://"))

    def test_every_exported_day_selector_resolves_to_real_tests(self):
        loader = unittest.defaultTestLoader
        for day in self.manifest["days"]:
            with self.subTest(day=day["day"]):
                words = shlex.split(day["engineering"]["proposed_acceptance_command"])
                self.assertGreaterEqual(len(words), 4)
                self.assertEqual(["python3", "-m", "unittest"], words[:3])
                names = words[3:]
                suite = unittest.TestSuite(loader.loadTestsFromName(name) for name in names)
                flattened = []
                def visit(node):
                    if isinstance(node, unittest.TestSuite):
                        for child in node:
                            visit(child)
                    else:
                        flattened.append(node)
                visit(suite)
                self.assertTrue(flattened, "selector must resolve to at least one test")
                self.assertFalse(any(isinstance(test, unittest.loader._FailedTest)
                                     for test in flattened), repr(flattened))


if __name__ == "__main__":
    unittest.main()
