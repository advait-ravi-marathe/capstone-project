#!/usr/bin/env python3
"""Fetch only the pinned public generation-model files; never execute repository code."""
from __future__ import annotations

import hashlib
import json
import os
from pathlib import Path

from huggingface_hub import HfApi, snapshot_download


ROOT = Path(__file__).resolve().parents[1]
LOCK = json.loads((ROOT / "ai/fde_v2/coverstory/model_lock.json").read_text(encoding="utf-8"))
DATA_ROOT = Path(os.environ.get("FDE_V2_RUNTIME_DIR", "~/.cache/fde-v2/models")).expanduser().resolve()


def digest(path: Path) -> str:
    value = hashlib.sha256()
    with path.open("rb") as source:
        for block in iter(lambda: source.read(1024 * 1024), b""):
            value.update(block)
    return value.hexdigest()


def fetch(name: str, config: dict) -> dict[str, str]:
    repo_id, revision = config["repository"], config["revision"]
    info = HfApi().model_info(repo_id=repo_id, revision=revision, files_metadata=True, token=False)
    remote = {item.rfilename for item in info.siblings}
    expected = set(config["expected_files"])
    missing = expected - remote
    if missing:
        raise RuntimeError(f"Pinned {repo_id}@{revision} does not publish expected files: {sorted(missing)}")
    destination = DATA_ROOT / name
    destination.mkdir(parents=True, exist_ok=True)
    snapshot_download(repo_id=repo_id, revision=revision, local_dir=destination,
                      allow_patterns=sorted(expected), token=False, max_workers=2)
    hashes: dict[str, str] = {}
    for relative in sorted(expected):
        file = destination / relative
        if file.is_symlink() or not file.is_file():
            raise RuntimeError(f"Pinned model file is absent or is a symlink: {file}")
        hashes[relative] = digest(file)
    return hashes


def main() -> None:
    if LOCK.get("trust_remote_code") is not False:
        raise RuntimeError("refusing to download: trust_remote_code must remain false")
    manifest = {
        "schema_version": 1,
        "status": "downloaded_and_sha256_verified",
        "downloaded_at_utc": __import__("datetime").datetime.now(__import__("datetime").timezone.utc).isoformat(),
        "trust_remote_code": False,
        "generation": {"repository": LOCK["generation"]["repository"], "revision": LOCK["generation"]["revision"],
                       "files": fetch("generation", LOCK["generation"])},
    }
    path = DATA_ROOT / "runtime-manifest.json"
    path.write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(json.dumps({"manifest": str(path), "generation_file_count": len(manifest["generation"]["files"]),
                      "trust_remote_code": False}, indent=2))


if __name__ == "__main__":
    main()
