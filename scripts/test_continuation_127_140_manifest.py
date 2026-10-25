import datetime
from decimal import Decimal
import json
import pathlib
import re
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
BASE = "e6efb002431b77d0b6c5ec1cad9602ba45f8ea2d"
CON = ROOT / "docs/reboot/continuation-127-140"


class Continuation127To140ManifestTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads((CON / "manifest.json").read_text())
        cls.export = json.loads((CON / "scrum-owner-manifest.json").read_text())

    def test_days_dates_budgets_and_sprint_are_exact(self):
        days = self.manifest["days"]
        self.assertEqual(list(range(127, 141)), [d["day"] for d in days])
        expected = datetime.date(2027, 3, 1)
        self.assertEqual([expected + datetime.timedelta(days=i) for i in range(14)],
                         [datetime.date.fromisoformat(d["date"]) for d in days])
        self.assertEqual("Sprint 10", self.manifest["sprint"])
        self.assertEqual("2027-03-14", self.manifest["end_date"])
        self.assertEqual([133, 140], [d["day"] for d in days if d["recovery_day"]])
        for day in days:
            e = day["engineering"]
            self.assertEqual(120, e["budget_minutes"])
            self.assertEqual({"implementation_and_testing": 85, "concept_or_course": 25, "evidence": 10}, e["allocation_minutes"])
            self.assertEqual(1500, e["study_seconds"]["total"])

    def test_course_metadata_titles_sections_durations_and_ticket_blocks_match(self):
        catalog = {c["course_id"]: c for c in self.manifest["course_catalog"]["courses"]}
        catalog_lessons = {
            "20263094": ("What is Cloud Computing?", "Shared Responsibility Model & AWS Acceptable Policy", 96, "3142166", "4759284"),
            "26097996": ("IAM & AWS CLI", "IAM Introduction: Users, Groups, Policies", 202, "2196488", "6258010"),
            "26098112": ("EC2 Fundamentals", "EC2 Basics", 212, "2196488", "6258026"),
            "26098134": ("EC2 Fundamentals", "Security Groups & Classic Ports Overview", 438, "2196488", "6258026"),
            "33878680": ("AWS Fundamentals: RDS + Aurora + ElastiCache", "RDS & Aurora - Backup and Monitoring", 336, "2196488", "3172806"),
            "20056176": ("Cloud Monitoring", "CloudWatch Metrics & CloudWatch Alarms Overview", 216, "3142166", "4759822"),
            "13528576": ("Other Services", "CloudFormation Intro", 211, "2196488", "3172902"),
            "20118372": ("Account Management, Billing & Support", "Monitoring Costs in the Cloud - Billing Alarms & AWS Budgets", 264, "3142166", "4759866"),
        }
        cards = {c["day_number"]: c for c in self.export["cards"]}
        self.assertEqual(14, len(cards))
        video_total = 0
        for day in self.manifest["days"]:
            block = day["engineering"]["concept_course"]
            self.assertEqual(25, block["minutes"])
            ss = day["engineering"]["study_seconds"]
            self.assertEqual(1500, ss["video"] + ss["worked_reference"] + ss["teachback"])
            self.assertEqual(1500, ss["total"])
            for lesson in block["lessons"]:
                section, title, seconds, cid, sid = catalog_lessons[lesson["source_item_id"]]
                self.assertEqual((section, title, seconds, cid, sid),
                                 (lesson["section_title"], lesson["title"], lesson["duration_seconds"], lesson["course_id"], lesson["section_id"]))
                self.assertIn(cid, catalog)
                self.assertIn(lesson["course_id"], {c["course_id"] for c in block["courses"]})
                video_total += seconds
            card = cards[day["day"]]
            self.assertEqual(day["title"], card["title"])
            self.assertEqual(day["engineering"]["acceptance_commands"], card["engineering"]["acceptance_commands"])
            self.assertEqual(block, card["engineering"]["concept_course"])
            contract = (ROOT / day["contract_file"]).read_text()
            self.assertTrue((ROOT / day["engineering"]["acceptance_test_path"]).is_file())
            harness = day["engineering"].get("instructor_harness_test_path")
            if harness:
                self.assertTrue((ROOT / harness).is_file())
            command_block = contract.split("- Commands:", 1)[1].split("- Evidence:", 1)[0]
            contract_commands = re.findall(r"- `([^`]+)`", command_block)
            self.assertEqual(day["engineering"]["acceptance_commands"], contract_commands, day["day"])
            for lesson in block["lessons"]:
                self.assertIn(lesson["title"], contract)
                self.assertIn(lesson["section_title"], contract)
        self.assertEqual(1975, video_total)
        self.assertFalse(self.manifest["course_catalog"]["courses"][0]["video_content_audited"])

    def test_packaged_cutover_harness_is_synchronized(self):
        selectors = {
            134: ("./mvnw -Plocal-app,learner-tests -Dtest="
                  "LocalReleaseCutoverAcceptanceTest#day134PackagedCandidateCheckLeavesKnownGood test"),
            135: ("./mvnw -Plocal-app,learner-tests -Dtest="
                  "LocalReleaseCutoverAcceptanceTest#day135PackagedRollbackBlocksAfterDivergence test"),
            139: ("./mvnw -Plocal-app,learner-tests -Dtest="
                  "LocalReleaseCutoverAcceptanceTest#day134PackagedCandidateCheckLeavesKnownGood+day135PackagedRollbackBlocksAfterDivergence test"),
            140: ("./mvnw -Plocal-app,learner-tests -Dtest="
                  "LocalReleaseCutoverAcceptanceTest#day134PackagedCandidateCheckLeavesKnownGood+day135PackagedRollbackBlocksAfterDivergence test"),
        }
        for number, selector in selectors.items():
            day = self.manifest["days"][number - 127]
            self.assertIn(selector, day["engineering"]["acceptance_commands"])
            contract = (ROOT / day["contract_file"]).read_text()
            self.assertIn(f"- `{selector}`", contract)
            export = next(card for card in self.export["cards"] if card["day_number"] == number)
            self.assertEqual(day["engineering"]["acceptance_commands"], export["engineering"]["acceptance_commands"])

    def test_day138_selector_resolves_to_a_real_test_method(self):
        day = self.manifest["days"][138 - 127]
        selector = "./mvnw -Plearner-tests -Dtest=AwsLabDesignPolicyAcceptanceTest#day138CostsRetainedResourcesWithoutInventingPrices test"
        self.assertIn(selector, day["engineering"]["acceptance_commands"])
        source = (ROOT / "src/test/java/assignments/ops/AwsLabDesignPolicyAcceptanceTest.java").read_text()
        self.assertRegex(source, r"@Test\s+void\s+day138CostsRetainedResourcesWithoutInventingPrices\s*\(")

    def test_every_named_method_selector_resolves_in_source(self):
        for day in self.manifest["days"]:
            for command in day["engineering"]["acceptance_commands"]:
                match = re.search(r"-Dtest=([\w.,+]+)#([\w+]+)", command)
                if not match:
                    continue
                classes, methods = match.groups()
                class_name = classes.split(",")[-1]
                source = ROOT / "src/test/java/assignments/ops" / f"{class_name}.java"
                self.assertTrue(source.is_file(), (day["day"], command, source))
                contents = source.read_text()
                for method in methods.split("+"):
                    self.assertRegex(contents, rf"@Test\s+void\s+{re.escape(method)}\s*\(",
                                     (day["day"], command, method))

    def test_restore_and_cleanup_selectors_are_published(self):
        d131 = self.manifest["days"][131 - 127]["engineering"]["acceptance_commands"]
        self.assertTrue(any("day131ValidArchiveFailureRollsBackWorkAlreadyStarted" in c for c in d131))
        d136 = self.manifest["days"][136 - 127]["engineering"]["acceptance_commands"]
        self.assertTrue(any("day136ToolWatchdogCleansOnlyItsContainerAndPreservesSentinel" in c for c in d136))

    def test_recovery_commands_are_prior_only_and_paths_resolve(self):
        for day in self.manifest["days"]:
            self.assertTrue((ROOT / day["contract_file"]).is_file())
            self.assertTrue((ROOT / day["engineering"]["evidence_reference"]).is_file())
            for command in day["engineering"]["acceptance_commands"]:
                if "cd frontend" in command:
                    self.assertIn("(cd frontend", command)
        d133 = " ".join(self.manifest["days"][6]["engineering"]["acceptance_commands"])
        d140 = " ".join(self.manifest["days"][13]["engineering"]["acceptance_commands"])
        self.assertNotIn("day134", d133.lower())
        self.assertNotIn("day140", d133.lower())
        self.assertNotIn("day141", d140.lower())
        self.assertTrue(self.manifest["days"][6]["recovery_day"])
        self.assertTrue(self.manifest["days"][13]["recovery_day"])

    def test_assistant_architecture_and_question_sessions_match_all_exports(self):
        expected_mocks = {
            128: ("Q36", "Object-store recovery", "S10-D128-MOCK", ["object-storage", "resilience"]),
            131: ("Q14", "Booking: retry after a lost response", "S10-D131-MOCK",
                  ["idempotency", "transaction-boundary", "frontend-state-machine"]),
            135: ("Q16", "Messaging: ordering and replay", "S10-D135-MOCK",
                  ["outbox-and-messaging", "idempotency", "transaction-boundary"]),
            138: ("Q28", "Feed: fan-out choice", "S10-D138-MOCK",
                  ["feed-fan-out", "read-scaling", "measure-before-scaling"]),
        }
        cards = {card["day_number"]: card for card in self.export["cards"]}
        self.assertEqual(set(range(127, 141)), set(cards))
        notes = (CON / "assistant-architecture-notes.md").read_text()
        question_bank = (ROOT / "docs/reboot/interview/question-bank.md").read_text().lower()
        pattern_library = (ROOT / "docs/reboot/interview/pattern-library.md").read_text().lower()

        for day in self.manifest["days"]:
            number = day["day"]
            note_ref = day["engineering"]["assistant_architecture_note"]
            self.assertTrue((ROOT / note_ref.split("#", 1)[0]).is_file(), number)
            self.assertIn(f"## Day {number}", notes)
            contract = (ROOT / day["contract_file"]).read_text()
            card = cards[number]
            self.assertIn("Assistant-provided architecture", contract)
            self.assertIn("no capstone design proposal", contract.lower())
            self.assertEqual(note_ref, card["learning_block"]["architecture_note"])
            self.assertIn(note_ref, day["engineering"]["concept_course"]["scope_note"])
            self.assertTrue(any("Assistant-provided capstone architecture" in line
                                and f"day-{number}" in line
                                for line in card["learning_block"]["lines"]))

            assignment = day["engineering"].get("interview_assignment")
            if number not in expected_mocks:
                self.assertIsNone(assignment, number)
                continue

            qid, title, deferred, patterns = expected_mocks[number]
            self.assertIsNotNone(assignment, number)
            self.assertEqual(qid, assignment["question_id"])
            self.assertEqual(title, assignment["question_title"])
            self.assertEqual("system_design_interview_question_only", assignment["proposal_scope"])
            self.assertEqual("assistant_supplied", assignment["capstone_architecture"])
            self.assertEqual(deferred, assignment["deferred_work_id"])
            self.assertEqual(85, assignment["practice_minutes_available"])
            self.assertEqual(45, assignment["mock_minutes"])
            self.assertEqual(40, assignment["practice_total_minutes"] - assignment["mock_minutes"])
            self.assertEqual(40, sum(assignment["eai_slice_minutes"].values()))
            self.assertEqual(85, assignment["mock_minutes"] + sum(assignment["eai_slice_minutes"].values()))
            self.assertEqual(45, sum(phase["minutes"] for phase in assignment["mock_phases"]))
            self.assertIn(qid.lower(), question_bank)
            self.assertEqual(patterns, [ref.rsplit("#", 1)[1] for ref in assignment["pattern_refs"]])
            for anchor in patterns:
                self.assertIn(f"id=\"{anchor}\"", pattern_library)
            self.assertIn(assignment["bounded_task"], contract)
            self.assertIn(deferred, contract)
            self.assertIn("45-minute mock", contract)
            self.assertIn("bounded capstone task", contract.lower())
            for href in re.findall(r"\]\(([^)]+)\)", contract):
                if href.startswith("../") or href.startswith("../../"):
                    self.assertTrue(((ROOT / day["contract_file"]).parent / href.split("#", 1)[0]).resolve().is_file(),
                                    (number, href))
            self.assertEqual(assignment, card["engineering"]["interview_assignment"])
            self.assertTrue(any(qid in line and "45-minute interview practice" in line
                                for line in card["learning_block"]["lines"]))

    def test_aggregate_recovery_runs_use_fresh_ordinary_jar_without_classifier_drift(self):
        consumers = (
            "LocalReleaseCutoverAcceptanceTest",
            "LocalRestoreApplicationAcceptanceTest",
            "LocalPackagedManualRecoveryAcceptanceTest",
        )
        for number in (139, 140):
            day = self.manifest["days"][number - 127]
            commands = day["engineering"]["acceptance_commands"]
            contract = (ROOT / day["contract_file"]).read_text()
            owner = next(card for card in self.export["cards"] if card["day_number"] == number)
            self.assertEqual(commands, owner["engineering"]["acceptance_commands"])
            frontend = next(i for i, c in enumerate(commands) if "npm run build:local" in c)
            ordinary_package = next(i for i, c in enumerate(commands)
                                    if "-Plocal-app -DskipTests package" in c)
            self.assertGreater(ordinary_package, frontend)
            self.assertTrue(all(ordinary_package < i for i, c in enumerate(commands)
                                if any(name in c for name in consumers)))
            package_commands = [c for c in commands if "-DskipTests package" in c]
            self.assertEqual(1, len(package_commands))
            self.assertIn("-Plocal-app -DskipTests package", package_commands[0])
            self.assertFalse(any("package-sprint" in c or "classifier" in c.lower()
                                 for c in commands))
            runtime_helper = (ROOT / "src/test/java/assignments/local/LocalPackagedRuntimeProcess.java").read_text()
            self.assertIn('Path.of("target/capstone-starter-1.0-SNAPSHOT.jar")', runtime_helper)
            self.assertNotIn("sprint6.jar", contract.lower())
            self.assertNotIn("sprint8.jar", contract.lower())
            self.assertIn(package_commands[0], contract)

    def test_offline_design_cannot_enable_cloud_or_create_resources(self):
        design = json.loads((CON / "aws-lab-design.json").read_text())
        self.assertEqual("offline_plan_only", design["status"])
        self.assertFalse(design["cloud_execution_authorized"])
        self.assertFalse(design["cloud_action_requested"])
        self.assertFalse(design["cloud_verified"])
        self.assertFalse(design["permission_review"]["grants_created"])
        self.assertFalse(design["candidate_topology"]["deployment_template"])
        self.assertFalse((CON / "cloudformation.yaml").exists())
        self.assertFalse(self.manifest["cloud_execution_not_performed"] is False)
        topology = design["candidate_topology"]
        self.assertFalse(topology["public_application"])
        self.assertFalse(topology["public_database"])
        self.assertFalse(topology["networking_selected"])
        model = design["cost_model"]
        lines = model["lines"]
        components = {line["component"] for line in lines}
        self.assertTrue({"compute", "database-storage", "backups-snapshots", "logs", "secrets", "transfer"} <= components)
        self.assertTrue(all(line["currency"] == "USD" and line["label"] == "FICTIONAL_TEST_RATE" for line in lines))
        for line in lines:
            self.assertEqual(Decimal(line["rate"]) * Decimal(line["quantity"]), Decimal(line["amount"]))
        self.assertEqual(sum((Decimal(line["amount"]) for line in lines), Decimal("0")), Decimal(model["total"]))


if __name__ == "__main__":
    unittest.main()
