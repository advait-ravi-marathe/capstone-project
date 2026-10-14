from __future__ import annotations

import json
import tempfile
import unittest
from unittest.mock import patch
from pathlib import Path

from ai.coverstory.cap_53 import LoadError, load_wording

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-53/corpus"


class CAP53AcceptanceTest(unittest.TestCase):
    def test_allowlisted_files_sort_by_name_and_preserve_labels_and_text(self) -> None:
        loaded = load_wording(FIXTURE)
        self.assertEqual([("source-A", "v2"), ("source-B", "v1")], [(x.source_id, x.version) for x in loaded])
        self.assertEqual("A public synthetic waiting period.", loaded[0].text)
        self.assertEqual(("SYNTHETIC", "PUBLIC"), (loaded[0].evidence_status, loaded[0].classification))

    def test_empty_allowlist_is_empty(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            Path(tmp, "manifest.json").write_text('{"files":[]}', encoding="utf-8")
            self.assertEqual([], load_wording(Path(tmp)))

    def test_malformed_or_missing_version_fails_atomically_with_filename_code(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp); (root / "manifest.json").write_text('{"files":["a.json","b.json"]}', encoding="utf-8")
            (root / "a.json").write_text('{"source_id":"A","version":"v1","text":"ok","evidenceStatus":"SYNTHETIC","classification":"PUBLIC"}', encoding="utf-8")
            (root / "b.json").write_text('{"source_id":"B","text":"missing version"}', encoding="utf-8")
            with self.assertRaises(LoadError) as caught: load_wording(root)
            self.assertEqual(("b.json", "MISSING_VERSION"), (caught.exception.filename, caught.exception.code))

    def test_path_traversal_is_rejected_before_any_outside_read_and_duplicates_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            parent = Path(tmp); root = parent / "corpus"; root.mkdir()
            outside = parent / "outside.json"
            outside.write_text('{"source_id":"SECRET","version":"v1","text":"must not be opened"}', encoding="utf-8")
            (root / "manifest.json").write_text('{"files":["../outside.json"]}', encoding="utf-8")
            opened_outside: list[Path] = []
            original_open = Path.open
            def guarded_open(path: Path, *args, **kwargs):
                if path.resolve() == outside.resolve():
                    opened_outside.append(path)
                    raise AssertionError("path traversal read attempted before validation")
                return original_open(path, *args, **kwargs)
            with patch.object(Path, "open", guarded_open):
                with self.assertRaises(LoadError) as caught: load_wording(root)
            self.assertEqual("INVALID_PATH", caught.exception.code)
            self.assertEqual([], opened_outside)
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp); (root / "manifest.json").write_text('{"files":["a.json","b.json"]}', encoding="utf-8")
            row = '{"source_id":"A","version":"v1","text":"ok","evidenceStatus":"SYNTHETIC","classification":"PUBLIC"}'
            (root / "a.json").write_text(row, encoding="utf-8"); (root / "b.json").write_text(row, encoding="utf-8")
            with self.assertRaises(LoadError) as caught: load_wording(root)
            self.assertEqual("DUPLICATE_SOURCE_VERSION", caught.exception.code)


if __name__ == "__main__": unittest.main()
