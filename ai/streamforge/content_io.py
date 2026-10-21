"""Instructor-owned protocol, validation, hashing, and JSON projection helpers."""
from __future__ import annotations

import hashlib
from collections import Counter
import re
from fractions import Fraction
from collections.abc import Mapping, Sequence
from typing import Any

from .content_models import GENRES, MAX_SCORED_PAIRS, PROFILE_PROTOCOL_ID, ProfileEvaluation, PredictorState, QueryPrediction, ViewerMetric
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
    if value is None or type(value) in (str, bool, int) or isinstance(value, Fraction):
        return True
    return type(value) is tuple and all(_deep_immutable(item) for item in value)

def _require_tuple_tree(value: object, label: str) -> None:
    if not _deep_immutable(value):
        raise ValueError(f"{label} must use deeply immutable tuples and scalar values")

def _protocol_dict(protocol: tuple[tuple[str, object], ...]) -> dict[str, object]:
    if type(protocol) is not tuple or any(type(pair) is not tuple or len(pair) != 2 or type(pair[0]) is not str for pair in protocol):
        raise ValueError("protocol must be a sorted immutable tuple of key/value pairs")
    keys = tuple(key for key, _ in protocol)
    if keys != tuple(sorted(keys)) or len(keys) != len(set(keys)):
        raise ValueError("protocol keys must be sorted and unique")
    return dict(protocol)

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
    if any(type(getattr(value,name)) is not tuple for name in ("candidate_ids","queries","per_viewer_metrics","counts","selection_mode_counts")):
        raise ValueError("evaluation collections must be immutable tuples")
    _require_tuple_tree((value.candidate_ids,value.counts,value.selection_mode_counts),"evaluation values")
    if any(type(q) is not QueryPrediction for q in value.queries):
        raise ValueError("query rows must use the typed immutable query contract")
    if any(type(row) is not ViewerMetric for row in value.per_viewer_metrics):
        raise ValueError("metric rows must use the typed immutable metric contract")
    if type(value.candidate_ids) is not tuple or any(type(x) is not str or not x for x in value.candidate_ids):
        raise ValueError("candidate IDs must be nonempty strings in an immutable tuple")
    if value.status == "available" and (type(value.precision_at_3) is not Fraction or type(value.recall_at_3) is not Fraction):
        raise ValueError("available evaluation must have both metrics")
    for metric in (value.precision_at_3,value.recall_at_3):
        if metric is not None and (type(metric) is not Fraction or not Fraction(0) <= metric <= Fraction(1)):
            raise ValueError("aggregate metrics must be exact Fractions between zero and one")
    if value.status in {"no_holdout", "no_positive_holdout"} and (value.precision_at_3 is not None or value.recall_at_3 is not None):
        raise ValueError("non-evaluable status requires null metrics")
    if value.status in {"no_holdout", "no_positive_holdout"} and (value.precision_at_3 is not None or value.recall_at_3 is not None):
        raise ValueError("empty-truth evaluation must have null metrics")
    if value.status == "no_candidates" and (value.precision_at_3 != Fraction(0) or value.recall_at_3 != Fraction(0)):
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
    if value.status == "no_holdout" and (counts["all_holdout_viewers"] != 0 or counts["evaluated_viewers"] != 0):
        raise ValueError("no_holdout status requires an empty holdout")
    if value.status == "no_positive_holdout" and (counts["all_holdout_viewers"] == 0 or counts["evaluated_viewers"] != 0 or counts["zero_positive_viewers"] == 0):
        raise ValueError("no_positive_holdout status requires only zero-positive viewers")
    if value.status == "available" and (counts["evaluated_viewers"] == 0 or counts["candidate_count"] == 0):
        raise ValueError("available status requires evaluated viewers and candidates")
    if value.status == "no_candidates" and (counts["candidate_count"] != 0 or counts["evaluated_viewers"] == 0):
        raise ValueError("no_candidates status requires positive truth and no candidates")
    if sum(counts[name] for name in ("cold_start_evaluated_viewers", "known_zero_profile_evaluated_viewers",
                                    "positive_profile_evaluated_viewers")) != counts["evaluated_viewers"]:
        raise ValueError("profile selection identities do not account for evaluated viewers")
    if counts["candidate_count"] != len(value.candidate_ids) or counts["returned_ids_total"] != sum(len(q.recommendation_ids) for q in value.queries):
        raise ValueError("profile evaluation candidate or returned-item counts disagree")
    if counts["evaluated_viewers"] != len(value.queries) or counts["evaluated_viewers"] != len(value.per_viewer_metrics):
        raise ValueError("evaluated viewer count must equal query and metric row counts")
    if counts["candidate_count"] == 0 and value.queries and any(q.mode != "no_candidates" for q in value.queries):
        raise ValueError("empty candidate set must use no_candidates mode")
    if counts["candidate_count"] > 0 and any(q.mode == "no_candidates" for q in value.queries):
        raise ValueError("nonempty candidate set cannot use no_candidates mode")
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
    identity_counts = {"cold":0,"zero":0,"positive":0}
    for query, row in zip(value.queries, value.per_viewer_metrics):
        if type(query.viewer_id) is not str or not query.viewer_id or query.mode not in allowed_modes or type(query.training_seen) is not bool:
            raise ValueError("query selection mode or training identity is invalid")
        if type(query.positive_train_pairs) is not int or query.positive_train_pairs < 0:
            raise ValueError("query positive-pair count must be nonnegative")
        if query.mode == "genre_profile" and (not query.training_seen or query.positive_train_pairs == 0):
            raise ValueError("genre_profile requires a seen viewer with positive training pairs")
        if query.mode == "global_cold_start" and (query.training_seen or query.positive_train_pairs != 0):
            raise ValueError("global_cold_start requires an unseen viewer")
        if query.mode == "global_no_positive_profile" and (not query.training_seen or query.positive_train_pairs != 0):
            raise ValueError("global_no_positive_profile requires a seen zero-positive viewer")
        identity = "cold" if not query.training_seen else ("positive" if query.positive_train_pairs else "zero")
        identity_counts[identity] += 1
        if type(query.scores_by_media) is not tuple or any(type(pair) is not tuple or len(pair) != 2 for pair in query.scores_by_media):
            raise ValueError("query scores must be immutable media/score pairs")
        if type(query.full_order) is not tuple or type(query.recommendation_ids) is not tuple or any(type(x) is not str or not x for x in query.full_order + query.recommendation_ids):
            raise ValueError("query order and recommendations must be immutable ID tuples")
        if type(row.positive_target_ids) is not tuple or any(type(x) is not str or not x for x in row.positive_target_ids):
            raise ValueError("positive target IDs must be immutable ID tuples")
        if any(type(score) is not Fraction or not Fraction(0) <= score <= Fraction(1) for _, score in query.scores_by_media):
            raise ValueError("candidate scores must be exact Fractions between zero and one")
        score_ids = tuple(media_id for media_id, _ in query.scores_by_media)
        if score_ids != tuple(sorted(candidate_set)):
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
        if any(type(metric) is not Fraction for metric in (row.precision_at_3,row.recall_at_3)):
            raise ValueError("per-viewer metrics must be exact Fractions")
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
    expected_identity_counts = {"cold_start_evaluated_viewers": identity_counts["cold"],
                                "known_zero_profile_evaluated_viewers": identity_counts["zero"],
                                "positive_profile_evaluated_viewers": identity_counts["positive"]}
    if any(counts[key] != n for key,n in expected_identity_counts.items()):
        raise ValueError("viewer identity diagnostics disagree with query identities")
    ensure_work_bound(len(value.candidate_ids), len(value.queries))


def validate_predictor_state(value: PredictorState) -> None:
    if not isinstance(value, PredictorState):
        raise TypeError("predictor state has an unsupported type")
    _require_tuple_tree(value.protocol, "predictor protocol")
    protocol = _protocol_dict(value.protocol)
    protocol_keys = {"id","k","genres","window_start_utc","cutoff_utc","window_end_utc","training_signal","normalization","similarity","tie_break","fallback_rule","availability_rule","evaluation_role","max_scored_pairs"}
    if set(protocol) != protocol_keys or protocol["id"] != PROFILE_PROTOCOL_ID or type(protocol["k"]) is not int or protocol["k"] != K:
        raise ValueError("predictor protocol does not match the frozen contract")
    if (protocol["genres"] != GENRES or protocol["training_signal"] != "distinct_positive_train_viewer_media"
            or protocol["normalization"] != "l1_exact_fraction" or protocol["similarity"] != "dot_product"
            or protocol["tie_break"] != "media_id_ascending" or protocol["fallback_rule"] != "cold_or_zero_profile_to_current_global"
            or protocol["availability_rule"] != "from_lte_cutoff_lt_to"
            or protocol["evaluation_role"] != "development_comparison_on_previously_inspected_synthetic_holdout"
            or type(protocol["max_scored_pairs"]) is not int or protocol["max_scored_pairs"] != MAX_SCORED_PAIRS):
        raise ValueError("predictor protocol values do not match the frozen contract")
    for key in ("window_start_utc", "cutoff_utc", "window_end_utc"):
        if type(protocol[key]) is not str or not re.fullmatch(r"[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z", protocol[key]):
            raise ValueError("predictor protocol window must use canonical UTC timestamps")
    _require_tuple_tree((value.candidate_ids,value.candidate_features,value.training_viewer_ids,value.profiles,value.global_support,value.global_order,value.global_recommendation_ids), "predictor state")
    if any(type(x) is not str or not x for x in value.candidate_ids + value.training_viewer_ids):
        raise ValueError("candidate and viewer IDs must be nonempty strings")
    if not re.fullmatch(r"[0-9a-f]{64}", value.fingerprint):
        raise ValueError("predictor fingerprint must be lowercase SHA-256")
    if tuple(sorted(value.candidate_ids)) != value.candidate_ids or len(set(value.candidate_ids)) != len(value.candidate_ids):
        raise ValueError("candidate IDs must be sorted and unique")
    if tuple(sorted(value.training_viewer_ids)) != value.training_viewer_ids or len(set(value.training_viewer_ids)) != len(value.training_viewer_ids):
        raise ValueError("training viewers must be sorted and unique")
    if tuple(mid for mid, _ in value.candidate_features) != value.candidate_ids:
        raise ValueError("candidate feature projection must exactly cover candidates")
    if any(type(pair) is not tuple or len(pair) != 2 or type(pair[0]) is not str or type(pair[1]) is not tuple for pair in value.candidate_features):
        raise ValueError("candidate features must be immutable media/vector pairs")
    if tuple(viewer for viewer, *_ in value.profiles) != value.training_viewer_ids:
        raise ValueError("profiles must exactly cover training viewers")
    for _, vector in value.candidate_features:
        if len(vector) != len(GENRES) or any(type(x) is not int or x not in (0,1) for x in vector) or sum(vector) != 1:
            raise ValueError("candidate feature must be a one-hot five-coordinate vector")
    for viewer, counts, weights, total in value.profiles:
        if type(viewer) is not str or type(counts) is not tuple or type(weights) is not tuple or len(counts) != 5 or len(weights) != 5 or type(total) is not int or total < 0:
            raise ValueError(f"invalid profile shape for {viewer}")
        if any(type(w) is not Fraction for w in weights):
            raise ValueError(f"profile weights must be exact Fractions for {viewer}")
        if any(type(x) is not int or x < 0 for x in counts) or sum(counts) != total:
            raise ValueError(f"invalid profile counts for {viewer}")
        expected = tuple(Fraction(x,total) for x in counts) if total else (Fraction(0),)*5
        if weights != expected:
            raise ValueError(f"profile weights disagree with counts for {viewer}")
    support = dict(value.global_support)
    if any(type(pair) is not tuple or len(pair) != 2 or type(pair[0]) is not str or type(pair[1]) is not int or pair[1] < 0 for pair in value.global_support):
        raise ValueError("global support must contain nonnegative integer values")
    if tuple(mid for mid, _ in value.global_support) != tuple(sorted(support)) or set(support) != set(value.candidate_ids):
        raise ValueError("global support must be sorted and cover candidates")
    expected_order = tuple(sorted(value.candidate_ids, key=lambda mid: (-support[mid], mid)))
    if value.global_order != expected_order or value.global_recommendation_ids != expected_order[:K]:
        raise ValueError("global ordering disagrees with current support")
    expected_fallback = "id_order_no_positive_training_signal" if not any(support.values()) else "popularity_ranked"
    if value.global_fallback_status != expected_fallback:
        raise ValueError("global fallback status disagrees with support")
