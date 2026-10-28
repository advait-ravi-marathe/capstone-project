"""Independent witness, mutation and sequential-unit tests for Days 15–28."""
from __future__ import annotations

import importlib
import types
import unittest
from unittest.mock import patch

from ai.fde_v2.coverstory import learner, reference
from ai.fde_v2.learner_acceptance import test_continuation_units as acceptance


SEAM_DAY = {
    "parse_document_record": 3, "generate_text": 10, "parse_answer_json": 13,
    "upsert_document_metadata": 14, "ingest_versioned_document": 15,
    "chunk_document": 16, "embed_texts": 17, "keyword_search": 20,
    "evaluate_retrieval": 20, "index_vectors": 21, "retrieve_vectors": 22,
    "compose_chat_messages": 24, "validate_citations": 24,
    "answer_with_evidence": 25, "should_abstain": 26,
    "classify_failure": 28, "build_eval_record": 28,
    "validate_fixture_path": 30,
}
SELECTOR_DAY = {
    "test_u011_versioned_ingestion": 15,
    "test_u012_chunk_provenance": 16,
    "test_u013_embedding_contract": 17,
    "test_u014_eval_fixture_counts_and_isolation": 18,
    "test_u015_bm25_and_metrics": 20,
    "test_u016_qdrant_persistence_and_indexing": 21,
    "test_u017_retrieve_and_reuse_metric_contract": 23,
    "test_u018_prompt_boundary_and_citation_membership": 24,
    "test_u019_grounded_generation_contract": 25,
    "test_u020_abstention_boundaries": 27,
    "test_u021_failure_taxonomy_and_redaction": 28,
}


class ContinuationMutationControls(unittest.TestCase):
    def setUp(self):
        self.previous = acceptance.candidate
        acceptance.candidate = reference

    def tearDown(self):
        acceptance.candidate = self.previous

    def run_selector(self, selector):
        case = acceptance.ContinuationAcceptance(selector)
        result = unittest.TestResult()
        case.run(result)
        self.assertEqual(result.errors, [], result.errors)
        self.assertEqual(result.failures, [], result.failures)

    def reject_mutant(self, selector, seam, mutant):
        original = getattr(acceptance.candidate, seam)
        setattr(acceptance.candidate, seam, mutant)
        try:
            case = acceptance.ContinuationAcceptance(selector)
            result = unittest.TestResult()
            case.run(result)
            self.assertTrue(result.failures or result.errors,
                            f"acceptance selector {selector} accepted faulty {seam} mutant")
        finally:
            setattr(acceptance.candidate, seam, original)

    def test_reference_passes_every_actual_continuation_acceptance_path(self):
        for selector in SELECTOR_DAY:
            with self.subTest(selector=selector):
                self.run_selector(selector)

    def test_selectors_do_not_require_future_learner_seams(self):
        for selector, day in SELECTOR_DAY.items():
            staged = types.SimpleNamespace()
            for name, due_day in SEAM_DAY.items():
                setattr(staged, name, getattr(reference if due_day <= day else learner, name))
            acceptance.candidate = staged
            with self.subTest(day=day, selector=selector):
                self.run_selector(selector)
            for name, due_day in SEAM_DAY.items():
                if due_day > day:
                    self.assertIs(getattr(staged, name), getattr(learner, name),
                                  f"future Day {due_day} seam {name} was replaced")

    def test_actual_acceptance_paths_reject_behavior_mutants(self):
        self.reject_mutant("test_u011_versioned_ingestion", "ingest_versioned_document",
                           lambda _raw, _store: "inserted")
        self.reject_mutant("test_u012_chunk_provenance", "chunk_document",
                           lambda _doc, **_kw: [])
        self.reject_mutant("test_u013_embedding_contract", "embed_texts",
                           lambda texts, _embedder: [[1.0, 0.0] for _ in texts])
        self.reject_mutant("test_u015_bm25_and_metrics", "keyword_search",
                           lambda *_args, **_kwargs: [])
        self.reject_mutant("test_u016_qdrant_persistence_and_indexing", "index_vectors",
                           lambda *_args, **_kwargs: None)
        self.reject_mutant("test_u017_retrieve_and_reuse_metric_contract", "retrieve_vectors",
                           lambda *_args, **_kwargs: [])
        self.reject_mutant("test_u018_prompt_boundary_and_citation_membership", "compose_chat_messages",
                           lambda question, _evidence: [{"role": "user", "content": question}])
        self.reject_mutant("test_u019_grounded_generation_contract", "answer_with_evidence",
                           lambda *_args, **_kwargs: reference.Answer("constant", (), (), False, "m", "r"))
        self.reject_mutant("test_u020_abstention_boundaries", "should_abstain",
                           lambda *_args, **_kwargs: False)
        self.reject_mutant("test_u021_failure_taxonomy_and_redaction", "classify_failure",
                           lambda *_args, **_kwargs: "unknown")


if __name__ == "__main__":
    unittest.main()
