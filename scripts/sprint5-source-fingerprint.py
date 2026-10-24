#!/usr/bin/env python3
"""Hash current source file names and contents for the disposable Sprint 5 receipt."""

from hashlib import sha256
from pathlib import Path
import subprocess


ROOT = Path(__file__).resolve().parents[1]
GENERATED = ("target/", "frontend/dist-local/")


def git_paths(*args: str) -> list[bytes]:
    result = subprocess.run(
        ["git", "-C", str(ROOT), "ls-files", "-z", *args],
        check=True,
        stdout=subprocess.PIPE,
    )
    return [path for path in result.stdout.split(b"\0") if path]


tracked = set(git_paths())
untracked = set(git_paths("--others", "--exclude-standard"))
paths = tracked | untracked
digest = sha256()
for raw in sorted(paths):
    relative = raw.decode("utf-8", "surrogateescape")
    if relative.startswith(GENERATED):
        continue
    path = ROOT / relative
    digest.update(raw)
    digest.update(b"\0")
    if not path.exists() and not path.is_symlink():
        digest.update(b"DELETED\0")
    elif path.is_symlink():
        digest.update(b"SYMLINK\0")
        digest.update(path.readlink().as_posix().encode())
    elif path.is_file():
        digest.update(b"FILE\0")
        digest.update(path.read_bytes())
    else:
        digest.update(b"OTHER\0")
    digest.update(b"\0")

print(digest.hexdigest())
