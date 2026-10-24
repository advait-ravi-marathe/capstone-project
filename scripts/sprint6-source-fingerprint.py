#!/usr/bin/env python3
"""Hash current source paths and contents for the bounded Sprint 6 archive."""

from hashlib import sha256
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
GENERATED = ("target/", "frontend/dist-local/")


def paths(*args: str) -> set[bytes]:
    result = subprocess.run(["git", "-C", str(ROOT), "ls-files", "-z", *args],
                            check=True, stdout=subprocess.PIPE)
    return {path for path in result.stdout.split(b"\0") if path}


digest = sha256()
for raw in sorted(paths() | paths("--others", "--exclude-standard")):
    relative = raw.decode("utf-8", "surrogateescape")
    if relative.startswith(GENERATED):
        continue
    path = ROOT / relative
    digest.update(raw + b"\0")
    if not path.exists() and not path.is_symlink():
        digest.update(b"DELETED\0")
    elif path.is_symlink():
        digest.update(b"SYMLINK\0" + path.readlink().as_posix().encode())
    elif path.is_file():
        digest.update(b"FILE\0" + path.read_bytes())
    else:
        digest.update(b"OTHER\0")
    digest.update(b"\0")

print(digest.hexdigest())
