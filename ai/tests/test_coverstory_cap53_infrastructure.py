from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]


class CAP53InfrastructureTest(unittest.TestCase):
    def test_allowlist_and_only_direct_child_synthetic_files_are_present(self) -> None:
        root = ROOT / "fixtures/coverstory/CAP-53/corpus"
        manifest = json.loads((root / "manifest.json").read_text(encoding="utf-8"))
        self.assertEqual(["b.json", "a.json"], manifest["files"])
        for name in manifest["files"]:
            path = root / name
            row = json.loads(path.read_text(encoding="utf-8"))
            self.assertEqual(root.resolve(), path.resolve().parent)
            self.assertEqual("SYNTHETIC", row["evidenceStatus"])
            self.assertEqual("PUBLIC", row["classification"])

    def test_instructor_reference_load_is_deterministic_resettable_and_ignores_unlisted_files(self) -> None:
        root = ROOT / "fixtures/coverstory/CAP-53/corpus"
        expected = [("source-A", "v2"), ("source-B", "v1")]
        self.assertEqual(expected, reference_load(root))
        self.assertEqual(expected, reference_load(root), "the same immutable fixture may be replayed from a clean read")
        with tempfile.TemporaryDirectory() as directory:
            copy = Path(directory)
            (copy / "manifest.json").write_bytes((root / "manifest.json").read_bytes())
            for name in ("a.json", "b.json"):
                (copy / name).write_bytes((root / name).read_bytes())
            (copy / "not-listed.json").write_text(
                '{"source_id":"unlisted","version":"v1","text":"ignored",'
                '"evidenceStatus":"SYNTHETIC","classification":"PUBLIC"}', encoding="utf-8")
            self.assertEqual(expected, reference_load(copy))

    def test_instructor_reference_rejects_traversal_duplicates_invalid_visibility_and_partial_return(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory); root = parent / "corpus"; root.mkdir()
            outside = parent / "outside.json"
            outside.write_text('{"source_id":"secret","version":"v1","text":"outside",'
                               '"evidenceStatus":"SYNTHETIC","classification":"PUBLIC"}', encoding="utf-8")
            (root / "manifest.json").write_text('{"files":["../outside.json"]}', encoding="utf-8")
            original_open = Path.open
            outside_reads = []
            def guarded_open(path, *args, **kwargs):
                if path.resolve() == outside.resolve():
                    outside_reads.append(path)
                    raise AssertionError("reference attempted an outside read")
                return original_open(path, *args, **kwargs)
            with patch.object(Path, "open", guarded_open):
                with self.assertRaisesRegex(ValueError, "INVALID_PATH"):
                    reference_load(root)
            self.assertEqual([], outside_reads)

        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            row = ('{"source_id":"source-A","version":"v1","text":"synthetic",'
                   '"evidenceStatus":"SYNTHETIC","classification":"PUBLIC"}')
            (root / "manifest.json").write_text('{"files":["a.json","b.json"]}', encoding="utf-8")
            (root / "a.json").write_text(row, encoding="utf-8")
            (root / "b.json").write_text(row, encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "DUPLICATE_SOURCE_VERSION"):
                reference_load(root)

        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "manifest.json").write_text('{"files":["a.json","b.json"]}', encoding="utf-8")
            (root / "a.json").write_text(row, encoding="utf-8")
            (root / "b.json").write_text('{"source_id":"source-B","version":"v1",'
                                           '"text":"not public","evidenceStatus":"SYNTHETIC",'
                                           '"classification":"RESTRICTED"}', encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "NON_PUBLIC_OR_NON_SYNTHETIC"):
                reference_load(root)


def reference_load(root: Path) -> list[tuple[str, str]]:
    """Independent test-only safe-loader oracle; it does not call the learner TODO."""
    resolved_root = root.resolve(strict=True)
    manifest = json.loads((resolved_root / "manifest.json").read_text(encoding="utf-8"))
    files = manifest.get("files")
    if not isinstance(files, list) or any(not isinstance(name, str) for name in files):
        raise ValueError("INVALID_MANIFEST")
    if len(files) != len(set(files)):
        raise ValueError("DUPLICATE_FILE")
    rows = []
    seen = set()
    for name in sorted(files):
        if (not name.endswith(".json") or name.startswith(".") or Path(name).name != name
                or name in {"", ".", ".."}):
            raise ValueError("INVALID_PATH")
        path = resolved_root / name
        if path.is_symlink() or path.resolve(strict=True).parent != resolved_root:
            raise ValueError("INVALID_PATH")
        row = json.loads(path.read_text(encoding="utf-8"))
        if not isinstance(row, dict):
            raise ValueError("INVALID_DOCUMENT")
        for field in ("source_id", "version", "text"):
            if not isinstance(row.get(field), str) or not row[field].strip():
                raise ValueError("MISSING_" + field.upper())
        if row.get("evidenceStatus") != "SYNTHETIC" or row.get("classification") != "PUBLIC":
            raise ValueError("NON_PUBLIC_OR_NON_SYNTHETIC")
        identity = (row["source_id"], row["version"])
        if identity in seen:
            raise ValueError("DUPLICATE_SOURCE_VERSION")
        seen.add(identity)
        rows.append(identity)
    return rows


if __name__ == "__main__": unittest.main()
