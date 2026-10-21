"""Bounded local fixture reads supplied by the instructor scaffold."""
from pathlib import Path

from .limits import MAX_INPUT_BYTES


def read_fixture_file(directory: Path, filename: str) -> bytes:
    """Read a named child file under the selected fixture with a hard byte limit."""
    if not filename or Path(filename).name != filename or filename in {".", ".."}:
        raise ValueError("fixture filenames must be simple child names")
    root = directory.resolve(strict=True)
    path = (root / filename).resolve(strict=True)
    if path.parent != root or not path.is_file():
        raise ValueError("fixture path escapes the registered fixture")
    with path.open("rb") as stream:
        payload = stream.read(MAX_INPUT_BYTES + 1)
    if len(payload) > MAX_INPUT_BYTES:
        raise ValueError("fixture input exceeds byte limit")
    return payload


def canonical_json_bytes(document: object) -> bytes:
    """Canonical JSON serialization with no non-finite values."""
    import json

    return (json.dumps(document, ensure_ascii=False, allow_nan=False,
                        sort_keys=True, separators=(",", ":")) + "\n").encode("utf-8")


def strict_json_loads(text: str) -> object:
    """Parse JSON while rejecting duplicate member names at every object depth."""
    import json

    def unique_object(pairs):
        result = {}
        for key, value in pairs:
            if key in result:
                raise ValueError(f"duplicate JSON object key: {key}")
            result[key] = value
        return result

    return json.loads(text, object_pairs_hook=unique_object)
