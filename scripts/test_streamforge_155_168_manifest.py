"""Integrity checks for the unpublished StreamForge Days 155–168 starter plan."""
from __future__ import annotations

import csv
import json
import re
import subprocess
import sys
import unittest
from datetime import date, timedelta
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
BATCH = ROOT / "docs/reboot/continuation-155-168"


class StreamForge155168ManifestTest(unittest.TestCase):
    @classmethod
    def _has_exact_readme_extension(cls, actual: bytes, original: bytes) -> bool:
        return actual.startswith(original) and b"FDE alignment: See" in actual

    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads((BATCH / "manifest.json").read_text(encoding="utf-8"))
        cls.owner = json.loads((BATCH / "scrum-owner-manifest.json").read_text(encoding="utf-8"))
        cls.plan = json.loads((BATCH / "transfer-plan.json").read_text(encoding="utf-8"))

    def test_fourteen_dates_and_recovery_days_are_bounded(self):
        days = self.manifest["days"]
        self.assertEqual(list(range(155, 169)), [row["day"] for row in days])
        start = date(2027, 3, 27)
        self.assertEqual([start + timedelta(days=i) for i in range(14)], [date.fromisoformat(row["date"]) for row in days])
        self.assertEqual([161, 168], [row["day"] for row in days if row["recovery_day"]])
        readme = (BATCH / "README.md").read_text(encoding="utf-8")
        self.assertIn("March 27–April 9, 2027", readme)
        self.assertNotIn("March 23–April 5, 2027", readme)
        self.assertEqual(14, len(self.owner["cards"]))
        self.assertFalse(self.manifest["board_writes"])
        self.assertFalse(self.owner["board_writes"])
        self.assertTrue(all(row["board_ticket_id"] is None and row["status"] == "draft_not_board_published"
                            for row in self.owner["cards"]))
        self.assertTrue(all(row["status"] == "draft_not_board_published" for row in days))
        self.assertTrue(all(row["learner_status"] == "not_assessed" and row["evidence_status"] == "planned_contract_only"
                            for row in self.owner["cards"]))

    def test_contract_manifest_owner_and_transfer_plan_selectors_match(self):
        owner_by_day = {row["day_number"]: row for row in self.owner["cards"]}
        plan_by_day = {row["day"]: row for row in self.plan["daily_contracts"]}
        for daily in self.manifest["days"]:
            day = daily["day"]
            contract = (BATCH / daily["contract_file"]).read_text(encoding="utf-8")
            command = daily["engineering"]["acceptance_commands"][0]
            owner = owner_by_day[day]
            self.assertEqual(command, owner["engineering"]["acceptance_commands"][0])
            self.assertEqual(command, plan_by_day[day]["selector"])
            self.assertIn(command, contract)
            self.assertEqual(daily["engineering"], owner["engineering"])

    def test_daily_engineering_time_remains_bounded(self):
        for daily in self.manifest["days"]:
            self.assertEqual(120, daily["engineering"]["budget_minutes"])
            self.assertEqual({"concept_or_course": 25, "implementation_and_testing": 85, "evidence": 10},
                             daily["engineering"]["allocation_minutes"])
            self.assertEqual(1500, daily["engineering"]["study_total_seconds"])
            self.assertEqual(0, daily["engineering"]["video_seconds"])
        self.assertEqual(120, self.manifest["engineering_minutes_per_day"])

    def test_catalog_has_only_official_docs_and_no_invented_python_course(self):
        catalog = json.loads((BATCH / "verified-data-foundation-155-168-catalog.json").read_text(encoding="utf-8"))
        self.assertFalse(catalog["exact_python_lessons_verified"] if "exact_python_lessons_verified" in catalog else False)
        self.assertIn("No exact Python", catalog["preferred_course_gap"])
        self.assertTrue(all(url.startswith(("https://docs.python.org/3.12/", "https://scikit-learn.org/"))
                            for url in [x["url"] for x in catalog["official_sources"]]))
        self.assertEqual("Python 3.12.14", self.manifest["python_runtime_observed"])
        self.assertEqual([], self.manifest["third_party_python_dependencies"])
        self.assertIn("https://docs.python.org/3.12/tutorial/errors.html#handling-exceptions",
                      [row["url"] for row in catalog["official_sources"]])

    def test_day155_preserves_timestamp_strings_and_day167_is_prior_only(self):
        day155 = next(row for row in self.manifest["days"] if row["day"] == 155)
        day167 = next(row for row in self.manifest["days"] if row["day"] == 167)
        self.assertIn("without parsing UTC strings", day155["engineering"]["scope"])
        self.assertEqual("prior-only-integration-verification", day167["engineering"]["learner_seam"])
        contract155 = (BATCH / day155["contract_file"]).read_text(encoding="utf-8")
        contract167 = (BATCH / day167["contract_file"]).read_text(encoding="utf-8")
        self.assertIn("Handling Exceptions", contract155)
        self.assertIn("readiness bridge", contract155)
        command = "python3 -m ai.streamforge.readiness_bridge"
        self.assertEqual(command, day155["engineering"]["readiness_command"])
        self.assertIn(command, day155["engineering"]["acceptance_commands"])
        owner = json.loads((BATCH / "scrum-owner-manifest.json").read_text(encoding="utf-8"))
        owner155 = next(card for card in owner["cards"] if card["day_number"] == 155)
        self.assertEqual(command, owner155["engineering"]["readiness_command"])
        self.assertIn(command, owner155["engineering"]["acceptance_commands"])
        plan = json.loads((BATCH / "transfer-plan.json").read_text(encoding="utf-8"))
        plan155 = next(row for row in plan["daily_contracts"] if row["day"] == 155)
        self.assertEqual(command, plan155["readiness_command"])
        self.assertIn("25-minute study block", plan155["readiness_gate"])
        self.assertIn("do not add a new semantic function", contract167)

    def test_four_kib_field_limit_is_utf8_bytes_not_characters(self):
        self.assertEqual(4096, self.manifest["fixture"]["max_field_bytes"])
        self.assertNotIn("max_field_chars", self.manifest["fixture"])
        original_readme = (BATCH / "streamforge-days-155-168-transfer-plan.md").read_bytes()
        updated_readme = (BATCH / "README.md").read_bytes()
        self.assertIn(b"## Data contract", updated_readme)
        self.assertIn(b"## Bounded architecture", updated_readme)
        self.assertNotIn(b"dsa-review-queue", updated_readme)
        self.assertEqual((BATCH / "transfer-plan.json").read_bytes(),
                         (BATCH / "streamforge-days-155-168-transfer-plan.json").read_bytes())

    def test_fixture_is_small_synthetic_and_has_temporal_boundary_examples(self):
        fixture = ROOT / "ai/streamforge/fixtures/basic"
        manifest = json.loads((fixture / "manifest.json").read_text(encoding="utf-8"))
        with (fixture / manifest["catalog_file"]).open(newline="", encoding="utf-8") as stream:
            catalog = list(csv.DictReader(stream))
        with (fixture / manifest["events_file"]).open(newline="", encoding="utf-8") as stream:
            events = list(csv.DictReader(stream))
        self.assertEqual("synthetic_hand_authored", manifest["source_kind"])
        self.assertEqual(30, len(catalog))
        self.assertEqual(200, len(events))
        self.assertIn(manifest["cutoff_utc"], [event["occurred_at_utc"] for event in events])
        self.assertTrue(any(event["occurred_at_utc"] >= manifest["window_end_utc"] for event in events))
        self.assertTrue(all(int(row["duration_s"]) in range(1, 7201) for row in catalog))
        self.assertTrue(all(0 <= int(row["watch_seconds"]) <= int(catalog[int(row["media_id"][-3:])-1]["duration_s"])
                            for row in events))

    def test_streamforge_scope_excludes_prior_cap44_and_cloud_work(self):
        readme = (BATCH / "README.md").read_text(encoding="utf-8").lower()
        self.assertIn("not a recommender", readme)
        self.assertIn("no aws/cloud", readme)
        self.assertIn("cap44", readme)
        self.assertIn("no arbitrary paths or urls", readme)
        self.assertEqual("5dc558eeda441177dc4aac87725d0a18417c5abd", self.manifest["base_commit"])

    def test_architecture_notes_interview_sessions_and_task_slices_are_synchronized(self):
        notes_path = BATCH / "assistant-architecture-notes.md"
        notes = notes_path.read_text(encoding="utf-8")
        owner_by_day = {row["day_number"]: row for row in self.owner["cards"]}
        plan_by_day = {row["day"]: row for row in self.plan["daily_contracts"]}
        mock_days = [155, 159, 163, 167]
        self.assertEqual([155, 159, 163, 167], [row["day"] for row in self.manifest["days"]
                                                if row["engineering"].get("interview_assignment")])
        self.assertEqual([4, 4, 4], [b - a for a, b in zip(mock_days, mock_days[1:])])
        question_ids = []
        for day in self.manifest["days"]:
            n = day["day"]
            eng = day["engineering"]
            owner = owner_by_day[n]
            plan = plan_by_day[n]
            note_ref = eng["assistant_architecture_note"]
            self.assertIn(f"## Day {n} —", notes)
            self.assertEqual(note_ref, owner["engineering"]["assistant_architecture_note"])
            self.assertEqual(eng["architecture_explanation"], plan["architecture_explanation"])
            self.assertFalse(eng["system_design_proposal_required"])
            self.assertEqual("assistant_supplied_capstone_architecture; learner_implements_and_tests",
                             eng["design_ownership"])
            contract = (BATCH / day["contract_file"]).read_text(encoding="utf-8")
            self.assertIn(" ".join(eng["architecture_explanation"].split()),
                          " ".join(contract.split()))
            self.assertIn("Assistant-supplied capstone architecture", contract)
            self.assertIn(note_ref.rsplit("#", 1)[1], contract)
            if n in mock_days:
                assignment = eng["interview_assignment"]
                question_ids.append(assignment["question_id"])
                self.assertEqual(assignment, owner["interview_assignment"])
                self.assertEqual(assignment, plan["interview_assignment"])
                self.assertEqual("system_design_interview_question_only", assignment["proposal_scope"])
                self.assertEqual("assistant_supplied", assignment["capstone_architecture"])
                self.assertEqual(45, assignment["mock_minutes"])
                self.assertEqual(40, assignment["eai_slice_minutes"])
                self.assertEqual(85, assignment["practice_minutes_available"])
                self.assertEqual(45, sum(x["minutes"] for x in assignment["mock_phases"]))
                self.assertEqual([5, 5, 15, 10, 10],
                                 [x["minutes"] for x in assignment["mock_phases"]])
                self.assertTrue(assignment["deferred_work"].startswith(f"S12-D{n}-FULL"))
                self.assertIn(f"question-bank.md#{assignment['question_id'].lower()}", contract)
                self.assertIn("45-minute system-design interview", contract)
                self.assertIn("40-minute implementation slice", contract)
                self.assertIn(assignment["deferred_work"], contract)
                for pattern in assignment["pattern_refs"]:
                    self.assertIn(f"<a id=\"{pattern}\"></a>",
                                  (ROOT / "docs/reboot/interview/pattern-library.md").read_text())
                question = (ROOT / "docs/reboot/interview/question-bank.md").read_text()
                self.assertIn(f'<a id="{assignment["question_id"].lower()}"></a>', question)
        self.assertEqual(4, len(set(question_ids)))

    def test_prior_ab_gate_uses_authoritative_evidence_and_can_become_met(self):
        gate = self.manifest["prior_readiness_gate"]
        sha40 = re.compile(r"^[0-9a-f]{40}$")
        sha64 = re.compile(r"^[0-9a-f]{64}$")

        def qualifies(record):
            hashes = record.get("artifact_hashes", [])
            by_role = {item.get("role"): item.get("sha256") for item in hashes if isinstance(item, dict)}
            provenance = record.get("source_provenance", {})
            return all((
                record.get("status") == "completed",
                record.get("evidence_status") == "verified",
                record.get("learner_completion") is True,
                record.get("temporary_witness") is False,
                isinstance(record.get("tested_source_sha"), str) and bool(sha40.fullmatch(record["tested_source_sha"])),
                {"baseline", "candidate"}.issubset(by_role),
                by_role.get("baseline") != by_role.get("candidate"),
                all(isinstance(value, str) and sha64.fullmatch(value) for value in by_role.values()),
                isinstance(record.get("matrix_selector"), str) and bool(record["matrix_selector"].strip()),
                record.get("matrix_exit_code") == 0,
                record.get("matrix_result") == "passed",
                all(isinstance(provenance.get(role), dict)
                    and provenance[role].get("source")
                    and provenance[role].get("build_recipe") for role in ("baseline", "candidate")),
                isinstance(record.get("proof_limits"), str) and bool(record["proof_limits"].strip()),
            ))

        qualifying_example = {
            "status": "completed", "evidence_status": "verified",
            "learner_completion": True, "temporary_witness": False,
            "tested_source_sha": "a" * 40,
            "artifact_hashes": [
                {"role": "baseline", "sha256": "1" * 64},
                {"role": "candidate", "sha256": "2" * 64},
            ],
            "matrix_selector": "TwoArtifactCompatibilityAcceptanceTest#day153RunsCompleteObservedPairMatrixAndFallbackDecision",
            "matrix_exit_code": 0, "matrix_result": "passed",
            "source_provenance": {
                "baseline": {"source": "pinned source SHA", "build_recipe": "clean baseline package"},
                "candidate": {"source": "learner source SHA", "build_recipe": "recorded candidate package"},
            },
            "proof_limits": "Local unchanged-schema result only.",
        }
        self.assertTrue(qualifies(qualifying_example))
        self.assertFalse(qualifies(dict(qualifying_example, temporary_witness=True)))

        actual = []
        for day in (153, 154):
            path = ROOT / f"evidence/continuation-141-154/day-{day}.json"
            self.assertTrue(path.is_file(), f"authoritative Day {day} evidence record should be inspected")
            record = json.loads(path.read_text(encoding="utf-8"))
            actual.append(record)
            self.assertEqual("evidence/continuation-141-154/day-" + str(day) + ".json",
                             gate["evidence_records"][day - 153]["path"])
            snapshot = gate["evidence_records"][day - 153]
            for key in ("status", "evidence_status", "tested_source_sha", "artifact_hashes",
                        "learner_completion", "temporary_witness"):
                self.assertEqual(record.get(key), snapshot.get(key))
        expected = "met" if all(qualifies(record) for record in actual) else "not_met"
        self.assertEqual(expected, gate["current_status"])
        self.assertEqual(actual[0]["status"], gate["observed_prior_status"]["day_153"])
        self.assertEqual(actual[0]["evidence_status"], gate["observed_prior_status"]["day_153_evidence"])
        self.assertEqual(actual[1]["status"], gate["observed_prior_status"]["day_154"])
        self.assertEqual(actual[1]["evidence_status"], gate["observed_prior_status"]["day_154_evidence"])
        self.assertEqual(gate, self.owner["prior_readiness_gate"])
        self.assertEqual(gate, self.plan["prior_readiness_gate"])
        contract = (BATCH / "contracts/day-155.md").read_text(encoding="utf-8")
        readme = (BATCH / "README.md").read_text(encoding="utf-8")
        self.assertIn("Prior A/B readiness gate: " + expected.replace("_", " "), contract)
        self.assertIn("status " + expected.replace("_", " "), readme)
        self.assertIn("evidence/continuation-141-154/day-153.json", readme)
        self.assertIn("cannot satisfy", readme)

    def test_sprint12_contract_generator_is_idempotent_and_keeps_existing_harnesses(self):
        paths = [BATCH / f"contracts/day-{day}.md" for day in range(155, 169)] + [
            BATCH / "README.md", BATCH / "assistant-architecture-notes.md",
            BATCH / "streamforge-days-155-168-transfer-plan.md"]
        before = [path.read_bytes() for path in paths]
        result = subprocess.run([sys.executable, str(ROOT / "scripts/create_days155_168_docs.py")],
                                cwd=ROOT, capture_output=True, check=False, timeout=10)
        self.assertEqual(0, result.returncode, result.stderr.decode("utf-8", "replace"))
        self.assertEqual(before, [path.read_bytes() for path in paths])
        source = (ROOT / "ai/streamforge/tests/test_day_contracts.py").read_text(encoding="utf-8")
        self.assertIn("class Day155Test", source)
        self.assertIn("class Day156Test", source)
        self.assertIn("class Day167Test", (ROOT / "ai/streamforge/tests/test_cli.py").read_text())


if __name__ == "__main__":
    unittest.main()
