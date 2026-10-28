from __future__ import annotations

from dataclasses import dataclass
from typing import Literal


@dataclass(frozen=True)
class Document:
    document_id: str
    version: int
    title: str
    source: str
    text: str
    content_sha256: str


@dataclass(frozen=True)
class Chunk:
    chunk_id: str
    document_id: str
    version: int
    title: str
    source: str
    start_char: int
    end_char: int
    text: str


@dataclass(frozen=True)
class RetrievedChunk:
    chunk: Chunk
    score: float


@dataclass(frozen=True)
class Answer:
    text: str
    citations: tuple[str, ...]
    retrieved_chunk_ids: tuple[str, ...]
    abstained: bool
    model_id: str
    model_revision: str


FailureCategory = Literal[
    "retrieval", "unsupported_generation", "wrong_citation", "schema",
    "latency_or_availability", "input_validation", "unknown",
]


class InvalidDocument(ValueError):
    """A source record is outside the explicitly synthetic fixture contract."""


class RuntimeUnavailable(RuntimeError):
    """Pinned real model artifacts or local runtime dependencies are unavailable."""
