#!/usr/bin/env python3
"""Run the U011 version-count query against the local synthetic corpus."""
from __future__ import annotations

import argparse
import json
import os
import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_DB = Path(os.environ.get("FDE_V2_DATA_DIR", ".runtime/fde-v2")).resolve() / "corpus.sqlite3"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--database", type=Path, default=DEFAULT_DB)
    args = parser.parse_args()
    query = (ROOT / "scripts/fde_v2_version_counts.sql").read_text(encoding="utf-8")
    with sqlite3.connect(args.database) as connection:
        rows = [dict(version=row[0], document_count=row[1]) for row in connection.execute(query)]
    print(json.dumps({"database": str(args.database), "version_counts": rows}, indent=2))


if __name__ == "__main__":
    main()
