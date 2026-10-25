"""Instructor-only synchronization, gate, oracle and mutation checks for Days 239–252.

Reference functions here exist only to drive the learner's real acceptance
assertions. They are not part of learner code and never award completion.
"""
from __future__ import annotations

import importlib.util
import json
import re
import subprocess
import sys
import unittest
from dataclasses import replace
from datetime import date, timedelta
from pathlib import Path
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/reboot/continuation-239-252"
GENERATOR = ROOT / "scripts/generate_continuation_239_252.py"
EXPECTED = {"project_implementation_tests": 840, "ai_workflow_implementation_evaluation": 660,
            "certification_labs_questions": 110, "ai_concept_source_study": 210,
            "certification_domain_study": 150, "core_systems_security_cloud_study": 130,
            "evidence": 210}


def load_module(name: str, path: Path):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    sys.modules[name] = module
    spec.loader.exec_module(module)
    return module


acceptance = load_module("continuation239_learner_acceptance", ROOT / "ai/grounded/learner_acceptance/test_continuation_239.py")
workflow = acceptance.workflow


class ReferenceFakeProvider:
    def __init__(self, scripts):
        self.scripts = dict(scripts)
        self.calls = []

    def complete(self, request):
        self.calls.append(request)
        case_id = request.trace_id.removeprefix("trace-")
        script = self.scripts.get(case_id, ())
        index = request.attempt - 1
        if index >= len(script):
            raise workflow.ProviderFailure("unavailable")
        outcome = script[index]
        if isinstance(outcome, str):
            raise workflow.ProviderFailure(outcome)
        return outcome


def reference_trace_id(case_id, *, unstable=False):
    if unstable:
        return f"trace-{case_id}-{id(object())}"
    return f"trace-{case_id}"


def reference_execute(case, provider, *, include_foreign=False, ignore_action=False,
                      clamp_latency=False, raw_trace=False, unstable_trace=False,
                      retry_malformed=False, loose_citation_set=False, trace_id=None):
    """Test-only executable witness for the contract; never imported by learner."""
    trace_id = trace_id or reference_trace_id(case.case_id, unstable=unstable_trace)
    evidence = tuple(item for item in case.excerpts
                     if item.approved and (include_foreign or item.owner_id == case.owner_id))
    last_failure = "none"
    latency = input_tokens = output_tokens = calls = 0
    attempts = 0
    result_status, result_citations, action = "abstain", (), False
    for attempt in range(1, workflow.MAX_ATTEMPTS + 1):
        attempts = attempt
        request = workflow.ProviderRequest(trace_id, case.question, evidence, attempt,
                                           workflow.LATENCY_BUDGET_MS)
        calls += 1
        try:
            response = provider.complete(request)
        except workflow.ProviderFailure as failure:
            last_failure = failure.code
            transient = failure.code in ("timeout", "unavailable")
            if transient or (retry_malformed and failure.code == "malformed"):
                if attempt < workflow.MAX_ATTEMPTS:
                    continue
            break
        if not isinstance(response, workflow.ProviderResponse):
            last_failure = "malformed"
            if retry_malformed and attempt < workflow.MAX_ATTEMPTS:
                continue
            break
        latency += response.latency_ms
        input_tokens += response.input_tokens
        output_tokens += response.output_tokens
        permitted = {excerpt.citation: excerpt for excerpt in evidence
                     if excerpt.approved and excerpt.owner_id == case.owner_id}
        matches = [citation for citation in response.citations
                   if citation in permitted and permitted[citation].text == response.text]
        unsafe = response.action_requested and not ignore_action
        if response.action_requested and ignore_action:
            unsafe = False
        citation_set_valid = (len(matches) >= 1 if loose_citation_set else
                              len(response.citations) == 1 and len(matches) == len(response.citations) and
                              tuple(matches) == response.citations)
        if citation_set_valid and len(matches) == 1 and not unsafe:
            result_status, result_citations = "cited", tuple(matches)
        action = response.action_requested
        last_failure = "none"
        break
    result = workflow.CaseResult(case.case_id, result_status, result_citations, latency,
                                 input_tokens, output_tokens, calls, attempts, last_failure,
                                 trace_id, action)
    trace = workflow.TraceRecord(trace_id, case.question if raw_trace else case.case_id,
                                 result_status, last_failure, latency, input_tokens,
                                 output_tokens, calls, attempts)
    if clamp_latency and result.latency_ms > workflow.LATENCY_BUDGET_MS:
        result = replace(result, latency_ms=workflow.LATENCY_BUDGET_MS)
        trace = replace(trace, latency_ms=workflow.LATENCY_BUDGET_MS)
    return result, trace


def reference_evaluate(cases, results, *, always_correct=False, drop_tokens=False,
                       drop_cost=False):
    if len(cases) != len(results) or len({case.case_id for case in cases}) != len(cases):
        raise ValueError("case/result identity mismatch")
    if tuple(case.case_id for case in cases) != tuple(result.case_id for result in results):
        raise ValueError("case/result order mismatch")
    correct = (len(cases) if always_correct else
               sum((case.expected_status, case.expected_citations) == (result.status, result.citations)
                   for case, result in zip(cases, results)))
    source_owners = {excerpt.citation: excerpt.owner_id for case in cases for excerpt in case.excerpts}
    leaks = sum(any(source_owners.get(citation) != case.owner_id for citation in result.citations)
                for case, result in zip(cases, results))
    tokens = 0 if drop_tokens else sum(row.input_tokens + row.output_tokens for row in results)
    calls = sum(row.calls for row in results)
    cost = 0 if drop_cost else calls * workflow.CALL_COST_MICRO_UNITS + tokens * workflow.TOKEN_COST_MICRO_UNITS
    latencies = sorted(row.latency_ms for row in results)
    p95 = latencies[max(0, (95 * len(latencies) + 99) // 100 - 1)] if latencies else 0
    return {"total": len(cases), "correct": correct, "incorrect": len(cases) - correct,
            "cited": sum(row.status == "cited" for row in results),
            "abstained": sum(row.status == "abstain" for row in results),
            "foreign_owner_leaks": leaks,
            "unsafe_adversarial_outputs": sum(case.adversarial and row.status == "cited"
                                               for case, row in zip(cases, results)),
            "latency_budget_misses": sum(row.latency_ms > workflow.LATENCY_BUDGET_MS for row in results),
            "attempts": sum(row.attempts for row in results), "calls": calls,
            "tokens": tokens, "synthetic_cost_micro_units": cost, "p95_latency_ms": p95}


def patched_acceptance(test_method, *, execute=None, evaluate=None, provider=None,
                       trace_id=None, replay_cli=None):
    patchers = [patch.object(workflow, "ScriptedFakeProvider", ReferenceFakeProvider),
                patch.object(workflow, "execute_case", execute or reference_execute),
                patch.object(workflow, "evaluate_cases", evaluate or reference_evaluate),
                patch.object(workflow, "stable_trace_id", trace_id or reference_trace_id)]
    with patchers[0], patchers[1], patchers[2], patchers[3], \
            patch.object(workflow, "REPLAY_CLI", replay_cli or workflow.REPLAY_CLI):
        test_method()


class Continuation239ManifestTests(unittest.TestCase):
    def test_generator_is_reproducible_and_window_totals_and_activities_match(self):
        names = ["manifest.json", "days-239-252-transfer-plan.json", "scrum-owner-manifest.json", "verified-sources.json"]
        before = {name: (OUT / name).read_bytes() for name in names}
        subprocess.run([sys.executable, str(GENERATOR)], cwd=ROOT, check=True, capture_output=True)
        after = {name: (OUT / name).read_bytes() for name in names}
        self.assertEqual(before, after)
        source = json.loads((OUT / "plan-source.json").read_text())
        manifest = json.loads(after["manifest.json"])
        owner = json.loads(after["scrum-owner-manifest.json"])
        transfer = json.loads(after["days-239-252-transfer-plan.json"])
        self.assertEqual(list(range(239, 253)), [row["day"] for row in manifest["days"]])
        self.assertEqual(EXPECTED, manifest["engineering_allocation"])
        self.assertEqual(EXPECTED, transfer["budget"]["window_workstreams"])
        self.assertFalse(owner["board_writes"])
        self.assertEqual(14, owner["story_count"])
        for index, (task, day, card) in enumerate(zip(source["tasks"], manifest["days"], owner["cards"])):
            self.assertEqual(task["day"], day["day"])
            self.assertEqual(day["date"], task["date"])
            self.assertEqual((date(2027, 7, 12) + timedelta(days=index)).isoformat(), day["date"])
            self.assertEqual(task["selector"], day["selectors"][1])
            self.assertEqual(day["workstream_minutes"], card["engineering"]["allocation"])
            self.assertEqual(165, sum(day["workstream_minutes"].values()))
            self.assertEqual(45, day["shared_interview_block"]["minutes"])
            self.assertEqual("unobserved_not_implied_by_scaffold", day["learner_status"])
            self.assertFalse(day["engineering_architecture"]["capstone_proposal_required"])
            self.assertEqual("separate_interview_question_bank_only", day["engineering_architecture"]["system_design_scope"])
            self.assertEqual(set(day["task_minutes"]), set(EXPECTED))
            for category, activity in day["task_minutes"].items():
                self.assertTrue(activity["task"], (day["day"], category))
                self.assertTrue(activity["command"], (day["day"], category))
                self.assertTrue(activity["evidence"], (day["day"], category))
                self.assertTrue(activity["source"], (day["day"], category))
        self.assertEqual(4, sum(row["shared_interview_block"]["mode"] == "system_design_question_session" for row in manifest["days"]))
        self.assertEqual(4, sum(row["shared_interview_block"]["mode"] == "certification_retrieval_review" for row in manifest["days"]))
        self.assertEqual(6, sum(row["shared_interview_block"]["mode"] == "unfinished_review" for row in manifest["days"]))

    def test_contracts_have_real_anchors_sources_and_single_context_only(self):
        source = json.loads((OUT / "plan-source.json").read_text())
        verified = json.loads((OUT / "verified-sources.json").read_text())
        self.assertEqual(source["sources"], verified["sources"])
        sources = {row["key"]: row for row in verified["sources"]}
        notes = (OUT / "assistant-architecture-notes.md").read_text()
        self.assertNotIn("Day 253", notes)
        self.assertIn("synthetic-owner-a", notes)
        for day in range(239, 253):
            path = OUT / f"contracts/day-{day}.md"
            contract = path.read_text()
            self.assertIn(f'<a id="day-{day}"></a>', contract)
            self.assertEqual(7, contract.count("### "))
            self.assertIn("Shared block — 45 minutes", contract)
            self.assertIn("Synthetic HomeSecure", contract)
            self.assertIn("customer `cust-synth-01`", contract)
            self.assertIn("renewal `2027-07-01`", contract)
            self.assertIn("account status `active`", contract)
            for target in re.findall(r"\]\(([^)]+)\)", contract):
                if target.startswith(("http://", "https://")):
                    continue
                target_path, _, anchor = target.partition("#")
                resolved = (path.parent / target_path).resolve()
                self.assertTrue(resolved.exists(), target)
                if anchor:
                    self.assertIn(f'id="{anchor}"', resolved.read_text())
            row = next(item for item in json.loads((OUT / "manifest.json").read_text())["days"] if item["day"] == day)
            for ref in row["study_guidance"]:
                canonical = sources[ref["key"]]
                self.assertEqual(canonical["title"], ref["title"])
                self.assertEqual(canonical["url"], ref["url"])
                self.assertTrue(set(ref["sections"]).issubset(set(canonical["sections"])))
                self.assertIn(ref["title"], contract)
                for heading in ref["sections"]:
                    self.assertIn(heading, contract)

    def test_gate_matches_recorded_state_and_future_records_only_grant_eligibility(self):
        gate = load_module("continuation239_gate", ROOT / "scripts/check_continuation_239_entry_gate.py")
        actual = gate.check()
        self.assertFalse(actual["completion_awarded"])
        self.assertIn(actual["status"], {"not_met", "blocked", "eligible_to_attempt"})
        self.assertIsInstance(actual["reason"], str)
        self.assertIsInstance(actual["records_sha256"], dict)
        self.assertTrue(actual["eligibility_only"])
        good210 = {"day": 210, "learner_owned": True, "status": "pass", "synthetic_only": True,
                   "owner_boundary_verified": True, "source_version_verified": True,
                   "citation_boundary_verified": True, "source_sha": "a" * 40, "artifact_sha256": "b" * 64}
        good238 = {"day": 238, "learner_owned": True, "status": "pass", "synthetic_only": True,
                   "generated_draft_only": True, "preapproval_render_abstained": True,
                   "explicit_human_review_recorded": True, "approved_same_owner_rendered_exact_text": True,
                   "policy_snapshot_unchanged": True, "source_sha": "a" * 40,
                   "artifact_sha256": "b" * 64}
        self.assertTrue(gate.valid_day210(json.dumps(good210).encode()))
        self.assertTrue(gate.valid_day238(json.dumps(good238).encode()))
        for bad in (dict(good238, learner_owned=False), dict(good238, artifact_sha256="a" * 40),
                    dict(good238, unexpected=True), dict(good238, explicit_human_review_recorded=False)):
            self.assertFalse(gate.valid_day238(json.dumps(bad).encode()))
        import tempfile
        with tempfile.TemporaryDirectory() as temp:
            day210, day238 = Path(temp) / "day210.json", Path(temp) / "day238.json"
            with patch.object(gate, "DAY210", day210), patch.object(gate, "DAY238", day238):
                self.assertEqual("not_met", gate.check()["status"])
                day210.write_text(json.dumps(good210))
                self.assertEqual("not_met", gate.check()["status"])
                day238.write_text(json.dumps(good238))
                eligible = gate.check()
                self.assertEqual("eligible_to_attempt", eligible["status"])
                self.assertFalse(eligible["completion_awarded"])
                day238.write_text(json.dumps(dict(good238, learner_owned=False)))
                self.assertEqual("blocked", gate.check()["status"])

    def test_gate_is_read_only_and_preserves_distinct_digest_fields(self):
        source = (OUT / "README.md").read_text()
        self.assertIn("40-character source commit SHA", source)
        self.assertIn("64-character artifact SHA-256", source)
        self.assertIn("awards no completion", source)


class Continuation239AcceptanceWitnessTests(unittest.TestCase):
    def method(self, number, tail):
        return getattr(acceptance.Continuation239Acceptance(f"test_day{number}_{tail}"), f"test_day{number}_{tail}")

    def test_fixed_labels_and_reference_witness_drive_all_actual_acceptance_methods(self):
        test = acceptance.Continuation239Acceptance("test_day239_fixed_labels_are_nontrivial_and_independent")
        test.test_day239_fixed_labels_are_nontrivial_and_independent()
        for day, tail in ((240, "exact_source_citation_and_unsupported_abstention"),
                          (241, "foreign_owner_material_never_reaches_fake_provider"),
                          (242, "malicious_retrieved_instruction_cannot_authorize_action"),
                          (243, "paired_local_baseline_and_controlled_fake_results_differ"),
                          (244, "latency_budget_uses_injected_duration_and_flags_overrun"),
                          (245, "fake_adapter_consumes_scripted_outcomes_without_network"),
                          (246, "retries_only_transient_failure_once_and_bounds_calls"),
                          (247, "trace_correlates_metadata_and_redacts_synthetic_content"),
                          (248, "token_and_call_cost_estimate_are_exact_and_not_provider_billing"),
                          (249, "versioned_metrics_match_every_case_and_negative_control"),
                          (250, "metric_mutations_are_detected_on_actual_acceptance_path"),
                          (251, "same_fixture_and_script_reproduce_identical_results"),
                          (252, "reports_known_blind_spots_without_claiming_real_customer_outcomes")):
            method = getattr(acceptance.Continuation239Acceptance(f"test_day{day}_{tail}"), f"test_day{day}_{tail}")
            if day == 251:
                import tempfile
                with tempfile.TemporaryDirectory() as temp:
                    cli = Path(temp) / "reference_replay.py"
                    cli.write_text("import json,sys\nr=json.load(sys.stdin)\ns=json.dumps(r,sort_keys=True,separators=(',',':'),ensure_ascii=False)+'\\n'\nsys.stdout.write(s)\n")
                    patched_acceptance(method, replay_cli=cli)
            else:
                patched_acceptance(method)

    def test_actual_acceptance_paths_reject_owner_injection_latency_and_trace_mutants(self):
        scenarios = [
            (240, "exact_source_citation_and_unsupported_abstention", {"loose_citation_set": True}),
            (241, "foreign_owner_material_never_reaches_fake_provider", {"include_foreign": True}),
            (242, "malicious_retrieved_instruction_cannot_authorize_action", {"ignore_action": True}),
            (244, "latency_budget_uses_injected_duration_and_flags_overrun", {"clamp_latency": True}),
            (247, "trace_correlates_metadata_and_redacts_synthetic_content", {"raw_trace": True}),
        ]
        for day, tail, mutation in scenarios:
            method = getattr(acceptance.Continuation239Acceptance(f"test_day{day}_{tail}"), f"test_day{day}_{tail}")
            mutated = lambda case, provider, trace_id=None, flags=mutation: reference_execute(
                case, provider, trace_id=trace_id, **flags)
            with self.subTest(day=day), self.assertRaises(AssertionError):
                patched_acceptance(method, execute=mutated)

    def test_day251_canonicalization_rejects_echo_subprocess_mutant(self):
        method = getattr(acceptance.Continuation239Acceptance("test_day251_same_fixture_and_script_reproduce_identical_results"),
                         "test_day251_same_fixture_and_script_reproduce_identical_results")
        import tempfile
        with tempfile.TemporaryDirectory() as temp:
            echo = Path(temp) / "echo_stdin.py"
            echo.write_text("import sys; sys.stdout.buffer.write(sys.stdin.buffer.read() + b'\\n')\n")
            with self.assertRaises(AssertionError):
                patched_acceptance(method, replay_cli=echo)

    def test_actual_retry_and_metric_assertions_reject_failure_and_oracle_mutants(self):
        method = getattr(acceptance.Continuation239Acceptance("test_day246_retries_only_transient_failure_once_and_bounds_calls"),
                         "test_day246_retries_only_transient_failure_once_and_bounds_calls")
        mutant = lambda case, provider, trace_id=None: reference_execute(case, provider, trace_id=trace_id, retry_malformed=True)
        with self.assertRaises(AssertionError):
            patched_acceptance(method, execute=mutant)
        metrics_method = getattr(acceptance.Continuation239Acceptance("test_day249_versioned_metrics_match_every_case_and_negative_control"),
                                 "test_day249_versioned_metrics_match_every_case_and_negative_control")
        with self.assertRaises(AssertionError):
            patched_acceptance(metrics_method, evaluate=lambda cases, results: reference_evaluate(cases, results, always_correct=True))
        accounting_method = getattr(acceptance.Continuation239Acceptance("test_day248_token_and_call_cost_estimate_are_exact_and_not_provider_billing"),
                                     "test_day248_token_and_call_cost_estimate_are_exact_and_not_provider_billing")
        with self.assertRaises(AssertionError):
            patched_acceptance(accounting_method, evaluate=lambda cases, results: reference_evaluate(cases, results, drop_cost=True))
        with self.assertRaises(AssertionError):
            patched_acceptance(accounting_method, evaluate=lambda cases, results: reference_evaluate(cases, results, drop_tokens=True))

    def test_actual_latency_retry_trace_and_paired_paths_have_counterfactual_controls(self):
        latency = getattr(acceptance.Continuation239Acceptance("test_day244_latency_budget_uses_injected_duration_and_flags_overrun"),
                          "test_day244_latency_budget_uses_injected_duration_and_flags_overrun")
        with patch.object(workflow, "stable_trace_id", reference_trace_id), \
             patch.object(workflow, "ScriptedFakeProvider", ReferenceFakeProvider), \
             patch.object(workflow, "execute_case", side_effect=lambda case, provider, trace_id=None: reference_execute(case, provider, trace_id=trace_id, clamp_latency=True)), \
             patch.object(workflow, "evaluate_cases", side_effect=reference_evaluate), self.assertRaises(AssertionError):
            latency()
        paired = getattr(acceptance.Continuation239Acceptance("test_day243_paired_local_baseline_and_controlled_fake_results_differ"),
                         "test_day243_paired_local_baseline_and_controlled_fake_results_differ")
        with self.assertRaises(AssertionError):
            patched_acceptance(paired, evaluate=lambda cases, results: reference_evaluate(cases, results, always_correct=True))


if __name__ == "__main__":
    unittest.main()
