"""Independent, hand-calculable fixture and output oracle for CLI witnesses."""
from __future__ import annotations

import hashlib
import json
from pathlib import Path

MANIFEST = {
    "schema_version": 1, "data_version": 1, "fixture_id": "basic",
    "source_kind": "synthetic_hand_authored", "media_source": "invented_text_only",
    "viewer_source": "invented_identifiers", "catalog_file": "catalog.csv", "events_file": "events.csv",
    "window_start_utc": "2027-01-01T00:00:00Z", "cutoff_utc": "2027-01-20T00:00:00Z",
    "window_end_utc": "2027-02-01T00:00:00Z",
}
CATALOG_TEXT = (
    "media_id,title,genre,duration_s,available_from_utc,available_to_utc\n"
    "m1,Glass Harbor,drama,100,2026-01-01T00:00:00Z,2028-01-01T00:00:00Z\n"
    "m2,Paper Comet,animation,100,2026-01-01T00:00:00Z,2028-01-01T00:00:00Z\n"
    "m3,Quiet Signal,documentary,100,2026-01-01T00:00:00Z,2028-01-01T00:00:00Z\n"
)
EVENTS_TEXT = (
    "event_id,viewer_id,media_id,occurred_at_utc,watch_seconds\n"
    "e-01,synthetic-viewer-1,m1,2027-01-01T00:00:00Z,10\n"
    "e-02,synthetic-viewer-2,m2,2027-01-02T00:00:00Z,20\n"
    "e-03,synthetic-viewer-1,m3,2027-01-20T00:00:00Z,0\n"
    "e-04,synthetic-viewer-3,m2,2027-02-01T00:00:00Z,30\n"
)
TRAIN = [
    {"event_id": "e-01", "viewer_id": "synthetic-viewer-1", "media_id": "m1",
     "occurred_at_utc": "2027-01-01T00:00:00Z", "watch_seconds": 10},
    {"event_id": "e-02", "viewer_id": "synthetic-viewer-2", "media_id": "m2",
     "occurred_at_utc": "2027-01-02T00:00:00Z", "watch_seconds": 20},
]
HOLDOUT = [{"event_id": "e-03", "viewer_id": "synthetic-viewer-1", "media_id": "m3",
            "occurred_at_utc": "2027-01-20T00:00:00Z", "watch_seconds": 0}]
OUTSIDE = [{"event_id": "e-04", "viewer_id": "synthetic-viewer-3", "media_id": "m2",
            "occurred_at_utc": "2027-02-01T00:00:00Z", "watch_seconds": 30}]
SUMMARY = {"status": "available", "event_count": 2, "distinct_viewer_count": 2,
           "media_event_counts": {"m1": 1, "m2": 1}, "media_watch_seconds": {"m1": 10, "m2": 20}}
COVERAGE = {"status": "available", "viewer_item_seen_in_train": 0,
            "viewer_seen_item_unseen": 1, "viewer_unseen_item_seen": 0, "viewer_item_unseen": 0}


def install_fixture(directory: Path) -> None:
    (directory / "manifest.json").write_text(json.dumps(MANIFEST, indent=2) + "\n", encoding="utf-8")
    (directory / "catalog.csv").write_text(CATALOG_TEXT, encoding="utf-8", newline="")
    (directory / "events.csv").write_text(EVENTS_TEXT, encoding="utf-8", newline="")


def expected_train_content_hash(train=TRAIN, summary=SUMMARY) -> str:
    payload = {
        "schema_version": 1,
        "window_start_utc": MANIFEST["window_start_utc"],
        "cutoff_utc": MANIFEST["cutoff_utc"],
        "train": train,
        "training": summary,
    }
    canonical = json.dumps(payload, ensure_ascii=False, allow_nan=False,
                           sort_keys=True, separators=(",", ":")).encode("utf-8")
    return hashlib.sha256(canonical).hexdigest()


def assert_golden_bundle(testcase, bundle: dict, fixture_directory: Path, *, train=TRAIN,
                         holdout=HOLDOUT, outside=OUTSIDE, summary=SUMMARY,
                         coverage=COVERAGE, duplicate_count=0) -> None:
    testcase.assertEqual(1, bundle.get("schema_version"), "bundle schema version")
    testcase.assertEqual(train, bundle.get("train"), "train rows/cutoff membership/order")
    testcase.assertEqual(holdout, bundle.get("holdout"), "holdout rows/cutoff membership/order")
    testcase.assertEqual(outside, bundle.get("outside_window"), "outside-window rows")
    expected_hash = expected_train_content_hash(train, summary)
    testcase.assertEqual(dict(summary, content_sha256=expected_hash), bundle.get("training"),
                         "complete train-only summary and content hash")
    testcase.assertEqual(coverage, bundle.get("holdout_diagnostics"), "holdout diagnostic classes")
    expected_sources = {
        name: hashlib.sha256((fixture_directory / name).read_bytes()).hexdigest()
        for name in ("manifest.json", "catalog.csv", "events.csv")
    }
    testcase.assertEqual(expected_sources, bundle.get("source_sha256"), "raw input provenance")
    testcase.assertEqual(duplicate_count, bundle.get("duplicate_events_collapsed"), "duplicate replay count")
