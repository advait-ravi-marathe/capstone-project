"""Typed values for the bounded offline popularity baseline."""
from __future__ import annotations

from dataclasses import dataclass
from fractions import Fraction
from typing import Mapping


PROTOCOL_ID = "streamforge-cutoff-popularity-v1"
K = 3


@dataclass(frozen=True)
class PopularityState:
    support_by_media: Mapping[str, int]
    no_positive_training_signal: bool


@dataclass(frozen=True)
class RankedList:
    candidate_ids: tuple[str, ...]
    recommendation_ids: tuple[str, ...]
    fallback_status: str


@dataclass(frozen=True)
class HoldoutTargets:
    targets_by_viewer: Mapping[str, frozenset[str]]
    all_holdout_viewer_ids: frozenset[str]
    zero_positive_viewer_count: int
    status: str


@dataclass(frozen=True)
class ViewerScore:
    hit_count: int
    positive_target_count: int
    precision_at_3: Fraction | None
    recall_at_3: Fraction | None
    status: str


@dataclass(frozen=True)
class BaselineEvaluation:
    ranked_list: RankedList
    precision_at_3: Fraction | None
    recall_at_3: Fraction | None
    all_holdout_viewers: int
    evaluated_viewers: int
    zero_positive_viewers: int
    positive_viewer_media_pairs: int
    candidate_unreachable_positive_pairs: int
    training_viewer_count: int
    cold_start_evaluated_viewers: int
    candidate_count: int
    returned_count: int
    status: str


@dataclass(frozen=True)
class BaselineComparison:
    popularity: BaselineEvaluation
    reference: BaselineEvaluation
    precision_delta: Fraction | None
    recall_delta: Fraction | None
    status: str
