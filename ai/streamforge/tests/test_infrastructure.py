"""Default-green checks for the supplied StreamForge scaffold, not learner mastery."""
from __future__ import annotations

import csv
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from ai.streamforge.fixture_io import canonical_json_bytes, read_fixture_file, strict_json_loads
from ai.streamforge.fixture_registry import FIXTURES, fixture_directory
from ai.streamforge.limits import MAX_FIELD_BYTES, MAX_INPUT_BYTES, MAX_ROWS_PER_INPUT


ROOT = Path(__file__).resolve().parents[1]


class StreamForgeInfrastructureTest(unittest.TestCase):
    def test_registered_fixture_is_bounded_synthetic_and_hand_authored_text(self):
        self.assertEqual({"basic"}, set(FIXTURES))
        directory = fixture_directory("basic")
        manifest = json.loads(read_fixture_file(directory, "manifest.json"))
        self.assertEqual("synthetic_hand_authored", manifest["source_kind"])
        self.assertEqual("invented_text_only", manifest["media_source"])
        self.assertEqual("invented_identifiers", manifest["viewer_source"])
        self.assertEqual("2027-01-20T00:00:00Z", manifest["cutoff_utc"])
        catalog = read_fixture_file(directory, manifest["catalog_file"])
        events = read_fixture_file(directory, manifest["events_file"])
        self.assertLessEqual(len(catalog), MAX_INPUT_BYTES)
        self.assertLessEqual(len(events), MAX_INPUT_BYTES)
        catalog_rows = list(csv.DictReader(catalog.decode("utf-8").splitlines()))
        event_rows = list(csv.DictReader(events.decode("utf-8").splitlines()))
        self.assertEqual(30, len(catalog_rows))
        self.assertEqual(200, len(event_rows))
        self.assertEqual(30, len({row["media_id"] for row in catalog_rows}))
        self.assertEqual(200, len({row["event_id"] for row in event_rows}))
        self.assertTrue(all(len(value.encode("utf-8")) <= MAX_FIELD_BYTES for row in catalog_rows + event_rows for value in row.values()))
        self.assertLessEqual(len(catalog_rows), MAX_ROWS_PER_INPUT)
        self.assertLessEqual(len(event_rows), MAX_ROWS_PER_INPUT)
        self.assertFalse(any(any(token in value.lower() for token in ("http://", "https://", ".mp4", ".mp3"))
                             for row in catalog_rows for value in row.values()))

    def test_fixture_registry_rejects_arbitrary_names_and_paths(self):
        with self.assertRaises(ValueError):
            fixture_directory("../../etc")
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            (root / "ok.txt").write_text("ok", encoding="utf-8")
            self.assertEqual(b"ok", read_fixture_file(root, "ok.txt"))
            with self.assertRaises(ValueError):
                read_fixture_file(root, "../outside.txt")

    def test_canonical_json_is_sorted_utf8_and_rejects_nonfinite_numbers(self):
        self.assertEqual('{"a":1,"z":"é"}\n'.encode("utf-8"), canonical_json_bytes({"z": "é", "a": 1}))
        with self.assertRaises(ValueError):
            canonical_json_bytes({"unsafe": float("nan")})

    def test_strict_json_rejects_duplicate_members_at_any_depth(self):
        self.assertEqual({"ok": {"count": 1}}, strict_json_loads('{"ok":{"count":1}}'))
        for text in ('{"schema_version":1,"schema_version":2}',
                     '{"outer":{"name":"a","name":"b"}}'):
            with self.subTest(text=text), self.assertRaisesRegex(ValueError, "duplicate JSON object key"):
                strict_json_loads(text)

    def test_cli_scope_is_registered_fixture_only_and_todo_error_has_no_success_document(self):
        bad = subprocess.run([sys.executable, "-m", "ai.streamforge.prepare", "--fixture", "unknown"],
                             cwd=Path(__file__).resolve().parents[3], capture_output=True, check=False, timeout=10)
        self.assertNotEqual(0, bad.returncode)
        self.assertEqual(b"", bad.stdout)
        valid = subprocess.run([sys.executable, "-m", "ai.streamforge.prepare", "--fixture", "basic"],
                               cwd=Path(__file__).resolve().parents[3], capture_output=True, check=False, timeout=10)
        self.assertNotEqual(0, valid.returncode, "starter must remain red until the learner implements Day 166")
        self.assertEqual(b"", valid.stdout, "no incomplete success JSON may be emitted")
        self.assertIn(b"Day 166 learner TODO", valid.stderr)

    def test_day155_python_readiness_bridge_is_runnable(self):
        bridge = subprocess.run([sys.executable, "-m", "ai.streamforge.readiness_bridge"],
                                cwd=Path(__file__).resolve().parents[3], capture_output=True,
                                check=False, timeout=5)
        self.assertEqual(0, bridge.returncode, bridge.stderr.decode("utf-8", "replace"))
        self.assertEqual(b"Python readiness bridge passed\n", bridge.stdout)

    def test_each_planned_learner_day_has_a_named_acceptance_class(self):
        source = (ROOT / "tests" / "test_day_contracts.py").read_text(encoding="utf-8")
        for day in (155, 156, 157, 158, 159, 160, 162, 163, 164, 165, 166):
            with self.subTest(day=day):
                self.assertIn(f"class Day{day}Test", source)
        self.assertIn("class Day161RecoveryInfrastructureTest", source)
        self.assertIn("class Day168RecoveryInfrastructureTest", source)
        cli_source = (ROOT / "tests" / "test_cli.py").read_text(encoding="utf-8")
        self.assertIn("class Day167Test", cli_source)
        self.assertIn("holdout", cli_source.lower())
        self.assertIn("network", cli_source.lower())
        self.assertNotIn("import pandas", source)
        self.assertNotIn("sklearn", source)

    def test_student_scope_does_not_modify_cap44_or_load_external_data(self):
        self.assertFalse((ROOT / "ranking.py").exists())
        self.assertFalse((ROOT / "download.py").exists())
        self.assertFalse((ROOT / "model.py").exists())


if __name__ == "__main__":
    unittest.main()
