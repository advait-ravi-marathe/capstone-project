import datetime
import json
import pathlib
import re
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
SPRINT = ROOT / "docs/reboot/continuation-141-154"
DSA_URL = "https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0"
MOCKS = {
    141: ("Q29", "Autocomplete: prefix lookup", "S11-D141-MOCK", ["search-index", "read-scaling"]),
    144: ("Q30", "Autocomplete: update and privacy", "S11-D144-MOCK", ["search-index", "ownership-boundary"]),
    148: ("Q34", "Analytics: late and duplicate data", "S11-D148-MOCK", ["analytics-pipeline", "idempotency"]),
    151: ("Q35", "Distributed rate limiting", "S11-D151-MOCK",
          ["rate-limiting", "multi-region-boundary", "load-balancing"]),
}


class Continuation141To154CurriculumTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads((SPRINT / "manifest.json").read_text())
        cls.owner = json.loads((SPRINT / "scrum-owner-manifest.json").read_text())
        cls.days = cls.manifest["days"]
        cls.cards = {card["day_number"]: card for card in cls.owner["cards"]}
        cls.question_bank = (ROOT / "docs/reboot/interview/question-bank.md").read_text().lower()
        cls.patterns = (ROOT / "docs/reboot/interview/pattern-library.md").read_text().lower()
        cls.notes = (SPRINT / "assistant-architecture-notes.md").read_text()

    def test_all_days_keep_dates_budgets_history_and_separate_dsa(self):
        self.assertEqual(list(range(141, 155)), [day["day"] for day in self.days])
        self.assertEqual(14, len(self.cards))
        self.assertEqual("2027-03-29", self.manifest["start_date"])
        self.assertEqual("2027-04-11", self.manifest["end_date"])
        first = datetime.date(2027, 3, 29)
        for i, day in enumerate(self.days):
            self.assertEqual((first + datetime.timedelta(days=i)).isoformat(), day["date"])
            self.assertEqual(day["date"], self.cards[day["day"]]["date"])
            self.assertEqual(day["title"], self.cards[day["day"]]["title"])
            eng = day["engineering"]
            self.assertEqual(120, eng["budget_minutes"])
            self.assertEqual({"implementation_and_testing": 85, "concept_or_course": 25, "evidence": 10},
                             eng["allocation_minutes"])
            self.assertEqual(eng, self.cards[day["day"]]["engineering"])
            self.assertEqual("not_attempted", day["status"])
            contract = (ROOT / day["contract_file"]).read_text()
            self.assertIn("120 engineering minutes (25 study / 85 practice / 10 evidence)", contract)
            self.assertIn("Assistant-provided architecture", contract)
            self.assertIn("No capstone design proposal", contract)
            note_ref = eng["assistant_architecture_note"]
            note_path, anchor = note_ref.split("#", 1)
            self.assertTrue((ROOT / note_path).is_file())
            self.assertIn(f"## Day {day['day']} —", self.notes)
            self.assertIn(anchor, contract)
            self.assertIn(note_ref, eng["concept_course"]["scope_note"])
            owner_learning = self.cards[day["day"]]["learning_block"]
            self.assertEqual(note_ref, owner_learning["architecture_note"])
            self.assertTrue(any("Assistant-provided capstone architecture" in line and anchor in line
                                for line in owner_learning["lines"]))
            self.assertTrue((ROOT / eng["acceptance_test_path"]).is_file())
            self.assertTrue((ROOT / eng["evidence_reference"]).is_file())
            for command in eng["acceptance_commands"]:
                self.assertTrue("./mvnw" in command or command.startswith("scripts/prepare-release-artifacts.sh"))

        dsa = self.manifest["separate_learning_repository"]
        self.assertEqual(DSA_URL, dsa["url"])
        self.assertEqual(DSA_URL, self.owner["separate_learning_repository"]["url"])
        self.assertEqual("859e161f6553e9aca8dc501fda152780cec66ef0", dsa["source_commit"])
        self.assertTrue(all("dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0" in
                            (ROOT / day["contract_file"]).read_text() for day in self.days))
        self.assertTrue(self.manifest["daily_budget_excludes_separate_dsa"])
        self.assertIsNone(self.manifest["daily_budget_minutes"])

    def test_four_question_only_sessions_replace_practice_time_without_adding_hours(self):
        expected_days = set(MOCKS)
        self.assertEqual(expected_days,
                         {day["day"] for day in self.days if day["engineering"].get("interview_assignment")})
        self.assertEqual([141, 144, 148, 151], self.manifest["interview_practice_summary"]["days"])
        self.assertEqual([141, 144, 148, 151], self.owner["interview_practice_summary"]["days"])
        self.assertEqual([3, 4, 3], [144 - 141, 148 - 144, 151 - 148])
        for day in self.days:
            number = day["day"]
            assignment = day["engineering"].get("interview_assignment")
            if number not in MOCKS:
                self.assertIsNone(assignment)
                self.assertNotIn("generated sprint11 interview block", (ROOT / day["contract_file"]).read_text())
                continue
            qid, title, deferred_id, pattern_ids = MOCKS[number]
            self.assertIsNotNone(assignment)
            self.assertEqual(qid, assignment["question_id"])
            self.assertEqual(title, assignment["question_title"])
            self.assertEqual("system_design_interview_question_only", assignment["proposal_scope"])
            self.assertEqual("assistant_supplied", assignment["capstone_architecture"])
            self.assertEqual(deferred_id, assignment["deferred_work_id"])
            self.assertEqual(45, assignment["duration_minutes"])
            self.assertEqual(85, assignment["practice_minutes_available"])
            self.assertEqual(85, assignment["practice_total_minutes"])
            self.assertEqual(45, sum(phase["minutes"] for phase in assignment["mock_phases"]))
            self.assertEqual(40, sum(assignment["eai_slice_minutes"].values()))
            self.assertEqual(pattern_ids, [ref.rsplit("#", 1)[1] for ref in assignment["pattern_refs"]])
            self.assertIn(f'<a id="q{qid[1:]}"></a>', self.question_bank)
            for pattern_id in pattern_ids:
                self.assertIn(f'id="{pattern_id}"', self.patterns)
            contract_path = ROOT / day["contract_file"]
            contract = contract_path.read_text()
            self.assertIn(f"#q{qid[1:]}", contract)
            self.assertIn(deferred_id, contract)
            self.assertIn("45 minutes of the 85-minute practice block", contract)
            self.assertIn("40 minutes total", contract)
            self.assertIn("Runnable selector", contract)
            self.assertIn(assignment["bounded_task"], contract)
            self.assertEqual(assignment, self.cards[number]["engineering"]["interview_assignment"])
            owner_lines = self.cards[number]["learning_block"]["lines"]
            self.assertTrue(any(qid in line and deferred_id in line for line in owner_lines))
            for href in re.findall(r"\]\(([^)]+)\)", contract):
                if href.startswith("../") or href.startswith("../../"):
                    self.assertTrue((contract_path.parent / href.split("#", 1)[0]).resolve().is_file(), href)

    def test_aggregate_and_pair_commands_preserve_artifact_identity(self):
        for number in (146, 147, 151):
            day = self.days[number - 141]
            commands = day["engineering"]["acceptance_commands"]
            contract = (ROOT / day["contract_file"]).read_text()
            package = next(i for i, command in enumerate(commands)
                           if "-Plocal-app -DskipTests package" in command)
            test = next(i for i, command in enumerate(commands) if "-Dtest=" in command)
            self.assertLess(package, test)
            self.assertIn("target/capstone-starter-1.0-SNAPSHOT.jar", contract)
            self.assertIn("Package/artifact preparation", contract)
            self.assertFalse(any("classifier" in command.lower() for command in commands))
            self.assertIn(commands[package], contract)
            self.assertIn(commands[test], contract)
        for number in (148, 149, 153):
            day = self.days[number - 141]
            commands = day["engineering"]["acceptance_commands"]
            self.assertEqual(2, len(commands))
            self.assertIn("prepare-release-artifacts.sh", commands[0])
            self.assertIn("-Dtest=", commands[1])
            self.assertNotIn("prepare-release-artifacts.sh", commands[1])
            contract = (ROOT / day["contract_file"]).read_text()
            self.assertIn(commands[0], contract)
            self.assertIn(commands[1], contract)
            joined = "\n".join(commands)
            self.assertIn("$COVERSTORY_RELEASE_PAIR_DIR/baseline.jar", joined)
            self.assertIn("$COVERSTORY_RELEASE_PAIR_DIR/candidate.jar", joined)
            self.assertIn("$COVERSTORY_RELEASE_PAIR_DIR/artifact-pair-manifest.json", joined)
            self.assertNotIn("-Dcoverstory.release.artifact.provenance=", joined)
            self.assertNotIn("classifier", joined.lower())
        day154 = self.days[154 - 141]["engineering"]["acceptance_commands"]
        self.assertEqual(2, len(day154))
        self.assertIn("prepare-release-artifacts.sh", day154[0])
        self.assertIn("artifact-pair-manifest.json", day154[1])
        prep = (ROOT / "scripts/prepare-release-artifacts.sh").read_text()
        self.assertIn("worktree add --detach", prep)
        self.assertIn("baseline.jar", prep)
        self.assertIn("candidate.jar", prep)
        self.assertIn("artifact-pair-manifest.json", prep)
        self.assertIn('git -C "$repo" diff --exit-code "$baseline_sha" "$candidate_sha" -- src/main/resources/db/migration', prep)
        self.assertIn('jar tf "$run_dir/baseline.jar"', prep)
        self.assertIn('jar tf "$run_dir/candidate.jar"', prep)
        for number in (146, 147, 151):
            contract = (ROOT / self.days[number - 141]["contract_file"]).read_text()
            self.assertIn("./mvnw -Plocal-app -DskipTests package", contract)

    def test_rendered_selector_commands_and_provenance_match_manifest(self):
        for day in self.days:
            contract = (ROOT / day["contract_file"]).read_text()
            prep_match = re.search(r"(?ms)^## Package/artifact preparation\n.*?```sh\n(.*?)\n```", contract)
            selector_match = re.search(r"(?ms)^## Selector\n\n```sh\n(.*?)\n```", contract)
            prep = prep_match.group(1).splitlines() if prep_match else []
            selectors = selector_match.group(1).splitlines() if selector_match else []
            expected = day["engineering"]["acceptance_commands"]
            self.assertEqual(expected, prep + selectors, day["day"])
        prep_script = (ROOT / "scripts/prepare-release-artifacts.sh").read_text()
        self.assertIn('sha256', prep_script)
        self.assertIn('source_commit', prep_script)
        self.assertIn('overlay', prep_script)

    def test_generator_syncs_documents_and_never_resets_attempt_evidence(self):
        generator = (ROOT / "scripts/create_days141_154_docs.py").read_text()
        self.assertIn("manifest = read_json(MANIFEST_PATH)", generator)
        self.assertIn("write_json(EXPORT_PATH, export)", generator)
        self.assertIn("sync_contract(day)", generator)
        self.assertNotRegex(generator, r"(?s)evidence[^\n]*\.write_text|write_text[^\n]*evidence")
        self.assertNotIn("status': 'not_attempted'", generator)
        readme = (SPRINT / "README.md").read_text()
        self.assertIn("four spaced 45-minute", readme)
        self.assertIn("assistant notes supply capstone architecture", readme)


if __name__ == "__main__":
    unittest.main()
