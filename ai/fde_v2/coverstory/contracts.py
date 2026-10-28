from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class Document:
    document_id: str
    version: int
    title: str
    source: str
    text: str
    content_sha256: str


class InvalidDocument(ValueError):
    """A source record is outside the explicitly synthetic fixture contract."""


class RuntimeUnavailable(RuntimeError):
    """Pinned real model artifacts or local runtime dependencies are unavailable."""
