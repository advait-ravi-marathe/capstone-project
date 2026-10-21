"""Actual fresh-process CLI checks with a hand-computed, mutable fixture."""
from __future__ import annotations

import csv
import hashlib
import json
import os
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from ai.streamforge.tests.golden import install_fixture, expected_train_content_hash


CATALOG = [
    ("m1", "Amber Field"), ("m2", "Blue Window"),
    ("m3", "Copper Kite"), ("m4", "Distant Orchard"),
    ("m5", "Evening Map"),
]
CATALOG_HEADER = ["media_id", "title", "genre", "duration_s", "available_from_utc", "available_to_utc"]
EVENT_HEADER = ["event_id", "viewer_id", "media_id", "occurred_at_utc", "watch_seconds"]
CATALOG_ROWS = [
    [mid, title, "drama", "100",
     "2027-01-21T00:00:00Z" if mid == "m5" else "2026-01-01T00:00:00Z",
     "2028-01-01T00:00:00Z"]
    for mid, title in CATALOG
]
EVENT_ROWS = [
    ["t1", "v1", "m4", "2027-01-02T00:00:00Z", "10"],
    ["t2", "v1", "m4", "2027-01-03T00:00:00Z", "10"],
    ["t3", "v2", "m4", "2027-01-04T00:00:00Z", "10"],
    ["t4", "v3", "m4", "2027-01-05T00:00:00Z", "10"],
    ["t5", "v1", "m1", "2027-01-06T00:00:00Z", "10"],
    ["t6", "v2", "m2", "2027-01-07T00:00:00Z", "10"],
    ["h1", "cold", "m4", "2027-01-20T00:00:00Z", "10"],
    ["h2", "cold", "m5", "2027-01-25T00:00:00Z", "10"],
    ["h3", "v1", "m3", "2027-01-21T00:00:00Z", "10"],
    ["h4", "zero", "m3", "2027-01-22T00:00:00Z", "0"],
    ["o1", "outside", "m1", "2027-02-01T00:00:00Z", "10"],
]


def _csv_bytes(header, rows):
    from io import StringIO
    stream = StringIO(newline="")
    writer = csv.writer(stream, lineterminator="\n")
    writer.writerow(header)
    writer.writerows(rows)
    return stream.getvalue().encode("utf-8")


def _write_rows(path: Path, header, rows):
    path.write_bytes(_csv_bytes(header, rows))


def _score(recommendations, truths):
    viewers = (set(truths) - {viewer for viewer, items in truths.items() if not items})
    precision, recall = [], []
    unreachable = 0
    for viewer in sorted(viewers):
        target = truths[viewer]
        hits = len(set(recommendations) & target)
        precision.append((hits, 3))
        recall.append((hits, len(target)))
        unreachable += len(target - set(("m1", "m2", "m3", "m4")))
    from fractions import Fraction
    return (sum((Fraction(a, b) for a, b in precision), Fraction()) / len(precision),
            sum((Fraction(a, b) for a, b in recall), Fraction()) / len(recall), unreachable)


class EvaluationCliFixture(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="streamforge-eval-offline-")
        self.root = Path(self.temp.name)
        repo = Path(__file__).resolve().parents[3]
        shutil.copytree(repo / "ai" / "streamforge", self.root / "ai" / "streamforge",
                        ignore=shutil.ignore_patterns("__pycache__", "*.pyc"))
        self.fixture = self.root / "ai" / "streamforge" / "fixtures" / "basic"
        install_fixture(self.fixture)
        _write_rows(self.fixture / "catalog.csv", CATALOG_HEADER, CATALOG_ROWS)
        _write_rows(self.fixture / "events.csv", EVENT_HEADER, EVENT_ROWS)
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
        self.assertEqual(before, self._input_bytes(), "the actual copied CLI inputs must remain byte-identical")
        return result

    def _successful_report(self):
        result = self._run()
        self.assertEqual(0, result.returncode, result.stderr.decode("utf-8", "replace"))
        self.assertTrue(result.stdout.endswith(b"\n"))
        report = json.loads(result.stdout, parse_constant=lambda token: (_ for _ in ()).throw(ValueError(token)))
        return result, report

    def _read_rows(self, name, header):
        with (self.fixture / name).open(newline="", encoding="utf-8") as stream:
            return list(csv.DictReader(stream, fieldnames=None))

    def _rewrite_events(self, rows):
        normalized = [[row[key] for key in EVENT_HEADER] for row in rows]
        _write_rows(self.fixture / "events.csv", EVENT_HEADER, normalized)

    @staticmethod
    def _expected_report(prediction, popularity_metrics, reference_metrics, *,
                         unreachable, source_hashes, duplicate_count=0):
        # Ratios below come from literal per-viewer sets, not production helpers.
        expected_candidates = list(prediction["candidate_ids"])
        payload = {
            "protocol_id": "streamforge-cutoff-popularity-v1",
            "cutoff_utc": "2027-01-20T00:00:00Z", "k": 3,
            "candidate_ids": expected_candidates,
            "support_by_media": prediction["support_by_media"],
            "recommendation_ids": prediction["recommendation_ids"],
            "fallback_status": prediction["fallback_status"],
        }
        fingerprint = hashlib.sha256(json.dumps(payload, ensure_ascii=False, allow_nan=False,
            sort_keys=True, separators=(",", ":")).encode("utf-8") + b"\n").hexdigest()
        return {
            "schema_version": 1,
            "protocol": {
                "protocol_id": "streamforge-cutoff-popularity-v1", "k": 3,
                "positive_rule": "watch_seconds > 0",
                "support": "distinct positive training viewers per media",
                "candidate_rule": "available_from_utc <= cutoff_utc < available_to_utc",
                "cutoff_utc": "2027-01-20T00:00:00Z",
                "ranking": "support descending, exact media_id ascending",
                "scope": "one global list for all viewers; no seen-item removal or event-time reranking",
                "precision_denominator": 3,
                "recall_targets": "all distinct positive holdout media IDs, including unreachable items",
                "aggregation": "unweighted macro over positive-target viewers",
            },
            "prediction": {**prediction, "fingerprint": fingerprint},
            "baselines": {
                "popularity": {"recommendation_ids": prediction["recommendation_ids"],
                    "status": "available", "precision_at_3": popularity_metrics[0],
                    "recall_at_3": popularity_metrics[1], "counts": {
                        "all_holdout_viewers": 3, "evaluated_viewers": 2, "zero_positive_viewers": 1,
                        "positive_viewer_media_pairs": 3,
                        "candidate_unreachable_positive_pairs": unreachable,
                        "training_viewer_count": 3, "cold_start_evaluated_viewers": 1,
                        "candidate_count": len(expected_candidates), "returned_count": len(prediction["recommendation_ids"])}},
                "id_order_reference": {"recommendation_ids": ["m1", "m2", "m3"] if len(expected_candidates) >= 3
                    else expected_candidates, "status": "available", "precision_at_3": reference_metrics[0],
                    "recall_at_3": reference_metrics[1], "counts": {
                        "all_holdout_viewers": 3, "evaluated_viewers": 2, "zero_positive_viewers": 1,
                        "positive_viewer_media_pairs": 3,
                        "candidate_unreachable_positive_pairs": unreachable,
                        "training_viewer_count": 3, "cold_start_evaluated_viewers": 1,
                        "candidate_count": len(expected_candidates), "returned_count": min(3, len(expected_candidates))}},
                "signed_deltas": {"precision_at_3": _fraction_delta(popularity_metrics[0], reference_metrics[0]),
                                  "recall_at_3": _fraction_delta(popularity_metrics[1], reference_metrics[1])},
                "status": "available",
            },
            "preparation": {
                "schema_version": 1, "data_version": 1, "fixture_id": "basic",
                "window_start_utc": "2027-01-01T00:00:00Z", "cutoff_utc": "2027-01-20T00:00:00Z",
                "window_end_utc": "2027-02-01T00:00:00Z",
                "preparation_status": {"training": "available", "holdout": "available"},
                "training": {"status": "available", "event_count": 6, "distinct_viewer_count": 3,
                    "media_event_counts": {"m4": 4, "m1": 1, "m2": 1},
                    "media_watch_seconds": {"m4": 40, "m1": 10, "m2": 10},
                    "content_sha256": prediction["_train_hash"]},
                "holdout_diagnostics": {"status": "available", "viewer_item_seen_in_train": 0,
                    "viewer_seen_item_unseen": 1, "viewer_unseen_item_seen": 1, "viewer_item_unseen": 2},
                "source_sha256": source_hashes,
                "duplicate_events_collapsed": duplicate_count,
                "manifest_sha256": source_hashes["manifest.json"],
                "catalog_count": 5,
                "partition_counts": {"train": 6, "holdout": 4, "outside_window": 1},
            },
            "limits": [
                "Synthetic observed-positive overlap only; no reliable negative labels or impressions.",
                "The catalog schedule is assumed known at cutoff; no historical catalog snapshot is demonstrated.",
                "One static global list is used over the holdout horizon; no per-event availability reranking.",
                "Metrics do not establish preference, unbiased real-user quality, causal lift, fairness, or business success.",
                "Content hashes identify bytes and do not establish rights, authenticity, or quality.",
            ],
        }


def _fraction_delta(left, right):
    from fractions import Fraction
    if left is None or right is None:
        return None
    delta = Fraction(left) - Fraction(right)
    return f"{delta.numerator}/{delta.denominator}"


class Day179Test(EvaluationCliFixture):
    maxDiff = None
    def test_cli_uses_prior_preparation_and_matches_full_hand_calculated_report(self):
        _, report = self._successful_report()
        prediction = {"protocol_id": "streamforge-cutoff-popularity-v1",
            "cutoff_utc": "2027-01-20T00:00:00Z", "k": 3,
            "candidate_ids": ["m1", "m2", "m3", "m4"],
            "support_by_media": {"m1": 1, "m2": 1, "m3": 0, "m4": 3},
            "recommendation_ids": ["m4", "m1", "m2"], "fallback_status": "popularity_ranked",
            "_train_hash": expected_train_content_hash(
                [{"event_id": row[0], "viewer_id": row[1], "media_id": row[2],
                 "occurred_at_utc": row[3], "watch_seconds": int(row[4])}
                 for row in EVENT_ROWS[:6]],
                {"status": "available", "event_count": 6, "distinct_viewer_count": 3,
                 "media_event_counts": {"m4": 4, "m1": 1, "m2": 1},
                 "media_watch_seconds": {"m4": 40, "m1": 10, "m2": 10}})}
        sources = {name: hashlib.sha256(data).hexdigest() for name, data in self._input_bytes().items()}
        # Per-viewer calculations: popularity has one hit for cold and one for
        # known; reference has one hit for known only. Both have two users.
        expected = self._expected_report(prediction, ("1/6", "1/4"), ("1/6", "1/2"),
                                         unreachable=1, source_hashes=sources)
        del expected["prediction"]["_train_hash"]
        self.assertEqual(expected["preparation"]["training"]["content_sha256"],
                         report["preparation"]["training"]["content_sha256"])
        self.assertEqual(expected, report)

    def test_exact_duplicate_replay_collapses_once_and_does_not_change_semantics(self):
        _, first = self._successful_report()
        rows = self._read_rows("events.csv", EVENT_HEADER)
        rows.append(dict(rows[0]))
        self._rewrite_events(rows)
        _, replayed = self._successful_report()
        self.assertEqual(1, replayed["preparation"]["duplicate_events_collapsed"])
        self.assertEqual(first["prediction"], replayed["prediction"])
        self.assertEqual(first["baselines"], replayed["baselines"])
        self.assertNotEqual(first["preparation"]["source_sha256"], replayed["preparation"]["source_sha256"])

    def test_manifest_duplicate_key_rejected_by_real_cli_without_success_stdout(self):
        raw = (self.fixture / "manifest.json").read_text(encoding="utf-8")
        self.assertIn('"schema_version": 1', raw)
        (self.fixture / "manifest.json").write_text(raw.replace('"schema_version": 1',
            '"schema_version": 1,\n  "schema_version": 2', 1), encoding="utf-8")
        result = self._run()
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(b"", result.stdout)
        self.assertIn(b"duplicate", result.stderr.lower())

    def test_cross_cutoff_duplicate_and_invalid_holdout_fail_before_report(self):
        rows = self._read_rows("events.csv", EVENT_HEADER)
        rows.append({"event_id": "t1", "viewer_id": "changed", "media_id": "m1",
                     "occurred_at_utc": "2027-01-20T00:00:00Z", "watch_seconds": "1"})
        self._rewrite_events(rows)
        result = self._run()
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(b"", result.stdout)
        self.assertIn(b"duplicate", result.stderr.lower())
        self.assertNotIn(b"Day 179 learner TODO", result.stderr)


class Day181Test(EvaluationCliFixture):
    def test_fresh_processes_are_byte_deterministic(self):
        first, first_report = self._successful_report()
        second, second_report = self._successful_report()
        self.assertEqual(first.stdout, second.stdout)
        self.assertEqual(first_report, second_report)

    def test_row_shuffle_preserves_semantics_but_changes_raw_hashes(self):
        _, first = self._successful_report()
        cat = list(reversed(CATALOG_ROWS))
        ev = self._read_rows("events.csv", EVENT_HEADER)
        ev = [[row[key] for key in EVENT_HEADER] for row in reversed(ev)]
        _write_rows(self.fixture / "catalog.csv", CATALOG_HEADER, cat)
        _write_rows(self.fixture / "events.csv", EVENT_HEADER, ev)
        _, shuffled = self._successful_report()
        for key in ("protocol", "prediction", "baselines", "limits"):
            self.assertEqual(first[key], shuffled[key])
        self.assertEqual(first["preparation"]["training"], shuffled["preparation"]["training"])
        self.assertNotEqual(first["preparation"]["source_sha256"], shuffled["preparation"]["source_sha256"])

    def test_positive_watch_magnitude_change_preserves_full_prediction_and_metrics(self):
        _, first = self._successful_report()
        rows = self._read_rows("events.csv", EVENT_HEADER)
        next(row for row in rows if row["event_id"] == "h1")["watch_seconds"] = "11"
        self._rewrite_events(rows)
        _, changed = self._successful_report()
        self.assertEqual(first["prediction"], changed["prediction"])
        self.assertEqual(first["baselines"], changed["baselines"])
        self.assertNotEqual(first["preparation"]["source_sha256"], changed["preparation"]["source_sha256"])

    def test_holdout_mutation_changes_exact_metrics_not_any_prediction_field(self):
        _, first = self._successful_report()
        rows = self._read_rows("events.csv", EVENT_HEADER)
        catalog_rows = list(CATALOG_ROWS) + [["m6", "Frosted Atlas", "drama", "100",
            "2027-01-21T00:00:00Z", "2028-01-01T00:00:00Z"]]
        _write_rows(self.fixture / "catalog.csv", CATALOG_HEADER, catalog_rows)
        rows.append({"event_id":"h5", "viewer_id":"cold", "media_id":"m6", "occurred_at_utc":"2027-01-26T00:00:00Z", "watch_seconds":"10"})
        self._rewrite_events(rows)
        _, changed = self._successful_report()
        self.assertEqual(first["prediction"], changed["prediction"])
        self.assertEqual("1/6", changed["baselines"]["popularity"]["precision_at_3"])
        self.assertEqual("1/6", changed["baselines"]["popularity"]["recall_at_3"])
        self.assertEqual("1/2", changed["baselines"]["id_order_reference"]["recall_at_3"])
        self.assertEqual("0/1", changed["baselines"]["signed_deltas"]["precision_at_3"])
        self.assertEqual("-1/3", changed["baselines"]["signed_deltas"]["recall_at_3"])
        self.assertEqual({"all_holdout_viewers": 3, "evaluated_viewers": 2,
                          "zero_positive_viewers": 1, "positive_viewer_media_pairs": 4,
                          "candidate_unreachable_positive_pairs": 2,
                          "training_viewer_count": 3, "cold_start_evaluated_viewers": 1,
                          "candidate_count": 4, "returned_count": 3},
                         changed["baselines"]["popularity"]["counts"])
        self.assertNotEqual(first["preparation"], changed["preparation"])

    def test_catalog_eligibility_changes_prediction_fingerprint(self):
        _, first = self._successful_report()
        rows = list(CATALOG_ROWS)
        rows[2] = [rows[2][0], rows[2][1], rows[2][2], rows[2][3], "2027-01-21T00:00:00Z", rows[2][5]]
        _write_rows(self.fixture / "catalog.csv", CATALOG_HEADER, rows)
        _, changed = self._successful_report()
        self.assertEqual(["m1", "m2", "m4"], changed["prediction"]["candidate_ids"])
        self.assertNotEqual(first["prediction"]["fingerprint"], changed["prediction"]["fingerprint"])

    def test_invalid_holdout_fails_without_partial_report(self):
        rows = self._read_rows("events.csv", EVENT_HEADER)
        rows.append({"event_id": "bad", "viewer_id": "v1", "media_id": "missing",
                     "occurred_at_utc": "2027-01-25T00:00:00Z", "watch_seconds": "7"})
        self._rewrite_events(rows)
        result = self._run()
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(b"", result.stdout)
        self.assertIn(b"media_id", result.stderr.lower())
        self.assertNotIn(b"Day 179 learner TODO", result.stderr)


if __name__ == "__main__":
    unittest.main()
