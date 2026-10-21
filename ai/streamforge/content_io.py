"""Instructor-owned protocol, validation, hashing, and JSON projection helpers."""
from __future__ import annotations

import hashlib
from collections import Counter
import re
from fractions import Fraction
from collections.abc import Mapping, Sequence
from typing import Any

from .content_models import GENRES, MAX_SCORED_PAIRS, PROFILE_PROTOCOL_ID, ProfileEvaluation, PredictorState
from .evaluation_models import K
from .fixture_io import canonical_json_bytes

LIMITS = (
    "The profile is an arithmetic feature aggregation, not an optimizer-trained model.",
    "The previously inspected holdout is development evaluation, not an untouched final test.",
    "Synthetic observed-positive overlap does not establish preference, unbiased quality, causal lift, fairness, or business success.",
    "Single-genre features are deliberately coarse and may miss interests; scores are not calibrated preference probabilities.",
    "Catalog schedule is assumed known at cutoff; no historical catalog snapshot is established.",
    "No real dataset, new dependency, model optimizer, cloud resource, service, paid enrollment, or new registered fixture dataset.",
    "Content hashes identify bytes/semantic projections, not authenticity, privacy, rights or quality.",
    "Lists are static at cutoff; no per-event availability reranking.",
)
RATIO = re.compile(r"(?:0|[1-9][0-9]*)/(?:[1-9][0-9]*)\Z")


def canonical_fingerprint(payload: Mapping[str, Any]) -> str:
    return hashlib.sha256(canonical_json_bytes(dict(payload))).hexdigest()

def _deep_immutable(value: object) -> bool:
    if value is None or isinstance(value, (str, bool, int, Fraction)):
        return True
    return isinstance(value, tuple) and all(_deep_immutable(item) for item in value)

def rendered_protocol(start: str, cutoff: str, end: str) -> dict[str, Any]:
    return {"id": PROFILE_PROTOCOL_ID, "k": K, "genres": list(GENRES),
            "window_start_utc": start, "cutoff_utc": cutoff, "window_end_utc": end,
            "training_signal": "distinct_positive_train_viewer_media",
            "normalization": "l1_exact_fraction", "similarity": "dot_product",
            "tie_break": "media_id_ascending", "fallback_rule": "cold_or_zero_profile_to_current_global",
            "availability_rule": "from_lte_cutoff_lt_to",
            "evaluation_role": "development_comparison_on_previously_inspected_synthetic_holdout",
            "max_scored_pairs": MAX_SCORED_PAIRS}


def ensure_work_bound(candidate_count: int, evaluated_viewer_count: int) -> int:
    for label, value in (("candidate_count", candidate_count), ("evaluated_viewer_count", evaluated_viewer_count)):
        if isinstance(value, bool) or not isinstance(value, int) or value < 0:
            raise ValueError(f"{label} must be a nonnegative integer")
    product = candidate_count * evaluated_viewer_count
    if product > MAX_SCORED_PAIRS:
        raise ValueError(f"scored-pair limit exceeded: {product} > {MAX_SCORED_PAIRS}")
    return product


def fraction_text(value: Fraction | None) -> str | None:
    if value is None:
        return None
    if not isinstance(value, Fraction):
        raise TypeError("metric must be Fraction or null")
    return f"{value.numerator}/{value.denominator}"


def validate_ratio(value: object, *, nullable: bool = False) -> None:
    if value is None and nullable:
        return
    if not isinstance(value, str) or not RATIO.fullmatch(value):
        raise ValueError("ratio must be canonical reduced numerator/denominator text")
    numerator, denominator = (int(part) for part in value.split("/"))
    if Fraction(numerator, denominator).numerator != numerator or Fraction(numerator, denominator).denominator != denominator:
        raise ValueError("ratio must be reduced")


def validate_profile_evaluation(value: ProfileEvaluation) -> None:
    if not isinstance(value, ProfileEvaluation):
        raise TypeError("profile evaluation has an unsupported type")
    if value.status not in {"available", "no_holdout", "no_positive_holdout", "no_candidates"}:
        raise ValueError("unknown profile evaluation status")
    if value.status == "available" and (value.precision_at_3 is None or value.recall_at_3 is None):
        raise ValueError("available evaluation must have both metrics")
    if value.status in {"no_holdout", "no_positive_holdout"} and (value.precision_at_3 is not None or value.recall_at_3 is not None):
        raise ValueError("empty-truth evaluation must have null metrics")
    if value.status == "no_candidates" and (value.precision_at_3 != 0 or value.recall_at_3 != 0):
        raise ValueError("positive truth and no candidates must score zero")
    if len(value.candidate_ids) != len(set(value.candidate_ids)):
        raise ValueError("candidate IDs must be unique")
    expected_modes = {"genre_profile", "global_cold_start", "global_no_positive_profile", "no_candidates"}
    modes = dict(value.selection_mode_counts)
    if len(modes) != len(value.selection_mode_counts) or set(modes) != expected_modes or sum(modes.values()) != len(value.queries):
        raise ValueError("selection mode counts must be complete and sum to query count")
    if any(type(count) is not int or count < 0 for count in modes.values()):
        raise ValueError("selection mode counts must be nonnegative integers")
    counts = dict(value.counts)
    required_counts = {"all_holdout_viewers", "evaluated_viewers", "zero_positive_viewers",
                       "positive_viewer_media_pairs", "candidate_unreachable_positive_pairs",
                       "training_viewer_count", "cold_start_evaluated_viewers",
                       "known_zero_profile_evaluated_viewers", "positive_profile_evaluated_viewers",
                       "candidate_count", "returned_ids_total"}
    if len(counts) != len(value.counts) or set(counts) != required_counts or any(type(n) is not int or n < 0 for n in counts.values()):
        raise ValueError("profile evaluation counts have an invalid shape or value")
    if counts["evaluated_viewers"] + counts["zero_positive_viewers"] != counts["all_holdout_viewers"]:
        raise ValueError("profile evaluation does not account for the holdout cohort")
    if sum(counts[name] for name in ("cold_start_evaluated_viewers", "known_zero_profile_evaluated_viewers",
                                    "positive_profile_evaluated_viewers")) != counts["evaluated_viewers"]:
        raise ValueError("profile selection identities do not account for evaluated viewers")
    if counts["candidate_count"] != len(value.candidate_ids) or counts["returned_ids_total"] != sum(len(q.recommendation_ids) for q in value.queries):
        raise ValueError("profile evaluation candidate or returned-item counts disagree")
    if len(value.per_viewer_metrics) != len(value.queries):
        raise ValueError("every evaluated query needs exactly one per-viewer metric")
    if tuple(row.viewer_id for row in value.per_viewer_metrics) != tuple(sorted(row.viewer_id for row in value.per_viewer_metrics)):
        raise ValueError("per-viewer metric rows must be sorted")
    if tuple(q.viewer_id for q in value.queries) != tuple(sorted(q.viewer_id for q in value.queries)):
        raise ValueError("query rows must be sorted by viewer ID")
    if len({q.viewer_id for q in value.queries}) != len(value.queries):
        raise ValueError("query viewer IDs must be unique")
    metric_ids = tuple(row.viewer_id for row in value.per_viewer_metrics)
    if metric_ids != tuple(q.viewer_id for q in value.queries):
        raise ValueError("query and per-viewer metric identities must match")
    allowed_modes = expected_modes
    candidate_set = set(value.candidate_ids)
    for query, row in zip(value.queries, value.per_viewer_metrics):
        if query.mode not in allowed_modes or type(query.training_seen) is not bool:
            raise ValueError("query selection mode or training identity is invalid")
        if type(query.positive_train_pairs) is not int or query.positive_train_pairs < 0:
            raise ValueError("query positive-pair count must be nonnegative")
        score_ids = tuple(media_id for media_id, _ in query.scores_by_media)
        if score_ids != tuple(sorted(candidate_set)) or any(not isinstance(score, Fraction) for _, score in query.scores_by_media):
            raise ValueError("query scores must cover every candidate with exact fractions")
        if len(query.full_order) != len(candidate_set) or set(query.full_order) != candidate_set:
            raise ValueError("query order must be an exact candidate permutation")
        if len(query.recommendation_ids) > K or query.recommendation_ids != query.full_order[:K]:
            raise ValueError("query recommendations must be its at-most-K order prefix")
        if row.status != "available" or tuple(sorted(set(row.positive_target_ids))) != row.positive_target_ids:
            raise ValueError("per-viewer target rows must be available and unique/sorted")
        if not row.positive_target_ids or row.positive_target_count != len(row.positive_target_ids):
            raise ValueError("per-viewer metrics require the complete positive target set")
        if type(row.hit_count) is not int or type(row.positive_target_count) is not int or type(row.candidate_unreachable_positive_pairs) is not int:
            raise ValueError("per-viewer metric counts must be integers")
        expected_hits = len(set(query.recommendation_ids) & set(row.positive_target_ids))
        expected_unreachable = len(set(row.positive_target_ids) - candidate_set)
        if row.hit_count != expected_hits or row.candidate_unreachable_positive_pairs != expected_unreachable:
            raise ValueError("per-viewer hits or unreachable-target count is inconsistent")
        if row.precision_at_3 != Fraction(expected_hits, K) or row.recall_at_3 != Fraction(expected_hits, len(row.positive_target_ids)):
            raise ValueError("per-viewer metrics must use the frozen denominators")
    if value.per_viewer_metrics:
        expected_p = sum((row.precision_at_3 for row in value.per_viewer_metrics), Fraction(0)) / len(value.per_viewer_metrics)
        expected_r = sum((row.recall_at_3 for row in value.per_viewer_metrics), Fraction(0)) / len(value.per_viewer_metrics)
        if value.status == "no_candidates":
            expected_p = expected_r = Fraction(0)
        if (value.precision_at_3, value.recall_at_3) != (expected_p, expected_r):
            raise ValueError("aggregate metrics must be macro means of per-viewer values")
    elif value.status in {"no_holdout", "no_positive_holdout"} and (value.precision_at_3 is not None or value.recall_at_3 is not None):
        raise ValueError("non-evaluable cohorts must have null aggregate metrics")
    if sum(row.positive_target_count for row in value.per_viewer_metrics) != counts["positive_viewer_media_pairs"]:
        raise ValueError("positive viewer-media pair count disagrees with target rows")
    if sum(row.candidate_unreachable_positive_pairs for row in value.per_viewer_metrics) != counts["candidate_unreachable_positive_pairs"]:
        raise ValueError("unreachable target count disagrees with per-viewer rows")
    actual_modes = Counter(query.mode for query in value.queries)
    if any(modes[mode] != actual_modes[mode] for mode in expected_modes):
        raise ValueError("selection mode counts disagree with queries")
    ensure_work_bound(len(value.candidate_ids), len(value.queries))


def validate_predictor_state(value: PredictorState) -> None:
    if not isinstance(value, PredictorState):
        raise TypeError("predictor state has an unsupported type")
    if not _deep_immutable(value.protocol):
        raise ValueError("predictor protocol must be deeply immutable")
    if not re.fullmatch(r"[0-9a-f]{64}", value.fingerprint):
        raise ValueError("predictor fingerprint must be lowercase SHA-256")
    if tuple(sorted(value.candidate_ids)) != value.candidate_ids or len(set(value.candidate_ids)) != len(value.candidate_ids):
        raise ValueError("candidate IDs must be sorted and unique")
    if tuple(sorted(value.training_viewer_ids)) != value.training_viewer_ids or len(set(value.training_viewer_ids)) != len(value.training_viewer_ids):
        raise ValueError("training viewers must be sorted and unique")
    if tuple(mid for mid, _ in value.candidate_features) != value.candidate_ids:
        raise ValueError("candidate feature projection must exactly cover candidates")
    if tuple(viewer for viewer, *_ in value.profiles) != value.training_viewer_ids:
        raise ValueError("profiles must exactly cover training viewers")
    for _, vector in value.candidate_features:
        if len(vector) != len(GENRES) or any(type(x) is not int or x not in (0,1) for x in vector) or sum(vector) != 1:
            raise ValueError("candidate feature must be a one-hot five-coordinate vector")
    for viewer, counts, weights, total in value.profiles:
        if len(counts) != 5 or len(weights) != 5 or type(total) is not int or total < 0:
            raise ValueError(f"invalid profile shape for {viewer}")
        if any(type(x) is not int or x < 0 for x in counts) or sum(counts) != total:
            raise ValueError(f"invalid profile counts for {viewer}")
        expected = tuple(Fraction(x,total) for x in counts) if total else (Fraction(0),)*5
        if weights != expected:
            raise ValueError(f"profile weights disagree with counts for {viewer}")
    support = dict(value.global_support)
    if tuple(mid for mid, _ in value.global_support) != tuple(sorted(support)) or set(support) != set(value.candidate_ids):
        raise ValueError("global support must be sorted and cover candidates")
    expected_order = tuple(sorted(value.candidate_ids, key=lambda mid: (-support[mid], mid)))
    if value.global_order != expected_order or value.global_recommendation_ids != expected_order[:K]:
        raise ValueError("global ordering disagrees with current support")
