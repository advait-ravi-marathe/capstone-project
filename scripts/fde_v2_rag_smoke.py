#!/usr/bin/env python3
"""Attempt real local RAG only; never substitutes a test double or extraction."""
from __future__ import annotations

import argparse
import argparse
import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from ai.fde_v2.coverstory.contracts import RuntimeUnavailable
from ai.fde_v2.coverstory.corpus import CorpusStore
from ai.fde_v2.coverstory.models import LocalEmbeddings, LocalModels
from ai.fde_v2.coverstory.pipeline import CoverStoryPipeline
from ai.fde_v2.coverstory.vector_store import PersistentVectorStore


def main() -> int:
    parser = argparse.ArgumentParser(description="Attempt real local RAG without network or fallback.")
    parser.parse_args()
    parser = argparse.ArgumentParser()
    parser.add_argument("--question", default="What is the Northstar individual in-network deductible?")
    args = parser.parse_args()
    data_dir = Path(os.environ.get("FDE_V2_DATA_DIR", ".runtime/fde-v2")).resolve()
    model_dir = Path(os.environ.get("FDE_V2_EMBEDDING_DIR", "~/.cache/fde-v2/embeddings/model")).expanduser()
    embedding_manifest = Path(os.environ.get(
        "FDE_V2_EMBEDDING_MANIFEST", "~/.cache/fde-v2/embeddings/manifest.json")).expanduser()
    try:
        # Both loaders are strictly local_files_only; this command never contacts a provider.
        generator = LocalModels.load()
        embedder = LocalEmbeddings.load(model_dir, embedding_manifest)
    except RuntimeUnavailable as exc:
        print(f"REAL_RAG_BLOCKED: {exc}", file=sys.stderr)
        return 2
    corpus = CorpusStore(data_dir / "corpus.sqlite3")
    vector_store = PersistentVectorStore(data_dir / "qdrant", embedder.dimension)
    pipeline = CoverStoryPipeline(generator, embedder, corpus, vector_store)
    try:
        answer, retrieved, timings = pipeline.answer(args.question)
    finally:
        vector_store.close()
    evidence = {hit.chunk.chunk_id: hit.chunk for hit in retrieved}
    print(json.dumps({"real_generation": True, "model_id": answer.model_id,
                      "model_revision": answer.model_revision, "answer": answer.text,
                      "citations": list(answer.citations), "abstained": answer.abstained,
                      "sources": [{"chunk_id": key, "document_id": evidence[key].document_id,
                                   "title": evidence[key].title, "text": evidence[key].text}
                                  for key in answer.citations], "stage_timings_ms": timings,
                      "prototype_limits": "local synthetic corpus; not production or independent benchmark"},
                     indent=2, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
