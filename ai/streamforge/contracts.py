"""Learner-owned semantic seams for the 155–168 data-preparation slice.

The bounded file adapter, fixture registry, models, CLI framing and output helper are
instructor scaffolding. Implement one named function per assigned day. No function
loads remote data, trains a model or calls the existing CAP44 ranking code.
"""
from __future__ import annotations

from datetime import datetime
from typing import Any, Iterable, Mapping, Sequence

from .models import (CatalogItem, FixtureAdmission, HoldoutCoverage, PartitionResult,
                     TrainingSummary, ViewingEvent)


def _todo(day: int, seam: str):
    raise NotImplementedError(f"Day {day} learner TODO: implement {seam}")


def admit_fixture_manifest(document: Mapping[str, Any]) -> FixtureAdmission:
    return _todo(155, "admit_fixture_manifest")


def read_csv_table(text: str, expected_header: Sequence[str]) -> list[dict[str, str]]:
    return _todo(156, "read_csv_table")


def parse_utc_instant(value: str) -> datetime:
    return _todo(157, "parse_utc_instant")


def validate_catalog(rows: Sequence[Mapping[str, str]]) -> list[CatalogItem]:
    return _todo(158, "validate_catalog")


def validate_events(rows: Sequence[Mapping[str, str]], catalog: Sequence[CatalogItem]) -> list[ViewingEvent]:
    return _todo(159, "validate_events")


def deduplicate_events(events: Sequence[ViewingEvent]) -> tuple[list[ViewingEvent], int]:
    return _todo(160, "deduplicate_events")


def partition_events(events: Sequence[ViewingEvent], window_start: datetime,
                     cutoff: datetime, window_end: datetime) -> PartitionResult:
    return _todo(162, "partition_events")


def holdout_coverage(train: Sequence[ViewingEvent], holdout: Sequence[ViewingEvent],
                     catalog: Sequence[CatalogItem]) -> HoldoutCoverage:
    return _todo(163, "holdout_coverage")


def summarize_training(train: Sequence[ViewingEvent], catalog: Sequence[CatalogItem]) -> TrainingSummary:
    return _todo(164, "summarize_training")


def assemble_bundle(admission: FixtureAdmission, catalog: Sequence[CatalogItem],
                    partition: PartitionResult, coverage: HoldoutCoverage,
                    summary: TrainingSummary, source_hashes: Mapping[str, str]) -> dict[str, Any]:
    return _todo(165, "assemble_bundle")


def prepare_dataset(fixture_name: str) -> dict[str, Any]:
    return _todo(166, "prepare_dataset")
