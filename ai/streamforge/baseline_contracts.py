"""Pure learner seams for a small frozen global popularity experiment.

No function here reads files, accesses a service, or changes the existing
155–168 data-preparation contracts.
"""
from __future__ import annotations

from datetime import datetime, timezone
from fractions import Fraction
from typing import Iterable, Mapping, Sequence

from .evaluation_models import (K, PROTOCOL_ID, BaselineComparison,
                                BaselineEvaluation, HoldoutTargets,
                                PopularityState, RankedList, ViewerScore)
from .models import CatalogItem, ViewingEvent


def _todo(day: int, seam: str):
    raise NotImplementedError(f"Day {day} learner TODO: implement {seam}")


def positive_pairs(events: Sequence[ViewingEvent]) -> frozenset[tuple[str, str]]:
    return _todo(169, "positive_pairs")


def cutoff_candidates(catalog: Sequence[CatalogItem], cutoff: datetime) -> tuple[str, ...]:
    return _todo(170, "cutoff_candidates")


def training_popularity(train_positive_pairs: Iterable[tuple[str, str]],
                        candidate_ids: Sequence[str]) -> PopularityState:
    return _todo(171, "training_popularity")


def rank_popularity(popularity_state: PopularityState) -> RankedList:
    return _todo(172, "rank_popularity")


def holdout_targets(holdout: Sequence[ViewingEvent]) -> HoldoutTargets:
    return _todo(173, "holdout_targets")


def score_viewer(recommendation_ids: Sequence[str],
                 positive_target_ids: Sequence[str] | frozenset[str]) -> ViewerScore:
    return _todo(174, "score_viewer")


def evaluate_baseline(ranked_list: RankedList, candidate_ids: Sequence[str],
                      targets: HoldoutTargets,
                      all_training_viewer_ids: frozenset[str]) -> BaselineEvaluation:
    return _todo(176, "evaluate_baseline")


def compare_baselines(popularity_ranked_list: RankedList, candidate_ids: Sequence[str],
                      targets: HoldoutTargets,
                      all_training_viewer_ids: frozenset[str]) -> BaselineComparison:
    return _todo(177, "compare_baselines")


def assemble_evaluation_report(prepared_lineage: Mapping[str, object], protocol: Mapping[str, object],
                               candidates: Sequence[str], popularity_state: PopularityState,
                               ranked_list: RankedList,
                               comparison: BaselineComparison) -> dict:
    return _todo(178, "assemble_evaluation_report")
