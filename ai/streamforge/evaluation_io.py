"""Instructor-owned fixed protocol, canonical hashes, and result validation."""
from __future__ import annotations

import hashlib
from collections.abc import Mapping, Sequence
from fractions import Fraction
from typing import Any

from .evaluation_models import (K, PROTOCOL_ID, BaselineComparison,
                                BaselineEvaluation, PopularityState, RankedList)
from .fixture_io import canonical_json_bytes


REPORT_SCHEMA_VERSION = 1
BASELINE_IDS = ("popularity_count", "id_order_reference")


def fixed_protocol(cutoff_utc: str) -> dict[str, Any]:
    """Return the frozen experiment policy; inputs cannot tune its semantics."""
    if not isinstance(cutoff_utc, str) or not cutoff_utc:
        raise ValueError("cutoff_utc must be a nonempty canonical UTC string")
    return {
        "protocol_id": PROTOCOL_ID,
        "k": K,
        "positive_rule": "watch_seconds > 0",
        "support": "distinct positive training viewers per media",
        "candidate_rule": "available_from_utc <= cutoff_utc < available_to_utc",
        "cutoff_utc": cutoff_utc,
        "ranking": "support descending, exact media_id ascending",
        "scope": "one global list for all viewers; no seen-item removal or event-time reranking",
        "precision_denominator": K,
        "recall_targets": "all distinct positive holdout media IDs, including unreachable items",
        "aggregation": "unweighted macro over positive-target viewers",
    }


def id_order_reference(candidate_ids: Sequence[str]) -> tuple[str, ...]:
    """The fixed nonlearned reference: exact IDs ascending, at most K."""
    _validate_unique_ids(candidate_ids, "candidate IDs")
    return tuple(sorted(candidate_ids)[:K])


def _validate_unique_ids(values: Sequence[str], label: str) -> None:
    if isinstance(values, (str, bytes)):
        raise TypeError(f"{label} must be a sequence of IDs")
    if any(not isinstance(value, str) or not value for value in values):
        raise ValueError(f"{label} must contain nonempty strings")
    if len(values) != len(set(values)):
        raise ValueError(f"{label} must not contain duplicates")


def validate_popularity_state(value: PopularityState, candidate_ids: Sequence[str]) -> None:
    _validate_unique_ids(candidate_ids, "candidate IDs")
    if not isinstance(value, PopularityState):
        raise TypeError("popularity state has an unsupported type or version")
    if not isinstance(value.no_positive_training_signal, bool):
        raise ValueError("no-positive-signal flag must be boolean")
    if set(value.support_by_media) != set(candidate_ids):
        raise ValueError("support keys must exactly match the candidate set")
    for media_id, score in value.support_by_media.items():
        if not isinstance(media_id, str) or isinstance(score, bool) or not isinstance(score, int) or score < 0:
            raise ValueError("support must contain nonnegative integer counts")
    if value.no_positive_training_signal != all(score == 0 for score in value.support_by_media.values()):
        raise ValueError("no-positive-signal flag disagrees with support counts")


def validate_ranked_list(value: RankedList, candidate_ids: Sequence[str]) -> None:
    _validate_unique_ids(candidate_ids, "candidate IDs")
    if not isinstance(value, RankedList):
        raise TypeError("ranked list has an unsupported type or version")
    _validate_unique_ids(value.candidate_ids, "ranked-list candidate IDs")
    _validate_unique_ids(value.recommendation_ids, "recommendation IDs")
    if set(value.candidate_ids) != set(candidate_ids):
        raise ValueError("ranked list must contain exactly the declared candidates")
    if len(value.recommendation_ids) > K or \
            tuple(value.recommendation_ids) != tuple(value.candidate_ids[:K]):
        raise ValueError("recommendations must be distinct candidates and no longer than K")
    if value.fallback_status not in {"popularity_ranked", "id_order_no_positive_training_signal"}:
        raise ValueError("unknown ranking fallback status")


def validate_fraction_or_none(value: Fraction | None, label: str) -> None:
    if value is not None and not isinstance(value, Fraction):
        raise TypeError(f"{label} must be an exact Fraction or null")


def validate_evaluation(value: BaselineEvaluation, candidate_ids: Sequence[str]) -> None:
    if not isinstance(value, BaselineEvaluation):
        raise TypeError("evaluation result has an unsupported type or version")
    validate_ranked_list(value.ranked_list, candidate_ids)
    validate_fraction_or_none(value.precision_at_3, "precision_at_3")
    validate_fraction_or_none(value.recall_at_3, "recall_at_3")
    count_names = ("all_holdout_viewers", "evaluated_viewers", "zero_positive_viewers",
                   "positive_viewer_media_pairs", "candidate_unreachable_positive_pairs",
                   "training_viewer_count", "cold_start_evaluated_viewers", "candidate_count",
                   "returned_count")
    for name in count_names:
        count = getattr(value, name)
        if isinstance(count, bool) or not isinstance(count, int) or count < 0:
            raise ValueError(f"{name} must be a nonnegative integer")
    if value.candidate_count != len(candidate_ids) or value.returned_count != len(value.ranked_list.recommendation_ids):
        raise ValueError("evaluation counts disagree with its candidate/ranked list")
    if value.evaluated_viewers + value.zero_positive_viewers != value.all_holdout_viewers:
        raise ValueError("holdout cohort counts do not account for every viewer")
    if value.cold_start_evaluated_viewers > value.evaluated_viewers or \
            value.candidate_unreachable_positive_pairs > value.positive_viewer_media_pairs:
        raise ValueError("evaluation diagnostic counts exceed their population")
    if value.status not in {"available", "no_holdout", "no_positive_holdout", "no_candidates"}:
        raise ValueError("unknown baseline evaluation status")
    if value.status in {"no_holdout", "no_positive_holdout"} and \
            (value.precision_at_3 is not None or value.recall_at_3 is not None):
        raise ValueError("empty evaluation cohorts must use null metrics")
    if value.status == "available" and \
            (value.precision_at_3 is None or value.recall_at_3 is None):
        raise ValueError("available evaluation metrics must both be defined")


def validate_comparison(value: BaselineComparison, candidate_ids: Sequence[str]) -> None:
    if not isinstance(value, BaselineComparison):
        raise TypeError("comparison result has an unsupported type or version")
    validate_evaluation(value.popularity, candidate_ids)
    validate_evaluation(value.reference, candidate_ids)
    validate_fraction_or_none(value.precision_delta, "precision_delta")
    validate_fraction_or_none(value.recall_delta, "recall_delta")
    cohort_fields = ("all_holdout_viewers", "evaluated_viewers", "zero_positive_viewers",
                    "positive_viewer_media_pairs", "candidate_unreachable_positive_pairs",
                    "training_viewer_count", "cold_start_evaluated_viewers", "candidate_count")
    if any(getattr(value.popularity, name) != getattr(value.reference, name) for name in cohort_fields):
        raise ValueError("baselines must use the same candidates and evaluation cohort")
    expected_precision_delta = (value.popularity.precision_at_3 - value.reference.precision_at_3
                               if value.popularity.precision_at_3 is not None and
                               value.reference.precision_at_3 is not None else None)
    expected_recall_delta = (value.popularity.recall_at_3 - value.reference.recall_at_3
                             if value.popularity.recall_at_3 is not None and
                             value.reference.recall_at_3 is not None else None)
    if (value.precision_delta, value.recall_delta) != (expected_precision_delta, expected_recall_delta):
        raise ValueError("comparison deltas must equal popularity minus reference metrics")
    if value.status not in {"available", "no_holdout", "no_positive_holdout", "no_candidates"}:
        raise ValueError("unknown comparison status")


def prediction_fingerprint(protocol: Mapping[str, Any], candidate_ids: Sequence[str],
                           popularity: PopularityState, ranked: RankedList) -> str:
    """Hash only declared prediction inputs, never holdout or source hashes."""
    if protocol.get("protocol_id") != PROTOCOL_ID or protocol.get("k") != K:
        raise ValueError("unknown prediction protocol or K")
    validate_popularity_state(popularity, candidate_ids)
    validate_ranked_list(ranked, candidate_ids)
    payload = {
        "protocol_id": PROTOCOL_ID,
        "cutoff_utc": protocol.get("cutoff_utc"),
        "k": K,
        "candidate_ids": list(candidate_ids),
        "support_by_media": {key: popularity.support_by_media[key] for key in sorted(candidate_ids)},
        "recommendation_ids": list(ranked.recommendation_ids),
        "fallback_status": ranked.fallback_status,
    }
    return hashlib.sha256(canonical_json_bytes(payload)).hexdigest()


def fraction_json(value: Fraction | None) -> str | None:
    """Serialize exact ratios without floating point or ambiguous decimal rounding."""
    validate_fraction_or_none(value, "metric")
    if value is None:
        return None
    return f"{value.numerator}/{value.denominator}"


def evaluation_json(value: BaselineEvaluation, candidate_ids: Sequence[str]) -> dict[str, Any]:
    """Return the full public evaluation projection after validating its boundary."""
    validate_evaluation(value, candidate_ids)
    return {
        "recommendation_ids": list(value.ranked_list.recommendation_ids),
        "status": value.status,
        "precision_at_3": fraction_json(value.precision_at_3),
        "recall_at_3": fraction_json(value.recall_at_3),
        "counts": {
            "all_holdout_viewers": value.all_holdout_viewers,
            "evaluated_viewers": value.evaluated_viewers,
            "zero_positive_viewers": value.zero_positive_viewers,
            "positive_viewer_media_pairs": value.positive_viewer_media_pairs,
            "candidate_unreachable_positive_pairs": value.candidate_unreachable_positive_pairs,
            "training_viewer_count": value.training_viewer_count,
            "cold_start_evaluated_viewers": value.cold_start_evaluated_viewers,
            "candidate_count": value.candidate_count,
            "returned_count": value.returned_count,
        },
    }


def comparison_json(value: BaselineComparison, candidate_ids: Sequence[str]) -> dict[str, Any]:
    """Return both baseline results and signed popularity-minus-reference deltas."""
    validate_comparison(value, candidate_ids)
    return {
        "popularity": evaluation_json(value.popularity, candidate_ids),
        "id_order_reference": evaluation_json(value.reference, candidate_ids),
        "signed_deltas": {
            "precision_at_3": fraction_json(value.precision_delta),
            "recall_at_3": fraction_json(value.recall_delta),
        },
        "status": value.status,
    }


REPORT_LIMITS = (
    "Synthetic observed-positive overlap only; no reliable negative labels or impressions.",
    "The catalog schedule is assumed known at cutoff; no historical catalog snapshot is demonstrated.",
    "One static global list is used over the holdout horizon; no per-event availability reranking.",
    "Metrics do not establish preference, unbiased real-user quality, causal lift, fairness, or business success.",
    "Content hashes identify bytes and do not establish rights, authenticity, or quality.",
)
