"""Local SQL/Qdrant boundary integration; no hosted identity or cloud path.

The application defaults to learner-owned identity/policy functions. Tests may
select the explicit reference adapter, but that never satisfies M2.2 evidence.
"""
from __future__ import annotations

import json
import sqlite3
import time
from uuid import NAMESPACE_URL, uuid5
from collections.abc import Mapping, Sequence
from typing import Any

from fastapi import Depends, FastAPI, HTTPException
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from pydantic import BaseModel, ConfigDict, Field
from qdrant_client import QdrantClient, models

from . import learner


class SearchRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")
    vector: list[float] = Field(min_length=2, max_length=2)
    limit: int = Field(default=5, ge=1, le=20)


class LocalSqlQdrantStore:
    """Real local SQLite + embedded Qdrant storage for integration mechanics."""

    def __init__(self, *, client: QdrantClient | None = None, collection: str = "m22_local"):
        self.sql = sqlite3.connect(":memory:", check_same_thread=False)
        self.sql.row_factory = sqlite3.Row
        self.sql.execute("""CREATE TABLE documents (
            document_id TEXT PRIMARY KEY, tenant_id TEXT NOT NULL, owner_id TEXT NOT NULL,
            reader_ids TEXT NOT NULL, writer_ids TEXT NOT NULL, deleted INTEGER NOT NULL,
            active_version INTEGER NOT NULL, active_chunk_ids TEXT NOT NULL, body TEXT NOT NULL
        )""")
        self.client = client or QdrantClient(":memory:")
        self.collection = collection
        if self.client.collection_exists(collection):
            self.client.delete_collection(collection)
        self.client.create_collection(collection_name=collection,
            vectors_config=models.VectorParams(size=2, distance=models.Distance.COSINE))
        self._hit_cache: dict[tuple[str, tuple[float, ...], int, int], list[dict[str, Any]]] = {}
        self._vector_epoch = 0

    def close(self) -> None:
        self.client.close()
        self.sql.close()

    def put_document(self, *, document_id: str, tenant_id: str, owner_id: str,
                     reader_ids: Sequence[str], writer_ids: Sequence[str], version: int,
                     chunks: Sequence[tuple[str, str, Sequence[float]]]) -> None:
        if type(version) is not int or version < 1 or not chunks:
            raise ValueError("a positive version and at least one chunk are required")
        chunk_ids = [chunk_id for chunk_id, _text, _vector in chunks]
        if len(set(chunk_ids)) != len(chunk_ids):
            raise ValueError("chunk IDs must be unique within a document version")
        self.sql.execute("""INSERT OR REPLACE INTO documents VALUES(?,?,?,?,?,?,?,?,?)""",
            (document_id, tenant_id, owner_id, json.dumps(list(reader_ids)),
             json.dumps(list(writer_ids)), 0, version, json.dumps(chunk_ids),
             " ".join(text for _chunk, text, _vector in chunks)))
        self.sql.commit()
        points = [models.PointStruct(id=self._point_id(document_id, version, chunk_id), vector=list(vector),
                    payload={"tenant_id": tenant_id, "document_id": document_id,
                             "chunk_id": chunk_id, "version": version, "text": text})
                  for chunk_id, text, vector in chunks]
        self.client.upsert(collection_name=self.collection, points=points, wait=True)
        self._vector_epoch += 1

    def revoke_reader(self, document_id: str, subject_id: str) -> None:
        row = self.sql.execute("SELECT reader_ids FROM documents WHERE document_id=?", (document_id,)).fetchone()
        if row is None:
            raise KeyError(document_id)
        readers = [x for x in json.loads(row["reader_ids"]) if x != subject_id]
        self.sql.execute("UPDATE documents SET reader_ids=? WHERE document_id=?",
                         (json.dumps(readers), document_id))
        self.sql.commit()

    def authoritative_documents(self) -> dict[str, dict[str, Any]]:
        rows = self.sql.execute("SELECT * FROM documents").fetchall()
        return {row["document_id"]: {
            "tenant_id": row["tenant_id"], "owner_id": row["owner_id"],
            "reader_ids": json.loads(row["reader_ids"]), "writer_ids": json.loads(row["writer_ids"]),
            "deleted": row["deleted"] == 1, "active_version": row["active_version"],
            "active_chunk_ids": json.loads(row["active_chunk_ids"]),
            "body": row["body"],
        } for row in rows}

    def raw_hits(self, tenant_id: str, vector: Sequence[float], limit: int) -> list[dict[str, Any]]:
        key = (tenant_id, tuple(float(x) for x in vector), limit, self._vector_epoch)
        if key not in self._hit_cache:
            response = self.client.query_points(collection_name=self.collection, query=list(vector),
                query_filter=models.Filter(must=[models.FieldCondition(
                    key="tenant_id", match=models.MatchValue(value=tenant_id))]),
                limit=limit, with_payload=True, with_vectors=False)
            self._hit_cache[key] = [dict(point.payload or {}) for point in response.points]
        return [dict(hit) for hit in self._hit_cache[key][:limit]]

    def document(self, document_id: str) -> dict[str, Any] | None:
        row = self.authoritative_documents().get(document_id)
        return row

    def chunk_text(self, hit: Mapping[str, Any]) -> str:
        point_id = self._point_id(hit["document_id"], hit["version"], hit["chunk_id"])
        points = self.client.retrieve(collection_name=self.collection, ids=[point_id], with_payload=True)
        if not points:
            return ""
        return str((points[0].payload or {}).get("text", ""))

    @staticmethod
    def _point_id(document_id: str, version: int, chunk_id: str):
        return uuid5(NAMESPACE_URL, f"m22:{document_id}:{version}:{chunk_id}")


def create_app(*, store: LocalSqlQdrantStore, verified_claims_provider,
               subject_lookup, expected_issuer: str, expected_audience: str,
               adapter=learner, clock=time.time) -> FastAPI:
    app = FastAPI(title="M2.2 local identity/access integration", version="conditional")
    bearer = HTTPBearer(auto_error=False)

    def identity(credentials: HTTPAuthorizationCredentials | None = Depends(bearer)):
        if credentials is None or credentials.scheme.lower() != "bearer":
            raise HTTPException(status_code=401, detail="bearer authentication required")
        try:
            return adapter.verify_identity(credentials.credentials, verified_claims_provider,
                subject_lookup, expected_issuer=expected_issuer,
                expected_audience=expected_audience, now=clock())
        except Exception as exc:
            raise HTTPException(status_code=401, detail="identity rejected") from exc

    @app.get("/documents/{document_id}")
    def read_document(document_id: str, context=Depends(identity)):
        doc = store.document(document_id)
        # Policy is an authorization boundary: only the literal boolean True
        # grants access. A malformed truthy result must never disclose content.
        if doc is None or adapter.authorize_document(context, doc, "read") is not True:
            raise HTTPException(status_code=404, detail="document unavailable")
        return {"document_id": document_id, "body": doc["body"], "version": doc["active_version"]}

    @app.post("/search")
    def search(body: SearchRequest, context=Depends(identity)):
        hits = store.raw_hits(context["tenant_id"], body.vector, body.limit)
        allowed = adapter.filter_retrieved_hits(context, hits, store.authoritative_documents())
        return {"results": [{**hit, "text": store.chunk_text(hit)} for hit in allowed]}

    return app
