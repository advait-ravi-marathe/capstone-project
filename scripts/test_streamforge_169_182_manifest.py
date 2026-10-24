import json
import copy
import shlex
import unittest
from datetime import date, timedelta
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
BATCH = ROOT / "docs/reboot/continuation-169-182"
GATE_PATHS = (
    "evidence/continuation-141-154/day-153.json",
    "evidence/continuation-141-154/day-154.json",
)
MOCK_QUESTIONS = {
    169: ("Q33", "Analytics: event to metric"),
    171: ("Q34", "Analytics: late and duplicate data"),
    173: ("Q23", "Event pipeline: ingest and process"),
    176: ("Q33", "Analytics: event to metric"),
    179: ("Q24", "Event pipeline: correction and replay"),
    182: ("Q34", "Analytics: late and duplicate data"),
}


class StreamForge169182ManifestTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads((BATCH / "manifest.json").read_text(encoding="utf-8"))
        cls.owner = json.loads((BATCH / "scrum-owner-manifest.json").read_text(encoding="utf-8"))

    def test_exact_source_branch_range_dates_and_contract_files(self):
        self.assertEqual("ae21926a7d61ef25db8609ddc533d9cdeeacff88", self.manifest["base_commit"])
        self.assertEqual("bb4cc3e5df7336c74a3f086b24510d325ea871e1", self.manifest["starter_reference_commit"])
        self.assertEqual("work/eai-sprint2-onward", self.manifest["branch"])
        self.assertEqual(14, len(self.manifest["days"]))
        self.assertEqual(list(range(169, 183)), [day["day"] for day in self.manifest["days"]])
        first = date.fromisoformat(self.manifest["start_date"])
        for offset, day in enumerate(self.manifest["days"]):
            self.assertEqual(first + timedelta(days=offset), date.fromisoformat(day["date"]))
            self.assertTrue((BATCH / day["contract_file"]).is_file())
            self.assertEqual("starter_draft_not_board_published", day["status"])
            prose = (BATCH / day["contract_file"]).read_text(encoding="utf-8")
            self.assertIn(day["title"], prose)
            if day["day"] >= 172:
                self.assertIn("165 engineering minutes", prose)
                self.assertIn("45 minutes total", prose)
                self.assertTrue("not additive" in prose or "counted once" in prose)
            else:
                self.assertIn("120 engineering minutes", prose)

    def test_budget_transition_is_explicit_at_day_172_including_recoveries(self):
        for day in self.manifest["days"]:
            engineering = day["engineering"]
            allocation = engineering["allocation_minutes"]
            self.assertEqual(engineering["budget_minutes"], sum(allocation.values()))
            self.assertEqual(10 if day["day"] <= 171 else 15, allocation["evidence"])
            if day["day"] <= 171:
                self.assertEqual(120, engineering["budget_minutes"])
                if day["day"] in (169, 171):
                    self.assertEqual(45, day["interview_practice"]["budget_minutes"])
                    self.assertTrue(day["interview_practice"]["within_engineering_practice"])
                    self.assertEqual(40, day["interview_practice"]["implementation_minutes_remaining"])
                    self.assertEqual(45, sum(day["interview_practice"]["allocation_minutes"].values()))
                else:
                    self.assertNotIn("interview_practice", day)
            else:
                block = day.get("interview_practice", {})
                self.assertEqual(165, engineering["budget_minutes"])
                self.assertEqual(45, block["budget_minutes"])
                self.assertEqual(210, block["total_daily_minutes"])
                if day["day"] in MOCK_QUESTIONS:
                    self.assertEqual("45_minute_mock", block["mode"])
                    self.assertEqual(45, sum(block["allocation_minutes"].values()))
                else:
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
            self.assertEqual("unobserved_not_implied_by_scaffold", card["learner_status"])
            self.assertEqual("not_recorded", card["evidence_status"])
            self.assertIsNone(card["board_ticket_id"])

    def test_total_engineering_allocation_matches_fourteen_day_plan(self):
        self.assertEqual(2175, sum(day["engineering"]["budget_minutes"] for day in self.manifest["days"]))
        self.assertEqual(495, sum(day["engineering"]["budget_minutes"] - 120
                                  for day in self.manifest["days"]))
        for key in self.manifest["source_references"]:
            self.assertTrue(self.manifest["source_references"][key]["url"].startswith("https://"))

    def test_assistant_architecture_and_interview_schedule_are_explicit_and_synchronized(self):
        notes = (BATCH / "assistant-architecture-notes.md").read_text(encoding="utf-8")
        schedule = (BATCH / "interview-and-review-schedule.md").read_text(encoding="utf-8")
        question_bank = (ROOT / "docs/reboot/interview/question-bank.md").read_text(encoding="utf-8")
        cards = {card["day_number"]: card for card in self.owner["cards"]}
        self.assertIn("assistant-architecture-notes.md", (BATCH / "README.md").read_text(encoding="utf-8"))
        self.assertIn("no capstone architecture proposal", notes)
        self.assertIn("use only the existing", schedule)
        for day in self.manifest["days"]:
            number = day["day"]
            eng = day["engineering"]
            card = cards[number]
            anchor = "#day-" + str(number)
            self.assertIn(anchor, eng["assistant_architecture_note"])
            self.assertEqual(eng["assistant_architecture_note"], card["assistant_architecture_note"])
            self.assertEqual(eng["architecture_explanation"], card["architecture_explanation"])
            self.assertEqual(day.get("interview_practice"), card.get("interview_practice"))
            self.assertEqual("assistant_supplied_capstone_architecture; learner_implements_and_tests",
                             eng["design_ownership"])
            self.assertFalse(eng["system_design_proposal_required"])
            self.assertEqual(day["review_practice_examples_note"], card["review_practice_examples_note"])
            self.assertNotIn("no company question bank is implied", day["review_practice_examples_note"])
            contract = (BATCH / day["contract_file"]).read_text(encoding="utf-8")
            self.assertIn("Assistant-supplied architecture", contract)
            self.assertIn(anchor, contract)
            self.assertIn("no capstone design proposal is required", contract)
            if eng.get("interview_practice"):
                self.assertEqual(eng["interview_practice"], card["interview_practice"])
            if (eng.get("interview_session") or {}).get("mode") == "45_minute_system_design_question":
                relative = "../../interview/question-bank.md"
                self.assertTrue((contract_path := (BATCH / day["contract_file"]).parent / relative).resolve().is_file())
            if number in MOCK_QUESTIONS:
                qid, title = MOCK_QUESTIONS[number]
                session = eng["interview_session"]
                self.assertEqual(qid, session["question_id"])
                self.assertEqual(title, session["question_title"])
                self.assertIn(f'<a id="{qid.lower()}"></a>', question_bank)
                self.assertIn(f"{qid} — {title}.", question_bank)
                self.assertEqual(45, session["duration_minutes"])
                self.assertEqual(45, sum(session["phase_minutes"].values()))
                self.assertIn(title, contract)
            elif number >= 172:
                self.assertEqual("shared_new_question_or_unfinished_review",
                                 eng["interview_session"]["mode"])
                self.assertEqual(45, eng["interview_session"]["duration_minutes"])
                self.assertIn("shared interview/new-question/review block", contract.lower())
            else:
                self.assertIsNone(eng["interview_session"])

        for day in (169, 171):
            eng = next(item["engineering"] for item in self.manifest["days"] if item["day"] == day)
            self.assertTrue(eng["interview_session"]["within_engineering_practice"])
            self.assertEqual(40, eng["interview_session"]["implementation_minutes_remaining"])
            self.assertEqual(85, eng["allocation_minutes"]["practice_and_test"])
            self.assertIn("leaving a 40-minute", (BATCH / f"contracts/day-{day}.md").read_text())
        self.assertEqual([169, 171, 173, 176, 179, 182], sorted(MOCK_QUESTIONS))

    def test_ab_gate_reads_authoritative_records_and_can_become_met(self):
        gate = self.manifest["prior_readiness_gate"]
        self.assertEqual(gate, self.owner["prior_readiness_gate"])
        self.assertEqual("not_met", gate["current_status"])
        self.assertEqual(list(GATE_PATHS), [record["path"] for record in gate["evidence_records"]])
        actual = []
        for relative, snapshot in zip(GATE_PATHS, gate["evidence_records"]):
            path = ROOT / relative
            self.assertTrue(path.is_file(), f"Authoritative record should exist: {relative}")
            record = json.loads(path.read_text(encoding="utf-8"))
            self.assertTrue(snapshot["exists"])
            for key in ("status", "evidence_status", "tested_source_sha", "artifact_hashes",
                        "learner_completion", "temporary_witness"):
                self.assertEqual(record.get(key), snapshot.get(key))
            actual.append(record)

        from scripts.check_streamforge_169_182_entry_gate import evaluate_records
        fresh_status = evaluate_records(actual)["gate"]
        self.assertEqual("not_met", fresh_status)
        self.assertEqual(actual[0]["status"], gate["observed_prior_status"]["day_153"])
        self.assertEqual(actual[0]["evidence_status"], gate["observed_prior_status"]["day_153_evidence"])
        self.assertEqual(actual[1]["status"], gate["observed_prior_status"]["day_154"])
        self.assertEqual(actual[1]["evidence_status"], gate["observed_prior_status"]["day_154_evidence"])

        valid = {
            "status": "completed", "evidence_status": "verified", "learner_completion": True,
            "temporary_witness": False, "tested_source_sha": "a" * 40,
            "artifact_hashes": [
                {"role": "baseline", "sha256": "1" * 64},
                {"role": "candidate", "sha256": "2" * 64},
            ],
            "matrix_selector": "the exact observed matrix selector", "matrix_exit_code": 0,
            "matrix_result": "passed",
            "source_provenance": {
                "baseline": {"source": "source sha", "build_recipe": "baseline recipe"},
                "candidate": {"source": "source sha", "build_recipe": "candidate recipe"},
            },
            "proof_limits": "Local unchanged-schema result only.",
        }
        self.assertEqual("met", evaluate_records([copy.deepcopy(valid), copy.deepcopy(valid)])["gate"])
        witness = copy.deepcopy(valid)
        witness["temporary_witness"] = True
        self.assertEqual("not_met", evaluate_records([witness, copy.deepcopy(valid)])["gate"])
        for day in self.manifest["days"]:
            contract = (BATCH / day["contract_file"]).read_text(encoding="utf-8")
            self.assertIn("Prior A/B evidence gate", contract)
            self.assertIn(GATE_PATHS[0], contract)
            self.assertIn("checker exits 2", contract)
            self.assertIn("After Day 171, no extra DSA is required", contract)

        owner_dsa = self.owner["separate_learning_repository"]
        self.assertIn("dsa-prep", owner_dsa["algorithm_practice"])
        self.assertIn("not a duplicate tracker", owner_dsa["ownership"])
        self.assertFalse(any("interview_or_remaining_reviews" in card for card in self.owner["cards"][3:]))

    def test_fde_alignment_summarizes_the_same_current_gate(self):
        alignment = json.loads((ROOT / "docs/reboot/fde-alignment.json").read_text(encoding="utf-8"))
        entry = next(row for row in alignment["phase_ranges"] if row.get("range") == "169-182")
        self.assertIn("Day 153/154 A/B evidence gate", entry["prerequisite_note"])
        self.assertIn(GATE_PATHS[0], entry["prerequisite_note"])
        self.assertIn("current gate is not met", entry["prerequisite_note"])

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
