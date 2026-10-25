"""Regression checks for the stable-unit/calendar-study-day migration."""
import json
import re
import subprocess
import sys
import unittest
from datetime import date
from pathlib import Path
from scripts import generate_study_calendar

ROOT = Path(__file__).resolve().parents[1]
CALENDAR = ROOT / "docs/reboot/calendar/study-calendar.json"
EXPORT = ROOT / "docs/reboot/calendar/story-export-1-305.json"


class StudyCalendarMigrationTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.calendar = json.loads(CALENDAR.read_text())
        cls.export = json.loads(EXPORT.read_text())

    def test_generator_matches_checked_in_crosswalk_and_export(self):
        subprocess.run([sys.executable, "scripts/generate_study_calendar.py", "--check"], cwd=ROOT, check=True)
        subprocess.run([sys.executable, "scripts/generate_gap_assignments.py", "--check"], cwd=ROOT, check=True)

    def test_exact_dates_insertions_and_horizon(self):
        self.assertEqual("2026-10-26", self.calendar["start_date"])
        self.assertEqual((732, 42, 774), (self.calendar["legacy_horizon"], self.calendar["inserted_study_days"], self.calendar["calendar_horizon"]))
        self.assertEqual("2028-12-07", self.calendar["end_date"])
        rows = self.calendar["calendar"]
        self.assertEqual(774, len(rows))
        self.assertEqual("2027-04-15", self.calendar["budget"]["calendar_day_172_date"])
        self.assertEqual("GAP-OPENAPI-01", rows[70]["unit_id"])
        self.assertEqual("GAP-OIDC-09", rows[83]["unit_id"])
        self.assertEqual("GAP-SCHEMA-07", rows[174]["unit_id"])
        self.assertEqual("GAP-AIGOV-07", rows[465]["unit_id"])
        self.assertEqual("GAP-ENTERPRISE-07", rows[563]["unit_id"])
        self.assertEqual("EAI-UNIT-732", rows[773]["unit_id"])
        self.assertEqual("2028-12-07", rows[773]["date"])

    def test_dsa_references_follow_calendar_and_remain_external(self):
        rows = self.calendar["calendar"]
        self.assertEqual((1, "daily_problem_day"), (rows[0]["dsa_calendar_reference"], rows[0]["dsa_reference_kind"]))
        self.assertEqual((150, "daily_problem_day"), (rows[149]["dsa_calendar_reference"], rows[149]["dsa_reference_kind"]))
        self.assertEqual((151, "attempt_relative_review_window_day"), (rows[150]["dsa_calendar_reference"], rows[150]["dsa_reference_kind"]))
        self.assertEqual((171, "attempt_relative_review_window_day"), (rows[170]["dsa_calendar_reference"], rows[170]["dsa_reference_kind"]))
        self.assertIsNone(rows[171]["dsa_calendar_reference"])
        self.assertTrue(self.calendar["dsa_boundary"]["actual_attempt_dates_and_evidence_mutated"] is False)
        self.assertFalse(any(p.name.startswith("dsa") for p in (ROOT / "docs").iterdir()))

    def test_305_story_crosswalk_keeps_ids_and_sprint_history(self):
        stories = self.export["stories"]
        self.assertEqual(305, self.export["story_count"])
        self.assertEqual(list(range(1, 306)), [row["calendar_study_day"] for row in stories])
        existing = [row for row in stories if row["legacy_engineering_unit_number"] is not None]
        self.assertEqual(list(range(1, 285)), [row["legacy_engineering_unit_number"] for row in existing])
        for row in existing:
            unit = row["legacy_engineering_unit_number"]
            expected_day = unit if unit <= 70 else unit + (14 if unit <= 154 else 21 if unit <= 438 else 28 if unit <= 522 else 42)
            self.assertEqual(expected_day, row["calendar_study_day"])
            self.assertEqual(row["calendar_study_day"], row["owner_card"]["calendar_study_day"])
            self.assertEqual(unit, row["owner_card"]["engineering_unit_number"])
            self.assertEqual(row["date"], row["owner_card"]["date"])
            self.assertEqual(row["calendar_sprint"], row["sprint_membership_migration"]["calendar_sprint_number"])
            if 71 <= unit <= 196:
                provenance = json.loads((ROOT / "docs/reboot/calendar/legacy-sprint-provenance.json").read_text())
                self.assertEqual(provenance["unit_sprint_labels"][str(unit)], row["owner_card"]["legacy_sprint_label"])
            if row["calendar_study_day"] <= 150:
                self.assertEqual(row["calendar_study_day"], row["dsa_section"].get("dsa_calendar_day"))
            elif row["calendar_study_day"] <= 171:
                self.assertEqual("attempt_relative_review_window", row["dsa_section"]["mode"])
            else:
                self.assertEqual("none", row["dsa_section"]["mode"])
        sprints = self.calendar["sprint_migration"]
        self.assertEqual((305, 21, 22, 11), (sprints["detailed_calendar_story_count"], sprints["full_calendar_sprints"], sprints["calendar_sprint_count"], sprints["final_partial_sprint_days"]))
        self.assertFalse(self.export["board_writes"])
        self.assertTrue(self.export["new_issue_ids_assigned"])

    def test_all_284_existing_eai_ids_are_mapped_and_only_new_gaps_are_unassigned(self):
        detailed = [row for row in self.export["stories"] if row["legacy_engineering_unit_number"] is not None]
        self.assertEqual([f"EAI-{unit}" for unit in range(1, 285)], [row["eai_issue_id"] for row in detailed])
        self.assertEqual({"verified_existing_eai_issue_id"}, {row["issue_id_mapping_status"] for row in detailed})
        inserted = [row for row in self.export["stories"] if row["unit_id"].startswith("GAP-")]
        self.assertEqual(21, len(inserted))
        self.assertEqual([f"EAI-{issue}" for issue in range(285, 306)], [row["new_eai_issue_id"] for row in inserted])
        self.assertTrue(all(row["eai_issue_id"] is None for row in inserted))
        self.assertTrue(all(row["issue_id_mapping_status"] == "verified_new_board_story_id" for row in inserted))
        self.assertIn("verified in the 2026-10-25 board publication receipt", self.calendar["source_id_provenance"])
        detailed_calendar_rows = [row for row in self.calendar["calendar"] if row["detail_status"] == "detailed_contract"]
        calendar_ids = [row["existing_eai_issue_id"] for row in detailed_calendar_rows]
        self.assertEqual([f"EAI-{issue}" for issue in range(1, 306)],
                         sorted(calendar_ids, key=lambda issue: int(issue.split("-")[1])))

    def test_board_publication_receipt_maps_all_ids_without_changing_learner_status(self):
        receipt = self.calendar["board_publication_receipt"]
        self.assertEqual({
            "verified_on": "2026-10-25",
            "status": "published_readback_verified",
            "published_story_count": 305,
            "unique_story_ids": 305,
            "all_story_statuses": "Open",
            "calendar_sprints": 22,
            "existing_sprints_reused": 21,
            "new_native_sprint_number": 22,
            "final_sprint_calendar_days": [295, 305],
            "final_sprint_dates": ["2027-08-16", "2027-08-26"],
            "existing_stories_rewritten": 214,
            "new_stories_created": 21,
            "first_70_stories_untouched": True,
            "conditional_milestones_saved_to_existing_project_description": 7,
            "default_dashboard_touched": False,
        }, {key: receipt[key] for key in (
            "verified_on", "status", "published_story_count", "unique_story_ids", "all_story_statuses",
            "calendar_sprints", "existing_sprints_reused", "new_native_sprint_number",
            "final_sprint_calendar_days", "final_sprint_dates", "existing_stories_rewritten",
            "new_stories_created", "first_70_stories_untouched",
            "conditional_milestones_saved_to_existing_project_description", "default_dashboard_touched")})
        self.assertEqual({
            **{f"GAP-OPENAPI-{day:02}": f"EAI-{issue}" for day, issue in zip(range(1, 6), range(285, 290))},
            **{f"GAP-OIDC-{day:02}": f"EAI-{issue}" for day, issue in zip(range(1, 10), range(290, 299))},
            **{f"GAP-SCHEMA-{day:02}": f"EAI-{issue}" for day, issue in zip(range(1, 8), range(299, 306))},
        }, receipt["new_story_issue_ids"])
        self.assertEqual("new_native_sprint_verified", self.calendar["sprint_migration"]["calendar_sprint_22_kind"])
        self.assertEqual("not_included_in_readback", self.calendar["sprint_migration"]["calendar_sprint_22_internal_id_status"])
        outcome_only = [row for row in self.calendar["calendar"] if row["detail_status"] == "outcome_only_not_ticketed"]
        self.assertTrue(all(row["existing_eai_issue_id"] is None for row in outcome_only))
        issue_ids = [story["eai_issue_id"] or story["new_eai_issue_id"] for story in self.export["stories"]]
        self.assertEqual([f"EAI-{issue}" for issue in range(1, 306)],
                         sorted(issue_ids, key=lambda issue: int(issue.split("-")[1])))
        self.assertEqual(305, len(set(issue_ids)))
        self.assertTrue(all(story["board_status"] == "Open" for story in self.export["stories"]))
        self.assertEqual(214, sum(story["legacy_engineering_unit_number"] is not None
                                  and story["calendar_study_day"] != story["previous_calendar_study_day"]
                                  for story in self.export["stories"]))
        first_70 = [story for story in self.export["stories"] if story["legacy_engineering_unit_number"] is not None
                    and story["legacy_engineering_unit_number"] <= 70]
        self.assertEqual(list(range(1, 71)), [story["calendar_study_day"] for story in first_70])
        calendar_by_day = {row["calendar_study_day"]: row for row in self.calendar["calendar"]}
        self.assertTrue(all(story["date"] == calendar_by_day[story["calendar_study_day"]]["date"]
                            and story["calendar_sprint"] == calendar_by_day[story["calendar_study_day"]]["calendar_sprint"]
                            for story in self.export["stories"]))
        self.assertEqual({"engineering_minutes": 120, "dsa_separate": True},
                         self.calendar["budget"]["before_calendar_day_172"])
        self.assertEqual({"engineering_minutes": 165, "shared_minutes": 45},
                         self.calendar["budget"]["from_calendar_day_172"])
        self.assertTrue(all(story["daily_contract_status"] == "existing_assignment_preserved" for story in self.export["stories"]
                            if not story["unit_id"].startswith("GAP-")))
        self.assertTrue(all(not story["owner_card"].get("learner_completion_awarded", False)
                            for story in self.export["stories"]))
        self.assertFalse(self.export["board_writes"])
        self.assertEqual("docs/reboot/calendar/study-calendar.json#board_publication_receipt",
                         self.export["publication_receipt_source"])

    def test_legacy_sprint_provenance_is_pinned_to_immutable_preamendment_owner_exports(self):
        provenance = json.loads((ROOT / "docs/reboot/calendar/legacy-sprint-provenance.json").read_text())
        self.assertEqual("4a07a57e707d2d1be68f4263d1cbac1e391886c4", provenance["source_commit"])
        self.assertEqual({str(unit) for unit in range(71, 197)}, set(provenance["unit_sprint_labels"]))
        self.assertEqual({f"Sprint {(unit - 1) // 14 + 1}" for unit in range(71, 197)},
                         set(provenance["unit_sprint_labels"].values()))

    def test_new_insertions_have_executable_contract_owner_command_and_source(self):
        inserted = [row for row in self.export["stories"] if row["unit_id"].startswith("GAP-")]
        self.assertEqual(21, len(inserted))
        for story in inserted:
            self.assertTrue(story["contract_path"])
            self.assertTrue((ROOT / story["contract_path"]).is_file())
            self.assertIsInstance(story["owner_card"], dict)
            self.assertTrue(story["command"].startswith("./mvnw "))
            self.assertIn("section", story["source"])
            self.assertEqual("planned_assignment_not_attempted", story["daily_contract_status"])
            self.assertFalse(story["owner_card"]["learner_completion_awarded"])
            for command in story["command"].split(" && "):
                match = re.search(r"-Dtest=([A-Za-z0-9_,]+)(?:#([A-Za-z0-9_]+))?\s+test", command)
                self.assertIsNotNone(match, f"command has an unrecognized Maven test selector: {command}")
                for selector in match.group(1).split(","):
                    class_file = next((ROOT / "src/test/java").rglob(f"{selector}.java"), None)
                    self.assertIsNotNone(class_file, f"missing test class for owner command: {selector}")
                    if match.group(2):
                        self.assertRegex(class_file.read_text(), rf"\b{re.escape(match.group(2))}\s*\(",
                                         f"missing test method for owner command: {selector}#{match.group(2)}")
            if story["calendar_study_day"] >= 172:
                shared = story["owner_card"]["shared_interview_block"]
                self.assertEqual(45, shared["minutes"])
                self.assertEqual("separate_system_design_question_bank_session", shared["mode"])
                self.assertFalse(shared["inside_engineering_budget"])
            else:
                self.assertNotIn("shared_interview_block", story["owner_card"])
        insertions = json.loads((ROOT / "docs/reboot/calendar/detailed-insertions.json").read_text())["units"]
        self.assertEqual(21, len(insertions))
        self.assertEqual(list(range(71, 85)) + list(range(169, 176)),
                         [unit["calendar_study_day"] for unit in insertions])
        plan = json.loads((ROOT / "docs/reboot/calendar/insertion-plan.json").read_text())
        self.assertEqual(21, len(plan["detailed_inserted_units"]))
        for unit in plan["detailed_inserted_units"]:
            self.assertIn("command", unit)
            self.assertIn("source", unit)
            self.assertIn("acceptance", unit)
            self.assertTrue((ROOT / unit["contract_path"]).is_file())
        later = plan["later_outcome_acceptance"]
        for key in ("GAP-AIGOV", "GAP-IAC", "GAP-ENTERPRISE"):
            self.assertTrue(later[key]["outcomes"])
            self.assertTrue(later[key]["sources"])
            self.assertTrue(all(source["status"] == "official_heading_verified" for source in later[key]["sources"]))

    def test_oidc_05_selector_covers_all_claimed_invalid_token_classes(self):
        row = next(item for item in self.export["stories"] if item["unit_id"] == "GAP-OIDC-05")
        expected_command = "./mvnw -Dtest=OidcContractLabInfrastructureTest#invalidTokenStructuresFailClosed test"
        self.assertEqual(expected_command, row["command"])
        self.assertEqual(expected_command, row["owner_card"]["command"])
        contract = (ROOT / row["contract_path"]).read_text()
        for token_case in ("malformed compact", "unsigned", "bad-signature", "unknown-kid"):
            self.assertIn(token_case, contract)
        test_source = (ROOT / "src/test/java/assignments/oidc/OidcContractLabInfrastructureTest.java").read_text()
        selector_body = re.search(r"void invalidTokenStructuresFailClosed\(\).*?\n    }", test_source, re.S)
        self.assertIsNotNone(selector_body)
        for fixture in ("issueWithUnknownKid", "unsignedToken", "tokenWithInvalidSignature", "not.a.jwt"):
            self.assertIn(fixture, selector_body.group())
        self.assertIn("andExpect(status().isUnauthorized())", selector_body.group())
        self.assertIn("synthetic-advait", selector_body.group())
        assignment = next(row for row in json.loads((ROOT / "docs/reboot/calendar/detailed-insertions.json").read_text())["units"]
                          if row["unit_id"] == "GAP-OIDC-05")
        self.assertIn("invalidTokenStructuresFailClosed", assignment["command"])

    def test_schema_04_runs_failure_injection_through_learner_backfill(self):
        row = next(item for item in self.export["stories"] if item["unit_id"] == "GAP-SCHEMA-04")
        self.assertIn("ResumableBackfillAcceptanceTest", row["command"])
        self.assertIn("SchemaEvolutionInfrastructureTest", row["command"])
        test_source = (ROOT / "src/test/java/assignments/schema/ResumableBackfillAcceptanceTest.java").read_text()
        body = re.search(r"void midBatchFailureRollsBackLearnerRowsAndCursorThenFreshConnectionResumesExactly\(\).*?\n    }", test_source, re.S)
        self.assertIsNotNone(body)
        for evidence in ("CREATE TRIGGER fail_backfill_second_row", "ResumableBackfill.runBatch", "assertLearnerSnapshot(db, 0)", "assertLearnerSnapshot(db, 3)", "assertLearnerSnapshot(db, 10)"):
            self.assertIn(evidence, body.group())
        self.assertIn("injected mid-batch failure", body.group())
        checkpoint = re.search(r"void checkpointWriteFailureRollsBackEarlierLearnerRowUpdatesThenFreshConnectionResumesExactly\(\).*?\n    }", test_source, re.S)
        self.assertIsNotNone(checkpoint)
        self.assertIn("BackfillAtomicityAcceptanceOracle.checkpointWriteFailureRollsBackAndResumes", checkpoint.group())
        self.assertIn("ResumableBackfill::runBatch", checkpoint.group())
        oracle = (ROOT / "src/test/java/assignments/schema/BackfillAtomicityAcceptanceOracle.java").read_text()
        for evidence in ("CREATE CONSTRAINT TRIGGER fail_backfill_checkpoint_write", "DEFERRABLE INITIALLY DEFERRED", "WHERE normalized_value IS NOT NULL", "assertSnapshot(db, jobId, 0)", "assertSnapshot(db, jobId, 10)"):
            self.assertIn(evidence, oracle)
        infrastructure = (ROOT / "src/test/java/assignments/schema/SchemaEvolutionInfrastructureTest.java").read_text()
        shared_test = re.search(r"void sharedCheckpointAtomicityOracleAcceptsReferenceAndRejectsNonTransactionalMutant\(\).*?\n    }", infrastructure, re.S)
        self.assertIsNotNone(shared_test)
        self.assertIn("ReferenceBackfill::runBatch", shared_test.group())
        self.assertIn("nonTransactionalBackfillMutant", shared_test.group())
        self.assertIn("assertThrows(AssertionError.class", shared_test.group())
        mutant = re.search(r"Integer nonTransactionalBackfillMutant\(.*?\n    }", infrastructure, re.S)
        self.assertIsNotNone(mutant)
        self.assertIn("UPDATE gap_backfill_item", mutant.group())
        self.assertIn("INSERT INTO gap_backfill_checkpoint", mutant.group())

    def test_schema_07_executes_real_contract_migration_through_learner_gate(self):
        row = next(item for item in self.export["stories"] if item["unit_id"] == "GAP-SCHEMA-07")
        self.assertIn("SchemaContractGateAcceptanceTest", row["command"])
        source = (ROOT / "src/test/java/assignments/schema/SchemaContractGateAcceptanceTest.java").read_text()
        body = re.search(r"void learnerGateControlsActualContractMigrationAndDeniedInputsPreserveRows\(\).*?\n    }", source, re.S)
        self.assertIsNotNone(body)
        for evidence in ("attemptLearnerContract", "assertFalse", "assertTrue", "hasLegacyColumn", "legacyAndNormalizedRows", "normalizedRows"):
            self.assertIn(evidence, body.group())
        self.assertIn("target(\"4\")", source)

    def test_oidc_and_schema_canonical_commands_match_rendered_cards(self):
        cards = json.loads((ROOT / "docs/reboot/calendar/gap-owner-manifest.json").read_text())["cards"]
        by_id = {card["id"]: card for card in cards}
        for unit_id in ("GAP-OIDC-05", "GAP-SCHEMA-04", "GAP-SCHEMA-07"):
            detail = next(row for row in json.loads((ROOT / "docs/reboot/calendar/detailed-insertions.json").read_text())["units"]
                          if row["unit_id"] == unit_id)
            self.assertEqual(detail["command"], by_id[unit_id]["command"])
            contract = (ROOT / f"docs/reboot/contracts/{unit_id.lower()}.md").read_text()
            for token in detail["command"].split(" && "):
                selector = re.search(r"-Dtest=[A-Za-z0-9_,]+(?:#([A-Za-z0-9_]+))?", token)
                self.assertIsNotNone(selector)
                if selector.group(1):
                    self.assertIn(selector.group(1), contract)

    def test_openapi_contract_discloses_unauthenticated_response_change(self):
        owner = next(card for card in json.loads((ROOT / "docs/reboot/calendar/gap-owner-manifest.json").read_text())["cards"]
                     if card["id"] == "GAP-OPENAPI-05")
        self.assertIn("compatibility-visible local contract change", owner["assignment"])
        readme = (ROOT / "docs/reboot/continuation-271-732/README.md").read_text()
        self.assertIn("prior `sendError`-generated body", readme)
        self.assertIn('HTTP 401 JSON `{"code":"UNAUTHENTICATED"}`', readme)
        openapi_test = (ROOT / "src/test/java/assignments/contracts/OpenApiCompatibilityAcceptanceTest.java").read_text()
        self.assertIn("inlinedResponseSchemaStillRejectsRemovalOfAConsumedField", openapi_test)

    def test_openapi_04_assignment_runs_fixed_learner_consumer_cases(self):
        row = next(item for item in self.export["stories"] if item["unit_id"] == "GAP-OPENAPI-04")
        self.assertIn("OpenApiCompatibilityAcceptanceTest", row["command"])
        source = (ROOT / "src/test/java/assignments/contracts/OpenApiCompatibilityAcceptanceTest.java").read_text()
        inline = re.search(r"void inlinedResponseSchemaStillRejectsRemovalOfAConsumedField\(\).*?\n    }", source, re.S)
        self.assertIsNotNone(inline)
        self.assertIn('remove("$ref")', inline.group())
        self.assertIn("assertLearnerResult", inline.group())
        self.assertIn("removed required field ownerId", inline.group())

    def test_role_dates_are_evidence_gated_and_dashboard_is_repo_source(self):
        dashboard = json.loads((ROOT / "docs/reboot/fde-alignment.json").read_text())
        milestones = dashboard["calendar_amendment"]["role_review_milestones"]
        self.assertEqual([(175, "2027-04-18"), (459, "2028-01-27"), (550, "2028-04-27"),
                          (648, "2028-08-03"), (718, "2028-10-12"), (774, "2028-12-07"),
                          (None, "2029-10-26")],
                         [(m["calendar_study_day"], m["date"]) for m in milestones])
        self.assertTrue(dashboard["calendar_amendment"]["application_may_begin_earlier"])
        self.assertFalse(dashboard["calendar_amendment"]["calendar_completion_grants_readiness"])
        self.assertEqual("2029-10-26", dashboard["calendar_amendment"]["professional_ownership_review_date"])
        self.assertEqual("published_planning_assignments_not_learner_completion", dashboard["new_batch_status"]["days197_to270"])
        self.assertFalse(dashboard["new_batch_status"]["learner_completion_or_eligibility_granted"])

    def test_days_155_through_171_cross_the_budget_boundary_without_changing_day_172(self):
        manifests = [ROOT / "docs/reboot/manifest.json", *sorted((ROOT / "docs/reboot").glob("continuation-*/manifest.json"))]
        by_unit = {}
        for path in manifests:
            for row in json.loads(path.read_text()).get("days", []):
                if isinstance(row.get("day"), int):
                    by_unit[row["day"]] = row
        self.assertEqual(set(range(155, 172)), set(range(155, 172)) & set(by_unit))
        self.assertEqual("2027-04-15", self.calendar["budget"]["calendar_day_172_date"])
        for unit in range(155, 172):
            row = by_unit[unit]
            expected_day = unit + 21
            self.assertEqual(expected_day, row["calendar_study_day"])
            self.assertEqual(165, row["engineering"]["budget_minutes"])
            self.assertEqual(165, sum(row["engineering"]["allocation_minutes"].values()))
            self.assertEqual(45, row["shared_interview_block"]["minutes"])
            self.assertEqual("separate_system_design_question_bank_session", row["shared_interview_block"]["mode"])
            self.assertEqual(45, row["calendar_transition_task"]["additional_minutes"])
            self.assertFalse(row["calendar_transition_task"]["new_feature_scope"])

    def test_shared_question_ids_match_every_migrated_contract_and_owner_card(self):
        cards = {}
        for path in [ROOT / "docs/reboot/scrum-owner-manifest.json", *sorted((ROOT / "docs/reboot").glob("continuation-*/scrum-owner-manifest.json"))]:
            for card in json.loads(path.read_text()).get("cards", []):
                unit = card.get("engineering_unit_number", card.get("legacy_engineering_unit_number", card.get("day_number")))
                if isinstance(unit, int) and 155 <= unit <= 171:
                    cards[unit] = card
        self.assertEqual(set(range(155, 172)), set(cards))
        for unit, card in cards.items():
            contract = next((path for path in (ROOT / "docs/reboot").glob(f"continuation-*/contracts/day-{unit}.md")), None)
            self.assertIsNotNone(contract)
            expected = re.search(r"question-bank\.md#q(\d+)", contract.read_text(), re.I)
            self.assertIsNotNone(expected)
            active = card.get("shared_interview_block") or {}
            self.assertEqual(f"Q{int(expected.group(1)):02}", active.get("question_id"), f"owner card QID for unit {unit}")
            row = next(item for item in self.export["stories"] if item.get("legacy_engineering_unit_number") == unit)
            self.assertEqual(active.get("question_id"), row["owner_card"].get("shared_interview_block", {}).get("question_id"))

    def test_rendered_contracts_follow_calendar_even_with_anchor_first(self):
        for story in self.export["stories"]:
            unit = story["legacy_engineering_unit_number"]
            contract = story["contract_path"]
            if unit is None or unit < 71 or not contract:
                continue
            text = (ROOT / contract).read_text()
            heading = next((line for line in text.splitlines() if line.startswith("# ")), "")
            self.assertIn(f"Calendar study day {story['calendar_study_day']}", heading, contract)
            self.assertIn(story["date"], heading, contract)
            if 155 <= unit <= 171:
                self.assertIn("165 engineering minutes (25 study, 130 implementation/testing, 10 evidence)", text, contract)
                self.assertIn("## Practice — 130 engineering minutes", text, contract)
                self.assertIn("## Shared 45-minute question-bank block", text, contract)
                self.assertNotIn("120 engineering minutes", text, contract)
                self.assertNotIn("inside the 85-minute practice block", text, contract)

    def test_migration_keeps_historical_sprint_and_title_across_reruns(self):
        sample = {
            "day_number": 150,
            "date": generate_study_calendar.current_date(164),
            "title": "Gate fallback on measured pair evidence",
            "sprint": "Sprint 12",
            "legacy_sprint_label": "Sprint 11",
            "engineering": {"budget_minutes": 120, "allocation_minutes": {
                "concept_or_course": 25, "implementation_and_testing": 85, "evidence": 10
            }},
        }
        first, _ = generate_study_calendar.migrate_assignment_json("fixture.json", sample)
        second, _ = generate_study_calendar.migrate_assignment_json("fixture.json", first)
        self.assertEqual("Sprint 11", first["legacy_sprint_label"])
        self.assertEqual("Sprint 11", second["legacy_sprint_label"])
        self.assertEqual("Gate fallback on measured pair evidence", first["title"])
        self.assertEqual(first["title"], second["title"])


if __name__ == "__main__":
    unittest.main()
