"""Day 1 worked example: inspect synthetic JSON and retrieve matching text.

This is an instructor-owned lexical baseline. It makes no model call and does
not count as a learner implementation or an AI/RAG pass.
"""
from __future__ import annotations

import json
import re
import sqlite3
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
CORPUS = ROOT / "fixtures/fde_v2/corpus"


def run(question: str = "What is the individual deductible?") -> list[tuple[str, str, int]]:
    manifest = json.loads((CORPUS / "manifest.json").read_text(encoding="utf-8"))
    db = sqlite3.connect(":memory:")
    db.execute("CREATE VIRTUAL TABLE documents USING fts5(document_id UNINDEXED, title, text)")
    for filename in manifest["documents"]:
        record = json.loads((CORPUS / filename).read_text(encoding="utf-8"))
        if record.get("version") != 1 or not record.get("text", "").startswith("TRAINING FIXTURE"):
            raise ValueError(f"unexpected training record: {filename}")
        db.execute("INSERT INTO documents VALUES(?,?,?)",
                   (record["document_id"], record["title"], record["text"]))
    terms = [token for token in re.findall(r"[A-Za-z0-9]+", question) if len(token) > 2]
    if not terms:
        return []
    expression = " OR ".join('"' + token.replace('"', '""') + '"' for token in terms)
    rows = db.execute("SELECT document_id,title,bm25(documents) FROM documents "
                      "WHERE documents MATCH ? ORDER BY bm25(documents), document_id LIMIT 3", (expression,))
    return [(str(doc_id), str(title), index + 1) for index, (doc_id, title, _score) in enumerate(rows)]


if __name__ == "__main__":
    for doc_id, title, rank in run():
        print(f"rank={rank} document={doc_id} title={title}")
    print("Instructor-owned keyword baseline only; no generation, embeddings, or RAG were run.")
