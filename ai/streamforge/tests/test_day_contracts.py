"""Chronological learner acceptance tests; TODO failures are expected before each day."""
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
from datetime import datetime, timezone
from pathlib import Path

from ai.streamforge import contracts
from ai.streamforge.errors import DatasetContractError
from ai.streamforge.models import (CatalogItem, FixtureAdmission, HoldoutCoverage,
                                  PartitionResult, TrainingSummary, ViewingEvent)
from ai.streamforge.tests.golden import assert_golden_bundle, install_fixture


def utc(text: str) -> datetime:
    return datetime.strptime(text, "%Y-%m-%dT%H:%M:%SZ").replace(tzinfo=timezone.utc)


MANIFEST = {
    "schema_version": 1, "data_version": 1, "fixture_id": "basic",
    "source_kind": "synthetic_hand_authored", "media_source": "invented_text_only",
    "viewer_source": "invented_identifiers", "catalog_file": "catalog.csv", "events_file": "events.csv",
    "window_start_utc": "2027-01-01T00:00:00Z", "cutoff_utc": "2027-01-20T00:00:00Z",
    "window_end_utc": "2027-02-01T00:00:00Z",
}
CATALOG_HEADER = ["media_id", "title", "genre", "duration_s", "available_from_utc", "available_to_utc"]
EVENT_HEADER = ["event_id", "viewer_id", "media_id", "occurred_at_utc", "watch_seconds"]
CATALOG_ROWS = [
    {"media_id": "sf-m-001", "title": "Glass Harbor", "genre": "drama", "duration_s": "1200",
     "available_from_utc": "2026-12-01T00:00:00Z", "available_to_utc": "2027-04-01T00:00:00Z"},
    {"media_id": "sf-m-002", "title": "Paper Comet", "genre": "animation", "duration_s": "900",
     "available_from_utc": "2026-12-01T00:00:00Z", "available_to_utc": "2027-04-01T00:00:00Z"},
]


class Day155Test(unittest.TestCase):
    def test_manifest_admission_and_rejection_without_mutation(self):
        original = dict(MANIFEST)
        admitted = contracts.admit_fixture_manifest(MANIFEST)
        self.assertEqual(MANIFEST["cutoff_utc"], admitted.cutoff_utc)
        self.assertIsInstance(admitted.window_start_utc, str)
        for key, value in (("schema_version", 2), ("media_source", "external"),
                           ("viewer_source", "real_account")):
            bad = dict(MANIFEST, **{key: value})
            with self.subTest(key=key), self.assertRaises(DatasetContractError):
                contracts.admit_fixture_manifest(bad)
        for bad in ({key: value for key, value in MANIFEST.items() if key != "cutoff_utc"},
                    dict(MANIFEST, unexpected="extra")):
            with self.subTest(bad=bad), self.assertRaises(DatasetContractError):
                contracts.admit_fixture_manifest(bad)
        self.assertEqual(original, MANIFEST)


class Day156Test(unittest.TestCase):
    def test_csv_keeps_quoted_comma_and_newline_and_rejects_shape_errors(self):
        text = 'media_id,title\nm1,"Quiet, Small Harbor"\nm2,"A line\nbreak"\n'
        rows = contracts.read_csv_table(text, ["media_id", "title"])
        self.assertEqual("Quiet, Small Harbor", rows[0]["title"])
        self.assertEqual("A line\nbreak", rows[1]["title"])
        for invalid in ("media_id,title\nm1\n", "media_id,title\nm1,x,y\n",
                        "media_id,title,title\nm1,x,x\n", "other,title\nm1,x\n", 'a,b\n"bad,x\n'):
            with self.subTest(invalid=invalid), self.assertRaises(DatasetContractError):
                contracts.read_csv_table(invalid, ["media_id", "title"])
        with self.assertRaises(DatasetContractError):
            contracts.read_csv_table('media_id,title\nm1,"unterminated\n', ["media_id", "title"])
        with self.assertRaises(DatasetContractError):
            contracts.read_csv_table("a,b\n" + "x,y\n" * 501, ["a", "b"])
        with self.assertRaises(DatasetContractError):
            contracts.read_csv_table("a,b\nx," + "t" * 4097 + "\n", ["a", "b"])
        self.assertEqual(1, len(contracts.read_csv_table("a,b\nx," + "é" * 2048 + "\n", ["a", "b"])))
        with self.assertRaises(DatasetContractError):
            contracts.read_csv_table("a,b\nx," + "é" * 2049 + "\n", ["a", "b"])


class Day157Test(unittest.TestCase):
    def test_only_canonical_utc_seconds_parse(self):
        parsed = contracts.parse_utc_instant("2024-02-29T23:59:59Z")
        self.assertEqual(datetime(2024, 2, 29, 23, 59, 59, tzinfo=timezone.utc), parsed)
        for invalid in ("2023-02-29T00:00:00Z", "2027-01-01T00:00:00", "2027-01-01T00:00:00+00:00",
                        "2027-01-01T00:00:00.1Z", "2027-01-01 00:00:00Z", "2027-1-01T00:00:00Z",
                        "２０２７-01-01T00:00:00Z"):
            with self.subTest(invalid=invalid), self.assertRaises(DatasetContractError):
                contracts.parse_utc_instant(invalid)


class Day158Test(unittest.TestCase):
    def test_catalog_is_typed_unique_and_half_open_availability_is_finite(self):
        result = contracts.validate_catalog(CATALOG_ROWS)
        self.assertEqual([CatalogItem("sf-m-001", "Glass Harbor", "drama", 1200,
                                      utc("2026-12-01T00:00:00Z"), utc("2027-04-01T00:00:00Z")),
                          CatalogItem("sf-m-002", "Paper Comet", "animation", 900,
                                      utc("2026-12-01T00:00:00Z"), utc("2027-04-01T00:00:00Z"))], result)
        self.assertEqual(1, contracts.validate_catalog([dict(CATALOG_ROWS[0], duration_s="1")])[0].duration_s)
        for bad in ([CATALOG_ROWS[0], CATALOG_ROWS[0]],
                    [dict(CATALOG_ROWS[0], duration_s="7200.5")],
                    [dict(CATALOG_ROWS[0], duration_s="7201")],
                    [dict(CATALOG_ROWS[0], duration_s="0")],
                    [dict(CATALOG_ROWS[0], duration_s="+1200")],
                    [dict(CATALOG_ROWS[0], duration_s="١٢٠٠")],
                    [dict(CATALOG_ROWS[0], available_to_utc="2026-12-01T00:00:00Z")],
                    [dict(CATALOG_ROWS[0], available_from_utc="2027-04-01T00:00:00Z")],
                    [dict(CATALOG_ROWS[0], media_id="")],
                    [dict(CATALOG_ROWS[0], media_id=" sf-m-001")],
                    [dict(CATALOG_ROWS[0], title=" Glass Harbor")],
                    [dict(CATALOG_ROWS[0], genre="unknown")]):
            with self.subTest(rows=bad), self.assertRaises(DatasetContractError):
                contracts.validate_catalog(bad)


class Day159Test(unittest.TestCase):
    def test_events_join_exact_ids_validate_watch_bounds_and_half_open_availability(self):
        catalog = [CatalogItem("m1", "Glass Harbor", "drama", 1200, utc("2027-01-01T00:00:00Z"), utc("2027-02-01T00:00:00Z"))]
        base = {"event_id": "e1", "viewer_id": "synthetic-viewer-1", "media_id": "m1",
                "occurred_at_utc": "2027-01-01T00:00:00Z", "watch_seconds": "0"}
        self.assertEqual(0, contracts.validate_events([base], catalog)[0].watch_seconds)
        self.assertEqual(ViewingEvent("e1", "synthetic-viewer-1", "m1", utc("2027-01-01T00:00:00Z"), 0),
                         contracts.validate_events([base], catalog)[0])
        for bad in (dict(base, media_id="unknown"), dict(base, watch_seconds="-1"),
                    dict(base, watch_seconds="1201"), dict(base, watch_seconds="1.2"),
                    dict(base, watch_seconds="+1"), dict(base, watch_seconds="١"),
                    dict(base, occurred_at_utc="2026-12-31T23:59:59Z"),
                    dict(base, occurred_at_utc="2027-02-01T00:00:00Z"),
                    dict(base, event_id=""), dict(base, event_id=" e1"),
                    dict(base, viewer_id=""), dict(base, viewer_id=" synthetic-viewer-1")):
            with self.subTest(row=bad), self.assertRaises(DatasetContractError):
                contracts.validate_events([bad], catalog)


class Day160Test(unittest.TestCase):
    def test_exact_replays_collapse_conflicts_fail_and_distinct_ids_remain(self):
        a = ViewingEvent("e1", "viewer-1", "m1", utc("2027-01-01T00:00:00Z"), 10)
        b = ViewingEvent("e2", "viewer-1", "m1", utc("2027-01-01T00:00:00Z"), 10)
        unique, duplicate_count = contracts.deduplicate_events([a, a, b])
        self.assertEqual([a, b], unique)
        self.assertEqual(1, duplicate_count)
        changed = ViewingEvent("e1", "viewer-1", "m1", utc("2027-01-20T00:00:00Z"), 10)
        with self.assertRaises(DatasetContractError):
            contracts.deduplicate_events([a, changed])
        # This changed timestamp crosses the fixed cutoff; validation/replay must
        # still reject it before any partition can hide the conflict.
        cross_cutoff = ViewingEvent("e1", "viewer-1", "m1", utc("2027-01-20T00:00:00Z"), 10)
        with self.assertRaises(DatasetContractError):
            contracts.deduplicate_events([a, cross_cutoff])


class Day161RecoveryInfrastructureTest(unittest.TestCase):
    def test_recovery_day_reuses_prior_only_selector_and_has_no_new_seam(self):
        self.assertEqual([], [name for name in vars(contracts) if name.startswith("day161")])
        self.assertIn("prior_only_155_160", (Path(__file__).parents[3] / "docs/reboot/continuation-155-168/contracts/day-161.md").read_text())


class Day162Test(unittest.TestCase):
    def test_fixed_half_open_temporal_partition_accounts_for_every_event(self):
        start, cutoff, end = utc("2027-01-01T00:00:00Z"), utc("2027-01-20T00:00:00Z"), utc("2027-02-01T00:00:00Z")
        events = [ViewingEvent(eid, "v", "m", t, 1) for eid, t in [
            ("z-train", start), ("b-cutoff", cutoff), ("a-cutoff", cutoff),
            ("end", end), ("outside", utc("2026-12-31T23:59:59Z"))]]
        result = contracts.partition_events(events, start, cutoff, end)
        self.assertEqual(["z-train"], [event.event_id for event in result.train])
        self.assertEqual(["a-cutoff", "b-cutoff"], [event.event_id for event in result.holdout])
        self.assertEqual(["outside", "end"], [event.event_id for event in result.outside_window])
        with self.assertRaises(DatasetContractError):
            contracts.partition_events(events, cutoff, cutoff, end)
        with self.assertRaises(DatasetContractError):
            contracts.partition_events(events, start, end, cutoff)
        no_events = contracts.partition_events([], start, cutoff, end)
        self.assertEqual((), no_events.train)
        self.assertEqual((), no_events.holdout)
        self.assertEqual("insufficient_training_data", no_events.status)
        train_only = contracts.partition_events([events[0]], start, cutoff, end)
        self.assertEqual("no_holdout", train_only.status)
        self.assertEqual((), train_only.holdout)
        holdout_only = contracts.partition_events([events[1]], start, cutoff, end)
        self.assertEqual("insufficient_training_data", holdout_only.status)
        self.assertEqual((), holdout_only.train)


class Day163Test(unittest.TestCase):
    def test_holdout_coverage_has_four_exclusive_classes_and_empty_status(self):
        catalog = [CatalogItem("known", "Glass Harbor", "drama", 1200, utc("2026-01-01T00:00:00Z"), utc("2028-01-01T00:00:00Z")),
                   CatalogItem("cold", "Paper Comet", "animation", 900, utc("2026-01-01T00:00:00Z"), utc("2028-01-01T00:00:00Z"))]
        train = [ViewingEvent("t1", "v1", "known", utc("2027-01-01T00:00:00Z"), 10)]
        holdout = []
        for i, (viewer, media, repeat) in enumerate([
            ("v1", "known", 2), ("v1", "cold", 1), ("v3", "known", 3), ("v4", "cold", 4)]):
            holdout.extend(ViewingEvent(f"h{i}-{n}", viewer, media, utc("2027-01-20T00:00:00Z"), 5)
                           for n in range(repeat))
        coverage = contracts.holdout_coverage(train, holdout, catalog)
        self.assertEqual((2, 1, 3, 4), (coverage.viewer_item_seen_in_train,
                                      coverage.viewer_seen_item_unseen,
                                      coverage.viewer_unseen_item_seen,
                                      coverage.viewer_item_unseen))
        self.assertEqual(10, sum((coverage.viewer_item_seen_in_train, coverage.viewer_seen_item_unseen,
                                 coverage.viewer_unseen_item_seen, coverage.viewer_item_unseen)))
        self.assertEqual(HoldoutCoverage(2, 1, 3, 4, "available"), coverage)
        empty = contracts.holdout_coverage(train, [], catalog)
        self.assertEqual("no_holdout", empty.status)
        self.assertEqual(0, sum(vars(empty)[key] for key in (
            "viewer_item_seen_in_train", "viewer_seen_item_unseen", "viewer_unseen_item_seen", "viewer_item_unseen")))


class Day164Test(unittest.TestCase):
    def test_summary_uses_train_only_and_empty_is_explicit(self):
        catalog = [CatalogItem("m1", "Glass Harbor", "drama", 1200, utc("2026-01-01T00:00:00Z"), utc("2028-01-01T00:00:00Z"))]
        train = [ViewingEvent("t1", "v1", "m1", utc("2027-01-01T00:00:00Z"), 10),
                 ViewingEvent("t2", "v1", "m1", utc("2027-01-02T00:00:00Z"), 15),
                 ViewingEvent("t3", "v2", "m2", utc("2027-01-03T00:00:00Z"), 7)]
        result = contracts.summarize_training(train, catalog)
        self.assertEqual(TrainingSummary(3, 2, {"m1": 2, "m2": 1}, {"m1": 25, "m2": 7}, "available"), result)
        empty = contracts.summarize_training([], catalog)
        self.assertEqual(TrainingSummary(0, 0, {}, {}, "insufficient_training_data"), empty)


class Day165Test(unittest.TestCase):
    def test_bundle_hashes_train_content_without_holdout_hash_leakage(self):
        admission = FixtureAdmission(1, 1, "synthetic_hand_authored", "catalog.csv", "events.csv",
                                     "2027-01-01T00:00:00Z", "2027-01-20T00:00:00Z", "2027-02-01T00:00:00Z")
        item = CatalogItem("m1", "Glass Harbor", "drama", 1200, utc("2026-12-01T00:00:00Z"), utc("2027-04-01T00:00:00Z"))
        train_event = ViewingEvent("t1", "v1", "m1", utc("2027-01-01T00:00:00Z"), 10)
        holdout_event = ViewingEvent("h1", "v1", "m1", utc("2027-01-20T00:00:00Z"), 5)
        first_coverage = HoldoutCoverage(1, 0, 0, 0, "available")
        second_coverage = HoldoutCoverage(0, 0, 1, 0, "available")
        summary = TrainingSummary(1, 1, {"m1": 1}, {"m1": 10}, "available")
        a = PartitionResult((train_event,), (holdout_event,), (), "available")
        changed_holdout = ViewingEvent("h2", "v2", "m1", holdout_event.occurred_at_utc, 7)
        b = PartitionResult((train_event,), (changed_holdout,), (), "available")
        first = contracts.assemble_bundle(admission, [item], a, first_coverage, summary, {"catalog.csv": "a", "events.csv": "b"})
        second = contracts.assemble_bundle(admission, [item], b, second_coverage, summary, {"catalog.csv": "a", "events.csv": "c"})
        expected_train = [{"event_id": "t1", "viewer_id": "v1", "media_id": "m1",
                           "occurred_at_utc": "2027-01-01T00:00:00Z", "watch_seconds": 10}]
        expected_summary = {"status": "available", "event_count": 1, "distinct_viewer_count": 1,
                            "media_event_counts": {"m1": 1}, "media_watch_seconds": {"m1": 10}}
        payload = {"schema_version": 1, "window_start_utc": "2027-01-01T00:00:00Z",
                   "cutoff_utc": "2027-01-20T00:00:00Z", "train": expected_train,
                   "training": expected_summary}
        expected_hash = hashlib.sha256(json.dumps(payload, ensure_ascii=False, allow_nan=False,
                                      sort_keys=True, separators=(",", ":")).encode("utf-8")).hexdigest()
        self.assertEqual(expected_train, first["train"])
        self.assertEqual(expected_summary, {k: v for k, v in first["training"].items() if k != "content_sha256"})
        self.assertEqual(expected_hash, first["training"]["content_sha256"])
        self.assertEqual(first["training"], second["training"], "entire train summary/hash excludes holdout")
        self.assertNotEqual(first["holdout_diagnostics"], second["holdout_diagnostics"])
        self.assertNotEqual(first["source_sha256"], second["source_sha256"])
        changed_train = ViewingEvent("t1", "v1", "m1", train_event.occurred_at_utc, 11)
        changed_summary = TrainingSummary(1, 1, {"m1": 1}, {"m1": 11}, "available")
        changed_partition = PartitionResult((changed_train,), (holdout_event,), (), "available")
        changed = contracts.assemble_bundle(admission, [item], changed_partition, first_coverage,
                                            changed_summary, {"catalog.csv": "a", "events.csv": "d"})
        self.assertNotEqual(first["training"]["content_sha256"], changed["training"]["content_sha256"])
        self.assertNotIn("holdout", payload)


class Day166Test(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="streamforge-day166-")
        self.root = Path(self.temp.name)
        source = Path(__file__).resolve().parents[1]
        shutil.copytree(source, self.root / "ai/streamforge",
                        ignore=shutil.ignore_patterns("__pycache__", "*.pyc"))
        self.fixture = self.root / "ai/streamforge/fixtures/basic"
        install_fixture(self.fixture)

    def tearDown(self):
        self.temp.cleanup()

    def run_cli(self):
        return subprocess.run([sys.executable, "-m", "ai.streamforge.prepare", "--fixture", "basic"],
                              cwd=self.root, capture_output=True, check=False, timeout=15,
                              env=dict(os.environ, PYTHONPATH=str(self.root)))

    def test_actual_cli_matches_independent_hand_calculated_rows_counts_and_hashes(self):
        result = self.run_cli()
        self.assertEqual(0, result.returncode, result.stderr.decode("utf-8", "replace"))
        document = json.loads(result.stdout)
        assert_golden_bundle(self, document, self.fixture)
        self.assertEqual(1, result.stdout.count(b"\n"), "exactly one JSON line on stdout")

    def test_actual_cli_invalid_record_fails_without_success_json(self):
        event_path = self.fixture / "events.csv"
        event_path.write_text(event_path.read_text(encoding="utf-8") +
                              "bad,synthetic-viewer-4,unknown,2027-01-25T00:00:00Z,2\n", encoding="utf-8")
        result = self.run_cli()
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(b"", result.stdout)
        self.assertTrue(result.stderr)

    def test_actual_cli_rejects_duplicate_manifest_keys_before_learner_composition(self):
        path = self.fixture / "manifest.json"
        path.write_text('{"schema_version":1,"schema_version":2}', encoding="utf-8")
        result = self.run_cli()
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(b"", result.stdout)
        self.assertIn(b"duplicate JSON object key", result.stderr)

    def test_fixture_manifest_does_not_parse_future_day157_timestamps(self):
        admission = contracts.admit_fixture_manifest(MANIFEST)
        self.assertEqual(MANIFEST["window_start_utc"], admission.window_start_utc)
        self.assertEqual(MANIFEST["cutoff_utc"], admission.cutoff_utc)


class Day168RecoveryInfrastructureTest(unittest.TestCase):
    def test_recovery_day_is_prior_only(self):
        self.assertIn("prior_only_155_167", (Path(__file__).parents[3] / "docs/reboot/continuation-155-168/contracts/day-168.md").read_text())


if __name__ == "__main__":
    unittest.main()
