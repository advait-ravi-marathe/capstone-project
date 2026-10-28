"""Assistant-supplied CoverStory orchestration around learner AI seams."""
from __future__ import annotations

from collections.abc import Mapping
import time
from typing import Any

from . import learner
from .contracts import Answer, RetrievedChunk


class CoverStoryPipeline:
    def __init__(self, generator, embedder, corpus, vector_store):
        self.generator = generator
        self.embedder = embedder
        self.corpus = corpus
        self.vector_store = vector_store

    def ingest(self, record: Mapping[str, Any]) -> str:
        result = learner.ingest_versioned_document(record, self.corpus)
        document = learner.parse_document_record(record)
        chunks = learner.chunk_document(document)
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

    def answer(self, question: str) -> tuple[Answer, list[RetrievedChunk], dict[str, float]]:
        retrieval_started = time.perf_counter()
        retrieved = self.retrieve(question)
        retrieval_ms = (time.perf_counter() - retrieval_started) * 1000
        generation_started = time.perf_counter()
        answer = learner.answer_with_evidence(question, retrieved, self.generator)
        generation_ms = (time.perf_counter() - generation_started) * 1000
        return answer, retrieved, {"retrieval": retrieval_ms, "generation": generation_ms}
