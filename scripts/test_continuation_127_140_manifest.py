import datetime
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
        expected = datetime.date(2027, 2, 23)
        self.assertEqual([expected + datetime.timedelta(days=i) for i in range(14)],
                         [datetime.date.fromisoformat(d["date"]) for d in days])
        self.assertEqual("Sprint 10", self.manifest["sprint"])
        self.assertEqual("2027-03-08", self.manifest["end_date"])
        self.assertEqual([133, 140], [d["day"] for d in days if d["recovery_day"]])
        for day in days:
            e = day["engineering"]
            self.assertEqual(120, e["budget_minutes"])
            self.assertEqual({"implementation_and_testing": 85, "concept_or_course": 25, "evidence": 10}, e["allocation_minutes"])
            self.assertEqual(1500, e["study_seconds"]["total"])
            self.assertEqual(60, day["java_dsa"]["budget_minutes"])
            self.assertTrue(day["java_dsa"]["actual_attempts_only"])
            self.assertFalse(day["java_dsa"]["new_anchor_required"])

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

    def test_offline_design_cannot_enable_cloud_or_create_resources(self):
        design = json.loads((CON / "aws-lab-design.json").read_text())
        self.assertEqual("offline_plan_only", design["status"])
        self.assertFalse(design["cloud_execution_authorized"])
        self.assertFalse(design["cloud_verified"])
        self.assertFalse(design["permission_review"]["grants_created"])
        self.assertFalse(design["candidate_topology"]["deployment_template"])
        self.assertFalse((CON / "cloudformation.yaml").exists())
        self.assertFalse(self.manifest["cloud_execution_not_performed"] is False)


if __name__ == "__main__":
    unittest.main()
