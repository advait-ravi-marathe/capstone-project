"""Local M2.1 version lifecycle API; access control is added by the M2.2 layer."""
from __future__ import annotations

import hashlib
from typing import Any

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, ConfigDict, Field

from ai.fde_v2.coverstory.contracts import Chunk, Document
from .workflow import VersionedWorkflow


class ChunkInput(BaseModel):
    model_config = ConfigDict(extra="forbid")
    chunk_id: str = Field(min_length=1, max_length=160)
    start_char: int = Field(ge=0)
    end_char: int = Field(gt=0)
    text: str = Field(min_length=1, max_length=16000)


class VersionInput(BaseModel):
    model_config = ConfigDict(extra="forbid")
    version: int = Field(ge=1)
    title: str = Field(min_length=1, max_length=300)
    source: str = Field(min_length=1, max_length=1000)
    text: str = Field(min_length=1, max_length=100000)
    chunks: list[ChunkInput] = Field(min_length=1, max_length=100)


class SearchInput(BaseModel):
    model_config = ConfigDict(extra="forbid")
    query: str = Field(min_length=1, max_length=2000)
    limit: int = Field(default=3, ge=1, le=10)


def create_app(workflow: VersionedWorkflow) -> FastAPI:
    app = FastAPI(title="M2.1 local version lifecycle API", version="conditional")

    @app.put("/documents/{document_id}/versions")
    def publish(document_id: str, body: VersionInput) -> dict[str, Any]:
        text_hash = hashlib.sha256(body.text.encode()).hexdigest()
        document = Document(document_id, body.version, body.title, body.source, body.text, text_hash)
        chunks = [Chunk(chunk.chunk_id, document_id, body.version, body.title, body.source,
                        chunk.start_char, chunk.end_char, chunk.text) for chunk in body.chunks]
        try:
            result = workflow.publish(document, chunks)
        except (ValueError, RuntimeError) as exc:
            raise HTTPException(status_code=409, detail="version was not activated") from exc
        return result

    @app.post("/search")
    def search(body: SearchInput) -> dict[str, Any]:
        try:
            hits = workflow.search(body.query, body.limit)
        except (ValueError, RuntimeError) as exc:
            raise HTTPException(status_code=503, detail="local versioned search unavailable") from exc
        return {"results": [{"document_id": hit.chunk.document_id, "version": hit.chunk.version,
                             "chunk_id": hit.chunk.chunk_id, "text": hit.chunk.text,
                             "score": hit.score} for hit in hits]}

    @app.delete("/documents/{document_id}")
    def delete(document_id: str) -> dict[str, Any]:
        try:
            return workflow.delete(document_id)
        except (KeyError, ValueError) as exc:
            raise HTTPException(status_code=404, detail="document unavailable") from exc

    return app
