#!/usr/bin/env python3
"""Embed existing chunks with verified local files and persist to local Qdrant."""
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
from ai.fde_v2.coverstory.contracts import RuntimeUnavailable
from ai.fde_v2.coverstory.corpus import CorpusStore
from ai.fde_v2.coverstory.models import LocalEmbeddings
from ai.fde_v2.coverstory.vector_store import PersistentVectorStore


def main() -> int:
    parser = argparse.ArgumentParser(description="Index chunk embeddings with verified local model files.")
    parser.parse_args()
    data_dir = Path(os.environ.get("FDE_V2_DATA_DIR", ".runtime/fde-v2")).resolve()
    model_dir = Path(os.environ.get("FDE_V2_EMBEDDING_DIR", "~/.cache/fde-v2/embeddings/model")).expanduser()
    manifest_path = Path(os.environ.get(
        "FDE_V2_EMBEDDING_MANIFEST", "~/.cache/fde-v2/embeddings/manifest.json")).expanduser()
    corpus = CorpusStore(data_dir / "corpus.sqlite3")
    chunks = corpus.chunks()
    if not chunks:
        raise SystemExit("no SQL chunks found; complete U011/U012 first")
    try:
        embedder = LocalEmbeddings.load(model_dir, manifest_path)
    except RuntimeUnavailable as exc:
        print(f"REAL_EMBEDDING_BLOCKED: {exc}")
        return 2
    store = PersistentVectorStore(data_dir / "qdrant", embedder.dimension)
    try:
        vectors = learner.embed_texts([chunk.text for chunk in chunks], embedder)
        learner.index_vectors(chunks, vectors, store)
    finally:
        store.close()
    print(json.dumps({"status": "local_vectors_indexed", "model_id": embedder.model_id,
                      "revision": embedder.model_revision, "license": embedder.license,
                      "dimension": embedder.dimension, "normalized": embedder.normalize_embeddings,
                      "chunk_count": len(chunks), "vector_store": "Qdrant local persistent mode",
                      "network": "not used by the loader"}, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
