"""Instructor-supplied plumbing for constructing validated document records."""
from __future__ import annotations

import hashlib
from collections.abc import Mapping
from typing import Any

from .contracts import Document


def build_document(raw: Mapping[str, Any]) -> Document:
    """Create the typed record and hash its text after learner validation."""
    text = raw["text"]
    return Document(
        document_id=raw["document_id"],
        version=raw["version"],
        title=raw["title"],
        source=raw["source"],
        text=text,
        content_sha256=hashlib.sha256(text.encode("utf-8")).hexdigest(),
    )
