"""Assistant-supplied persistent Qdrant local-mode adapter.

This single-process local learning store is not a production cluster. Tests use
real Qdrant local persistence with instructor-owned vectors; that says nothing
about the separate real embedding/model gates.
"""
from __future__ import annotations

import uuid
from pathlib import Path
from typing import Sequence

from .contracts import Chunk, RetrievedChunk


COLLECTION_NAME = "coverstory_synthetic_chunks_v1"


class PersistentVectorStore:
    def __init__(self, path: Path, vector_size: int):
        if type(vector_size) is not int or vector_size < 1:
            raise ValueError("vector_size must be a positive integer")
        try:
            from qdrant_client import QdrantClient, models
        except ImportError as exc:
            raise RuntimeError("install requirements-fde-v2-sprint2.txt for Qdrant local mode") from exc
        if path.exists() and path.is_symlink():
            raise ValueError("vector-store directory cannot be a symlink")
        self.path = path.resolve()
        self.path.mkdir(parents=True, exist_ok=True)
        self.vector_size = vector_size
        self.client = QdrantClient(path=str(self.path))
        if not self.client.collection_exists(COLLECTION_NAME):
            self.client.create_collection(
                collection_name=COLLECTION_NAME,
                vectors_config=models.VectorParams(size=vector_size, distance=models.Distance.COSINE),
            )
        else:
            info = self.client.get_collection(COLLECTION_NAME)
            current = info.config.params.vectors.size
            if current != vector_size:
                self.client.close()
                raise ValueError(f"stored collection dimension {current} does not match {vector_size}")

    @staticmethod
    def _point_id(chunk_id: str) -> str:
        return str(uuid.uuid5(uuid.NAMESPACE_URL, "fde-v2-coverstory:" + chunk_id))

    def upsert(self, chunks: Sequence[Chunk], vectors: Sequence[Sequence[float]]) -> None:
        from qdrant_client import models

        if len(chunks) != len(vectors):
            raise ValueError("each chunk must have exactly one vector")
        points = []
        for chunk, vector in zip(chunks, vectors, strict=True):
            if len(vector) != self.vector_size:
                raise ValueError("vector dimension differs from collection")
            points.append(models.PointStruct(
                id=self._point_id(chunk.chunk_id), vector=[float(value) for value in vector],
                payload={"chunk_id": chunk.chunk_id, "document_id": chunk.document_id,
                         "version": chunk.version, "title": chunk.title, "source": chunk.source,
                         "start_char": chunk.start_char, "end_char": chunk.end_char, "text": chunk.text},
            ))
        if points:
            self.client.upsert(collection_name=COLLECTION_NAME, points=points, wait=True)

    def delete_document(self, document_id: str) -> None:
        from qdrant_client import models

        if not isinstance(document_id, str) or not document_id:
            raise ValueError("document_id must be nonempty")
        self.client.delete(collection_name=COLLECTION_NAME,
                           points_selector=models.FilterSelector(
                               filter=models.Filter(must=[models.FieldCondition(
                                   key="document_id", match=models.MatchValue(value=document_id))])),
                           wait=True)

    def search(self, vector: Sequence[float], limit: int = 3) -> list[RetrievedChunk]:
        if len(vector) != self.vector_size or type(limit) is not int or not 1 <= limit <= 10:
            raise ValueError("vector dimension and limit 1..10 must match the collection")
        result = self.client.query_points(collection_name=COLLECTION_NAME, query=[float(x) for x in vector],
                                          limit=limit, with_payload=True)
        hits = []
        for point in result.points:
            payload = point.payload or {}
            chunk = Chunk(str(payload["chunk_id"]), str(payload["document_id"]), int(payload["version"]),
                          str(payload["title"]), str(payload["source"]), int(payload["start_char"]),
                          int(payload["end_char"]), str(payload["text"]))
            hits.append(RetrievedChunk(chunk, float(point.score)))
        return hits

    def close(self) -> None:
        self.client.close()
