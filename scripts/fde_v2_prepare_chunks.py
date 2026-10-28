#!/usr/bin/env python3
"""Build versioned SQL chunks through the learner's U012 seam; no model calls."""
from __future__ import annotations

import argparse
import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from ai.fde_v2.coverstory import learner
from ai.fde_v2.coverstory.corpus import CorpusStore


def main() -> int:
    parser = argparse.ArgumentParser(description="Prepare deterministic SQL chunks from stored synthetic documents.")
    parser.parse_args()
    data_dir = Path(os.environ.get("FDE_V2_DATA_DIR", ".runtime/fde-v2")).resolve()
    store = CorpusStore(data_dir / "corpus.sqlite3")
    rows = store.metadata()
    if len(rows) != 10:
        raise SystemExit(f"expected ten versioned metadata rows, found {len(rows)}; run U011 first")
    counts = []
    for row in rows:
        document = store.get_document(str(row["document_id"]))
        if document is None or not document.text:
            raise SystemExit(f"missing stored source body for {row['document_id']}; complete U011 first")
        chunks = learner.chunk_document(document)
        store.upsert_version(document, chunks)
        counts.append({"document_id": document.document_id, "version": document.version,
                       "chunk_count": len(chunks)})
    print(json.dumps({"status": "chunks_persisted", "document_count": len(counts),
                      "chunk_count": sum(row["chunk_count"] for row in counts),
                      "documents": counts, "embeddings": "not run"}, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
