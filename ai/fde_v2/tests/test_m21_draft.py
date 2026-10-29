"""Instructor mechanics witnesses for the conditional M2.1 draft.

Fixtures are synthetic and use instructor-supplied vectors. They test local
storage mechanics only; they are not embedding, generation, or label evidence.
"""
from __future__ import annotations

import hashlib
import json
import math
import tempfile
import unittest
from pathlib import Path

from ai.fde_v2.coverstory.contracts import Chunk, Document, RetrievedChunk
from ai.fde_v2.coverstory.vector_store import PersistentVectorStore
from ai.fde_v2.m21 import learner, reference
from ai.fde_v2.learner_acceptance import m21_acceptance


def make_document(version: int, text: str, source: str = "fixture://policy") -> Document:
    return Document("policy-1", version, "Synthetic Policy", source, text,
                    hashlib.sha256(text.encode()).hexdigest())


def make_chunks(version: int, parts: list[tuple[str, str]]) -> list[Chunk]:
    result = []
    cursor = 0
    for suffix, text in parts:
        result.append(Chunk(f"policy-1-v{version}-{suffix}", "policy-1", version,
                            "Synthetic Policy", "fixture://policy", cursor,
                            cursor + len(text), text))
        cursor += len(text)
    return result


def cases_and_manifest():
    rows = []
    development, heldout = [], []
    for family_number in range(20):
        family = f"family-{family_number:02d}"
        split = "development" if family_number < 12 else "heldout"
        (development if split == "development" else heldout).append(family)
        for index in range(5):
            answerable = index < 3 if split == "heldout" else index < 3
            text = f"Synthetic policy fact {family_number}-{index}."
            rows.append({
                "case_id": f"case-{family_number:02d}-{index}",
                "scenario_family": family,
                "source_document_id": f"doc-{family_number:02d}",
                "source_version": 1,
                "source_sha256": hashlib.sha256(text.encode()).hexdigest(),
                "question": f"What is fact {family_number}-{index}?",
                "answerability": "answerable" if answerable else "unanswerable",
                "expected_atomic_facts": [text] if answerable else [],
                "abstention_reason": None if answerable else "No supporting fact in the source.",
                "evidence_spans": [text] if answerable else [],
                "tags": ["synthetic", "adversarial" if index == 4 else "ordinary"],
                "origin": "synthetic", "author": "fixture-author", "reviewer": "fixture-reviewer",
                "annotation_status": "source_verified", "split": split,
            })
    # The test fixture's keys/counts exercise schema mechanics only. The
    # "source_verified" field here is fictional mechanics data, not an assertion
    # that any M2.1 learner dataset has been reviewed.
    return rows, {"development": development, "heldout": heldout}


class M21DraftMechanics(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory(prefix="fde-v2-m21-")
        self.root = Path(self.tmp.name)

    def tearDown(self):
        self.tmp.cleanup()

    def store_and_ledger(self, name="run"):
        from ai.fde_v2.m21.ledger import VersionLedger
        vector = PersistentVectorStore(self.root / f"{name}-qdrant", vector_size=2)
        ledger = VersionLedger(self.root / f"{name}.sqlite")
        self.addCleanup(vector.close)
        return ledger, vector

    def activate(self, ledger, vector, version, parts):
        doc = make_document(version, "".join(text for _, text in parts))
        chunks = make_chunks(version, parts)
        ledger.stage(doc, chunks)
        vector.upsert(chunks, [[1.0, 0.0] for _ in chunks])
        ledger.activate(doc.document_id, version, vector)
        return doc, chunks

    def test_schedule_math_and_budget_are_exact(self):
        path = Path(__file__).parents[1] / "m21" / "draft_schedule.json"
        schedule = json.loads(path.read_text())
        days = schedule["days"]
        self.assertEqual([row["day"] for row in days], list(range(42, 63)))
        self.assertEqual(sum(row["m21_substantive"] for row in days), 1500)
        self.assertEqual(sum(row["m21_buffer"] for row in days), 240)
        self.assertEqual(sum(row["visibility"] for row in days), 90)
        self.assertEqual(sum(row["unallocated"] for row in days), 60)
        self.assertTrue(all(row["dsa"] == 60 and row["role_aws"] == 30 and row["ai"] == 90 for row in days))
        self.assertTrue(all(row["m21_substantive"] + row["m21_buffer"] + row["visibility"] + row["unallocated"] == row["ai"] for row in days))

    def test_reference_dataset_shape_and_mutations(self):
        rows, manifest = cases_and_manifest()
        result = reference.audit_dataset(rows, manifest)
        self.assertEqual(result["case_count"], 100)
        self.assertEqual(result["heldout_answerable"], 24)
        self.assertGreaterEqual(result["adversarial_tag_count"], 10)
        broken = [dict(row) for row in rows]
        broken[0]["scenario_family"] = broken[60]["scenario_family"]
        with self.assertRaises(ValueError):
            reference.audit_dataset(broken, manifest)
        missing = [dict(row) for row in rows]
        missing[0]["source_sha256"] = "bad"
        with self.assertRaises(ValueError):
            reference.audit_dataset(missing, manifest)

    def test_version_update_keeps_old_head_until_new_vectors_complete(self):
        ledger, vector = self.store_and_ledger()
        _, old_chunks = self.activate(ledger, vector, 1, [("a", "old A"), ("b", "old B")])
        new_doc = make_document(2, "new C")
        new_chunks = make_chunks(2, [("c", "new C")])
        ledger.stage(new_doc, new_chunks)
        self.assertEqual(ledger.active_version("policy-1"), 1)
        with self.assertRaises(ValueError):
            ledger.activate("policy-1", 2, vector)
        self.assertEqual(ledger.active_version("policy-1"), 1)
        vector.upsert(new_chunks, [[0.0, 1.0]])
        self.assertEqual(ledger.activate("policy-1", 2, vector), 1)
        self.assertEqual(ledger.active_version("policy-1"), 2)
        self.assertEqual({x["chunk_id"] for x in vector.list_document_versions()},
                         {c.chunk_id for c in old_chunks + new_chunks})
        self.assertEqual(ledger.staged_and_retired_chunk_ids(), set(c.chunk_id for c in old_chunks))

    def test_staging_failure_and_restart_leave_old_version_authoritative(self):
        ledger, vector = self.store_and_ledger("restart")
        self.activate(ledger, vector, 1, [("a", "old A")])
        new_doc = make_document(2, "new B")
        new_chunks = make_chunks(2, [("b", "new B")])
        ledger.stage(new_doc, new_chunks)
        # Simulate failure before the vector upsert. A new ledger instance is
        # the restart boundary; it must observe the persisted old head.
        with self.assertRaises(ValueError):
            ledger.activate("policy-1", 2, vector)
        from ai.fde_v2.m21.ledger import VersionLedger
        reopened = VersionLedger(self.root / "restart.sqlite")
        self.assertEqual(reopened.active_version("policy-1"), 1)

    def test_version_change_removes_old_points_and_reconciliation_detects_failure(self):
        ledger, vector = self.store_and_ledger("cleanup")
        _, old_chunks = self.activate(ledger, vector, 1, [("a", "old A"), ("b", "old B")])
        new_doc = make_document(2, "new C")
        new_chunks = make_chunks(2, [("c", "new C")])
        ledger.stage(new_doc, new_chunks)
        vector.upsert(new_chunks, [[0.0, 1.0]])
        ledger.activate("policy-1", 2, vector)
        vector.delete_document_version("policy-1", 1)
        self.assertEqual({x["chunk_id"] for x in vector.list_document_versions()}, {new_chunks[0].chunk_id})
        self.assertEqual(reference.reconcile_versions(ledger, vector)["stale_vector_chunk_ids"], [])
        self.assertEqual(old_chunks[0].version, 1)

    def test_cleanup_failure_keeps_stale_hits_ineligible_and_is_reconcilable(self):
        ledger, vector = self.store_and_ledger("cleanup-failure")
        _, old_chunks = self.activate(ledger, vector, 1, [("a", "old A")])
        new_doc = make_document(2, "new B")
        new_chunks = make_chunks(2, [("b", "new B")])
        ledger.stage(new_doc, new_chunks)
        vector.upsert(new_chunks, [[0.0, 1.0]])
        ledger.activate("policy-1", 2, vector)
        stale_hit = RetrievedChunk(old_chunks[0], 0.99)
        original_delete = vector.delete_document_version
        vector.delete_document_version = lambda *_args: (_ for _ in ()).throw(RuntimeError("fixture failure"))
        try:
            self.assertEqual(reference.filter_current_candidates([stale_hit], ledger), [])
            report = reference.reconcile_versions(ledger, vector)
            self.assertEqual(report["stale_vector_chunk_ids"], [old_chunks[0].chunk_id])
        finally:
            vector.delete_document_version = original_delete
        vector.delete_document_version("policy-1", 1)
        self.assertEqual(reference.reconcile_versions(ledger, vector)["stale_vector_chunk_ids"], [])

    def test_tombstone_makes_hits_ineligible_before_physical_cleanup(self):
        ledger, vector = self.store_and_ledger("delete")
        _, chunks = self.activate(ledger, vector, 1, [("a", "current A")])
        hits = [RetrievedChunk(chunks[0], 0.9)]
        self.assertEqual(reference.filter_current_candidates(hits, ledger), hits)
        self.assertEqual(ledger.delete("policy-1"), 1)
        self.assertEqual(reference.filter_current_candidates(hits, ledger), [])
        self.assertEqual(len(vector.list_document_versions()), 1)
        vector.delete_document("policy-1")
        self.assertEqual(vector.list_document_versions(), [])

    def test_metrics_are_deduplicated_and_failures_penalized(self):
        result = reference.score_retrieval(["A", "C"], ["B", "A", "A", "C"], 3)
        self.assertEqual(result["recall_at_k"], 1.0)
        self.assertEqual(result["reciprocal_rank"], 0.5)
        self.assertFalse(reference.score_retrieval([], ["A"], 3)["included_in_answerable_denominator"])
        lat = reference.summarize_latency([{"ok": True, "elapsed_seconds": 3}, {"ok": False},
                                           {"ok": True, "elapsed_seconds": 4}], 60)
        self.assertEqual(lat["p95_nearest_rank_seconds"], 60.0)
        self.assertEqual(lat["failure_count"], 1)
        with self.assertRaises(ValueError):
            reference.summarize_latency([{"ok": True, "elapsed_seconds": math.nan}], 60)

    def test_proposed_threshold_math_is_exact_but_not_an_evidence_pass(self):
        cases = [f"c{i:02d}" for i in range(40)]
        answerable, unsupported = cases[:24], cases[24:]
        criterion = {"expected_case_ids": cases, "answerable_case_ids": answerable,
                     "unanswerable_case_ids": unsupported, "min_answerable_successes": 22,
                     "min_unanswerable_successes": 15, "max_severe_errors": 0,
                     "min_net_gain": 4, "max_p95_seconds": 30, "timeout_seconds": 60}
        baseline = [{"case_id": case, "success": i < 18, "severe_error": False}
                    for i, case in enumerate(cases)]
        candidate = [{"case_id": case, "success": i < 22 if i < 24 else i < 39,
                      "severe_error": False} for i, case in enumerate(cases)]
        metrics = reference._heldout_metrics(baseline, candidate, criterion,
                                             {"p95_nearest_rank_seconds": 29.0})
        self.assertTrue(metrics["criterion_satisfied"])
        candidate[21]["success"] = False
        metrics = reference._heldout_metrics(baseline, candidate, criterion,
                                             {"p95_nearest_rank_seconds": 29.0})
        self.assertFalse(metrics["criterion_satisfied"])
        # Even mathematically meeting thresholds is only a test fixture. The
        # real evidence gate refuses it while P1 is blocked.
        self.assertEqual(reference.evaluate_release_gate([], [], [], criterion, "P1_BLOCKED")["status"],
                         "BLOCKED")

    def test_complete_gate_mechanics_can_only_become_review_eligible_never_m21_pass(self):
        # Every row below is synthetic test data. The evidence_kind labels are
        # schema fixtures only; this test does not assert that P1 or any real
        # local model was run. It validates the final decision control flow.
        cases = [f"case-{i:02d}" for i in range(40)]
        answerable, unanswerable = cases[:24], cases[24:]
        criterion = {"expected_case_ids": cases, "answerable_case_ids": answerable,
                     "unanswerable_case_ids": unanswerable, "min_answerable_successes": 22,
                     "min_unanswerable_successes": 15, "max_severe_errors": 0,
                     "min_net_gain": 4, "max_p95_seconds": 30, "timeout_seconds": 60,
                     "frozen_before_heldout": True, "heldout_key_separated": True}
        baseline_judgments = []
        candidate_judgments = []
        baseline_runs = []
        candidate_runs = []
        for i, case_id in enumerate(cases):
            baseline_success = i < 18
            candidate_success = i < 22 or 24 <= i < 39
            shared_judgment = {"case_id": case_id, "reviewer": "fixture reviewer",
                               "source_reviewed": True, "severe_error": False}
            baseline_judgments.append(dict(shared_judgment, success=baseline_success))
            candidate_judgments.append(dict(shared_judgment, success=candidate_success))
            baseline_runs.append({"case_id": case_id, "success": baseline_success, "ok": True,
                                 "evidence_kind": "actual_p1_keyword_baseline"})
            candidate_runs.append({"case_id": case_id, "success": candidate_success, "ok": True,
                                   "elapsed_seconds": 2.0,
                                   "evidence_kind": "actual_pinned_local_rag_run"})
        result = reference.evaluate_release_gate(
            baseline_runs, candidate_runs,
            {"baseline": baseline_judgments, "candidate": candidate_judgments}, criterion,
            "P1_ACTUAL_EVIDENCE_INDEPENDENTLY_CLEARED")
        self.assertEqual(result["status"], "ELIGIBLE_FOR_INDEPENDENT_REVIEW")
        self.assertFalse(result["m21_pass"])
        # A failed request cannot be made a successful outcome by its judgment.
        candidate_runs[0]["ok"] = False
        self.assertEqual(reference.evaluate_release_gate(
            baseline_runs, candidate_runs,
            {"baseline": baseline_judgments, "candidate": candidate_judgments}, criterion,
            "P1_ACTUAL_EVIDENCE_INDEPENDENTLY_CLEARED")["status"], "INVALID")

    def test_comparison_requires_exact_case_set_and_gate_fails_closed(self):
        baseline = [{"case_id": "a", "success": False}, {"case_id": "b", "success": True}]
        candidate = [{"case_id": "a", "success": True}, {"case_id": "b", "success": True}]
        self.assertEqual(reference.compare_runs(baseline, candidate)["net_change"], 1)
        with self.assertRaises(ValueError):
            reference.compare_runs(baseline, candidate[:1])
        blocked = reference.evaluate_release_gate(baseline, candidate, [], {}, "P1_BLOCKED")
        self.assertEqual(blocked["status"], "BLOCKED")
        self.assertFalse(blocked["m21_pass"])

    def test_learner_seams_are_still_todos(self):
        with self.assertRaises(NotImplementedError):
            learner.score_retrieval(["A"], ["A"], 1)
        with self.assertRaises(NotImplementedError):
            learner.stage_version_update(None, {}, [], [], None)

    def test_reference_passes_declared_learner_acceptance_selectors(self):
        result = m21_acceptance.run_all(reference)
        self.assertEqual(result.errors, [])
        self.assertEqual(result.failures, [])
        self.assertEqual(result.testsRun, 9)

    def test_acceptance_selectors_reject_material_mutants(self):
        original = reference.score_retrieval
        reference.score_retrieval = lambda _relevant, _ranked, _k: {
            "recall_at_k": 1.0, "reciprocal_rank": 1.0}
        try:
            result = m21_acceptance.run_all(reference)
            self.assertTrue(result.failures or result.errors)
        finally:
            reference.score_retrieval = original
        original = reference.assign_group_splits
        reference.assign_group_splits = lambda rows, _manifest: {
            row["case_id"]: "development" for row in rows}
        try:
            result = m21_acceptance.run_all(reference)
            self.assertTrue(result.failures or result.errors)
        finally:
            reference.assign_group_splits = original

    def test_gate_cannot_turn_missing_prerequisite_into_pass(self):
        result = reference.evaluate_release_gate([], [], [], {"frozen_before_heldout": True}, "P1_NOT_CLEARED")
        self.assertNotEqual(result["status"], "PASS")
        self.assertFalse(result["m21_pass"])


if __name__ == "__main__":
    unittest.main()
