#!/usr/bin/env python3
"""Seed versioned synthetic document metadata; does not embed or generate."""
from __future__ import annotations

import json
import os
import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from ai.fde_v2.coverstory import learner
from ai.fde_v2.coverstory.corpus import CorpusStore

FIXTURES = ROOT / "fixtures/fde_v2/corpus"
DATA = Path(os.environ.get("FDE_V2_DATA_DIR", ".runtime/fde-v2")).resolve()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--limit", type=int, choices=(1, 10), default=1,
                        help="Day 14 partial seed accepts one row; completed U011 accepts all ten")
    args = parser.parse_args()
    if type(args.limit) is not int:
        parser.error("limit must be an integer")
    store = CorpusStore(DATA / "corpus.sqlite3")
    count = 0
    manifest_path = FIXTURES / "manifest.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    names = manifest.get("documents")
    if not isinstance(names, list) or len(names) != 10 or any(not isinstance(name, str) for name in names):
        raise ValueError("synthetic corpus manifest must enumerate exactly ten document filenames")
    paths = [FIXTURES / name for name in names[:args.limit]]
    for path in paths:
        resolved = path.resolve(strict=True)
        if FIXTURES.resolve() not in resolved.parents or path.is_symlink() or resolved.stat().st_size > 50_000:
            raise ValueError(f"unsafe fixture file: {path.name}")
        raw = json.loads(resolved.read_text(encoding="utf-8"))
        if args.limit == 1:
            # Preserve Day 14's deliberately partial single metadata-row task.
            document = learner.parse_document_record(raw)
            with store.connect() as connection:
                learner.upsert_document_metadata(document, connection)
        else:
            # Day 15 completes version/body-aware ingestion through the new seam.
            learner.ingest_versioned_document(raw, store)
        count += 1
    print(json.dumps({"status": "metadata_seeded", "documents_this_run": count,
                      "metadata_rows": len(store.metadata()), "path": str(DATA / "corpus.sqlite3"),
                      "body_persistence": args.limit == 10,
                      "embedding": "not run by this script", "generation": "not run by this script"}, indent=2))


if __name__ == "__main__":
    main()
