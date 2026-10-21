"""Instructor-owned bridge from validated preparation output to typed evaluation inputs.

This adapter never parses event/catalog CSV or performs preparation itself. It
calls the existing learner preparation path exactly once and rejects incomplete
or internally inconsistent bundles before evaluation can use them.
"""
from __future__ import annotations

import hashlib
import json
import re
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any, Mapping

from .contracts import prepare_dataset
from .fixture_io import read_fixture_file, strict_json_loads
from .fixture_registry import fixture_directory
from .models import CatalogItem, ViewingEvent


_CATALOG_FIELDS = {"media_id", "title", "genre", "duration_s",
                   "available_from_utc", "available_to_utc"}
_EVENT_FIELDS = {"event_id", "viewer_id", "media_id", "occurred_at_utc", "watch_seconds"}
_UTC_PATTERN = re.compile(r"[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z\Z")
_SHA256_PATTERN = re.compile(r"[0-9a-f]{64}\Z")


@dataclass(frozen=True)
class PreparedEvaluationInput:
    fixture_id: str
    cutoff_utc: str
    cutoff: datetime
    catalog: tuple[CatalogItem, ...]
    train: tuple[ViewingEvent, ...]
    holdout: tuple[ViewingEvent, ...]
    outside_window: tuple[ViewingEvent, ...]
    all_training_viewer_ids: frozenset[str]
    lineage: Mapping[str, Any]


def _utc(value: object, label: str) -> datetime:
    if not isinstance(value, str) or not _UTC_PATTERN.fullmatch(value):
        raise ValueError(f"prepared {label} must use canonical UTC seconds")
    try:
        return datetime.strptime(value, "%Y-%m-%dT%H:%M:%SZ").replace(tzinfo=timezone.utc)
    except ValueError as exc:
        raise ValueError(f"prepared {label} is not a real UTC instant") from exc


def _rows(document: Mapping[str, Any], key: str, fields: set[str]) -> list[Mapping[str, Any]]:
    value = document.get(key)
    if not isinstance(value, list):
        raise ValueError(f"prepared bundle {key} must be a list")
    result = []
    for index, row in enumerate(value):
        if not isinstance(row, dict) or set(row) != fields:
            raise ValueError(f"prepared bundle {key}[{index}] has an unsupported shape")
        result.append(row)
    return result


def read_fixture_manifest(fixture_name: str) -> tuple[dict[str, Any], dict[str, bytes]]:
    """Read only registered inputs and reject duplicate JSON keys before preparation."""
    directory = fixture_directory(fixture_name)
    manifest_bytes = read_fixture_file(directory, "manifest.json")
    manifest = strict_json_loads(manifest_bytes.decode("utf-8"))
    if not isinstance(manifest, dict):
        raise ValueError("fixture manifest must be a JSON object")
    required = {"schema_version", "data_version", "fixture_id", "source_kind", "media_source",
                "viewer_source", "catalog_file", "events_file", "window_start_utc", "cutoff_utc",
                "window_end_utc"}
    if set(manifest) != required:
        raise ValueError("fixture manifest has missing or unsupported fields")
    if type(manifest["schema_version"]) is not int or manifest["schema_version"] != 1 or \
            type(manifest["data_version"]) is not int or manifest["data_version"] != 1:
        raise ValueError("unsupported fixture schema or data version")
    if manifest["fixture_id"] != fixture_name or manifest["source_kind"] != "synthetic_hand_authored" or \
            manifest["media_source"] != "invented_text_only" or \
            manifest["viewer_source"] != "invented_identifiers":
        raise ValueError("fixture identity or synthetic-source declaration is unsupported")
    if manifest["catalog_file"] != "catalog.csv" or manifest["events_file"] != "events.csv":
        raise ValueError("fixture manifest names unsupported input files")
    start = _utc(manifest["window_start_utc"], "window_start_utc")
    cutoff = _utc(manifest["cutoff_utc"], "cutoff_utc")
    end = _utc(manifest["window_end_utc"], "window_end_utc")
    if not start < cutoff < end:
        raise ValueError("fixture observation window must satisfy start < cutoff < end")
    inputs = {"manifest.json": manifest_bytes,
              "catalog.csv": read_fixture_file(directory, "catalog.csv"),
              "events.csv": read_fixture_file(directory, "events.csv")}
    return manifest, inputs


def adapt_prepared_bundle(fixture_name: str, manifest: Mapping[str, Any],
                          source_bytes: Mapping[str, bytes], bundle: object) -> PreparedEvaluationInput:
    """Validate the full prior output and convert its rows without re-preparing."""
    if not isinstance(bundle, dict) or type(bundle.get("schema_version")) is not int or \
            bundle.get("schema_version") != 1:
        raise ValueError("prepared bundle has an unsupported schema version")
    required_bundle = {"schema_version", "reference_catalog", "train", "holdout", "outside_window",
                       "training", "holdout_diagnostics", "source_sha256", "duplicate_events_collapsed"}
    if set(bundle) != required_bundle:
        raise ValueError("prepared bundle has missing or unsupported top-level fields")

    raw_catalog = _rows(bundle, "reference_catalog", _CATALOG_FIELDS)
    raw_train = _rows(bundle, "train", _EVENT_FIELDS)
    raw_holdout = _rows(bundle, "holdout", _EVENT_FIELDS)
    raw_outside = _rows(bundle, "outside_window", _EVENT_FIELDS)

    catalog = []
    catalog_ids: set[str] = set()
    for index, row in enumerate(raw_catalog):
        media_id = row["media_id"]
        title = row["title"]
        genre = row["genre"]
        duration = row["duration_s"]
        if not isinstance(media_id, str) or not media_id or media_id in catalog_ids:
            raise ValueError(f"prepared reference_catalog[{index}] has invalid or duplicate media_id")
        if not isinstance(title, str) or not title or not isinstance(genre, str) or not genre:
            raise ValueError(f"prepared reference_catalog[{index}] has invalid descriptive fields")
        if type(duration) is not int or not 1 <= duration <= 7200:
            raise ValueError(f"prepared reference_catalog[{index}] has invalid duration_s")
        available_from = _utc(row["available_from_utc"], f"reference_catalog[{index}].available_from_utc")
        available_to = _utc(row["available_to_utc"], f"reference_catalog[{index}].available_to_utc")
        if available_from >= available_to:
            raise ValueError(f"prepared reference_catalog[{index}] has invalid availability interval")
        catalog_ids.add(media_id)
        catalog.append(CatalogItem(media_id, title, genre, duration, available_from, available_to))

    start = _utc(manifest["window_start_utc"], "window_start_utc")
    cutoff = _utc(manifest["cutoff_utc"], "cutoff_utc")
    end = _utc(manifest["window_end_utc"], "window_end_utc")
    partition_rows = (raw_train, raw_holdout, raw_outside)
    typed_partitions: list[tuple[ViewingEvent, ...]] = []
    global_event_ids: set[str] = set()
    for part_name, part_rows in zip(("train", "holdout", "outside_window"), partition_rows):
        converted = []
        previous_key = None
        for index, row in enumerate(part_rows):
            event_id, viewer_id, media_id = row["event_id"], row["viewer_id"], row["media_id"]
            if any(not isinstance(value, str) or not value for value in (event_id, viewer_id, media_id)):
                raise ValueError(f"prepared {part_name}[{index}] has invalid identity fields")
            if event_id in global_event_ids:
                raise ValueError(f"prepared event ID {event_id!r} occurs more than once across partitions")
            if media_id not in catalog_ids:
                raise ValueError(f"prepared {part_name}[{index}] references unknown media_id")
            instant = _utc(row["occurred_at_utc"], f"{part_name}[{index}].occurred_at_utc")
            watch = row["watch_seconds"]
            catalog_item = next(x for x in catalog if x.media_id == media_id)
            if type(watch) is not int or not 0 <= watch <= catalog_item.duration_s:
                raise ValueError(f"prepared {part_name}[{index}] has invalid watch_seconds")
            if not catalog_item.available_from_utc <= instant < catalog_item.available_to_utc:
                raise ValueError(f"prepared {part_name}[{index}] violates catalog availability")
            if part_name == "train" and not start <= instant < cutoff:
                raise ValueError("prepared train partition contains an event outside [start, cutoff)")
            if part_name == "holdout" and not cutoff <= instant < end:
                raise ValueError("prepared holdout partition contains an event outside [cutoff, end)")
            if part_name == "outside_window" and start <= instant < end:
                raise ValueError("prepared outside_window contains an event inside the observation window")
            ordering_key = (instant, event_id)
            if previous_key is not None and ordering_key < previous_key:
                raise ValueError(f"prepared {part_name} partition is not in canonical event order")
            previous_key = ordering_key
            global_event_ids.add(event_id)
            converted.append(ViewingEvent(event_id, viewer_id, media_id, instant, watch))
        typed_partitions.append(tuple(converted))

    training = bundle["training"]
    if not isinstance(training, dict) or set(training) != {"status", "event_count", "distinct_viewer_count",
            "media_event_counts", "media_watch_seconds", "content_sha256"}:
        raise ValueError("prepared training summary has an unsupported shape")
    expected_training_status = "available" if raw_train else "insufficient_training_data"
    if training["status"] != expected_training_status:
        raise ValueError("prepared training status disagrees with train partition")
    if type(training["event_count"]) is not int or training["event_count"] != len(raw_train):
        raise ValueError("prepared training event_count disagrees with train partition")
    if type(training["distinct_viewer_count"]) is not int or training["distinct_viewer_count"] != \
            len({row["viewer_id"] for row in raw_train}):
        raise ValueError("prepared training distinct_viewer_count disagrees with train partition")
    if not isinstance(training["media_event_counts"], dict) or not isinstance(training["media_watch_seconds"], dict):
        raise ValueError("prepared training per-media summaries must be objects")
    for summary_name in ("media_event_counts", "media_watch_seconds"):
        summary_values = training[summary_name]
        if any(not isinstance(key, str) or not key or type(value) is not int or value < 0
               for key, value in summary_values.items()):
            raise ValueError(f"prepared training {summary_name} contains an invalid key or count")
    expected_media_events: dict[str, int] = {}
    expected_media_watch: dict[str, int] = {}
    for row in raw_train:
        media = row["media_id"]
        expected_media_events[media] = expected_media_events.get(media, 0) + 1
        expected_media_watch[media] = expected_media_watch.get(media, 0) + row["watch_seconds"]
    if training["media_event_counts"] != expected_media_events or training["media_watch_seconds"] != expected_media_watch:
        raise ValueError("prepared training per-media summaries disagree with train partition")
    if not isinstance(training["content_sha256"], str) or not _SHA256_PATTERN.fullmatch(training["content_sha256"]):
        raise ValueError("prepared training content hash is malformed")
    train_projection = {
        "schema_version": 1,
        "window_start_utc": manifest["window_start_utc"],
        "cutoff_utc": manifest["cutoff_utc"],
        "train": raw_train,
        "training": {key: training[key] for key in (
            "status", "event_count", "distinct_viewer_count", "media_event_counts", "media_watch_seconds")},
    }
    expected_training_hash = hashlib.sha256(json.dumps(
        train_projection, ensure_ascii=False, allow_nan=False, sort_keys=True,
        separators=(",", ":")).encode("utf-8")).hexdigest()
    if training["content_sha256"] != expected_training_hash:
        raise ValueError("prepared training content hash does not match its train-only projection")

    diagnostics = bundle["holdout_diagnostics"]
    diag_fields = {"status", "viewer_item_seen_in_train", "viewer_seen_item_unseen",
                   "viewer_unseen_item_seen", "viewer_item_unseen"}
    if not isinstance(diagnostics, dict) or set(diagnostics) != diag_fields:
        raise ValueError("prepared holdout diagnostics have an unsupported shape")
    if diagnostics["status"] != ("available" if raw_holdout else "no_holdout"):
        raise ValueError("prepared holdout status disagrees with holdout partition")
    diag_names = ("viewer_item_seen_in_train", "viewer_seen_item_unseen",
                  "viewer_unseen_item_seen", "viewer_item_unseen")
    diag_values = [diagnostics[field] for field in diag_names]
    if any(type(value) is not int or value < 0 for value in diag_values) or sum(diag_values) != len(raw_holdout):
        raise ValueError("prepared holdout diagnostic counts do not account for holdout rows")
    train_viewers = {row["viewer_id"] for row in raw_train}
    train_media = {row["media_id"] for row in raw_train}
    expected_diagnostics = {"viewer_item_seen_in_train": 0, "viewer_seen_item_unseen": 0,
                            "viewer_unseen_item_seen": 0, "viewer_item_unseen": 0}
    for row in raw_holdout:
        viewer_seen = row["viewer_id"] in train_viewers
        item_seen = row["media_id"] in train_media
        category = ("viewer_item_seen_in_train" if viewer_seen and item_seen else
                    "viewer_seen_item_unseen" if viewer_seen else
                    "viewer_unseen_item_seen" if item_seen else "viewer_item_unseen")
        expected_diagnostics[category] += 1
    if any(diagnostics[key] != expected_diagnostics[key] for key in expected_diagnostics):
        raise ValueError("prepared holdout diagnostics disagree with train and holdout partitions")

    source_hashes = bundle["source_sha256"]
    if not isinstance(source_hashes, dict) or set(source_hashes) != set(source_bytes):
        raise ValueError("prepared source hash map is incomplete")
    expected_hashes = {name: hashlib.sha256(payload).hexdigest() for name, payload in source_bytes.items()}
    if source_hashes != expected_hashes:
        raise ValueError("prepared source hashes do not match current registered inputs")
    duplicate_count = bundle["duplicate_events_collapsed"]
    if type(duplicate_count) is not int or duplicate_count < 0:
        raise ValueError("prepared duplicate replay count is invalid")

    manifest_hash = hashlib.sha256(source_bytes["manifest.json"]).hexdigest()
    lineage = {
        "schema_version": bundle["schema_version"],
        "data_version": manifest["data_version"],
        "fixture_id": fixture_name,
        "window_start_utc": manifest["window_start_utc"],
        "cutoff_utc": manifest["cutoff_utc"],
        "window_end_utc": manifest["window_end_utc"],
        "preparation_status": {"training": training["status"], "holdout": diagnostics["status"]},
        "training": dict(training),
        "holdout_diagnostics": dict(diagnostics),
        "source_sha256": dict(source_hashes),
        "duplicate_events_collapsed": duplicate_count,
        "manifest_sha256": manifest_hash,
        "catalog_count": len(catalog),
        "partition_counts": {"train": len(raw_train), "holdout": len(raw_holdout),
                             "outside_window": len(raw_outside)},
    }
    return PreparedEvaluationInput(fixture_name, manifest["cutoff_utc"], cutoff, tuple(catalog),
                                   typed_partitions[0], typed_partitions[1], typed_partitions[2],
                                   frozenset(row.viewer_id for row in typed_partitions[0]), lineage)


def prepare_evaluation_input(fixture_name: str) -> PreparedEvaluationInput:
    """Strictly preflight inputs, call the canonical preparation path once, then adapt."""
    manifest, source_bytes = read_fixture_manifest(fixture_name)
    prepared = prepare_dataset(fixture_name)
    return adapt_prepared_bundle(fixture_name, manifest, source_bytes, prepared)
