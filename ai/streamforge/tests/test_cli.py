"""Independent golden and adversarial checks against fresh offline CLI processes."""
from __future__ import annotations

import copy
import csv
import json
import os
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from ai.streamforge.tests.golden import (
    CATALOG_TEXT, COVERAGE, EVENTS_TEXT, HOLDOUT, MANIFEST, OUTSIDE, SUMMARY, TRAIN,
    assert_golden_bundle, expected_train_content_hash, install_fixture,
)


class Day167Test(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="streamforge-offline-")
        self.root = Path(self.temp.name)
        repo = Path(__file__).resolve().parents[3]
        shutil.copytree(repo / "ai" / "streamforge", self.root / "ai" / "streamforge",
                        ignore=shutil.ignore_patterns("__pycache__", "*.pyc"))
        self.fixture = self.root / "ai" / "streamforge" / "fixtures" / "basic"
        install_fixture(self.fixture)
        self.guard = self.root / "network_guard"
        self.guard.mkdir()
        (self.guard / "sitecustomize.py").write_text(
            "import socket, urllib.request\n"
            "def _blocked(*args, **kwargs): raise RuntimeError('offline guard blocked network API')\n"
            "socket.socket = _blocked\n"
            "socket.create_connection = _blocked\n"
            "urllib.request.urlopen = _blocked\n", encoding="utf-8")
        self.env = dict(os.environ)
        self.env["PYTHONPATH"] = str(self.guard) + os.pathsep + str(self.root)

    def tearDown(self):
        self.temp.cleanup()

    def cli(self):
        return [sys.executable, "-m", "ai.streamforge.prepare", "--fixture", "basic"]

    def fixture_bytes(self):
        return {name: (self.fixture / name).read_bytes()
                for name in ("manifest.json", "catalog.csv", "events.csv")}

    def run_cli(self):
        before = self.fixture_bytes()
        result = subprocess.run(self.cli(), cwd=self.root, env=self.env, capture_output=True,
                                check=False, timeout=20)
        self.assertEqual(before, self.fixture_bytes(), "CLI must not modify its copied input files")
        return result

    def read_events(self):
        with (self.fixture / "events.csv").open(newline="", encoding="utf-8") as stream:
            return list(csv.DictReader(stream))

    def write_events(self, rows):
        with (self.fixture / "events.csv").open("w", newline="", encoding="utf-8") as stream:
            writer = csv.DictWriter(stream, fieldnames=["event_id", "viewer_id", "media_id", "occurred_at_utc", "watch_seconds"], lineterminator="\n")
            writer.writeheader()
            writer.writerows(rows)

    def successful_bundle(self):
        result = self.run_cli()
        self.assertEqual(0, result.returncode, result.stderr.decode("utf-8", "replace"))
        self.assertTrue(result.stdout.endswith(b"\n"))
        return result, json.loads(result.stdout)

    def test_fresh_processes_match_independent_golden_and_preserve_copied_inputs(self):
        source_before = self.fixture_bytes()
        first, first_bundle = self.successful_bundle()
        second, second_bundle = self.successful_bundle()
        assert_golden_bundle(self, first_bundle, self.fixture)
        assert_golden_bundle(self, second_bundle, self.fixture)
        self.assertEqual(first.stdout, second.stdout)
        self.assertEqual(source_before, self.fixture_bytes())

    def test_shuffled_source_rows_preserve_semantics_but_change_raw_hash(self):
        before, before_bundle = self.successful_bundle()
        assert_golden_bundle(self, before_bundle, self.fixture)
        rows = self.read_events()
        rows.reverse()
        self.write_events(rows)
        after, after_bundle = self.successful_bundle()
        assert_golden_bundle(self, after_bundle, self.fixture)
        self.assertEqual(before_bundle["train"], after_bundle["train"])
        self.assertEqual(before_bundle["training"], after_bundle["training"])
        self.assertNotEqual(before_bundle["source_sha256"], after_bundle["source_sha256"])

    def test_valid_holdout_mutation_leaves_entire_training_result_unchanged(self):
        _, before = self.successful_bundle()
        rows = self.read_events()
        rows.append({"event_id": "e-05", "viewer_id": "synthetic-viewer-1", "media_id": "m1",
                     "occurred_at_utc": "2027-01-25T12:00:00Z", "watch_seconds": "7"})
        self.write_events(rows)
        _, after = self.successful_bundle()
        self.assertEqual(before["train"], after["train"])
        self.assertEqual(before["training"], after["training"], "all train counts and hash are unchanged")
        self.assertNotEqual(before["holdout"], after["holdout"])
        self.assertNotEqual(before["holdout_diagnostics"], after["holdout_diagnostics"])
        self.assertNotEqual(before["source_sha256"], after["source_sha256"])

    def test_cross_cutoff_conflicting_id_fails_before_partition_and_has_no_output(self):
        rows = self.read_events()
        rows.append({"event_id": "e-01", "viewer_id": "synthetic-viewer-1", "media_id": "m1",
                     "occurred_at_utc": "2027-01-20T00:00:00Z", "watch_seconds": "10"})
        self.write_events(rows)
        result = self.run_cli()
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(b"", result.stdout)
        self.assertIn(b"duplicate", result.stderr.lower())

    def test_duplicate_manifest_keys_at_any_depth_fail_before_success_output(self):
        path = self.fixture / "manifest.json"
        for raw in ('{"schema_version":1,"schema_version":2}',
                    '{"schema_version":1,"metadata":{"source":"a","source":"b"}}'):
            with self.subTest(raw=raw):
                path.write_text(raw, encoding="utf-8")
                result = self.run_cli()
                self.assertNotEqual(0, result.returncode)
                self.assertEqual(b"", result.stdout)
                self.assertIn(b"duplicate JSON object key", result.stderr)

    def test_invalid_holdout_and_outside_records_fail_without_partial_bundle(self):
        baseline = self.read_events()
        for row in (
            {"event_id": "bad-holdout", "viewer_id": "synthetic-viewer-1", "media_id": "missing",
             "occurred_at_utc": "2027-01-25T00:00:00Z", "watch_seconds": "1"},
            {"event_id": "bad-outside", "viewer_id": "synthetic-viewer-3", "media_id": "m2",
             "occurred_at_utc": "2027-02-01T00:00:00Z", "watch_seconds": "101"},
        ):
            with self.subTest(event=row["event_id"]):
                self.write_events(baseline + [row])
                result = self.run_cli()
                self.assertNotEqual(0, result.returncode)
                self.assertEqual(b"", result.stdout)
        invalid_replay = {"event_id": "bad-replay", "viewer_id": "synthetic-viewer-1", "media_id": "unknown",
                          "occurred_at_utc": "2027-01-02T00:00:00Z", "watch_seconds": "1"}
        self.write_events(baseline + [invalid_replay, invalid_replay])
        replay_result = self.run_cli()
        self.assertNotEqual(0, replay_result.returncode)
        self.assertEqual(b"", replay_result.stdout)
        self.assertIn(b"media", replay_result.stderr.lower())

    def test_wrong_cutoff_holdout_leakage_and_constant_or_canned_output_fail_oracle(self):
        _, valid = self.successful_bundle()
        wrong_cutoff = copy.deepcopy(valid)
        wrong_cutoff["train"] = wrong_cutoff["train"] + wrong_cutoff["holdout"]
        wrong_cutoff["holdout"] = []
        with self.assertRaisesRegex(AssertionError, "train rows/cutoff"):
            assert_golden_bundle(self, wrong_cutoff, self.fixture)

        holdout_leak = copy.deepcopy(valid)
        holdout_leak["train"] = holdout_leak["train"] + holdout_leak["holdout"]
        with self.assertRaisesRegex(AssertionError, "train rows/cutoff"):
            assert_golden_bundle(self, holdout_leak, self.fixture)

        constant_hash = copy.deepcopy(valid)
        constant_hash["training"]["content_sha256"] = "0" * 64
        with self.assertRaisesRegex(AssertionError, "content hash"):
            assert_golden_bundle(self, constant_hash, self.fixture)

        rows = self.read_events()
        rows[0]["watch_seconds"] = "11"
        self.write_events(rows)
        _, changed = self.successful_bundle()
        changed_train = [dict(TRAIN[0], watch_seconds=11), TRAIN[1]]
        changed_summary = dict(SUMMARY, media_watch_seconds={"m1": 11, "m2": 20})
        assert_golden_bundle(self, changed, self.fixture, train=changed_train, summary=changed_summary)
        with self.assertRaisesRegex(AssertionError, "train rows/cutoff"):
            assert_golden_bundle(self, valid, self.fixture, train=changed_train, summary=changed_summary)

    def test_python_network_api_guard_blocks_child_probes_and_allows_local_cli(self):
        for code in ("import socket; socket.socket()",
                     "import urllib.request; urllib.request.urlopen('https://example.invalid')"):
            probe = subprocess.run([sys.executable, "-c", code], cwd=self.root, env=self.env,
                                   capture_output=True, check=False, timeout=5)
            self.assertNotEqual(0, probe.returncode)
            self.assertIn(b"offline guard blocked", probe.stderr)
        self.successful_bundle()


if __name__ == "__main__":
    unittest.main()
