"""Selectors for proposed M2.1 learner seams (not an active curriculum).

Default candidate is the intentionally unsolved learner module. Instructor
witnesses may explicitly replace it with `reference`; the ordinary selectors
never silently import that reference.
"""
from __future__ import annotations

import hashlib
import tempfile
import unittest
from pathlib import Path

from ai.fde_v2.coverstory.contracts import Chunk, Document
from ai.fde_v2.m21 import learner

candidate = learner


def _record():
    text = "Synthetic clause 7."
    return {"case_id": "case-1", "scenario_family": "family-1", "source_document_id": "doc-1",
            "source_version": 1, "source_sha256": hashlib.sha256(text.encode()).hexdigest(),
            "question": "What does clause 7 say?", "answerability": "answerable",
            "expected_atomic_facts": [text], "abstention_reason": None, "evidence_spans": [text],
            "tags": ["synthetic"], "origin": "synthetic", "author": "author", "reviewer": "reviewer",
            "annotation_status": "source_verified", "split": "development"}


def _chunks(version, suffix_text):
    cursor = 0
    result = []
    for suffix, text in suffix_text:
        result.append(Chunk(f"d1-v{version}-{suffix}", "doc-1", version, "Synthetic", "fixture://doc-1",
                            cursor, cursor + len(text), text))
        cursor += len(text)
    return result


class M21LearnerAcceptance(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory(prefix="fde-m21-acceptance-")
        self.root = Path(self.tmp.name)

    def tearDown(self):
        self.tmp.cleanup()

    def make_storage(self, suffix="run"):
        from ai.fde_v2.m21.ledger import VersionLedger
        from ai.fde_v2.coverstory.vector_store import PersistentVectorStore
        ledger = VersionLedger(self.root / f"{suffix}.sqlite")
        vectors = PersistentVectorStore(self.root / f"{suffix}-qdrant", vector_size=2)
        self.addCleanup(vectors.close)
        return ledger, vectors

    def make_doc_and_chunks(self, version, parts):
        text = "".join(value for _, value in parts)
        doc = Document("doc-1", version, "Synthetic", "fixture://doc-1", text,
                       hashlib.sha256(text.encode()).hexdigest())
        position, chunks = 0, []
        for suffix, value in parts:
            chunks.append(Chunk(f"d1-v{version}-{suffix}", "doc-1", version, "Synthetic",
                                "fixture://doc-1", position, position + len(value), value))
            position += len(value)
        return doc, chunks

    def activate(self, ledger, vectors, version, parts):
        doc, chunks = self.make_doc_and_chunks(version, parts)
        ledger.stage(doc, chunks)
        vectors.upsert(chunks, [[1.0, 0.0] for _ in chunks])
        ledger.activate("doc-1", version, vectors)
        return doc, chunks

    def test_u_m21_case_provenance(self):
        row = _record()
        self.assertEqual(candidate.validate_case(row), row)
        for key, value in (("source_sha256", "bad"), ("source_version", 0), ("reviewer", " ")):
            bad = dict(row, **{key: value})
            with self.subTest(key=key), self.assertRaises(ValueError):
                candidate.validate_case(bad)

    def test_u_m21_group_split_and_dataset_shape(self):
        from ai.fde_v2.tests.test_m21_draft import cases_and_manifest
        rows, manifest = cases_and_manifest()
        assignment = candidate.assign_group_splits(rows, manifest)
        self.assertEqual(len(assignment), 100)
        audit = candidate.audit_dataset(rows, manifest)
        self.assertEqual((audit["development_count"], audit["heldout_count"]), (60, 40))
        bad = dict(manifest, heldout=manifest["heldout"] + [manifest["development"][0]])
        with self.assertRaises(ValueError):
            candidate.assign_group_splits(rows, bad)

    def test_u_m21_version_diff(self):
        old = _chunks(1, [("a", "old A"), ("b", "old B")])
        new = _chunks(2, [("b", "changed B"), ("c", "new C")])
        plan = candidate.plan_version_update(old, new)
        self.assertEqual(plan["add_chunk_ids"], ["d1-v2-b", "d1-v2-c"])
        self.assertEqual(plan["remove_chunk_ids"], ["d1-v1-a", "d1-v1-b"])

    def test_u_m21_metric_and_latency_contract(self):
        scored = candidate.score_retrieval(["a", "b"], ["x", "a", "a", "b"], 3)
        self.assertEqual(scored["recall_at_k"], 1.0)
        self.assertEqual(scored["reciprocal_rank"], 0.5)
        samples = [{"ok": True, "elapsed_seconds": 3}, {"ok": False}, {"ok": True, "elapsed_seconds": 4}]
        latency = candidate.summarize_latency(samples, 60)
        self.assertEqual(latency["p95_nearest_rank_seconds"], 60)
        with self.assertRaises(ValueError):
            candidate.score_retrieval(["a"], ["a"], 0)

    def test_u_m21_exact_comparison_and_frozen_gate(self):
        base = [{"case_id": "x", "success": False}, {"case_id": "y", "success": True}]
        changed = [{"case_id": "x", "success": True}, {"case_id": "y", "success": True}]
        self.assertEqual(candidate.compare_runs(base, changed)["net_change"], 1)
        with self.assertRaises(ValueError):
            candidate.compare_runs(base, changed[:1])
        result = candidate.evaluate_release_gate(base, changed, [], {}, "P1_NOT_CLEARED")
        self.assertEqual(result["status"], "BLOCKED")
        self.assertFalse(result["m21_pass"])

    def test_u_m21_stage_switch_and_cleanup(self):
        ledger, vectors = self.make_storage("stage")
        self.activate(ledger, vectors, 1, [("a", "old clause")])
        doc, chunks = self.make_doc_and_chunks(2, [("b", "new clause")])
        result = candidate.stage_version_update(ledger, doc, chunks, [[0.0, 1.0]], vectors)
        self.assertEqual(result["active_version"], 2)
        self.assertEqual(ledger.active_version("doc-1"), 2)
        self.assertEqual({r["chunk_id"] for r in vectors.list_document_versions()}, {"d1-v2-b"})

    def test_u_m21_delete_tombstones_before_cleanup(self):
        from ai.fde_v2.m21 import reference
        ledger, vectors = self.make_storage("delete")
        _, chunks = self.activate(ledger, vectors, 1, [("a", "old clause")])
        result = candidate.delete_document("doc-1", ledger, vectors)
        self.assertEqual(result["status"], "deleted")
        self.assertEqual(ledger.active_version("doc-1"), None)
        self.assertEqual(vectors.list_document_versions(), [])
        self.assertEqual(reference.filter_current_candidates([], ledger), [])

    def test_u_m21_reconciliation_and_workflow_scoring(self):
        ledger, vectors = self.make_storage("reconcile")
        _, self.chunks = self.activate(ledger, vectors, 1, [("a", "supported clause")])
        report = candidate.reconcile_versions(ledger, vectors)
        self.assertEqual(report["missing_active_chunk_ids"], [])
        self.assertEqual(report["stale_vector_chunk_ids"], [])
        case = {"case_id": "c1", "answerability": "answerable"}
        result = {"case_id": "c1", "status": "completed", "citation_chunk_ids": ["d1-v1-a"]}
        judgment = {"case_id": "c1", "reviewer": "reviewer", "source_reviewed": True,
                    "all_required_facts_supported": True, "severe_error": False}
        self.assertTrue(candidate.score_workflow(case, result, judgment)["success"])
        with self.assertRaises(ValueError):
            candidate.score_workflow(case, result, dict(judgment, case_id="wrong"))

    def test_u_m21_freeze_is_immutable_and_blocked_gate_stays_blocked(self):
        bundle = {"baseline_run_sha256": "a" * 64, "candidate_config_sha256": "b" * 64,
                  "corpus_manifest_sha256": "c" * 64, "case_manifest_sha256": "d" * 64,
                  "rubric_sha256": "e" * 64, "criterion": {"fixed": True}}
        output = self.root / "freeze.json"
        digest = candidate.freeze_release(bundle, output)
        self.assertEqual(len(digest), 64)
        with self.assertRaises(FileExistsError):
            candidate.freeze_release(bundle, output)
        result = candidate.evaluate_release_gate([], [], [], {}, "P1_NOT_CLEARED")
        self.assertEqual(result["status"], "BLOCKED")
        self.assertFalse(result["m21_pass"])


def run_all(candidate_module):
    previous = globals()["candidate"]
    globals()["candidate"] = candidate_module
    try:
        suite = unittest.defaultTestLoader.loadTestsFromTestCase(M21LearnerAcceptance)
        result = unittest.TestResult()
        suite.run(result)
        return result
    finally:
        globals()["candidate"] = previous
