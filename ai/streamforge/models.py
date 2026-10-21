"""Small typed values shared by the StreamForge preparation exercises."""
from __future__ import annotations

from dataclasses import asdict, dataclass
from datetime import datetime
from typing import Any


@dataclass(frozen=True)
class FixtureAdmission:
    schema_version: int
    data_version: int
    source_kind: str
    catalog_file: str
    events_file: str
    # Retain these source strings during Day 155 admission. Day 157 teaches
    # timestamp parsing; Day 162 converts them before partitioning.
    window_start_utc: str
    cutoff_utc: str
    window_end_utc: str


@dataclass(frozen=True)
class CatalogItem:
    media_id: str
    title: str
    genre: str
    duration_s: int
    available_from_utc: datetime
    available_to_utc: datetime


@dataclass(frozen=True)
class ViewingEvent:
    event_id: str
    viewer_id: str
    media_id: str
    occurred_at_utc: datetime
    watch_seconds: int


@dataclass(frozen=True)
class PartitionResult:
    train: tuple[ViewingEvent, ...]
    holdout: tuple[ViewingEvent, ...]
    outside_window: tuple[ViewingEvent, ...]
    status: str


@dataclass(frozen=True)
class HoldoutCoverage:
    viewer_item_seen_in_train: int
    viewer_seen_item_unseen: int
    viewer_unseen_item_seen: int
    viewer_item_unseen: int
    status: str


@dataclass(frozen=True)
class TrainingSummary:
    event_count: int
    distinct_viewer_count: int
    media_event_counts: dict[str, int]
    media_watch_seconds: dict[str, int]
    status: str


def as_json_value(value: Any) -> Any:
    """Instructor helper for converting dataclasses and UTC datetimes to JSON values."""
    if isinstance(value, datetime):
        return value.isoformat().replace("+00:00", "Z")
    if hasattr(value, "__dataclass_fields__"):
        return {key: as_json_value(item) for key, item in asdict(value).items()}
    if isinstance(value, dict):
        return {str(key): as_json_value(item) for key, item in value.items()}
    if isinstance(value, (tuple, list)):
        return [as_json_value(item) for item in value]
    return value
