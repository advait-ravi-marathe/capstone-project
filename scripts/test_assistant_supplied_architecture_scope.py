import json
import re
import unittest
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MOCK_DAYS = {17, 21, 24, 28, 31, 35, 38, 42, 45, 49, 52, 56, 58, 60, 66, 69}


class AssistantSuppliedArchitectureScopeTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.notes = (ROOT / "docs/reboot/assistant-architecture-notes-15-70.md").read_text()
        cls.days = {}
        cls.assignments = {}
        for day in range(15, 31):
            canonical = json.loads((ROOT / "docs/reboot/manifest.json").read_text())
            owner = json.loads((ROOT / "docs/reboot/scrum-owner-manifest.json").read_text())
            c = next(row for row in canonical["days"] if row["day"] == day)
            o = next(row for row in owner["cards"] if row["day_number"] == day)
            cls.days[day] = (c, o)
            cls.assignments[day] = (c["interview_practice"], o["interview_practice"])
        for lo, hi, folder, curriculum_name in [
            (31, 44, "continuation-31-44", "fde31-44-v2-curriculum.json"),
            (45, 56, "continuation-45-56", "fde45-56-v2-curriculum.json"),
        ]:
            manifest = json.loads((ROOT / f"docs/reboot/{folder}/manifest.json").read_text())
            owner = json.loads((ROOT / f"docs/reboot/{folder}/scrum-owner-manifest.json").read_text())
            curriculum = json.loads((ROOT / f"docs/reboot/{folder}/{curriculum_name}").read_text())
            for day in range(lo, hi + 1):
                c = next(row for row in manifest["days"] if row["day"] == day)
                o = next(row for row in owner["cards"] if row["day_number"] == day)
                u = next(row for row in curriculum["days"] if row["day"] == day)
                cls.days[day] = (c, o, u)
                cls.assignments[day] = (c["interview_practice"], o["interview_practice"], u["interview_practice"])
        manifest = json.loads((ROOT / "docs/reboot/continuation-57-70/manifest.json").read_text())
        owner = json.loads((ROOT / "docs/reboot/continuation-57-70/scrum-owner-manifest.json").read_text())
        curriculum = json.loads((ROOT / "docs/reboot/continuation-57-70/fde57-70-v2-curriculum.json").read_text())
        for day in range(57, 71):
            c = next(row for row in manifest["days"] if row["day"] == day)
            o = next(row for row in owner["cards"] if row["day_number"] == day)
            u = next(row for row in curriculum["assignments"] if row["day"] == day)
            cls.days[day] = (c, o, u)
            cls.assignments[day] = (
                c["engineering"]["interview_assignment"],
                o["engineering"]["interview_assignment"],
                u,
            )

    def contract(self, day):
        if day <= 30:
            p = ROOT / f"docs/reboot/contracts/day-{day:02d}.md"
        elif day <= 44:
            p = ROOT / f"docs/reboot/continuation-31-44/contracts/day-{day}.md"
        elif day <= 56:
            p = ROOT / f"docs/reboot/continuation-45-56/contracts/day-{day}.md"
        else:
            p = ROOT / f"docs/reboot/continuation-57-70/contracts/day-{day}.md"
        return p, p.read_text()

    def test_every_assignment_is_synchronized_and_has_a_real_note_anchor(self):
        self.assertEqual(set(range(15, 71)), set(self.assignments))
        for day, assignments in self.assignments.items():
            with self.subTest(day=day):
                for assignment in assignments[1:]:
                    self.assertEqual(assignments[0], assignment)
                assignment = assignments[0]
                self.assertEqual("assistant_supplied", assignment["capstone_architecture"])
                self.assertEqual(
                    "system_design_interview_question_only" if day in MOCK_DAYS else "none",
                    assignment["proposal_scope"],
                )
                note_path = assignment["assistant_architecture_note"]
                target = ROOT / note_path.split("#", 1)[0]
                anchor = note_path.split("#", 1)[1]
                self.assertTrue(target.is_file(), note_path)
                self.assertIn(f'<a id="{anchor}"></a>', target.read_text())
                self.assertIn(f'<a id="day-{day}"></a>', self.notes)

    def test_non_mock_days_have_no_capstone_proposal_and_keep_full_acceptance_time(self):
        for day in set(range(15, 71)) - MOCK_DAYS:
            with self.subTest(day=day):
                assignments = self.assignments[day]
                assignment = assignments[0]
                self.assertEqual("assistant_supplied_capstone_architecture", assignment["mode"])
                self.assertEqual(0, assignment["duration_minutes"])
                self.assertFalse(assignment["proposal_before_hints_reference_or_ai"])
                self.assertNotIn("proposal_prompt", assignment)
                self.assertNotIn("proposal_minutes", assignment)
                self.assertEqual(85, assignment["practice_minutes_available"])
                self.assertEqual(85, assignment["practice_total_minutes"])
                p, text = self.contract(day)
                self.assertIn("Assistant-provided capstone architecture", text)
                self.assertNotRegex(text, r"(?m)^## (?:Learner-first|Interview-first) .*proposal")
                self.assertNotIn("before hints, reference answers, or AI", text)
                self.assertNotRegex(text, r"(?i)save (?:your|the) (?:initial )?proposal before")
                self.assertTrue(any(header in text for header in ("## Evidence", "## Evidence allocation", "## Practice and evidence")))
                self.assertTrue("changed paths" in text.lower() or "source SHA" in text)

    def test_sprint1_day09_scope_is_not_rewritten_as_owner_scoped(self):
        source = (ROOT / "src/main/java/assignments/insurance/QuoteRepositoryAdapter.java").read_text()
        self.assertIn("find(String journeyId, String quoteVersionId)", source)
        self.assertIn("(journeyId, quoteVersionId)", self.notes)
        self.assertIn("boundary introduced by the Day 12 principal guard", self.notes)
        self.assertNotIn("Bind owner and quote identifiers", self.notes)

    def test_day28_and_day56_notes_match_their_actual_harness_boundaries(self):
        day28 = next(row for row in json.loads((ROOT / "docs/reboot/manifest.json").read_text())["days"] if row["day"] == 28)
        day28_owner = next(row for row in json.loads((ROOT / "docs/reboot/scrum-owner-manifest.json").read_text())["cards"] if row["day_number"] == 28)
        day28_test = (ROOT / "src/test/java/assignments/reboot/Day28BrowserToDatabaseTest.java").read_text()
        self.assertIn("SpringApplicationBuilder", day28_test)
        self.assertIn('@GetMapping(value = "/__e2e/home-secure"', day28_test)
        self.assertIn("in-process Spring test route serving the built frontend", day28["engineering"]["assistant_architecture_note"]["decision"])
        self.assertEqual(day28["engineering"]["assistant_architecture_note"]["decision"], day28_owner["engineering"]["assistant_architecture_note"]["decision"])
        self.assertNotIn("packaged browser", day28["engineering"]["assistant_architecture_note"]["decision"].lower())

        day56 = next(row for row in json.loads((ROOT / "docs/reboot/continuation-45-56/manifest.json").read_text())["days"] if row["day"] == 56)
        day56_owner = next(row for row in json.loads((ROOT / "docs/reboot/continuation-45-56/scrum-owner-manifest.json").read_text())["cards"] if row["day_number"] == 56)
        day56_test = (ROOT / "src/test/java/assignments/renewal/RenewalPreviewTransportInfrastructureTest.java").read_text()
        self.assertIn("day56PreviewReadIsStableAcrossRestartAndDoesNotWrite", day56_test)
        self.assertIn("EmbeddedPostgres", day56_test)
        decision = day56["engineering"]["assistant_architecture_note"]["decision"]
        self.assertIn("two Spring server contexts in the same test JVM", decision)
        self.assertIn("not logout invalidation", decision)
        self.assertEqual(decision, day56_owner["engineering"]["assistant_architecture_note"]["decision"])

    def test_mock_proposals_are_limited_to_the_question_session(self):
        self.assertEqual(MOCK_DAYS, {day for day, rows in self.assignments.items()
                                     if rows[0]["mode"] == "45_minute_mock"})
        for day in MOCK_DAYS:
            with self.subTest(day=day):
                assignments = self.assignments[day]
                for assignment in assignments:
                    self.assertTrue(assignment["proposal_before_hints_reference_or_ai"])
                    self.assertEqual(45, assignment["duration_minutes"])
                    self.assertEqual("system_design_interview_question_only", assignment["proposal_scope"])
                _, text = self.contract(day)
                self.assertIn("Assistant-provided capstone architecture", text)
                self.assertIn("interview question only", text.lower())
                self.assertRegex(text, r"45[- ]minute")

    def test_sprint5_session_scope_uses_real_harnesses_and_no_nonmock_proposals(self):
        text = (ROOT / "docs/reboot/interview/sprint5-session-scope.md").read_text()
        for selector in (
            "day59VersionedBootstrapIsRepeatableAndNonDestructive",
            "day60OwnerScopedPreviewReadDoesNotWrite",
            "day68PackagedUiIssuesReadsBackAndReplaysExactlyOnce",
            "day69PreviewIsStableAcrossPackagedRestartAndLogout",
        ):
            self.assertIn(selector, text)
        self.assertNotIn("Five-minute pre-work", text)
        self.assertIn("No capstone design proposal", text)

    def test_day70_is_recovery_and_factual_handover_not_an_extra_design_mock(self):
        for source in (
            "docs/reboot/continuation-57-70/contracts/day-70.md",
            "docs/reboot/continuation-57-70/manifest.json",
            "docs/reboot/continuation-57-70/scrum-owner-manifest.json",
            "docs/reboot/continuation-57-70/fde57-70-v2-curriculum.json",
        ):
            text = (ROOT / source).read_text()
            self.assertIn("evidence handover", text.lower())
            self.assertNotIn("Complete the mastery, recovery and design mock", text)
            self.assertNotIn("C4/trust-boundary explanation and recovery/interview mock", text)
        contract = (ROOT / "docs/reboot/continuation-57-70/contracts/day-70.md").read_text()
        self.assertIn("55 minutes", contract)
        self.assertIn("20 minutes", contract)
        self.assertIn("10 minutes", contract)
        self.assertIn("not a system-design interview", contract.lower())

    def test_career_checkpoints_preserve_daily_budget_and_separate_dsa(self):
        text = (ROOT / "docs/reboot/fde-north-star-amendment.md").read_text()
        self.assertIn("Prepared:** 21 October 2026", text)  # historical provenance remains intact
        self.assertIn("120 engineering minutes per day apart from tiered DSA", text)
        self.assertIn("60/75/90-minute tiers through Day 171", text)
        self.assertIn("Backend engineer / SDE-II knowledge", text)
        self.assertIn("Senior / SDE-III capability", text)
        self.assertIn("Staff / Principal-shaped capability", text)
        self.assertIn("Senior FDE / AI deployment progression", text)
        self.assertIn("not another schedule or tracker", text)
        self.assertIn("https://job-boards.greenhouse.io/anthropic/jobs/5449229008", text)
        self.assertIn("https://www.google.com/about/careers/applications/jobs/results/83160180652417734-forward-deployed-engineer/", text)

    def test_north_star_schedule_and_teaching_policy_match_published_records(self):
        text = (ROOT / "docs/reboot/fde-north-star-amendment.md").read_text()
        manifest = json.loads((ROOT / "docs/reboot/continuation-31-44/manifest.json").read_text())
        authoritative = {row["day"]: row["date"] for row in manifest["days"]}
        documented = {}
        for day, label in re.findall(r"^\| (\d+) · ([A-Z][a-z]{2} \d{1,2}) \|", text, re.M):
            documented[int(day)] = datetime.strptime(f"{label} 2026", "%b %d %Y").date().isoformat()
        self.assertEqual({day: authoritative[day] for day in range(31, 45)}, documented)
        self.assertEqual("2026-11-25", authoritative[31])
        self.assertEqual("2026-12-08", authoritative[44])
        self.assertNotIn("19 November–2 December 2026", text)
        self.assertNotIn("31 · Nov 19", text)
        self.assertNotIn("Days 197–210 are paused", text)
        self.assertIn("continuation-197-270-inventory.json", text)
        self.assertIn("the assistant supplies the architecture and explains what it does and why", text)
        self.assertIn("System-design practice stays in the separate interview question bank.", text)
        self.assertNotIn("explain-backs", text)
        self.assertNotIn("Five-line simulated problem statement", text)
        self.assertNotIn("Tiny ADR", text)

    def test_old_continuation_proposal_is_labeled_historical_and_links_published_windows(self):
        text = (ROOT / "docs/reboot/proposals/days-197-270-continuation-proposal.md").read_text()
        self.assertIn("Status: superseded planning provenance", text)
        self.assertIn("sprint intents", text)
        self.assertIn("The published daily contracts now give their exact source titles and durations", text)
        self.assertNotIn("Status: review proposal only", text)
        inventory = json.loads((ROOT / "docs/reboot/continuation-197-270-inventory.json").read_text())
        self.assertEqual(270, inventory["through_day"])
        self.assertEqual(74, sum(window["day_count"] for window in inventory["windows"]))
        for window in inventory["windows"]:
            manifest = window["manifest"]
            self.assertIn(f"](../{manifest.removeprefix('docs/reboot/')})", text)
            self.assertTrue((ROOT / manifest).is_file())


if __name__ == "__main__":
    unittest.main()
