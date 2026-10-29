"""Versioned ingestion/search path with assistant-supplied lifecycle code by default.

The existing P1 lexical/RAG app does not silently adopt this sidecar. Callers
must explicitly construct this workflow; fixed encoders in tests are fixtures.
The learner adapters remain available for scoped AI/data evaluation practice.
"""
from __future__ import annotations

from collections.abc import Sequence
from typing import Any

from . import reference as application_adapter


class VersionedWorkflow:
    def __init__(self, ledger, vector_store, embedder, *, adapter=application_adapter):
        self.ledger = ledger
        self.vector_store = vector_store
        self.embedder = embedder
        self.adapter = adapter

    def publish(self, document, chunks) -> dict[str, Any]:
        vectors = self.embedder.encode([chunk.text for chunk in chunks])
        return self.adapter.stage_version_update(self.ledger, document, chunks,
                                                 vectors, self.vector_store)

    def delete(self, document_id: str) -> dict[str, Any]:
        return self.adapter.delete_document(document_id, self.ledger, self.vector_store)

    def search(self, query: str, limit: int = 3):
        vectors = self.embedder.encode([query])
        if not isinstance(vectors, Sequence) or len(vectors) != 1:
            raise ValueError("query embedding must produce exactly one vector")
        hits = self.vector_store.search(vectors[0], limit=limit)
        return self.adapter.filter_current_candidates(hits, self.ledger)
