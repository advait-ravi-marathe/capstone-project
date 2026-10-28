"""Assistant-supplied SQLite metadata schema for Sprint 1's synthetic corpus."""
from __future__ import annotations

import sqlite3
from pathlib import Path

from .contracts import Chunk, Document


SCHEMA = """
CREATE TABLE IF NOT EXISTS documents (
    document_id TEXT PRIMARY KEY,
    version INTEGER NOT NULL CHECK(version >= 1),
    title TEXT NOT NULL,
    source TEXT NOT NULL,
    content_sha256 TEXT NOT NULL CHECK(length(content_sha256) = 64)
);
CREATE TABLE IF NOT EXISTS document_bodies (
    document_id TEXT PRIMARY KEY REFERENCES documents(document_id) ON DELETE CASCADE,
    text TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS chunks (
    chunk_id TEXT PRIMARY KEY,
    document_id TEXT NOT NULL REFERENCES documents(document_id) ON DELETE CASCADE,
    version INTEGER NOT NULL CHECK(version >= 1),
    title TEXT NOT NULL,
    source TEXT NOT NULL,
    start_char INTEGER NOT NULL CHECK(start_char >= 0),
    end_char INTEGER NOT NULL CHECK(end_char > start_char),
    text TEXT NOT NULL CHECK(length(text) > 0)
);
CREATE VIRTUAL TABLE IF NOT EXISTS chunks_fts USING fts5(chunk_id UNINDEXED, text, title);
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

    def get_document(self, document_id: str) -> Document | None:
        with self.connect() as db:
            row = db.execute(
                "SELECT d.document_id,d.version,d.title,d.source,COALESCE(b.text,'') AS text,d.content_sha256 "
                "FROM documents d LEFT JOIN document_bodies b USING(document_id) WHERE d.document_id=?",
                (document_id,),
            ).fetchone()
        if row is None:
            return None
        return Document(row["document_id"], row["version"], row["title"], row["source"],
                        row["text"], row["content_sha256"])

    def upsert_version(self, document: Document, chunks: list[Chunk]) -> None:
        """Assistant-owned atomic persistence; caller owns version policy."""
        if any((part.document_id, part.version) != (document.document_id, document.version) for part in chunks):
            raise ValueError("chunk identity/version must match document")
        with self.connect() as db:
            db.execute("BEGIN IMMEDIATE")
            old_ids = [row[0] for row in db.execute(
                "SELECT chunk_id FROM chunks WHERE document_id=?", (document.document_id,))]
            for chunk_id in old_ids:
                db.execute("DELETE FROM chunks_fts WHERE chunk_id=?", (chunk_id,))
            db.execute("DELETE FROM chunks WHERE document_id=?", (document.document_id,))
            db.execute(
                "INSERT INTO documents(document_id,version,title,source,content_sha256) VALUES(?,?,?,?,?) "
                "ON CONFLICT(document_id) DO UPDATE SET version=excluded.version,title=excluded.title,"
                "source=excluded.source,content_sha256=excluded.content_sha256",
                (document.document_id, document.version, document.title, document.source,
                 document.content_sha256),
            )
            db.execute("INSERT INTO document_bodies(document_id,text) VALUES(?,?) "
                       "ON CONFLICT(document_id) DO UPDATE SET text=excluded.text",
                       (document.document_id, document.text))
            for chunk in chunks:
                db.execute("INSERT INTO chunks VALUES(?,?,?,?,?,?,?,?)", (
                    chunk.chunk_id, chunk.document_id, chunk.version, chunk.title, chunk.source,
                    chunk.start_char, chunk.end_char, chunk.text))
                db.execute("INSERT INTO chunks_fts(chunk_id,text,title) VALUES(?,?,?)",
                           (chunk.chunk_id, chunk.text, chunk.title))

    def upsert_metadata_version(self, document: Document) -> None:
        """Persist document metadata/text and invalidate prior SQL chunks atomically."""
        with self.connect() as db:
            db.execute("BEGIN IMMEDIATE")
            old_ids = [row[0] for row in db.execute(
                "SELECT chunk_id FROM chunks WHERE document_id=?", (document.document_id,))]
            for chunk_id in old_ids:
                db.execute("DELETE FROM chunks_fts WHERE chunk_id=?", (chunk_id,))
            db.execute("DELETE FROM chunks WHERE document_id=?", (document.document_id,))
            db.execute(
                "INSERT INTO documents(document_id,version,title,source,content_sha256) VALUES(?,?,?,?,?) "
                "ON CONFLICT(document_id) DO UPDATE SET version=excluded.version,title=excluded.title,"
                "source=excluded.source,content_sha256=excluded.content_sha256",
                (document.document_id, document.version, document.title, document.source,
                 document.content_sha256),
            )
            db.execute("INSERT INTO document_bodies(document_id,text) VALUES(?,?) "
                       "ON CONFLICT(document_id) DO UPDATE SET text=excluded.text",
                       (document.document_id, document.text))

    def chunks(self) -> list[Chunk]:
        with self.connect() as db:
            rows = db.execute("SELECT * FROM chunks ORDER BY document_id,version,start_char").fetchall()
        return [Chunk(row["chunk_id"], row["document_id"], row["version"], row["title"], row["source"],
                      row["start_char"], row["end_char"], row["text"]) for row in rows]

    def fts_bm25(self, query: str, limit: int = 3) -> list[tuple[Chunk, float]]:
        """Supplied SQLite FTS5 transport used by the learner's ranking seam."""
        if not isinstance(query, str) or not query.strip() or type(limit) is not int or not 1 <= limit <= 10:
            raise ValueError("query and limit 1..10 are required")
        tokens = [token for token in query.split() if token.isalnum()]
        if not tokens:
            return []
        expression = " OR ".join('"' + token.replace('"', '""') + '"' for token in tokens)
        with self.connect() as db:
            rows = db.execute(
                "SELECT c.*,bm25(chunks_fts) AS rank FROM chunks_fts JOIN chunks c "
                "ON c.chunk_id=chunks_fts.chunk_id WHERE chunks_fts MATCH ? "
                "ORDER BY rank ASC,c.chunk_id ASC LIMIT ?", (expression, limit)).fetchall()
        return [(Chunk(row["chunk_id"], row["document_id"], row["version"], row["title"], row["source"],
                       row["start_char"], row["end_char"], row["text"]), -float(row["rank"])) for row in rows]
