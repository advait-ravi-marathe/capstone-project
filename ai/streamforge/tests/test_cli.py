"""Actual subprocess acceptance for the local offline preparation CLI."""
from __future__ import annotations

import subprocess
import sys
import csv
import json
import os
import shutil
import tempfile
import unittest
from pathlib import Path


class Day167Test(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="streamforge-offline-")
        self.root = Path(self.temp.name)
        self.repo = Path(__file__).resolve().parents[3]
        shutil.copytree(self.repo / "ai" / "streamforge", self.root / "ai" / "streamforge",
                        ignore=shutil.ignore_patterns("__pycache__", "*.pyc"))
        self.fixture = self.root / "ai" / "streamforge" / "fixtures" / "basic"
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

    def run_cli(self):
        return subprocess.run(self.cli(), cwd=self.root, env=self.env, capture_output=True,
                              check=False, timeout=20)

    def read_events(self):
        with (self.fixture / "events.csv").open(newline="", encoding="utf-8") as stream:
            return list(csv.DictReader(stream))

    def write_events(self, rows):
        with (self.fixture / "events.csv").open("w", newline="", encoding="utf-8") as stream:
            writer = csv.DictWriter(stream, fieldnames=["event_id", "viewer_id", "media_id", "occurred_at_utc", "watch_seconds"], lineterminator="\n")
            writer.writeheader()
            writer.writerows(rows)

    def test_fresh_process_runs_match_and_original_inputs_are_unchanged(self):
        root = Path(__file__).resolve().parents[3]
        originals = {name: (self.repo / "ai/streamforge/fixtures/basic" / name).read_bytes()
                     for name in ("manifest.json", "catalog.csv", "events.csv")}
        first = self.run_cli()
        second = self.run_cli()
        self.assertEqual(0, first.returncode, first.stderr.decode("utf-8", "replace"))
        self.assertEqual(first.stdout, second.stdout)
        self.assertTrue(first.stdout.endswith(b"\n"))
        self.assertEqual(originals, {name: (self.repo / "ai/streamforge/fixtures/basic" / name).read_bytes()
                                    for name in originals})

    def test_shuffled_rows_preserve_semantic_train_payload_but_change_source_hash(self):
        before = self.run_cli()
        self.assertEqual(0, before.returncode, before.stderr.decode("utf-8", "replace"))
        rows = self.read_events()
        rows.reverse()
        self.write_events(rows)
        after = self.run_cli()
        self.assertEqual(0, after.returncode, after.stderr.decode("utf-8", "replace"))
        a, b = json.loads(before.stdout), json.loads(after.stdout)
        self.assertEqual(a["training"], b["training"])
        self.assertEqual(a["training"]["content_sha256"], b["training"]["content_sha256"])
        self.assertNotEqual(a["source_sha256"], b["source_sha256"])

    def test_valid_holdout_mutation_changes_provenance_not_training_content(self):
        before = self.run_cli()
        self.assertEqual(0, before.returncode, before.stderr.decode("utf-8", "replace"))
        rows = self.read_events()
        rows.append({"event_id": "sf-e-holdout-extra", "viewer_id": "synthetic-viewer-099",
                     "media_id": "sf-m-030", "occurred_at_utc": "2027-01-25T12:00:00Z", "watch_seconds": "42"})
        self.write_events(rows)
        after = self.run_cli()
        self.assertEqual(0, after.returncode, after.stderr.decode("utf-8", "replace"))
        a, b = json.loads(before.stdout), json.loads(after.stdout)
        self.assertEqual(a["train"], b["train"])
        self.assertEqual(a["training"]["content_sha256"], b["training"]["content_sha256"])
        self.assertNotEqual(a["source_sha256"], b["source_sha256"])
        self.assertNotEqual(a["holdout_diagnostics"], b["holdout_diagnostics"])

    def test_conflicting_duplicate_has_no_partial_success_json(self):
        rows = self.read_events()
        conflict = dict(rows[0], watch_seconds=str(int(rows[0]["watch_seconds"]) + 1))
        rows.append(conflict)
        self.write_events(rows)
        result = self.run_cli()
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(b"", result.stdout)
        self.assertIn(b"duplicate", result.stderr.lower())

    def test_python_network_api_guard_blocks_child_probe_and_allows_local_cli(self):
        probe = subprocess.run([sys.executable, "-c", "import socket; socket.socket()"], cwd=self.root,
                               env=self.env, capture_output=True, check=False, timeout=5)
        self.assertNotEqual(0, probe.returncode)
        self.assertIn(b"offline guard blocked", probe.stderr)
        url_probe = subprocess.run([sys.executable, "-c", "import urllib.request; urllib.request.urlopen('https://example.invalid')"],
                                   cwd=self.root, env=self.env, capture_output=True, check=False, timeout=5)
        self.assertNotEqual(0, url_probe.returncode)
        self.assertIn(b"offline guard blocked", url_probe.stderr)
        result = self.run_cli()
        self.assertEqual(0, result.returncode, result.stderr.decode("utf-8", "replace"))


if __name__ == "__main__":
    unittest.main()
