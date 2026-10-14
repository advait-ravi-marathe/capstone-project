"""CAP-71 chunk value object; deterministic slicing remains learner-owned."""
from __future__ import annotations
from dataclasses import dataclass

@dataclass(frozen=True)
class Chunk:
    source_id: str
    version: str
    start: int
    end: int
    text: str


def chunk_text(source_id: str, version: str, text: str, max_chars: int, overlap: int) -> list[Chunk]:
    # LEARNER TODO: validate metadata/bounds, then emit deterministic Unicode
    # codepoint slices with overlap while preserving exact offsets and text.
    raise NotImplementedError("CAP-71 learner TODO: implement chunk_text")
