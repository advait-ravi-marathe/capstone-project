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
        start = date(2026, 10, 26)
        self.assertEqual(date(2027, 7, 22), start + timedelta(days=269))
        self.assertEqual(date(2027, 7, 23), start + timedelta(days=270))
        self.assertEqual(date(2028, 10, 26), start + timedelta(days=731))
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
        self.assertEqual(("2027-07-23", "2027-08-05"), (man["date_range"]["start"], man["date_range"]["end"]))
        self.assertEqual((2310, 630, 462), (man["engineering_minutes_total"], man["shared_minutes_total"], man["remediation_reserve_minutes"]))
        self.assertFalse(man["learner_completion_awarded"])
        owner_path = WINDOW / "scrum-owner-manifest.json"
        if owner_path.exists():
            owner = json.loads(owner_path.read_text())
            self.assertFalse(owner["board_writes"])
            self.assertEqual(14, owner["story_count"])
        self.assertEqual(14, len({d["shared_interview_block"]["task"] for d in man["days"]}))
        self.assertGreaterEqual(len({tuple(d["study_guidance"][0]["sections"]) for d in man["days"]}), 7)
        question_bank = (ROOT / "docs/reboot/interview/question-bank.md").read_text()
        expected_dependencies = {
            271: [], 272: [271], 273: [272], 274: [273], 275: [271, 274], 276: [273, 274, 275],
            277: [271], 278: [], 279: [278], 280: [278],
            281: [273, 274, 275, 276], 282: [271], 283: [271],
            284: [271, 283],
        }
        for d in man["days"]:
            self.assertEqual(165, sum(d["workstream_minutes"].values()))
            self.assertEqual(45, d["shared_interview_block"]["minutes"])
            self.assertEqual("unobserved_not_implied_by_scaffold", d["learner_status"])
            self.assertEqual(expected_dependencies[d["day"]], d["depends_on_days"])
            prompt = d["shared_interview_block"]["task"]
            match = re.search(r"Q(\d{2})", prompt)
            self.assertIsNotNone(match, prompt)
            self.assertIn(f'id="q{match.group(1)}"', question_bank)
            for cat, spec in d["task_minutes"].items():
                self.assertEqual(d["workstream_minutes"][cat], spec["minutes"])
                self.assertTrue(spec["task"] and spec["command"] and spec["evidence"])
                if spec["minutes"]:
                    self.assertTrue(spec["source_paths"] or spec["source_keys"])
                if cat == "remediation_integration_observation_reserve":
                    for key in ("fallback_task", "fallback_command", "fallback_evidence"):
                        self.assertTrue(spec[key], (d["day"], key))
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
        day284 = next(d for d in man["days"] if d["day"] == 284)
        day284_lab_command = day284["task_minutes"]["project_implementation_tests"]["command"]
        for gate in ("check_continuation_239_entry_gate.py", "check_continuation_253_entry_gate.py",
                     "check_streamforge_169_182_entry_gate.py"):
            self.assertNotIn(gate, day284_lab_command)

    def test_new_roadmap_readme_relative_links_resolve(self):
        for path in (ROOT / "docs/reboot/continuation-271-284/README.md",
                     ROOT / "docs/reboot/continuation-271-732/README.md"):
            text = path.read_text()
            for target in re.findall(r"\]\(([^)]+)\)", text):
                if target.startswith(("http://", "https://")):
                    continue
                local = target.split("#", 1)[0]
                self.assertTrue((path.parent / local).resolve().exists(), (path, target))

    def test_actual_postgres_fixture_is_complete_and_source_pinned(self):
        obj = json.loads((ROOT / "labs/runtime_sql/fixtures/observation.json").read_text())
        self.assertEqual("postgres18-owner-policy-revision-v1", obj["fixture_id"])
        self.assertEqual(5, len(obj["expected_ids"]))
        self.assertEqual(obj["expected_ids"], obj["observed_rows"])
        self.assertEqual(list(range(8, 3, -1)), [r["revision"] for r in obj["observed_rows"]])
        plan = obj["explain_json"][0]["Plan"]
        self.assertEqual(5, plan["Actual Rows"])
        # These exact counters identify the checked-in capture only; fresh captures are tested against their own plan below.
        self.assertEqual(3, plan["Shared Hit Blocks"])
        self.assertEqual(2, plan["Shared Read Blocks"])
        self.assertEqual("postgres:18.6@sha256:74935e72241653ca55e0414067e6d8763aceb8a810eb51b452253ec3dcfc4336", obj["postgres_image"])

    def test_acceptance_reference_and_mutants_use_actual_methods(self):
        from labs.runtime_sql.tests import test_acceptance as acceptance
        from labs.runtime_sql import learner_analysis, reference_analysis
        methods = ("test_exact_result_estimate_and_buffer_observations",
                   "test_each_composite_key_component_and_order_are_enforced",
                   "test_missing_and_extra_rows_are_detected",
                   "test_wrong_plan_estimate_changes_q_error_and_zero_rows_are_explicit",
                   "test_negative_or_malformed_counters_fail_closed")
        for name in methods:
            case = acceptance.LearnerAcceptance(name)
            with patch.object(learner_analysis, "analyze_observation", side_effect=reference_analysis.analyze_observation):
                result = unittest.TestResult()
                case.run(result)
            self.assertTrue(result.wasSuccessful(), result.errors + result.failures)
        def compare_owner_only(obs):
            out = reference_analysis.analyze_observation(obs)
            out["row_correct"] = all(a["owner_id"] == e["owner_id"] for a, e in zip(obs["observed_rows"], obs["expected_ids"]))
            return out
        def ignore_order(obs):
            out = reference_analysis.analyze_observation(obs)
            out["row_correct"] = sorted(obs["observed_rows"], key=lambda row: row["revision"], reverse=True) == obs["expected_ids"]
            return out
        def count_only(obs):
            out = reference_analysis.analyze_observation(obs)
            out["row_correct"] = len(obs["observed_rows"]) == len(obs["expected_ids"])
            return out
        def bad_ratio(obs):
            return dict(reference_analysis.analyze_observation(obs), q_error=1.0)
        def bad_buffer(obs):
            return dict(reference_analysis.analyze_observation(obs), shared_read_blocks=0)
        def ignore_invalid(_):
            return reference_analysis.analyze_observation(
                json.loads((ROOT / "labs/runtime_sql/fixtures/observation.json").read_text()))
        def overclaim(obs):
            return dict(reference_analysis.analyze_observation(obs), single_run_performance_claim="fast")
        for name, mutant in ((methods[1], compare_owner_only), (methods[1], ignore_order),
                             (methods[1], count_only), (methods[2], count_only),
                             (methods[3], bad_ratio), (methods[0], bad_buffer),
                             (methods[0], overclaim), (methods[4], ignore_invalid)):
            case = acceptance.LearnerAcceptance(name)
            with patch.object(learner_analysis, "analyze_observation", side_effect=mutant):
                result = unittest.TestResult()
                case.run(result)
            self.assertTrue(result.failures, name)

    def test_default_capture_does_not_overwrite_canonical_fixture_and_counters_are_run_local(self):
        from labs.runtime_sql import reference_analysis
        canonical = ROOT / "labs/runtime_sql/fixtures/observation.json"
        before = canonical.read_bytes()
        output = ROOT / "target/runtime-sql-lab/observation.json"
        output.unlink(missing_ok=True)
        subprocess.run(["scripts/capture_runtime_sql_fixture.sh"], cwd=ROOT, check=True, capture_output=True, text=True)
        self.assertEqual(before, canonical.read_bytes())
        self.assertTrue(output.is_file())
        captured = json.loads(output.read_text())
        self.assertEqual("baseline", captured["variant"])
        result = reference_analysis.analyze_observation(captured)
        plan = captured["explain_json"][0]["Plan"]
        self.assertTrue(result["row_correct"])
        self.assertEqual(plan["Shared Hit Blocks"], result["shared_hit_blocks"])
        self.assertEqual(plan["Shared Read Blocks"], result["shared_read_blocks"])
        self.assertGreaterEqual(result["shared_hit_blocks"], 0)
        self.assertGreaterEqual(result["shared_read_blocks"], 0)

    def test_skew_analyze_and_index_variants_preserve_complete_results(self):
        from labs.runtime_sql import reference_analysis
        for variant in ("skew", "without-index", "reverse-index"):
            output = ROOT / f"target/runtime-sql-lab/{variant}.json"
            subprocess.run(["scripts/capture_runtime_sql_fixture.sh", str(output.relative_to(ROOT)), variant],
                           cwd=ROOT, check=True, capture_output=True, text=True)
            captured = json.loads(output.read_text())
            self.assertEqual(variant, captured["variant"])
            self.assertEqual(captured["expected_ids"], captured["observed_rows"])
            self.assertTrue(reference_analysis.analyze_observation(captured)["row_correct"])
            if variant == "skew":
                experiment = captured["skew_statistics_experiment"]
                self.assertEqual(4240, experiment["expected_owner_rows"])
                self.assertEqual(4240, experiment["observed_owner_rows"])
                self.assertEqual(1, len(experiment["before_analyze"]))
                self.assertEqual(1, len(experiment["after_analyze"]))
                report = json.loads(subprocess.run([sys.executable, "scripts/report_runtime_sql_skew.py", str(output)],
                                                   cwd=ROOT, check=True, capture_output=True, text=True).stdout)
                self.assertEqual(4240, report["owner_rows"])
                self.assertFalse(report["estimate_change_required"])

    def test_timing_summarizer_reports_center_and_spread_without_a_threshold(self):
        from scripts.summarize_runtime_sql_runs import summarize
        import tempfile
        with tempfile.TemporaryDirectory() as raw:
            paths=[]
            for index, time_ms in enumerate((1.0, 2.0, 4.0)):
                obj={"variant":"baseline","expected_ids":[{"owner_id":"a","policy_id":"p","revision":1}],
                     "observed_rows":[{"owner_id":"a","policy_id":"p","revision":1}],
                     "explain_json":[{"Plan":{"Actual Total Time":time_ms}}]}
                path=Path(raw)/f"run-{index}.json"; path.write_text(json.dumps(obj)); paths.append(str(path))
            result=summarize(paths)
            self.assertEqual(3,result["run_count"])
            self.assertEqual(2.0,result["median_ms"])
            self.assertAlmostEqual(1.247219128924647,result["population_stdev_ms"])
            self.assertIn("no performance",result["interpretation"])


if __name__ == "__main__":
    unittest.main()
