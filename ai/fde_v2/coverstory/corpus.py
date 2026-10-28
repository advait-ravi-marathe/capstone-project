"""Assistant-supplied SQLite metadata schema for Sprint 1's synthetic corpus."""
from __future__ import annotations

import sqlite3
from pathlib import Path


SCHEMA = """
CREATE TABLE IF NOT EXISTS documents (
    document_id TEXT PRIMARY KEY,
    version INTEGER NOT NULL CHECK(version >= 1),
    title TEXT NOT NULL,
    source TEXT NOT NULL,
    content_sha256 TEXT NOT NULL CHECK(length(content_sha256) = 64)
);
"""


class CorpusStore:
    """Small persistent metadata store; text, chunks and vectors are later work."""

    def __init__(self, path: Path):
        self.path = path
        self.path.parent.mkdir(parents=True, exist_ok=True)
        with self.connect() as db:
            db.executescript(SCHEMA)

    def connect(self) -> sqlite3.Connection:
        db = sqlite3.connect(self.path)
        db.row_factory = sqlite3.Row
        return db

    def metadata(self) -> list[dict[str, object]]:
        with self.connect() as db:
            rows = db.execute(
                "SELECT document_id,version,title,source,content_sha256 "
                "FROM documents ORDER BY document_id"
            ).fetchall()
        return [dict(row) for row in rows]
