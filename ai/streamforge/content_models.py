"""Instructor-owned immutable value boundaries for the genre-profile exercise."""
from __future__ import annotations

from dataclasses import dataclass
from fractions import Fraction
from typing import Mapping

GENRES = ("animation", "comedy", "documentary", "drama", "science-fiction")
PROFILE_PROTOCOL_ID = "streamforge-cutoff-genre-profile-v1"
MAX_SCORED_PAIRS = 2500


@dataclass(frozen=True)
class ProfileCounts:
    # Sorted (viewer_id, five integer counts, positive distinct-pair count).
    rows: tuple[tuple[str, tuple[int, ...], int], ...]


@dataclass(frozen=True)
class ProfileWeights:
    # Sorted (viewer_id, five exact Fraction weights, positive pair count).
    rows: tuple[tuple[str, tuple[Fraction, ...], int], ...]


@dataclass(frozen=True)
class QueryPrediction:
    viewer_id: str
    training_seen: bool
    positive_train_pairs: int
    mode: str
    scores_by_media: tuple[tuple[str, Fraction], ...]
    full_order: tuple[str, ...]
    recommendation_ids: tuple[str, ...]


@dataclass(frozen=True)
class PredictorState:
    protocol: tuple[tuple[str, object], ...]
    candidate_ids: tuple[str, ...]
    candidate_features: tuple[tuple[str, tuple[int, ...]], ...]
    training_viewer_ids: tuple[str, ...]
    profiles: tuple[tuple[str, tuple[int, ...], tuple[Fraction, ...], int], ...]
    global_support: tuple[tuple[str, int], ...]
    global_order: tuple[str, ...]
    global_recommendation_ids: tuple[str, ...]
    global_fallback_status: str
    fingerprint: str


@dataclass(frozen=True)
class ViewerMetric:
    viewer_id: str
    positive_target_ids: tuple[str, ...]
    hit_count: int
    positive_target_count: int
    candidate_unreachable_positive_pairs: int
    precision_at_3: Fraction
    recall_at_3: Fraction
    status: str = "available"


@dataclass(frozen=True)
class ProfileEvaluation:
    candidate_ids: tuple[str, ...]
    queries: tuple[QueryPrediction, ...]
    per_viewer_metrics: tuple[ViewerMetric, ...]
    precision_at_3: Fraction | None
    recall_at_3: Fraction | None
    counts: tuple[tuple[str, int], ...]
    selection_mode_counts: tuple[tuple[str, int], ...]
    status: str


@dataclass(frozen=True)
class ProfileComparison:
    profile: ProfileEvaluation
    global_evaluation: object
    precision_delta: Fraction | None
    recall_delta: Fraction | None
    status: str
