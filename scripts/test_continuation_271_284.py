"""Regression tests for the Day 271–732 extension and first sprint."""
from __future__ import annotations
import json
import re
import subprocess
import sys
import unittest
from datetime import date, timedelta
from pathlib import Path
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[1]
WINDOW = ROOT / "docs/reboot/continuation-271-284"


class ExtensionRegression(unittest.TestCase):
    def test_exact_day_math_and_capacity(self):
        start = date(2026, 10, 24)
        self.assertEqual(date(2027, 7, 20), start + timedelta(days=269))
        self.assertEqual(date(2027, 7, 21), start + timedelta(days=270))
        self.assertEqual(date(2028, 10, 24), start + timedelta(days=731))
        self.assertEqual(462, 732 - 270)
        self.assertEqual(33, 462 // 14)
        self.assertEqual((76230, 1270.5), (462 * 165, 462 * 165 / 60))
        self.assertEqual((20790, 346.5), (462 * 45, 462 * 45 / 60))
        self.assertEqual((15246, 254.1), (462 * 165 * 20 // 100, 462 * 165 * 0.20 / 60))
        self.assertEqual((60984, 1016.4), (462 * 165 - 15246, (462 * 165 - 15246) / 60))
        phase_days = (84, 84, 84, 84, 70, 56)
        self.assertEqual(462, sum(phase_days))
        self.assertEqual(1270.5, sum(days * 165 / 60 for days in phase_days))
        self.assertEqual(346.5, sum(days * 45 / 60 for days in phase_days))
        self.assertAlmostEqual(254.1, sum(days * 33 / 60 for days in phase_days))

    def test_extension_phases_are_measurable_and_not_preticketed(self):
        text = (ROOT / "docs/reboot/continuation-271-732/README.md").read_text()
        for phrase in ("JVM/OS/concurrency", "PostgreSQL internals", "Redis, Kafka and RabbitMQ",
                       "AWS deployment/security/operations", "Python/ML grounding",
                       "prompt-injection", "multiple release/restore cycles", "second materially distinct enterprise",
                       "measured reuse across both projects", "offline rehearsal", "October 2029"):
            self.assertIn(phrase.lower(), text.lower())
        self.assertIn("Only Days 271–284 are ticket-ready", text)
        self.assertIn("1,270.5 h", text)
        self.assertIn("346.5 h", text)
        self.assertIn("does not establish workplace delivery history", text)

    def test_teaching_policy_regressions_and_historical_window_provenance(self):
        root = (ROOT / "docs/reboot/README.md").read_text().lower()
        old = (ROOT / "docs/reboot/continuation-183-196/README.md").read_text().lower()
        for stale in ("system design is practiced inside the 85/25/10 engineering split",
                      "further scheduling waits for a sustainable daily time budget"):
            self.assertNotIn(stale, root)
        for stale in ("five-minute explain-back inside the 35-minute block",
                      "does not authorize days 197 onward"):
            self.assertNotIn(stale, old)
        self.assertIn("current assignment inventory", old)
        self.assertIn("current roadmap", old)
        self.assertIn("assistant supplies the architecture", root)
        self.assertIn("separate interview question-bank sessions", root)

    def test_generator_and_contract_exports_match_source(self):
        mode = "--check" if (WINDOW / "scrum-owner-manifest.json").is_file() else "--check-core"
        subprocess.run([sys.executable, "scripts/generate_continuation_271_284.py", mode], cwd=ROOT, check=True)
        src = json.loads((WINDOW / "plan-source.json").read_text())
        man = json.loads((WINDOW / "manifest.json").read_text())
        self.assertEqual(list(range(271, 285)), [d["day"] for d in man["days"]])
        self.assertEqual([d["date"] for d in src["days"]], [d["date"] for d in man["days"]])
        self.assertEqual(("2027-07-21", "2027-08-03"), (man["date_range"]["start"], man["date_range"]["end"]))
        self.assertEqual((2310, 630, 462), (man["engineering_minutes_total"], man["shared_minutes_total"], man["remediation_reserve_minutes"]))
        self.assertFalse(man["learner_completion_awarded"])
        owner_path = WINDOW / "scrum-owner-manifest.json"
        if owner_path.exists():
            owner = json.loads(owner_path.read_text())
            self.assertFalse(owner["board_writes"])
            self.assertEqual(14, owner["story_count"])
        self.assertEqual(14, len({d["shared_interview_block"]["task"] for d in man["days"]}))
        question_bank = (ROOT / "docs/reboot/interview/question-bank.md").read_text()
        for d in man["days"]:
            self.assertEqual(165, sum(d["workstream_minutes"].values()))
            self.assertEqual(45, d["shared_interview_block"]["minutes"])
            self.assertEqual("unobserved_not_implied_by_scaffold", d["learner_status"])
            prompt = d["shared_interview_block"]["task"]
            match = re.search(r"Q(\d{2})", prompt)
            self.assertIsNotNone(match, prompt)
            self.assertIn(f'id="q{match.group(1)}"', question_bank)
            for cat, spec in d["task_minutes"].items():
                self.assertEqual(d["workstream_minutes"][cat], spec["minutes"])
                self.assertTrue(spec["task"] and spec["command"] and spec["evidence"])
                if spec["minutes"]:
                    self.assertTrue(spec["source_paths"] or spec["source_keys"])
            contract = (WINDOW / "contracts" / f"day-{d['day']}.md").read_text()
            self.assertIn(f'id="day-{d["day"]}"', contract)
            self.assertIn("Assistant-supplied architecture", contract)
            for target in re.findall(r"\]\(([^)]+)\)", contract):
                if target.startswith(("http://", "https://")):
                    continue
                path, _, anchor = target.partition("#")
                dest = (WINDOW / "contracts" / path).resolve()
                self.assertTrue(dest.exists(), target)
                if anchor:
                    self.assertIn(f'id="{anchor}"', dest.read_text())

    def test_actual_postgres_fixture_is_complete_and_source_pinned(self):
        obj = json.loads((ROOT / "labs/runtime_sql/fixtures/observation.json").read_text())
        self.assertEqual("postgres18-owner-policy-revision-v1", obj["fixture_id"])
        self.assertEqual(5, len(obj["expected_ids"]))
        self.assertEqual(obj["expected_ids"], obj["observed_rows"])
        self.assertEqual(list(range(8, 3, -1)), [r["revision"] for r in obj["observed_rows"]])
        plan = obj["explain_json"][0]["Plan"]
        self.assertEqual(5, plan["Actual Rows"])
        self.assertGreaterEqual(plan["Shared Hit Blocks"], 0)
        self.assertGreaterEqual(plan["Shared Read Blocks"], 0)
        self.assertEqual("postgres:18.6@sha256:74935e72241653ca55e0414067e6d8763aceb8a810eb51b452253ec3dcfc4336", obj["postgres_image"])

    def test_acceptance_reference_and_mutants_use_actual_methods(self):
        from labs.runtime_sql.tests import test_acceptance as acceptance
        from labs.runtime_sql import learner_analysis, reference_analysis
        methods = ("test_exact_result_estimate_and_buffer_observations",
                   "test_foreign_or_missing_result_row_is_not_hidden_by_matching_count",
                   "test_wrong_plan_estimate_changes_q_error_and_zero_rows_are_explicit",
                   "test_negative_or_malformed_counters_fail_closed")
        for name in methods:
            case = acceptance.LearnerAcceptance(name)
            with patch.object(learner_analysis, "analyze_observation", side_effect=reference_analysis.analyze_observation):
                result = unittest.TestResult()
                case.run(result)
            self.assertTrue(result.wasSuccessful(), result.errors + result.failures)
        def wrong_rows(_):
            out = reference_analysis.analyze_observation(acceptance.load_fixture())
            return dict(out, row_correct=True)
        def bad_ratio(obs):
            return dict(reference_analysis.analyze_observation(obs), q_error=1.0)
        def bad_buffer(obs):
            return dict(reference_analysis.analyze_observation(obs), shared_read_blocks=0)
        def ignore_invalid(_):
            return reference_analysis.analyze_observation(
                json.loads((ROOT / "labs/runtime_sql/fixtures/observation.json").read_text()))
        for name, mutant in ((methods[1], wrong_rows), (methods[2], bad_ratio),
                             (methods[0], bad_buffer), (methods[3], ignore_invalid)):
            case = acceptance.LearnerAcceptance(name)
            with patch.object(learner_analysis, "analyze_observation", side_effect=mutant):
                result = unittest.TestResult()
                case.run(result)
            self.assertTrue(result.failures, name)


if __name__ == "__main__":
    unittest.main()
