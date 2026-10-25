"""Instructor checks for synchronized Days 225–238 roadmap artifacts."""
from __future__ import annotations

import importlib.util
import json
import re
import subprocess
import sys
import unittest
from dataclasses import replace
from pathlib import Path
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/reboot/continuation-225-238"
GENERATOR = ROOT / "scripts/generate_continuation_225_238.py"
EXPECTED = {
    "project_implementation_tests": 1140,
    "ai_workflow_implementation_evaluation": 360,
    "certification_labs_questions": 110,
    "ai_concept_source_study": 150,
    "certification_domain_study": 120,
    "core_systems_security_cloud_study": 220,
    "evidence": 210,
}


def load_module(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    sys.modules[name] = module
    spec.loader.exec_module(module)
    return module


def reference_review_for_harness(workflow, *, enforce_revision=True, authorize_before_replay=True,
                                 preserve_audit=True, sequence_always_one=False,
                                 replay_foreign_owner=False, replay_blank_reviewer=False):
    """Test-only review witness; not imported by or credited to the learner."""
    def review(ledger, snapshot, command):
        draft = next((d for d in ledger.drafts if d.draft_id == command.draft_id), None)
        authorized = bool(command.reviewer_id.strip()) and command.reviewer_owner_id == snapshot.owner_id and draft is not None and draft.owner_id == command.reviewer_owner_id

        def denied():
            return workflow.ReviewResult("denied", ledger, None)

        def fingerprint(cmd):
            return (cmd.draft_id, cmd.reviewer_id, cmd.reviewer_owner_id, cmd.decision,
                    cmd.expected_policy_version, cmd.expected_snapshot_sha256, str(cmd.expected_revision))

        def replay_result(receipt):
            event = ledger.audit[receipt.event_sequence - 1]
            return workflow.ReviewResult("replayed", ledger, event)

        receipt = next((r for r in ledger.receipts if r.idempotency_key == command.idempotency_key), None)
        receipt = next((r for r in ledger.receipts if r.idempotency_key == command.idempotency_key), None)
        if not authorized:
            if receipt is not None and not authorize_before_replay:
                return replay_result(receipt)
            if receipt is not None and replay_foreign_owner and command.reviewer_owner_id != snapshot.owner_id:
                return replay_result(receipt)
            if receipt is not None and replay_blank_reviewer and not command.reviewer_id.strip():
                return replay_result(receipt)
            return denied()
        if receipt is not None:
            if receipt.command_fingerprint == fingerprint(command):
                return replay_result(receipt)
            return workflow.ReviewResult("conflict", ledger, None)
        if (enforce_revision and command.expected_revision != ledger.revision) or draft is None or draft.status != "DRAFT":
            return workflow.ReviewResult("conflict", ledger, None)
        if (command.expected_policy_version != snapshot.policy_version or
                command.expected_snapshot_sha256 != snapshot.snapshot_sha256 or
                draft.policy_version != snapshot.policy_version or draft.snapshot_sha256 != snapshot.snapshot_sha256 or
                command.decision not in ("approve", "reject")):
            return workflow.ReviewResult("conflict", ledger, None)
        to_status = "APPROVED" if command.decision == "approve" else "REJECTED"
        sequence = 1 if sequence_always_one else ledger.revision + 1
        event = workflow.AuditEvent(sequence, draft.draft_id, draft.owner_id, command.reviewer_id,
                                    command.decision, draft.status, to_status, draft.policy_version,
                                    draft.snapshot_sha256, command.idempotency_key)
        drafts = tuple(replace(d, status=to_status) if d.draft_id == draft.draft_id else d for d in ledger.drafts)
        audit = ledger.audit + (event,)
        if not preserve_audit and ledger.audit:
            audit = (event,)
        receipt = workflow.ReviewReceipt(command.idempotency_key, fingerprint(command), event.sequence)
        updated = replace(ledger, drafts=drafts, audit=audit, receipts=ledger.receipts + (receipt,))
        return workflow.ReviewResult("approved" if to_status == "APPROVED" else "rejected", updated, event)
    return review


def reference_classifier_for_harness(workflow, *, mutation=None):
    """Test-only evidence classifier; expected labels are never inputs."""
    def classify(snapshot, candidate):
        if mutation == "always_abstain" or candidate is None:
            return workflow.ClassificationObservation("abstain", ())
        if mutation == "cite_any_candidate":
            return workflow.ClassificationObservation("cited", candidate.citations, approval_requested=True)
        available = {excerpt.citation: excerpt for excerpt in snapshot.excerpts if excerpt.owner_id == snapshot.owner_id}
        valid = (candidate.owner_id == snapshot.owner_id and candidate.policy_version == snapshot.policy_version and
                 candidate.snapshot_sha256 == snapshot.snapshot_sha256 and len(candidate.citations) == 1 and
                 candidate.citations[0] in available and candidate.text == available[candidate.citations[0]].text)
        if valid:
            return workflow.ClassificationObservation("cited", candidate.citations)
        return workflow.ClassificationObservation("abstain", ())
    return classify


def reference_evaluator_for_harness(workflow):
    """Test-only injected evaluator for checking acceptance assertions."""
    def evaluate(cases, snapshot, *, classifier):
        observations = tuple(classifier(snapshot, case.candidate) for case in cases)
        correct = sum((o.status, o.citations) == (c.expected_status, c.expected_citations)
                      for c, o in zip(cases, observations))
        source_owners = {excerpt.citation: excerpt.owner_id for excerpt in snapshot.excerpts}
        leaks = sum(any(source_owners.get(citation, case.owner_id) != case.owner_id for citation in o.citations)
                    for case, o in zip(cases, observations))
        return {"total": len(cases), "correct": correct, "incorrect": len(cases) - correct,
                "cited": sum(o.status == "cited" for o in observations),
                "abstained": sum(o.status == "abstain" for o in observations),
                "foreign_owner_leaks": leaks,
                "unexpected_approval": sum(o.approval_requested for o in observations)}
    return evaluate


def reference_renderer_for_harness(workflow):
    """Test-only owner/status gate used to reach the metric assertion path."""
    def render(ledger, draft_id, viewer_owner_id):
        draft = next((d for d in ledger.drafts if d.draft_id == draft_id), None)
        if draft is None or draft.status != "APPROVED" or draft.owner_id != viewer_owner_id:
            return {"status": "abstain", "text": workflow.ABSTAIN_TEXT, "citations": ()}
        return {"status": "cited", "text": draft.text, "citations": draft.citations}
    return render


class Continuation225ManifestTests(unittest.TestCase):
    def test_generator_is_reproducible_and_all_exports_match_source(self):
        names = ["manifest.json", "days-225-238-transfer-plan.json", "scrum-owner-manifest.json", "verified-sources.json"]
        before = {name: (OUT / name).read_bytes() for name in names}
        subprocess.run([sys.executable, str(GENERATOR)], cwd=ROOT, check=True, capture_output=True)
        after = {name: (OUT / name).read_bytes() for name in names}
        self.assertEqual(before, after)
        source = json.loads((OUT / "plan-source.json").read_text())
        manifest = json.loads(after["manifest.json"])
        transfer = json.loads(after["days-225-238-transfer-plan.json"])
        owner = json.loads(after["scrum-owner-manifest.json"])
        self.assertEqual(list(range(225, 239)), [d["day"] for d in manifest["days"]])
        self.assertEqual(14, owner["story_count"])
        self.assertEqual(14, len(transfer["days"]))
        self.assertEqual(EXPECTED, manifest["engineering_allocation"])
        self.assertEqual(EXPECTED, transfer["budget"]["window_workstreams"])
        self.assertEqual(EXPECTED, {k: sum(c["engineering"]["allocation"][k] for c in owner["cards"]) for k in EXPECTED})
        self.assertEqual(source["gate"], manifest["gate_policy"])
        self.assertEqual(source["base_commit"], owner["base_commit"])
        self.assertFalse(owner["board_writes"])
        self.assertEqual([225], [t["day"] for t in source["tasks"] if t.get("ai_fixture_only")])
        for i, (day, card, task) in enumerate(zip(manifest["days"], owner["cards"], source["tasks"])):
            self.assertEqual(task["day"], day["day"])
            self.assertEqual(day["workstream_minutes"], card["engineering"]["allocation"])
            self.assertEqual(165, sum(day["workstream_minutes"].values()))
            self.assertEqual(45, day["shared_interview_block"]["minutes"])
            self.assertEqual(day["date"], card["date"])
            self.assertEqual(f"2027-06-{7+i:02d}", day["date"])
            self.assertEqual(task["selector"], day["selectors"][1])
            self.assertTrue(day["task_minutes"]["project_implementation_tests"])
            self.assertTrue(day["task_minutes"]["ai_workflow_implementation_evaluation"])
            if day["day"] == 225:
                self.assertIn("does not call the classifier", day["task_minutes"]["ai_workflow_implementation_evaluation"])
            else:
                self.assertIn("only when the gate is eligible", day["task_minutes"]["ai_workflow_implementation_evaluation"])
            for category in ("ai_concept_source_study", "certification_domain_study", "core_systems_security_cloud_study", "evidence"):
                self.assertTrue(day["task_minutes"][category], (day["day"], category))
            self.assertEqual("unobserved_not_implied_by_scaffold", day["learner_status"])
            self.assertFalse(day["engineering_architecture"]["capstone_proposal_required"])
            self.assertEqual("separate_interview_question_bank_only", day["engineering_architecture"]["system_design_scope"])
            self.assertEqual(day["study_refs"], [s["url"] for s in day["study_guidance"]])
        self.assertEqual(4, sum(d["shared_interview_block"]["mode"] == "system_design_question_session" for d in manifest["days"]))
        self.assertEqual(4, sum(d["shared_interview_block"]["mode"] == "certification_retrieval_review" for d in manifest["days"]))
        self.assertEqual(6, sum(d["shared_interview_block"]["mode"] == "unfinished_review" for d in manifest["days"]))

    def test_day_anchors_contract_links_exact_sources_and_no_day239(self):
        notes = (OUT / "assistant-architecture-notes.md").read_text()
        readme = (OUT / "README.md").read_text()
        self.assertIn("entry-gate-and-recovery", readme)
        self.assertNotIn("Day 239", notes)
        source = json.loads((OUT / "plan-source.json").read_text())
        verified = json.loads((OUT / "verified-sources.json").read_text())
        verified_by_key = {s["key"]: s for s in verified["sources"]}
        self.assertEqual(source["sources"], verified["sources"])
        for n in range(225, 239):
            contract_path = OUT / f"contracts/day-{n}.md"
            contract = contract_path.read_text()
            self.assertIn(f'<a id="day-{n}"></a>', notes)
            self.assertIn(f'<a id="day-{n}"></a>', contract)
            self.assertIn("Engineering budget (165m)", contract)
            self.assertIn("Study references:", contract)
            self.assertIn("Shared 45-minute block:", contract)
            for target in re.findall(r"\]\(([^)]+)\)", contract):
                if target.startswith(("http://", "https://")):
                    continue
                path, _, fragment = target.partition("#")
                resolved = (contract_path.parent / path).resolve()
                self.assertTrue(resolved.exists(), f"broken link {target} in Day {n}")
                if fragment:
                    self.assertIn(f'id="{fragment}"', resolved.read_text())
        for day in json.loads((OUT / "manifest.json").read_text())["days"]:
            task = day["task"]
            for ref in day["study_guidance"]:
                canonical = verified_by_key[ref["key"]]
                self.assertEqual(canonical["title"], ref["title"])
                self.assertEqual(canonical["url"], ref["url"])
                self.assertTrue(set(ref["sections"]).issubset(set(canonical["sections"])))
                contract = (OUT / f"contracts/day-{day['day']}.md").read_text()
                self.assertIn(ref["title"], contract)
                for section in ref["sections"]:
                    self.assertIn(section, contract)
            self.assertIn("python3 scripts/check_continuation_211_entry_gate.py", day["selectors"])
            self.assertEqual("SAA_GUIDE" if task["cert_source_section"].startswith("Content Domain") else "DEV_ASSOCIATE", task["cert_source_key"])

    def test_day225_actual_fixture_acceptance_rejects_counterfactual_label_mutations(self):
        acceptance = load_module("continuation_225_acceptance_day225", ROOT / "ai/grounded/learner_acceptance/test_continuation_225.py")
        method = acceptance.Continuation225Acceptance("test_day225_independent_evaluation_cases_have_nontrivial_expected_outcomes")
        method.test_day225_independent_evaluation_cases_have_nontrivial_expected_outcomes()
        labels = acceptance._day225_eval_cases()
        mutants = (
            labels[:-1],
            (replace(labels[0], expected_citations=("synthetic-policy-001@v6",)), *labels[1:]),
            (replace(labels[0], expected_status="abstain", expected_citations=()), *labels[1:]),
            (replace(labels[1], candidate=acceptance.VALID), *labels[1:2], *labels[2:]),
        )
        for mutant in mutants:
            with self.subTest(mutant=mutant):
                with patch.object(acceptance, "_day225_eval_cases", return_value=mutant):
                    with self.assertRaises(AssertionError):
                        method.test_day225_independent_evaluation_cases_have_nontrivial_expected_outcomes()

    def test_day230_actual_approval_acceptance_rejects_mutated_review_results(self):
        acceptance = load_module("continuation_225_acceptance_day230", ROOT / "ai/grounded/learner_acceptance/test_continuation_225.py")
        workflow = acceptance.workflow
        review = reference_review_for_harness(workflow)
        method = acceptance.Continuation225Acceptance("test_day230_human_review_approves_once_and_audits_the_transition")
        with patch.object(workflow, "review_draft", side_effect=review):
            method.test_day230_human_review_approves_once_and_audits_the_transition()
        good = review(acceptance._ledger(), acceptance.SNAPSHOT, acceptance._command())
        event_type = workflow.AuditEvent
        mutants = (
            replace(good, ledger=replace(good.ledger, audit=())),
            replace(good, ledger=replace(good.ledger, audit=(good.event, good.event))),
            replace(good, event=event_type(1, good.event.draft_id, "owner-b", good.event.reviewer_id,
                                          good.event.decision, good.event.from_status, good.event.to_status,
                                          good.event.policy_version, good.event.snapshot_sha256,
                                          good.event.idempotency_key)),
            replace(good, status="replayed"),
        )
        for mutant in mutants:
            with self.subTest(mutant=mutant):
                with patch.object(workflow, "review_draft", return_value=mutant):
                    with self.assertRaises(AssertionError):
                        method.test_day230_human_review_approves_once_and_audits_the_transition()

    def test_day232_actual_replay_acceptance_rejects_auth_bypassing_replays(self):
        acceptance = load_module("continuation_225_acceptance_day232", ROOT / "ai/grounded/learner_acceptance/test_continuation_225.py")
        workflow = acceptance.workflow
        method = acceptance.Continuation225Acceptance("test_day232_exact_replay_returns_same_audit_event_without_duplicate")
        with patch.object(workflow, "review_draft", side_effect=reference_review_for_harness(workflow)):
            method.test_day232_exact_replay_returns_same_audit_event_without_duplicate()
        for mutation in ("foreign_owner", "blank_reviewer"):
            with self.subTest(replay_auth_mutation=mutation):
                mutant_review = reference_review_for_harness(
                    workflow, replay_foreign_owner=mutation == "foreign_owner",
                    replay_blank_reviewer=mutation == "blank_reviewer")
                with patch.object(workflow, "review_draft", side_effect=mutant_review):
                    with self.assertRaises(AssertionError):
                        method.test_day232_exact_replay_returns_same_audit_event_without_duplicate()

    def test_day234_actual_audit_acceptance_rejects_reset_or_duplicate_sequence(self):
        acceptance = load_module("continuation_225_acceptance_day234", ROOT / "ai/grounded/learner_acceptance/test_continuation_225.py")
        workflow = acceptance.workflow
        method = acceptance.Continuation225Acceptance("test_day234_audit_is_ordered_append_only_and_contains_no_source_text")
        with patch.object(workflow, "review_draft", side_effect=reference_review_for_harness(workflow)):
            method.test_day234_audit_is_ordered_append_only_and_contains_no_source_text()
        mutants = (reference_review_for_harness(workflow, preserve_audit=False),
                   reference_review_for_harness(workflow, sequence_always_one=True))
        for mutant_review in mutants:
            with self.subTest(mutant=mutant_review):
                with patch.object(workflow, "review_draft", side_effect=mutant_review):
                    with self.assertRaises(AssertionError):
                        method.test_day234_audit_is_ordered_append_only_and_contains_no_source_text()

    def test_day235_actual_metric_acceptance_detects_counterfactual_and_bad_classifiers(self):
        acceptance = load_module("continuation_225_acceptance_day235", ROOT / "ai/grounded/learner_acceptance/test_continuation_225.py")
        workflow = acceptance.workflow
        method = acceptance.Continuation225Acceptance("test_day235_only_approved_same_owner_draft_can_be_rendered")
        evaluator = reference_evaluator_for_harness(workflow)
        review = reference_review_for_harness(workflow)
        with patch.object(workflow, "review_draft", side_effect=review), \
             patch.object(workflow, "render_explanation", side_effect=reference_renderer_for_harness(workflow)), \
             patch.object(workflow, "classify_candidate", side_effect=reference_classifier_for_harness(workflow)), \
             patch.object(workflow, "evaluate_cases", side_effect=evaluator):
            method.test_day235_only_approved_same_owner_draft_can_be_rendered()
        for mutant in ("always_abstain", "cite_any_candidate"):
            with self.subTest(classifier_mutation=mutant):
                with patch.object(workflow, "review_draft", side_effect=review), \
                     patch.object(workflow, "render_explanation", side_effect=reference_renderer_for_harness(workflow)), \
                     patch.object(workflow, "classify_candidate", side_effect=reference_classifier_for_harness(workflow, mutation=mutant)), \
                     patch.object(workflow, "evaluate_cases", side_effect=evaluator):
                    with self.assertRaises(AssertionError):
                        method.test_day235_only_approved_same_owner_draft_can_be_rendered()

    def test_day237_actual_revision_acceptance_rejects_ignored_expected_revision(self):
        acceptance = load_module("continuation_225_acceptance_day237", ROOT / "ai/grounded/learner_acceptance/test_continuation_225.py")
        workflow = acceptance.workflow
        method = acceptance.Continuation225Acceptance("test_day237_stale_ledger_revision_cannot_overwrite_concurrent_review")
        with patch.object(workflow, "review_draft", side_effect=reference_review_for_harness(workflow)):
            method.test_day237_stale_ledger_revision_cannot_overwrite_concurrent_review()
        mutant_review = reference_review_for_harness(workflow, enforce_revision=False)
        with patch.object(workflow, "review_draft", side_effect=mutant_review):
            with self.assertRaises(AssertionError):
                method.test_day237_stale_ledger_revision_cannot_overwrite_concurrent_review()


if __name__ == "__main__":
    unittest.main()
