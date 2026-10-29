"""Assistant-owned SQLite version ledger used by the M2.1 staged lifecycle lab.

This sidecar is a learning scaffold, not a production migration or a claim that
the existing P1 application now uses it. The learner implements the orchestration
seam; this class supplies bounded persistence and failure-safe primitives.
"""
from __future__ import annotations

import hashlib
import sqlite3
from contextlib import contextmanager
from pathlib import Path

from ..coverstory.contracts import Chunk, Document


_SCHEMA = """
PRAGMA foreign_keys=ON;
CREATE TABLE IF NOT EXISTS m21_heads (
    document_id TEXT PRIMARY KEY,
    active_version INTEGER,
    deleted INTEGER NOT NULL DEFAULT 0 CHECK(deleted IN (0,1))
);
CREATE TABLE IF NOT EXISTS m21_versions (
    document_id TEXT NOT NULL,
    version INTEGER NOT NULL CHECK(version >= 1),
    title TEXT NOT NULL,
    source TEXT NOT NULL,
    content_sha256 TEXT NOT NULL CHECK(length(content_sha256)=64),
    body TEXT NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('staged','active','retired','tombstoned')),
    PRIMARY KEY(document_id, version)
);
CREATE TABLE IF NOT EXISTS m21_chunks (
    chunk_id TEXT PRIMARY KEY,
    document_id TEXT NOT NULL,
    version INTEGER NOT NULL,
    title TEXT NOT NULL,
    source TEXT NOT NULL,
    start_char INTEGER NOT NULL CHECK(start_char >= 0),
    end_char INTEGER NOT NULL CHECK(end_char > start_char),
    text TEXT NOT NULL CHECK(length(text)>0),
    FOREIGN KEY(document_id,version) REFERENCES m21_versions(document_id,version) ON DELETE CASCADE
);
"""


class VersionLedger:
    def __init__(self, path: Path):
        self.path = path
        self.path.parent.mkdir(parents=True, exist_ok=True)
        with self.session() as connection:
            connection.executescript(_SCHEMA)

    @contextmanager
    def session(self):
        connection = self.connect()
        try:
            with connection:
                yield connection
        finally:
            connection.close()

    def connect(self):
        connection = sqlite3.connect(self.path)
        connection.row_factory = sqlite3.Row
        connection.execute("PRAGMA foreign_keys=ON")
        return connection

    @staticmethod
    def _validate(document: Document, chunks: list[Chunk]) -> None:
        if not isinstance(document, Document) or not document.document_id.strip() or document.version < 1:
            raise ValueError("a valid document record is required")
        if hashlib.sha256(document.text.encode()).hexdigest() != document.content_sha256:
            raise ValueError("document content digest does not match its body")
        if not chunks or any(not isinstance(chunk, Chunk) for chunk in chunks):
            raise ValueError("a nonempty list of validated chunks is required")
        if len({chunk.chunk_id for chunk in chunks}) != len(chunks):
            raise ValueError("chunk IDs must be unique")
        if any((chunk.document_id, chunk.version, chunk.title, chunk.source) !=
               (document.document_id, document.version, document.title, document.source)
               for chunk in chunks):
            raise ValueError("all chunks must match the staged document identity/version")

    def stage(self, document: Document, chunks: list[Chunk]) -> str:
        """Persist a staged revision; never changes the active head."""
        self._validate(document, chunks)
        with self.session() as connection:
            connection.execute("BEGIN IMMEDIATE")
            existing = connection.execute(
                "SELECT content_sha256,status FROM m21_versions WHERE document_id=? AND version=?",
                (document.document_id, document.version)).fetchone()
            if existing:
                if existing["content_sha256"] != document.content_sha256:
                    raise ValueError("same document version cannot be rewritten")
                saved = connection.execute(
                    "SELECT chunk_id,text,start_char,end_char FROM m21_chunks WHERE document_id=? AND version=? "
                    "ORDER BY chunk_id", (document.document_id, document.version)).fetchall()
                expected = sorted((c.chunk_id, c.text, c.start_char, c.end_char) for c in chunks)
                if [tuple(row) for row in saved] != expected:
                    raise ValueError("same document version has conflicting chunk data")
                return "unchanged"
            head = connection.execute("SELECT active_version,deleted FROM m21_heads WHERE document_id=?",
                                      (document.document_id,)).fetchone()
            if head and head["active_version"] is not None and document.version <= head["active_version"]:
                raise ValueError("staged revision must advance beyond the active version")
            connection.execute(
                "INSERT INTO m21_versions VALUES(?,?,?,?,?,?, 'staged')",
                (document.document_id, document.version, document.title, document.source,
                 document.content_sha256, document.text))
            for chunk in chunks:
                connection.execute("INSERT INTO m21_chunks VALUES(?,?,?,?,?,?,?,?)",
                                   (chunk.chunk_id, chunk.document_id, chunk.version, chunk.title, chunk.source,
                                    chunk.start_char, chunk.end_char, chunk.text))
            return "staged"

    def active_version(self, document_id: str) -> int | None:
        with self.session() as connection:
            row = connection.execute("SELECT active_version,deleted FROM m21_heads WHERE document_id=?",
                                     (document_id,)).fetchone()
        if row is None or row["deleted"]:
            return None
        return row["active_version"]

    def version_chunks(self, document_id: str, version: int) -> list[Chunk]:
        with self.session() as connection:
            rows = connection.execute("SELECT * FROM m21_chunks WHERE document_id=? AND version=? "
                                      "ORDER BY start_char,chunk_id", (document_id, version)).fetchall()
        return [Chunk(row["chunk_id"], row["document_id"], row["version"], row["title"], row["source"],
                      row["start_char"], row["end_char"], row["text"]) for row in rows]

    def activate(self, document_id: str, version: int, vector_store) -> int | None:
        """Atomically switch the SQL head only after all expected points exist."""
        with self.session() as connection:
            connection.execute("BEGIN IMMEDIATE")
            staged = connection.execute("SELECT status FROM m21_versions WHERE document_id=? AND version=?",
                                        (document_id, version)).fetchone()
            if staged is None or staged["status"] not in {"staged", "active"}:
                raise ValueError("only a staged or idempotently active version may be activated")
            expected = {row[0] for row in connection.execute(
                "SELECT chunk_id FROM m21_chunks WHERE document_id=? AND version=?", (document_id, version))}
            present = {row["chunk_id"] for row in vector_store.list_document_versions()
                       if row["document_id"] == document_id and row["version"] == version}
            if expected != present or not expected:
                raise ValueError("complete staged vector set must be present before switching active version")
            head = connection.execute("SELECT active_version FROM m21_heads WHERE document_id=?",
                                      (document_id,)).fetchone()
            old = head["active_version"] if head else None
            if old == version:
                return old
            if old is not None:
                connection.execute("UPDATE m21_versions SET status='retired' WHERE document_id=? AND version=?",
                                   (document_id, old))
            connection.execute("UPDATE m21_versions SET status='active' WHERE document_id=? AND version=?",
                               (document_id, version))
            connection.execute("INSERT INTO m21_heads(document_id,active_version,deleted) VALUES(?,?,0) "
                               "ON CONFLICT(document_id) DO UPDATE SET active_version=excluded.active_version,deleted=0",
                               (document_id, version))
            return old

    def delete(self, document_id: str) -> int | None:
        """Tombstone in SQL first, making all versions ineligible immediately."""
        if not isinstance(document_id, str) or not document_id:
            raise ValueError("document_id must be nonempty")
        with self.session() as connection:
            connection.execute("BEGIN IMMEDIATE")
            row = connection.execute("SELECT active_version FROM m21_heads WHERE document_id=?",
                                     (document_id,)).fetchone()
            old = row["active_version"] if row else None
            connection.execute("UPDATE m21_versions SET status='tombstoned' WHERE document_id=? "
                               "AND status IN ('active','retired','staged')", (document_id,))
            connection.execute("INSERT INTO m21_heads(document_id,active_version,deleted) VALUES(?,NULL,1) "
                               "ON CONFLICT(document_id) DO UPDATE SET active_version=NULL,deleted=1",
                               (document_id,))
            return old

    def is_current(self, document_id: str, version: int) -> bool:
        return self.active_version(document_id) == version

    def active_rows(self) -> list[dict[str, object]]:
        with self.session() as connection:
            rows = connection.execute("SELECT h.document_id,h.active_version AS version,v.status "
                                      "FROM m21_heads h LEFT JOIN m21_versions v ON "
                                      "v.document_id=h.document_id AND v.version=h.active_version "
                                      "WHERE h.deleted=0 ORDER BY h.document_id").fetchall()
        return [dict(row) for row in rows if row["version"] is not None]

    def staged_and_retired_chunk_ids(self) -> set[str]:
        with self.session() as connection:
            rows = connection.execute("SELECT c.chunk_id FROM m21_chunks c JOIN m21_versions v "
                                      "USING(document_id,version) WHERE v.status!='active'").fetchall()
        return {row[0] for row in rows}
