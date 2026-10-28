import hashlib
import json
import re
import subprocess
import sys
import unittest
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class ModelFirstRoadmapTests(unittest.TestCase):
    def test_calendar_budget_phase_arithmetic_and_gate_positions(self):
        manifest = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())
        start = date.fromisoformat(manifest["start_date"])
        self.assertEqual(date(2026, 10, 27), start)
        self.assertEqual(date(2028, 4, 26), start + timedelta(days=547))
        self.assertEqual("2028-04-26", manifest["end_date"])
        self.assertEqual(548, manifest["horizon_days"])
        self.assertEqual([
            (1, "2026-10-27", "2026-11-09", 14),
            (2, "2026-11-10", "2026-11-23", 14),
            (3, "2026-11-24", "2026-12-07", 2),
        ], [(s["sprint"], s["start_date"], s["end_date"], s["detailed_card_count"])
            for s in manifest["detailed_sprint_windows"]])
        budget = manifest["budget"]
        self.assertEqual(1378.75, budget["engineering_hours"])
        self.assertEqual(282.75, budget["shared_hours"])
        self.assertEqual(120, budget["days_1_to_171"]["engineering_minutes"])
        self.assertEqual(165, budget["days_172_to_548"]["engineering_minutes"])
        self.assertEqual(45, budget["days_172_to_548"]["shared_minutes"])
        phases = manifest["phases"]
        for first, second in zip(phases, phases[1:]):
            self.assertEqual(first["days"][1] + 1, second["days"][0])
        self.assertEqual([1, 61, 151, 231, 301, 366, 451, 521], [p["days"][0] for p in phases])
        self.assertEqual(["2026-10-27", "2026-12-25"], phases[0]["dates"])
        self.assertEqual(["2026-10-27", "2028-04-26"], [manifest["start_date"], manifest["end_date"]])
        self.assertEqual(["2027-05-25", "2027-10-26"], manifest["project_gates"][0]["target_dates"])
        milestone_dates = {item["study_day"]: item["date"] for item in manifest["milestone_reviews"] if item["study_day"] is not None}
        self.assertEqual({150:"2027-03-25",274:"2027-07-27",365:"2027-10-26",450:"2028-01-19",520:"2028-03-29",548:"2028-04-26"}, milestone_dates)
        self.assertEqual("2029-10-26", manifest["milestone_reviews"][-1]["date"])
        self.assertEqual(625.25, manifest["feasibility"]["nine_month_engineering_hours_by_day_274"])
        self.assertIn("one strong end-to-end coverstory", manifest["feasibility"]["nine_month_target"].lower())
        self.assertIn("no-go", phases[3]["exit"])
        self.assertFalse(any(day["learner_completion"] for day in manifest["detailed_days"]))

    def test_thirty_contracts_have_prerequisite_sources_commands_negative_controls_and_dates(self):
        manifest = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())
        days = manifest["detailed_days"]
        self.assertEqual(30, len(days))
        for index, day in enumerate(days, 1):
            self.assertEqual(index, day["day"])
            self.assertEqual(date(2026, 10, 27) + timedelta(days=index - 1), date.fromisoformat(day["date"]))
            for key in ("prerequisite", "source_title_or_path", "source", "acceptance_command", "acceptance", "negative_control", "evidence", "evidence_category"):
                self.assertTrue(day[key], (index, key))
            allocated = [int(value) for value in re.findall(r"(\d+)m", day["budget"])]
            self.assertEqual(120, sum(allocated), (index, day["budget"]))
            contract = ROOT / f"docs/model-first-roadmap/contracts/day-{index:02d}.md"
            self.assertTrue(contract.exists())
            self.assertIn(day["acceptance_command"].split()[-1].split(".")[-1], contract.read_text())
            if index > 1:
                completed = subprocess.run(day["acceptance_command"].split(), cwd=ROOT, capture_output=True, text=True)
                self.assertNotEqual(0, completed.returncode, f"Day {index} learner TODO should remain red")
                self.assertIn("NotImplementedError", completed.stderr)
        self.assertIn("logistic-regression", days[0]["learner_scope"].lower())
        self.assertIn("per-row gradient", days[0]["worked_lesson"])
        self.assertIn("[0,-0.25,0.25]", days[0]["worked_lesson"])
        self.assertNotIn("API call", days[0]["learner_scope"])
        self.assertIn("Days 1–14 use only Python's standard library", (ROOT / "docs/model-first-roadmap/README.md").read_text())
        self.assertTrue(all("code" in days[i]["negative_control"].lower() for i in (25, 26, 27)))

    def test_day1_learner_acceptance_is_red_only_at_explicit_todo(self):
        day = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())["detailed_days"][0]
        result = subprocess.run(day["acceptance_command"].split(), cwd=ROOT, capture_output=True, text=True)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("NotImplementedError: Day 1", result.stderr)

    def test_migration_is_305_unique_records_with_only_first30_active(self):
        migration = json.loads((ROOT / "docs/model-first-roadmap/legacy-issue-migration.json").read_text())
        self.assertEqual(30, migration["active_issue_count"])
        self.assertEqual(275, migration["superseded_issue_count"])
        self.assertEqual(305, migration["total_legacy_issue_count"])
        active_ids = [row["existing_issue_id"] for row in migration["active_mappings"]]
        superseded_ids = [row["existing_issue_id"] for row in migration["superseded_records"]]
        self.assertEqual([f"EAI-{n}" for n in range(211, 241)], active_ids)
        self.assertEqual(305, len(set(active_ids + superseded_ids)))
        self.assertEqual(30, len(set(active_ids)))
        self.assertEqual([(1, 14), (2, 14), (3, 2)],
                         [(s["sprint"], s["detailed_card_count"]) for s in migration["detailed_sprint_windows"]])
        self.assertEqual(3, next(r["active_sprint_number"] for r in migration["active_mappings"] if r["active_day"] == 30))
        for row in migration["active_mappings"]:
            self.assertTrue(row["legacy_definition_immutable"])
            self.assertEqual("Open", row["legacy_status_preserved"])
            self.assertFalse(row["learner_completion_credit"])
            self.assertEqual(64, len(row["legacy_definition_sha256"]))
        self.assertFalse(migration["board_writes"])
        self.assertEqual(0, migration["completion_count"])

    def test_migration_preserves_each_immutable_source_record(self):
        source = json.loads((ROOT / "docs/reboot/calendar/story-export-1-305.json").read_text())
        migration = json.loads((ROOT / "docs/model-first-roadmap/legacy-issue-migration.json").read_text())
        by_issue = {row.get("eai_issue_id") or row.get("new_eai_issue_id"): row for row in source["stories"]}
        self.assertEqual(305, len(by_issue))
        for row in migration["active_mappings"] + migration["superseded_records"]:
            original = by_issue[row["existing_issue_id"]]
            canonical = json.dumps(original, ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode()
            self.assertEqual(hashlib.sha256(canonical).hexdigest(), row["legacy_definition_sha256"])
            self.assertEqual(original["unit_id"], row.get("legacy_unit_id"))
            self.assertEqual(original["title"], row.get("legacy_title"))
            self.assertEqual(original.get("calendar_study_day"), row.get("legacy_study_day"))
            self.assertEqual(original.get("contract_path"), row.get("legacy_contract_path"))
            self.assertEqual(original.get("board_status"), row.get("legacy_status_preserved"))

    def test_dsa_external_pointer_and_model_only_scope(self):
        manifest = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())
        self.assertEqual("c2caee396002bb48b2a34d407545ecd96ecdb1ab", manifest["dsa_source"]["commit"])
        text = (ROOT / "docs/model-first-roadmap/README.md").read_text()
        self.assertIn("learner ui/controller/auth implementation", text.lower())
        self.assertIn("reads/traces that code", text)
        self.assertIn("Redis", text)
        self.assertIn("Kafka", text)
        self.assertIn("MLOps", text)
        self.assertIn("global or production scale", text)

    def test_read_only_application_answers_are_anchored_to_supplied_code(self):
        ui = (ROOT / "frontend/src/local/LocalCoverStoryApp.tsx").read_text()
        api = (ROOT / "src/main/java/assignments/local/LocalPolicyIssueController.java").read_text()
        fixture = json.loads((ROOT / "ai/model_building/fixtures/day-28-example.json").read_text())
        self.assertIn("CoverStory local training", ui)
        self.assertIn("after the local API is wired", ui)
        self.assertNotIn("fetch(", ui)
        self.assertIn('@RequestMapping("/api/policies")', api)
        self.assertIn("@PostMapping", api)
        self.assertIn("ownerResolver.ownerId(authentication)", api)
        self.assertIn("issueReceiptReadStore.find(ownerId.get(), request.clientRequestId())", api)
        self.assertIn("persistIssueAtomically", api)
        self.assertEqual({
            "execution_mode": "offline_synthetic_model_output_example",
            "score": 0.92,
            "predicted_label": 1,
            "downstream_decision": "not_assessed",
            "provider_request_made": False,
            "live_quality_evaluated": False,
        }, fixture)
        manifest = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())
        self.assertTrue(all(manifest["detailed_days"][day]["comprehension_questions"] for day in (25, 26, 27)))

    def test_acceptance_scope_matches_taught_exercises_and_evidence_categories(self):
        manifest = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())
        days = {row["day"]: row for row in manifest["detailed_days"]}
        self.assertIn("scalar sigmoid", days[2]["learner_scope"])
        self.assertIn("does not accept matrix rows", days[4]["negative_control"])
        self.assertIn("threshold classifier", days[8]["learner_scope"])
        self.assertIn("fresh process", days[20]["acceptance"])
        self.assertIn("score fixed predictions", days[22]["learner_scope"])
        self.assertIn("does not retrain a model per fold", days[22]["concept"])
        classical_exit = next(row["exit"] for row in manifest["phases"] if row["id"] == "classical-ml")
        self.assertIn("fresh estimator/pipeline is fitted inside every development fold", classical_exit)
        self.assertIn("Day 22 fixed-prediction bookkeeping alone is not sufficient", classical_exit)
        self.assertIn("observed-positive fraction", days[23]["learner_scope"])
        for number in (26, 27, 28):
            self.assertEqual("manual_read_only_application_comprehension", days[number]["evidence_category"])
            self.assertIn("no FE/BE code-edit audit", days[number]["evidence"])
        result = subprocess.run([sys.executable, "-S", "-m", "ai.model_building.tests.stdlib_days_02_14"],
                                cwd=ROOT, capture_output=True, text=True, check=True)
        self.assertIn("15 witness-backed checks passed; NumPy was not imported", result.stdout)

    def test_generation_checks_and_local_markdown_links(self):
        for command in ([sys.executable, "scripts/generate_model_first_roadmap.py", "--check"],
                        [sys.executable, "scripts/generate_model_first_migration.py", "--check"]):
            subprocess.run(command, cwd=ROOT, check=True, capture_output=True, text=True)
        paths = [ROOT / "README.md", ROOT / "docs/model-first-roadmap/README.md"]
        paths.append(ROOT / "docs/model-first-roadmap/release-scope.md")
        paths.extend(sorted((ROOT / "docs/model-first-roadmap/contracts").glob("*.md")))
        for source in paths:
            for target in re.findall(r"\[[^\]]+\]\(([^)]+)\)", source.read_text()):
                if target.startswith(("https://", "http://", "#")):
                    continue
                resolved = (source.parent / target.split("#", 1)[0]).resolve()
                self.assertTrue(resolved.exists(), f"{source.relative_to(ROOT)} -> {target}")

    def test_release_boundary_is_exactly_first_30_and_excludes_api_first_candidate(self):
        manifest = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())
        tracked = subprocess.run(["git", "ls-files"], cwd=ROOT, capture_output=True, text=True, check=True).stdout.splitlines()
        self.assertEqual(30, len(manifest["detailed_days"]))
        self.assertEqual(list(range(1, 31)), [row["day"] for row in manifest["detailed_days"]])
        self.assertFalse(any(path.startswith("ai/coverstory_assistant/") for path in tracked))
        self.assertFalse(any(path.startswith("docs/ai-first-roadmap/") for path in tracked))
        self.assertNotIn("frontend/src/ai/AiFirstCoverStoryApp.tsx", tracked)
        self.assertNotIn("src/main/java/assignments/local/LocalAiAssistantController.java", tracked)
        self.assertEqual("docs/model-first-roadmap/role-alignment.md", manifest["active_role_benchmarks"])
        for number in range(1, 31):
            self.assertTrue((ROOT / f"docs/model-first-roadmap/contracts/day-{number:02d}.md").is_file())
        self.assertFalse(any((ROOT / "docs/model-first-roadmap/contracts").glob("day-3[1-9].md")))

    def test_integration_ownership_is_additive_bounded_and_not_yet_published(self):
        manifest = json.loads((ROOT / "docs/model-first-roadmap/manifest.json").read_text())
        amendment = json.loads((ROOT / "docs/model-first-roadmap/integration-ownership-amendment.json").read_text())
        self.assertEqual(amendment, manifest["integration_ownership_amendment"])
        self.assertEqual(
            {
                "general_rule": "The application_comprehension_and_scalability_lane learner_work remains the default for all other roadmap days.",
                "exception": "Only the five integration_ownership_amendment targets explicitly assign small learner-owned application-integration edits.",
                "target_unit_ids": ["OWN-INT-225", "OWN-INT-332", "OWN-INT-333", "OWN-INT-420", "OWN-INT-507"],
                "first_30_contracts_unchanged": True,
            },
            manifest["scoped_ownership_precedence"],
        )
        self.assertEqual("review_branch_only_not_merged_or_board_published", amendment["status"])
        self.assertEqual([225, 332, 333, 420, 507], [slot["study_day"] for slot in amendment["targets"]])
        self.assertEqual(["2027-06-08", "2027-09-23", "2027-09-24", "2027-12-20", "2028-03-16"],
                         [slot["date"] for slot in amendment["targets"]])
        phase_ranges = {phase["id"]: phase["days"] for phase in manifest["phases"]}
        self.assertEqual(["neural-models", "coverstory", "coverstory", "streamforge", "developer-workflow"], [
            next(phase_id for phase_id, (first, last) in phase_ranges.items() if first <= slot["study_day"] <= last)
            for slot in amendment["targets"]
        ])
        self.assertEqual(["OWN-INT-225", "OWN-INT-332", "OWN-INT-333", "OWN-INT-420", "OWN-INT-507"],
                         [slot["unit_id"] for slot in amendment["targets"]])
        self.assertTrue(all(slot["budget"] == {
            "engineering_minutes": 165, "shared_minutes": 45,
            "shared_block_policy": "This task replaces equivalent engineering work, and the shared block remains unchanged.",
        } for slot in amendment["targets"]))
        self.assertEqual([
            "starter_and_test_harness_in_this_review_branch; learner_adapter_remains_todo",
            "starter_and_test_harness_in_this_review_branch; learner_error_handling_remains_todo",
            "starter_and_test_harness_in_this_review_branch; learner_component_remains_todo",
            "planning_only_not_yet_published_as_a_daily_assignment",
            "planning_only_not_yet_published_as_a_daily_assignment",
        ], [slot["status"] for slot in amendment["targets"]])
        self.assertTrue(all(slot["acceptance_command"] for slot in amendment["targets"][:3]))
        self.assertTrue(all(slot["acceptance_command"] is None for slot in amendment["targets"][3:]))
        self.assertIn("model-serving request/response contract", amendment["targets"][0]["focus"].lower())
        self.assertIn("passed Day 30", amendment["targets"][0]["prerequisite"])
        self.assertIn("held-out ranking baseline", amendment["targets"][3]["prerequisite"])
        self.assertIn("stale-response", amendment["targets"][2]["focus"])
        self.assertIn("scoped exception", amendment["policy"])
        self.assertIn("test-only monkeypatch", amendment["targets"][1]["assistant_scaffolding"][1])
        self.assertIn("test_own_002_failure_does_not_mutate_model_and_same_server_recovers", amendment["targets"][1]["acceptance_command"])
        self.assertIn("do not add days, engineering minutes, or shared-block work", amendment["policy"])
        self.assertIn("separate repository", amendment["budget_rule"])
        self.assertEqual(30, len(manifest["detailed_days"]))
        self.assertTrue(all(manifest["detailed_days"][number - 1]["evidence_category"] == "manual_read_only_application_comprehension"
                            for number in (26, 27, 28)))
        self.assertFalse(any(day["learner_completion"] for day in manifest["detailed_days"]))
        self.assertIn("not yet published as daily contracts", (ROOT / "docs/model-first-roadmap/integration-ownership-amendment.md").read_text())


if __name__ == "__main__":
    unittest.main()
