"""Learner starter for a reproducible, local portfolio artifact receipt.

This hashes the exact selected files at the checked-out source commit. It says
nothing about learner completion, application readiness, customer outcomes or
professional ownership.
"""
from __future__ import annotations

import hashlib
import argparse
import json
import re
import subprocess
from pathlib import Path

HEX40 = re.compile(r"^[0-9a-f]{40}$")


def build_receipt(root: Path, source_sha: str, paths: tuple[str, ...]) -> dict[str, object]:
    """TODO Days 267–268: bind a manifest to committed source and real bytes.

    Reject a stale/non-commit source SHA, duplicate/unsafe/missing paths and
    symlinks. Hash every file independently, then hash canonical manifest
    bytes. The 40-hex git commit and 64-hex artifact digest are distinct.
    """
    raise NotImplementedError("TODO: reproducible portfolio receipt")


def main() -> int:
    parser=argparse.ArgumentParser(description="Hash actual committed local portfolio files; no completion is recorded.")
    parser.add_argument("--source-sha",required=True)
    parser.add_argument("--path",action="append",required=True,dest="paths")
    args=parser.parse_args()
    root=Path(__file__).resolve().parents[2]
    print(json.dumps(build_receipt(root,args.source_sha,tuple(args.paths)),sort_keys=True))
    return 0


if __name__=="__main__":
    raise SystemExit(main())
