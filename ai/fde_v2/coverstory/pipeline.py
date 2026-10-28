"""Assistant-supplied CoverStory orchestration around learner AI seams."""
from __future__ import annotations

from collections.abc import Mapping
import time
from typing import Any

from . import learner
from .contracts import Answer, RetrievedChunk


class CoverStoryPipeline:
    def __init__(self, generator, embedder, corpus, vector_store, minimum_score: float = 0.2):
        # ``generator`` may be a lazy factory. Retrieval can then start without
        # loading generation weights, while answer generation stays on demand.
        self.generator = generator
        self.embedder = embedder
        self.corpus = corpus
        self.vector_store = vector_store
        self.minimum_score = minimum_score

    def ingest(self, record: Mapping[str, Any]) -> str:
        """Persist a complete replacement vector set for the document ID."""
        result = learner.ingest_versioned_document(record, self.corpus)
        document = learner.parse_document_record(record)
        chunks = learner.chunk_document(document)
        # Replace a document's complete vector set. SQL ingestion removes old
        # chunks before this point; deleting all prior Qdrant points prevents
        # old-version evidence from surviving an update or partial-row repair.
        self.vector_store.delete_document(document.document_id)
        vectors = learner.embed_texts([chunk.text for chunk in chunks], self.embedder)
        learner.index_vectors(chunks, vectors, self.vector_store)
        return result

    def retrieve(self, question: str, limit: int = 3) -> list[RetrievedChunk]:
        return learner.retrieve_vectors(question, self.embedder, self.vector_store, limit=limit)

    def retrieve_keyword(self, question: str, limit: int = 3) -> list[RetrievedChunk]:
        return learner.keyword_search(question, self.corpus.chunks(), limit=limit)

    def evaluate(self, cases, vector: bool, k: int = 3):
        search = self.retrieve if vector else self.retrieve_keyword
        return learner.evaluate_retrieval(cases, lambda question: search(question, k), k=k)

    def answer(self, question: str, limit: int = 3) -> tuple[Answer, list[RetrievedChunk], dict[str, float]]:
        retrieval_started = time.perf_counter()
        retrieved = self.retrieve(question, limit=limit)
        retrieval_ms = (time.perf_counter() - retrieval_started) * 1000
        if learner.should_abstain(retrieved, self.minimum_score):
            answer = Answer("I don't know based on the available sources.", (),
                            tuple(hit.chunk.chunk_id for hit in retrieved), True,
                            "not-invoked", "not-invoked")
            return answer, retrieved, {"retrieval": retrieval_ms, "generation": 0.0}
        generation_started = time.perf_counter()
        generator = (self.generator if hasattr(self.generator, "generate_messages")
                     else self.generator())
        answer = learner.answer_with_evidence(question, retrieved, generator, self.minimum_score)
        generation_ms = (time.perf_counter() - generation_started) * 1000
        return answer, retrieved, {"retrieval": retrieval_ms, "generation": generation_ms}
