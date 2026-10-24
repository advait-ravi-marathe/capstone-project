import datetime
import json
import pathlib
import re
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
BASE = ROOT / "docs/reboot/continuation-113-126"
EXPECTED_LESSONS = {
    115: ("Step 12 - Creating a custom Spring JDBC RowMapper", "Journey From Spring  JDBC to JPA", "7907420", 298),
    116: ("Step 11 - Implementing insert and update Spring JDBC Update Methods", "Journey From Spring  JDBC to JPA", "7907416", 519),
    120: ("Step 63 - Implementing Transaction Management - 3 Things to Decide", "Transaction Management", "7907620", 278),
    122: ("Step 62 - Choosing between Isolation Levels", "Transaction Management", "7907618", 243),
    124: ("Step 06 - Execute the findAll method using CommandLineRunner", "Journey From Spring  JDBC to JPA", "7907404", 345),
}

class ContinuationManifestTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads((BASE / "manifest.json").read_text())
        cls.export = json.loads((BASE / "scrum-owner-manifest.json").read_text())

    def test_day_date_budget_and_export_identity(self):
        self.assertEqual(list(range(113, 127)), [d["day"] for d in self.manifest["days"]])
        self.assertEqual(14, self.export["story_count"])
        self.assertEqual(14, len(self.export["cards"]))
        for offset, (day, card) in enumerate(zip(self.manifest["days"], self.export["cards"])):
            self.assertEqual(113 + offset, day["day"])
            self.assertEqual((datetime.date(2027, 2, 13) + datetime.timedelta(days=offset)).isoformat(), day["date"])
            self.assertEqual(day["day"], card["day_number"])
            self.assertEqual(day["date"], card["date"])
            self.assertEqual(day["title"], card["title"])
            self.assertEqual(day["engineering"], card["engineering"])
            self.assertEqual(120, day["engineering"]["budget_minutes"])
            self.assertEqual({"implementation_and_testing": 85, "concept_or_course": 25, "evidence": 10}, day["engineering"]["allocation_minutes"])
            study = day["engineering"]["study_seconds"]
            self.assertEqual(1500, study["total"])
            self.assertEqual(1500, study["video"] + study["worked_reference"] + study["teachback"])
            contract = ROOT / day["contract_file"]
            self.assertTrue(contract.is_file(), contract)
            text = contract.read_text()
            self.assertIn(day["title"], text)
            for command in day["engineering"]["acceptance_commands"]:
                self.assertIn(command, text)
            self.assertTrue((ROOT / day["engineering"]["evidence_reference"]).is_file())

    def test_exact_verified_course_records_only_on_matched_days(self):
        found = {}
        for day in self.manifest["days"]:
            lessons = day["engineering"]["concept_course"]["lessons"]
            if lessons:
                found[day["day"]] = lessons[0]
                self.assertEqual(1500, day["engineering"]["study_seconds"]["total"])
                course = day["engineering"]["concept_course"]["courses"][0]
                self.assertEqual("1341538", course["course_id"])
                self.assertEqual("https://www.udemy.com/course/hibernate-jpa-tutorial-for-beginners-in-100-steps/", course["url"])
                self.assertEqual("unconfirmed_reference_only", course["access_status"])
                md = (ROOT / day["contract_file"]).read_text()
                self.assertIn(lessons[0]["title"], md)
                self.assertIn(lessons[0]["section_title"], md)
        self.assertEqual(set(EXPECTED_LESSONS), set(found))
        for day, expected in EXPECTED_LESSONS.items():
            lesson = found[day]
            self.assertEqual(expected, (lesson["title"], lesson["section_title"], lesson["source_item_id"], lesson["duration_seconds"]))
            minutes, seconds = divmod(lesson["duration_seconds"], 60)
            self.assertEqual(f"{minutes}:{seconds:02d}", lesson["duration"])


    def test_assistant_architecture_and_interview_only_mocks_are_synchronized(self):
        note = BASE / "assistant-architecture-notes.md"
        self.assertTrue(note.is_file())
        mock_days = {115: "Q20", 118: "Q23", 122: "Q16", 125: "Q22"}
        for day, card in zip(self.manifest["days"], self.export["cards"]):
            number = day["day"]
            eng = day["engineering"]
            owner_eng = card["engineering"]
            self.assertEqual(eng, owner_eng)
            note_ref = eng["assistant_architecture_note"]
            self.assertTrue(note_ref.endswith(f"#day-{number}"))
            contract = (ROOT / day["contract_file"]).read_text()
            self.assertIn("Assistant-provided architecture", contract)
            self.assertIn(f"#day-{number}", contract)
            self.assertIn("no capstone proposal", contract.lower())
            self.assertNotIn("propose the capstone architecture", contract.lower())
            assignment = eng.get("interview_assignment")
            if number not in mock_days:
                self.assertIsNone(assignment, f"day {number} should not acquire a mock")
                continue
            self.assertIsNotNone(assignment)
            self.assertEqual(mock_days[number], assignment["question_id"])
            self.assertEqual("system_design_interview_question_only", assignment["proposal_scope"])
            self.assertEqual("assistant_supplied", assignment["capstone_architecture"])
            self.assertEqual(45, sum(p["minutes"] for p in assignment["mock_phases"]))
            self.assertEqual(40, sum(assignment["eai_slice_minutes"].values()))
            self.assertEqual(85, assignment["practice_total_minutes"])
            self.assertEqual(f"S9-D{number}-MOCK", assignment["deferred_work_id"])
            self.assertIn("do not make capstone design decisions", contract.lower())
            self.assertIn(assignment["question_href"], contract)
            self.assertIn(assignment["acceptance_commands"][0], contract)
            for ref in [assignment["question_href"], *assignment["pattern_refs"]]:
                target, anchor = ref.split("#", 1)
                path = (BASE / target).resolve()
                self.assertTrue(path.is_file(), path)
                self.assertIn(f"id=\"{anchor}\"", path.read_text(), f"missing anchor {ref}")

    def test_day124_command_selector_covers_positive_and_negative_argument_paths(self):
        day = next(d for d in self.manifest["days"] if d["day"] == 124)
        command = "./mvnw -Plearner-tests -Dtest=LocalManualRecoveryCommandTest test"
        self.assertIn(command, day["engineering"]["acceptance_commands"])
        card = next(c for c in self.export["cards"] if c["day_number"] == 124)
        self.assertIn(command, card["engineering"]["acceptance_commands"])
        self.assertEqual("installed_expected_todo_red", day["engineering"]["acceptance_selector_status"])
        contract = (ROOT / day["contract_file"]).read_text()
        self.assertIn(command, contract)
        source = (ROOT / "src/test/java/assignments/delivery/recovery/LocalManualRecoveryCommandTest.java").read_text()
        for method in ("oneActionCommandRejectsInvalidAndConflictingFlags", "oneExplicitInspectDispatchesOnlyTheRequestedExactKey", "ordinaryStartupDoesNotDispatchRecovery"):
            self.assertIn(method, source)

    def test_days125_126_rebuild_receipt_bound_sprint8_artifact_before_consumers(self):
        package = "scripts/package-sprint8-demo.sh --no-clean"
        consumer = "./mvnw -Plocal-app,learner-tests -Dtest=LocalPackagedIssuanceDeliveryAcceptanceTest test"
        helper = (ROOT / "src/test/java/assignments/local/Sprint8PackagedRuntimeProcess.java").read_text()
        script = (ROOT / "scripts/package-sprint8-demo.sh").read_text()
        fingerprint = (ROOT / "scripts/sprint8-source-fingerprint.py").read_text()
        self.assertIn("verifyArtifactReceipt", helper)
        self.assertIn("sourceCommit", helper)
        self.assertIn("sourceContentSha256", helper)
        self.assertIn("sourceContentSha256", script)
        self.assertIn('source_sha="$(python3 scripts/sprint8-source-fingerprint.py)"', script)
        self.assertIn("sprint8-source-fingerprint.py", script)
        self.assertIn("--no-clean", script)
        consumer_test = (ROOT / "src/test/java/assignments/local/LocalPackagedIssuanceDeliveryAcceptanceTest.java").read_text()
        self.assertIn("Sprint8PackagedRuntimeProcess.start", consumer_test)
        for number in (125, 126):
            day = next(d for d in self.manifest["days"] if d["day"] == number)
            card = next(c for c in self.export["cards"] if c["day_number"] == number)
            commands = day["engineering"]["acceptance_commands"]
            self.assertEqual(commands, card["engineering"]["acceptance_commands"])
            contract = (ROOT / day["contract_file"]).read_text()
            self.assertIn(package, commands)
            self.assertLess(commands.index("./mvnw -Plocal-app -DskipTests package"), commands.index(package))
            self.assertLess(commands.index(package), commands.index(consumer))
            self.assertLess(contract.index("./mvnw -Plocal-app -DskipTests package"), contract.index(package))
            self.assertLess(contract.index(package), contract.index(consumer))
        self.assertIn("from hashlib import sha256", fingerprint)
        self.assertIn("digest = sha256()", fingerprint)

    def test_sprint8_dedicated_runtime_is_not_mislabeled_broad(self):
        contract = (ROOT / "docs/reboot/continuation-99-112/contracts/day-110.md").read_text().lower()
        self.assertIn("dedicated sprint 8 bounded packaged runtime", contract)
        self.assertNotIn("broad default runtime", contract)
        for name in ("manifest.json", "scrum-owner-manifest.json"):
            data = json.loads((ROOT / "docs/reboot/continuation-99-112" / name).read_text())
            items = data.get("days") or data["cards"]
            day110 = next(d for d in items if d.get("day", d.get("day_number")) == 110)
            serialized = json.dumps(day110["engineering"]).lower()
            self.assertNotIn("broad default runtime", serialized)

    def test_recovery_days_only_run_prior_work_and_no_unresolved_commands(self):
        for day in (119, 126):
            text = (ROOT / f"docs/reboot/continuation-113-126/contracts/day-{day}.md").read_text()
            self.assertIn("Recovery/remediation only", text)
        for day in self.manifest["days"]:
            for command in day["engineering"]["acceptance_commands"]:
                self.assertNotIn("TODO", command)
                if command.startswith("(cd frontend &&"):
                    self.assertEqual("(cd frontend && npm ci && npm run build && npm run build:local)", command)
                elif command.startswith("scripts/package-sprint8-demo.sh"):
                    self.assertEqual("scripts/package-sprint8-demo.sh --no-clean", command)
                else:
                    self.assertIn("./mvnw", command)

if __name__ == "__main__":
    unittest.main()
