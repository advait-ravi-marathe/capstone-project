"""Fresh-process CLI acceptance using a copied synthetic fixture and oracle."""
from __future__ import annotations

import csv
import json
import os
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from ai.streamforge.tests.golden import install_fixture


class EvaluationCliFixture(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="streamforge-eval-offline-")
        self.root = Path(self.temp.name)
        repo = Path(__file__).resolve().parents[3]
        shutil.copytree(repo / "ai" / "streamforge", self.root / "ai" / "streamforge",
                        ignore=shutil.ignore_patterns("__pycache__", "*.pyc"))
        self.fixture = self.root / "ai" / "streamforge" / "fixtures" / "basic"
        install_fixture(self.fixture)
        with (self.fixture / "catalog.csv").open("a", encoding="utf-8", newline="") as stream:
            stream.write("m4,Hidden Harbor,comedy,100,2026-01-01T00:00:00Z,2028-01-01T00:00:00Z\n")
        guard = self.root / "network_guard"
        guard.mkdir()
        (guard / "sitecustomize.py").write_text(
            "import socket, urllib.request\n"
            "def _blocked(*args, **kwargs): raise RuntimeError('offline guard blocked network API')\n"
            "socket.socket = _blocked\n"
            "socket.create_connection = _blocked\n"
            "urllib.request.urlopen = _blocked\n", encoding="utf-8")
        self.env = dict(os.environ)
        self.env["PYTHONPATH"] = str(guard) + os.pathsep + str(self.root)

    def tearDown(self):
        self.temp.cleanup()

    def _input_bytes(self):
        return {name: (self.fixture / name).read_bytes()
                for name in ("manifest.json", "catalog.csv", "events.csv")}

    def _run(self):
        before = self._input_bytes()
        result = subprocess.run([sys.executable, "-m", "ai.streamforge.evaluate", "--fixture", "basic"],
                                cwd=self.root, env=self.env, capture_output=True,
                                check=False, timeout=20)
        self.assertEqual(before, self._input_bytes(), "evaluation must not modify copied fixture inputs")
        return result

    def _successful_report(self):
        result = self._run()
        self.assertEqual(0, result.returncode, result.stderr.decode("utf-8", "replace"))
        self.assertTrue(result.stdout.endswith(b"\n"))
        report = json.loads(result.stdout, parse_constant=lambda token: (_ for _ in ()).throw(ValueError(token)))
        return result, report

    def _read_events(self):
        with (self.fixture / "events.csv").open(newline="", encoding="utf-8") as stream:
            return list(csv.DictReader(stream))

    def _write_events(self, rows):
        with (self.fixture / "events.csv").open("w", newline="", encoding="utf-8") as stream:
            writer = csv.DictWriter(stream, fieldnames=["event_id", "viewer_id", "media_id",
                                                       "occurred_at_utc", "watch_seconds"],
                                    lineterminator="\n")
            writer.writeheader()
            writer.writerows(rows)


class Day179Test(EvaluationCliFixture):
    def test_cli_runs_prior_preparation_and_emits_hand_calculated_report(self):
        result, report = self._successful_report()
        self.assertEqual(1, report["schema_version"])
        self.assertEqual("streamforge-cutoff-popularity-v1", report["protocol"]["protocol_id"])
        self.assertEqual(["m1", "m2", "m3", "m4"], report["prediction"]["candidate_ids"])
        self.assertEqual({"m1": 1, "m2": 1, "m3": 0, "m4": 0}, report["prediction"]["support_by_media"])
        self.assertEqual(["m1", "m2", "m3"], report["prediction"]["recommendation_ids"])
        self.assertEqual("no_positive_holdout", report["baselines"]["popularity"]["status"])
        self.assertIsNone(report["baselines"]["popularity"]["precision_at_3"])
        self.assertIsNone(report["baselines"]["popularity"]["recall_at_3"])
        self.assertEqual(1, report["baselines"]["popularity"]["counts"]["zero_positive_viewers"])
        self.assertEqual(b"", result.stderr)

    def test_conflicting_duplicate_across_cutoff_fails_before_any_success_document(self):
        rows = self._read_events()
        rows.append({"event_id": "e-01", "viewer_id": "synthetic-viewer-1", "media_id": "m1",
                     "occurred_at_utc": "2027-01-20T00:00:00Z", "watch_seconds": "10"})
        self._write_events(rows)
        result = self._run()
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(b"", result.stdout)
        self.assertIn(b"duplicate", result.stderr.lower())


class Day181Test(EvaluationCliFixture):
    def test_fresh_processes_are_byte_deterministic_and_inputs_stay_unchanged(self):
        first, report = self._successful_report()
        second, second_report = self._successful_report()
        self.assertEqual(first.stdout, second.stdout)
        self.assertEqual(report, second_report)

    def test_training_change_changes_prediction_while_holdout_change_does_not(self):
        _, initial = self._successful_report()
        base_prediction = initial["prediction"]
        rows = self._read_events()
        rows.extend([
            {"event_id": "e-05", "viewer_id": "synthetic-viewer-4", "media_id": "m4",
             "occurred_at_utc": "2027-01-03T00:00:00Z", "watch_seconds": "5"},
            {"event_id": "e-06", "viewer_id": "synthetic-viewer-5", "media_id": "m4",
             "occurred_at_utc": "2027-01-04T00:00:00Z", "watch_seconds": "5"},
            {"event_id": "e-07", "viewer_id": "synthetic-viewer-6", "media_id": "m4",
             "occurred_at_utc": "2027-01-05T00:00:00Z", "watch_seconds": "5"},
        ])
        self._write_events(rows)
        _, trained = self._successful_report()
        self.assertNotEqual(base_prediction["fingerprint"], trained["prediction"]["fingerprint"])
        self.assertEqual(3, trained["prediction"]["support_by_media"]["m4"])
        self.assertEqual(["m4", "m1", "m2"], trained["prediction"]["recommendation_ids"])
        rows = self._read_events()
        rows.append({"event_id": "e-08", "viewer_id": "synthetic-viewer-2", "media_id": "m3",
                     "occurred_at_utc": "2027-01-25T00:00:00Z", "watch_seconds": "7"})
        self._write_events(rows)
        _, heldout = self._successful_report()
        self.assertEqual(trained["prediction"]["fingerprint"], heldout["prediction"]["fingerprint"])
        self.assertNotEqual(trained["baselines"], heldout["baselines"])

    def test_invalid_holdout_fails_without_partial_report(self):
        rows = self._read_events()
        rows.append({"event_id": "bad", "viewer_id": "synthetic-viewer-1", "media_id": "missing",
                     "occurred_at_utc": "2027-01-25T00:00:00Z", "watch_seconds": "7"})
        self._write_events(rows)
        result = self._run()
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(b"", result.stdout)


if __name__ == "__main__":
    unittest.main()
