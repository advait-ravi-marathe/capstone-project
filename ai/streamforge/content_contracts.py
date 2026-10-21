"""Chronological learner seams for the bounded content-profile experiment.

All seams operate on supplied typed values. File access and CLI composition are
owned by instructor scaffolding in content_evaluate.py.
"""
from __future__ import annotations

from collections.abc import Mapping, Sequence
from datetime import datetime
from fractions import Fraction
from typing import Any

from .content_models import ProfileCounts, ProfileWeights, PredictorState, ProfileEvaluation, ProfileComparison, QueryPrediction
from .evaluation_models import HoldoutTargets


def _todo(day: int, seam: str):
    raise NotImplementedError(f"Day {day} learner TODO: implement {seam}")


def encode_genre(genre: str) -> tuple[int, ...]:
    return _todo(183, "encode_genre")


def catalog_features(catalog: Sequence[Any]) -> tuple[tuple[str, tuple[int, ...]], ...]:
    return _todo(184, "catalog_features")


def profile_counts(train_pairs: Sequence[tuple[str, str]] | frozenset[tuple[str, str]],
                   features: Mapping[str, tuple[int, ...]],
                   training_viewer_ids: Sequence[str] | frozenset[str]) -> ProfileCounts:
    return _todo(185, "profile_counts")


def normalize_profiles(count_state: ProfileCounts) -> ProfileWeights:
    return _todo(186, "normalize_profiles")


def score_candidates(weights: Sequence[Fraction],
                     candidate_features: Mapping[str, tuple[int, ...]]) -> tuple[tuple[str, Fraction], ...]:
    return _todo(187, "score_candidates")


def rank_for_viewer(viewer_id: str, profiles: ProfileWeights, counts: ProfileCounts,
                    candidates: Sequence[str], features: Mapping[str, tuple[int, ...]],
                    global_rank: Sequence[str]) -> QueryPrediction:
    return _todo(188, "rank_for_viewer")


def _recovery(day: int):
    return _todo(day, "prior_only_recovery_assertions")


def prior_only_183_188() -> None:
    return _recovery(189)


def assemble_predictor_state(protocol: Mapping[str, Any], features: Mapping[str, tuple[int, ...]],
                              counts: ProfileCounts, profiles: ProfileWeights,
                              training_viewers: Sequence[str], candidates: Sequence[str],
                              global_state: Any, global_rank: Sequence[str]) -> PredictorState:
    return _todo(190, "assemble_predictor_state")


def evaluate_profiles(state: PredictorState, targets: HoldoutTargets) -> ProfileEvaluation:
    return _todo(191, "evaluate_profiles")


def compare_against_global(profile_evaluation: ProfileEvaluation,
                            global_evaluation: Any) -> ProfileComparison:
    return _todo(192, "compare_against_global")


def assemble_profile_report(protocol: Mapping[str, Any], state: PredictorState,
                             profile_evaluation: ProfileEvaluation,
                             comparison: ProfileComparison,
                             lineage: Mapping[str, Any]) -> dict[str, Any]:
    return _todo(193, "assemble_profile_report")


def prior_only_183_195() -> None:
    return _recovery(196)
