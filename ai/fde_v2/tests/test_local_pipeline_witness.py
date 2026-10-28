"""End-to-end orchestration witness with explicit fixed model doubles.

This exercises real SQLite/Qdrant persistence and the API pipeline boundaries;
the fixed embedding/generation adapters are test fixtures, never model evidence.
"""
from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from ai.fde_v2.coverstory import learner, reference
from ai.fde_v2.coverstory.corpus import CorpusStore
from ai.fde_v2.coverstory.pipeline import CoverStoryPipeline
from ai.fde_v2.coverstory.vector_store import PersistentVectorStore
from fastapi.testclient import TestClient
from ai.fde_v2.coverstory.api import create_app


class FixedEmbeddingFixture:
    model_id = "test-double-only"
    model_revision = "fixture"
    dimension = 2
    normalize_embeddings = True
    license = "synthetic-test-fixture"

    def encode(self, texts):
        return [[1.0, 0.0] if "deductible" in text.lower() else [0.0, 1.0] for text in texts]


class FixedGeneratorFixture:
    model_id = "test-double-only"
    model_revision = "fixture"

    def generate_messages(self, messages, max_new_tokens):
        return json.dumps({"answer": "Fixture answer", "citations": ["policy:v1:0"], "abstained": False}), 1.0


class LocalPipelineWitness(unittest.TestCase):
    def test_real_sqlite_qdrant_pipeline_wiring_with_fixed_model_fixtures(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            corpus = CorpusStore(root / "corpus.sqlite3")
            raw = {"document_id": "policy", "version": 1, "title": "Policy", "source": "CC0 synthetic",
                   "text": "TRAINING FIXTURE: individual deductible is 250 units."}
            noise = {"document_id": "appointments", "version": 1, "title": "Appointments",
                     "source": "CC0 synthetic", "text": "TRAINING FIXTURE: book a clinic appointment."}
            reference.ingest_versioned_document(raw, corpus)
            reference.ingest_versioned_document(noise, corpus)
            vectors = PersistentVectorStore(root / "qdrant", vector_size=2)
            pipeline = CoverStoryPipeline(FixedGeneratorFixture(), FixedEmbeddingFixture(), corpus, vectors)
            seams = {
                "ingest_versioned_document": reference.ingest_versioned_document,
                "parse_document_record": reference.parse_document_record,
                "chunk_document": reference.chunk_document,
                "embed_texts": reference.embed_texts,
                "index_vectors": reference.index_vectors,
                "retrieve_vectors": reference.retrieve_vectors,
                "keyword_search": reference.keyword_search,
                "evaluate_retrieval": reference.evaluate_retrieval,
                "compose_chat_messages": reference.compose_chat_messages,
                "validate_citations": reference.validate_citations,
                "answer_with_evidence": reference.answer_with_evidence,
                "should_abstain": reference.should_abstain,
            }
            try:
                with patch.multiple(learner, **seams):
                    # Re-ingest through the supplied pipeline to exercise learner seam wiring.
                    self.assertEqual(pipeline.ingest(raw), "unchanged")
                    hits = pipeline.retrieve("What is the deductible?", limit=2)
                    self.assertEqual(hits[0].chunk.chunk_id, "policy:v1:0")
                    answer, cited_hits, timings = pipeline.answer("What is the deductible?")
                self.assertEqual(answer.text, "Fixture answer")
                self.assertEqual(answer.citations, ("policy:v1:0",))
                self.assertEqual(cited_hits[0].chunk.document_id, "policy")
                self.assertEqual(set(timings), {"retrieval", "generation"})
                self.assertTrue(all(value >= 0 for value in timings.values()))
            finally:
                vectors.close()

    def test_version_replacement_removes_old_vector_evidence(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            corpus = CorpusStore(root / "corpus.sqlite3")
            vectors = PersistentVectorStore(root / "qdrant", vector_size=2)
            pipeline = CoverStoryPipeline(FixedGeneratorFixture(), FixedEmbeddingFixture(), corpus, vectors)
            raw = {"document_id": "policy", "version": 1, "title": "Policy", "source": "CC0 synthetic",
                   "text": "TRAINING FIXTURE: old deductible is 250 units."}
            revised = {**raw, "version": 2, "text": "TRAINING FIXTURE: new deductible is 300 units."}
            seams = {
                "ingest_versioned_document": reference.ingest_versioned_document,
                "parse_document_record": reference.parse_document_record,
                "chunk_document": reference.chunk_document,
                "embed_texts": reference.embed_texts,
                "index_vectors": reference.index_vectors,
                "retrieve_vectors": reference.retrieve_vectors,
            }
            try:
                with patch.multiple(learner, **seams):
                    self.assertEqual(pipeline.ingest(raw), "inserted")
                    self.assertEqual([h.chunk.version for h in pipeline.retrieve("deductible", 10)], [1])
                    self.assertEqual(pipeline.ingest(revised), "updated")
                    hits = pipeline.retrieve("deductible", 10)
                self.assertEqual([(h.chunk.version, h.chunk.text) for h in hits], [(2, revised["text"])])
                self.assertEqual([row.version for row in corpus.chunks()], [])
            finally:
                vectors.close()

    def test_failed_vector_replacement_fails_closed_and_retry_repairs(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            corpus = CorpusStore(root / "corpus.sqlite3")
            vectors = PersistentVectorStore(root / "qdrant", vector_size=2)
            pipeline = CoverStoryPipeline(FixedGeneratorFixture(), FixedEmbeddingFixture(), corpus, vectors)
            raw = {"document_id": "policy", "version": 1, "title": "Policy", "source": "CC0 synthetic",
                   "text": "TRAINING FIXTURE: old deductible is 250 units."}
            revised = {**raw, "version": 2, "text": "TRAINING FIXTURE: new deductible is 300 units."}
            seams = {
                "ingest_versioned_document": reference.ingest_versioned_document,
                "parse_document_record": reference.parse_document_record,
                "chunk_document": reference.chunk_document,
                "embed_texts": reference.embed_texts,
                "index_vectors": reference.index_vectors,
                "retrieve_vectors": reference.retrieve_vectors,
            }
            try:
                with patch.multiple(learner, **seams):
                    pipeline.ingest(raw)
                    with patch.object(learner, "embed_texts", side_effect=RuntimeError("fixture embedding failure")):
                        with self.assertRaisesRegex(RuntimeError, "fixture embedding failure"):
                            pipeline.ingest(revised)
                    # A failed replacement must not leave v1 evidence searchable.
                    self.assertEqual(vectors.search([1.0, 0.0], limit=10), [])
                    # Retrying the same v2 record is idempotent in SQLite and
                    # repairs the vector set after the temporary failure.
                    self.assertEqual(pipeline.ingest(revised), "unchanged")
                    hits = pipeline.retrieve("deductible", 10)
                self.assertEqual([(hit.chunk.version, hit.chunk.text) for hit in hits], [(2, revised["text"])])
            finally:
                vectors.close()

    def test_api_abstains_end_to_end_without_loading_generator(self):
        class EmptyStore:
            def search(self, _vector, limit=3):
                self.limit = limit
                return []

        class EmptyEmbedder:
            def encode(self, _texts):
                return [[1.0, 0.0]]

        class EmptyCorpus:
            def chunks(self):
                return []

        generator_calls = []
        def generator_factory():
            generator_calls.append(True)
            raise AssertionError("generation model must stay unloaded for an empty-evidence abstention")

        pipeline = CoverStoryPipeline(generator_factory, EmptyEmbedder(), EmptyCorpus(), EmptyStore())
        with patch.object(learner, "retrieve_vectors", return_value=[]) as retrieve_vectors, \
                patch.object(learner, "should_abstain", side_effect=reference.should_abstain):
            client = TestClient(create_app(lambda: object(), rag_pipeline_factory=lambda: pipeline))
            response = client.post("/ask", json={"question": "What is the annual limit?", "limit": 2})
        self.assertEqual(retrieve_vectors.call_args.kwargs["limit"], 2)
        self.assertEqual(response.status_code, 200, response.text)
        payload = response.json()
        self.assertTrue(payload["abstained"])
        self.assertEqual(payload["citations"], [])
        self.assertEqual(payload["sources"], [])
        self.assertEqual(payload["answer"], "I don't know based on the available sources.")
        self.assertEqual(payload["model_id"], "not-invoked")
        self.assertEqual(generator_calls, [])


if __name__ == "__main__":
    unittest.main()
