"""Chronological acceptance for the frozen toy popularity protocol.

The expected values below are literal hand calculations, independent of the
learner functions. This suite is intentionally red at the named learner TODOs
until each seam is completed.
"""
from __future__ import annotations

import unittest
import hashlib
import json
from datetime import datetime, timezone
from fractions import Fraction

from ai.streamforge import baseline_contracts as seams
from ai.streamforge.evaluation_models import (PROTOCOL_ID, HoldoutTargets,
                                             PopularityState, RankedList)
from ai.streamforge.evaluation_io import fixed_protocol
from ai.streamforge.models import CatalogItem, ViewingEvent


def instant(value: str) -> datetime:
    return datetime.strptime(value, "%Y-%m-%dT%H:%M:%SZ").replace(tzinfo=timezone.utc)


def item(media_id: str, start: str, end: str) -> CatalogItem:
    return CatalogItem(media_id, f"Synthetic {media_id}", "drama", 100,
                       instant(start), instant(end))


def event(event_id: str, viewer: str, media: str, watch: int, when: str) -> ViewingEvent:
    return ViewingEvent(event_id, viewer, media, instant(when), watch)


class BaselineFixtureMixin:
    cutoff = instant("2027-01-20T00:00:00Z")
    catalog = (
        item("a", "2027-01-20T00:00:00Z", "2027-02-01T00:00:00Z"),
        item("b", "2027-01-01T00:00:00Z", "2027-03-01T00:00:00Z"),
        item("c", "2027-01-01T00:00:00Z", "2027-03-01T00:00:00Z"),
        item("d", "2027-01-01T00:00:00Z", "2027-03-01T00:00:00Z"),
        item("e", "2027-01-21T00:00:00Z", "2027-03-01T00:00:00Z"),
        item("x", "2027-01-01T00:00:00Z", "2027-01-20T00:00:00Z"),
    )
    training = (
        event("t-d-v1-a", "v1", "d", 1, "2027-01-10T00:00:00Z"),
        event("t-d-v1-b", "v1", "d", 2, "2027-01-11T00:00:00Z"),
        event("t-d-v2", "v2", "d", 1, "2027-01-12T00:00:00Z"),
        event("t-d-v3", "v3", "d", 1, "2027-01-13T00:00:00Z"),
        event("t-b-v1", "v1", "b", 1, "2027-01-10T00:00:00Z"),
        event("t-b-v2", "v2", "b", 1, "2027-01-11T00:00:00Z"),
        event("t-b-v4-zero", "v4", "b", 0, "2027-01-12T00:00:00Z"),
        event("t-c-v1", "v1", "c", 1, "2027-01-13T00:00:00Z"),
        *(event(f"t-x-{viewer}", viewer, "x", 1, "2027-01-14T00:00:00Z")
          for viewer in ("v1", "v2", "v3", "v4")),
    )
    holdout = (
        event("h-v1-d-a", "v1", "d", 1, "2027-01-22T00:00:00Z"),
        event("h-v1-d-b", "v1", "d", 1, "2027-01-23T00:00:00Z"),
        event("h-v2-b", "v2", "b", 1, "2027-01-22T00:00:00Z"),
        event("h-v2-d", "v2", "d", 1, "2027-01-23T00:00:00Z"),
        event("h-new-c", "new", "c", 1, "2027-01-22T00:00:00Z"),
        event("h-new-e", "new", "e", 1, "2027-01-22T00:00:00Z"),
        event("h-zero-b", "zero", "b", 0, "2027-01-22T00:00:00Z"),
    )
    candidate_ids = ("a", "b", "c", "d")


class Day169Test(BaselineFixtureMixin, unittest.TestCase):
    def test_positive_pairs_are_distinct_and_ignore_zero_watch_without_mutation(self):
        source = list(self.training)
        pairs = seams.positive_pairs(source)
        self.assertEqual(frozenset({("v1", "d"), ("v2", "d"), ("v3", "d"),
                                    ("v1", "b"), ("v2", "b"), ("v1", "c"),
                                    ("v1", "x"), ("v2", "x"), ("v3", "x"), ("v4", "x")}), pairs)
        self.assertEqual(list(self.training), source)


class Day170Test(BaselineFixtureMixin, unittest.TestCase):
    def test_cutoff_candidate_boundaries_and_zero_history_item(self):
        self.assertEqual(("a", "b", "c", "d"), seams.cutoff_candidates(self.catalog, self.cutoff))

    def test_unsorted_mutable_catalog_is_not_reordered_in_place(self):
        source = list(reversed(self.catalog))
        before = list(source)
        self.assertEqual(("a", "b", "c", "d"), seams.cutoff_candidates(source, self.cutoff))
        self.assertEqual(before, source)


class Day171Test(BaselineFixtureMixin, unittest.TestCase):
    def test_support_counts_distinct_positive_train_viewers_not_events(self):
        pairs = seams.positive_pairs(self.training)
        state = seams.training_popularity(pairs, self.candidate_ids)
        self.assertEqual({"a": 0, "b": 2, "c": 1, "d": 3}, dict(state.support_by_media))
        self.assertFalse(state.no_positive_training_signal)
        self.assertEqual(state, seams.training_popularity(pairs, self.candidate_ids))
        self.assertNotEqual(state, seams.training_popularity(pairs | {("v5", "d")}, self.candidate_ids))
        changed_positive_magnitude = tuple(
            event(e.event_id, e.viewer_id, e.media_id, 2 if e.watch_seconds == 1 else e.watch_seconds,
                  e.occurred_at_utc.strftime("%Y-%m-%dT%H:%M:%SZ")) for e in self.training
        )
        self.assertEqual(pairs, seams.positive_pairs(changed_positive_magnitude))


class Day172Test(BaselineFixtureMixin, unittest.TestCase):
    def test_global_top_three_is_stable_on_ties_and_short_lists(self):
        state = PopularityState({"a": 0, "b": 2, "c": 1, "d": 3}, False)
        ranked = seams.rank_popularity(state)
        self.assertEqual(("d", "b", "c", "a"), ranked.candidate_ids)
        self.assertEqual(("d", "b", "c"), ranked.recommendation_ids)
        tied = seams.rank_popularity(PopularityState({"z": 1, "a": 1, "m": 1}, False))
        self.assertEqual(("a", "m", "z"), tied.recommendation_ids)
        self.assertEqual(("a", "b"), seams.rank_popularity(
            PopularityState({"b": 0, "a": 0}, True)).recommendation_ids)
        fallback = seams.rank_popularity(PopularityState({"b": 0, "a": 0}, True))
        self.assertEqual("id_order_no_positive_training_signal", fallback.fallback_status)

    def test_same_candidates_with_changed_support_change_the_ranking(self):
        first = seams.rank_popularity(PopularityState({"a": 0, "b": 2, "c": 1, "d": 3}, False))
        changed = seams.rank_popularity(PopularityState({"a": 5, "b": 0, "c": 0, "d": 1}, False))
        self.assertEqual(set(first.candidate_ids), set(changed.candidate_ids))
        self.assertEqual(("d", "b", "c"), first.recommendation_ids)
        self.assertEqual(("a", "d", "b"), changed.recommendation_ids)
        self.assertNotEqual(first.recommendation_ids, changed.recommendation_ids)


class Day173Test(BaselineFixtureMixin, unittest.TestCase):
    def test_targets_keep_unreachable_items_and_count_zero_positive_viewer(self):
        source = list(reversed(self.holdout))
        before = list(source)
        targets = seams.holdout_targets(source)
        self.assertEqual({"v1": frozenset({"d"}), "v2": frozenset({"b", "d"}),
                          "new": frozenset({"c", "e"})}, dict(targets.targets_by_viewer))
        self.assertEqual(frozenset({"v1", "v2", "new", "zero"}), targets.all_holdout_viewer_ids)
        self.assertEqual(1, targets.zero_positive_viewer_count)
        self.assertEqual(before, source)


class Day174Test(unittest.TestCase):
    def test_fixed_precision_denominator_and_full_recall_truth(self):
        score = seams.score_viewer(("b", "a"), frozenset({"a", "c"}))
        self.assertEqual(1, score.hit_count)
        self.assertEqual(2, score.positive_target_count)
        self.assertEqual(Fraction(1, 3), score.precision_at_3)
        self.assertEqual(Fraction(1, 2), score.recall_at_3)
        all_hit = seams.score_viewer(("a", "b", "c"), frozenset({"a", "b", "c"}))
        self.assertEqual((Fraction(1, 1), Fraction(1, 1)),
                         (all_hit.precision_at_3, all_hit.recall_at_3))
        all_miss = seams.score_viewer(("a", "b", "c"), frozenset({"d"}))
        self.assertEqual((Fraction(0, 1), Fraction(0, 1)),
                         (all_miss.precision_at_3, all_miss.recall_at_3))
        with self.assertRaises((TypeError, ValueError)):
            seams.score_viewer(("a", "a"), frozenset({"a"}))
        with self.assertRaises((TypeError, ValueError)):
            seams.score_viewer(("a", "b", "c", "d"), frozenset({"a"}))
        empty_targets = seams.score_viewer(("a",), frozenset())
        self.assertIsNone(empty_targets.precision_at_3)
        self.assertIsNone(empty_targets.recall_at_3)


class Day176Test(BaselineFixtureMixin, unittest.TestCase):
    def test_macro_metrics_include_all_positive_targets_and_explicit_cohort_counts(self):
        candidates = seams.cutoff_candidates(self.catalog, self.cutoff)
        pairs = seams.positive_pairs(self.training)
        state = seams.training_popularity(pairs, candidates)
        ranked = seams.rank_popularity(state)
        targets = seams.holdout_targets(self.holdout)
        result = seams.evaluate_baseline(ranked, candidates, targets,
                                         frozenset(e.viewer_id for e in self.training))
        self.assertEqual(Fraction(4, 9), result.precision_at_3)
        self.assertEqual(Fraction(5, 6), result.recall_at_3)
        self.assertEqual((4, 3, 1, 5, 1, 4, 1, 4, 3),
                         (result.all_holdout_viewers, result.evaluated_viewers,
                          result.zero_positive_viewers, result.positive_viewer_media_pairs,
                          result.candidate_unreachable_positive_pairs, result.training_viewer_count,
                          result.cold_start_evaluated_viewers, result.candidate_count,
                          result.returned_count))

    def test_empty_states_are_explicit_and_never_fabricate_metrics(self):
        candidates = ("a", "b", "c", "d")
        ranking = seams.rank_popularity(PopularityState({key: 0 for key in candidates}, True))
        empty_holdout = seams.holdout_targets(())
        no_holdout = seams.evaluate_baseline(ranking, candidates, empty_holdout, frozenset())
        self.assertEqual("no_holdout", no_holdout.status)
        self.assertIsNone(no_holdout.precision_at_3)
        self.assertIsNone(no_holdout.recall_at_3)

        only_zero = (event("zero-only", "viewer-z", "a", 0, "2027-01-22T00:00:00Z"),)
        no_positive = seams.holdout_targets(only_zero)
        empty_truth = seams.evaluate_baseline(ranking, candidates, no_positive, frozenset())
        self.assertEqual("no_positive_holdout", empty_truth.status)
        self.assertEqual(1, empty_truth.zero_positive_viewers)
        self.assertIsNone(empty_truth.precision_at_3)
        self.assertIsNone(empty_truth.recall_at_3)

        target = seams.holdout_targets((event("future-target", "cold", "e", 1,
                                              "2027-01-22T00:00:00Z"),))
        no_candidates = seams.evaluate_baseline(
            seams.rank_popularity(PopularityState({}, True)), (), target, frozenset())
        self.assertEqual("no_candidates", no_candidates.status)
        self.assertEqual(Fraction(0, 1), no_candidates.precision_at_3)
        self.assertEqual(Fraction(0, 1), no_candidates.recall_at_3)
        self.assertEqual(1, no_candidates.candidate_unreachable_positive_pairs)

    def test_shared_unreachable_item_counts_each_viewer_pair_and_zero_watch_train_is_seen(self):
        zero_only_training = (event("zero-only-training", "zero-train", "a", 0,
                                    "2027-01-12T00:00:00Z"),)
        holdout = (
            event("unreachable-v1", "cold-1", "future", 1, "2027-01-22T00:00:00Z"),
            event("unreachable-v2", "cold-2", "future", 1, "2027-01-22T00:00:00Z"),
            event("known-by-zero-only", "zero-train", "future", 1, "2027-01-22T00:00:00Z"),
        )
        targets = seams.holdout_targets(holdout)
        ranking = seams.rank_popularity(PopularityState({}, True))
        training_viewers = frozenset(value.viewer_id for value in zero_only_training)
        self.assertEqual(frozenset({"zero-train"}), training_viewers)
        result = seams.evaluate_baseline(ranking, (), targets, training_viewers)
        self.assertEqual(3, result.positive_viewer_media_pairs)
        self.assertEqual(3, result.candidate_unreachable_positive_pairs)
        self.assertEqual(2, result.cold_start_evaluated_viewers)
        self.assertEqual(1, result.training_viewer_count)


class Day177Test(BaselineFixtureMixin, unittest.TestCase):
    def test_same_cohort_reference_and_signed_deltas(self):
        candidates = seams.cutoff_candidates(self.catalog, self.cutoff)
        state = seams.training_popularity(seams.positive_pairs(self.training), candidates)
        compared = seams.compare_baselines(seams.rank_popularity(state), candidates,
                                           seams.holdout_targets(self.holdout),
                                           frozenset(e.viewer_id for e in self.training))
        self.assertEqual(Fraction(4, 9), compared.popularity.precision_at_3)
        self.assertEqual(Fraction(5, 6), compared.popularity.recall_at_3)
        self.assertEqual(Fraction(2, 9), compared.reference.precision_at_3)
        self.assertEqual(Fraction(1, 3), compared.reference.recall_at_3)
        self.assertEqual(Fraction(2, 9), compared.precision_delta)
        self.assertEqual(Fraction(1, 2), compared.recall_delta)

    def test_separate_fixture_allows_the_fixed_reference_to_win(self):
        candidates = ("a", "b", "c", "d")
        popularity = seams.training_popularity(frozenset({("v1", "d"), ("v2", "d"),
                                                          ("v3", "b"), ("v4", "c")}), candidates)
        ranking = seams.rank_popularity(popularity)
        targets = seams.holdout_targets((event("held", "test-viewer", "a", 1,
                                                "2027-01-22T00:00:00Z"),))
        compared = seams.compare_baselines(ranking, candidates, targets, frozenset({"v1", "v2", "v3", "v4"}))
        self.assertEqual(Fraction(0, 1), compared.popularity.recall_at_3)
        self.assertEqual(Fraction(1, 1), compared.reference.recall_at_3)
        self.assertEqual(Fraction(-1, 1), compared.recall_delta)


class Day178Test(BaselineFixtureMixin, unittest.TestCase):
    EXPECTED_PROTOCOL = {
        "protocol_id": "streamforge-cutoff-popularity-v1", "k": 3,
        "positive_rule": "watch_seconds > 0",
        "support": "distinct positive training viewers per media",
        "candidate_rule": "available_from_utc <= cutoff_utc < available_to_utc",
        "cutoff_utc": "2027-01-20T00:00:00Z",
        "ranking": "support descending, exact media_id ascending",
        "scope": "one global list for all viewers; no seen-item removal or event-time reranking",
        "precision_denominator": 3,
        "recall_targets": "all distinct positive holdout media IDs, including unreachable items",
        "aggregation": "unweighted macro over positive-target viewers",
    }
    EXPECTED_LIMITS = [
        "Synthetic observed-positive overlap only; no reliable negative labels or impressions.",
        "The catalog schedule is assumed known at cutoff; no historical catalog snapshot is demonstrated.",
        "One static global list is used over the holdout horizon; no per-event availability reranking.",
        "Metrics do not establish preference, unbiased real-user quality, causal lift, fairness, or business success.",
        "Content hashes identify bytes and do not establish rights, authenticity, or quality.",
    ]

    def _comparison(self, candidates, holdout):
        state = seams.training_popularity(seams.positive_pairs(self.training), candidates)
        ranked = seams.rank_popularity(state)
        comparison = seams.compare_baselines(ranked, candidates, seams.holdout_targets(holdout),
                                             frozenset(e.viewer_id for e in self.training))
        return state, ranked, comparison

    def _lineage(self):
        return {
            "schema_version": 1, "data_version": 1, "fixture_id": "synthetic-unit",
            "window_start_utc": "2027-01-01T00:00:00Z", "cutoff_utc": "2027-01-20T00:00:00Z",
            "window_end_utc": "2027-02-01T00:00:00Z",
            "preparation_status": {"training": "available", "holdout": "available"},
            "training": {"status": "available", "event_count": len(self.training),
                         "distinct_viewer_count": 4,
                         "media_event_counts": {"d": 4, "b": 3, "x": 4, "c": 1},
                         "media_watch_seconds": {"d": 5, "b": 2, "x": 4, "c": 1},
                         "content_sha256": "a" * 64},
            "holdout_diagnostics": {"status": "available", "viewer_item_seen_in_train": 4,
                                    "viewer_seen_item_unseen": 0, "viewer_unseen_item_seen": 2,
                                    "viewer_item_unseen": 1},
            "source_sha256": {"manifest.json": "b" * 64, "catalog.csv": "c" * 64,
                              "events.csv": "d" * 64},
            "duplicate_events_collapsed": 0, "manifest_sha256": "b" * 64,
            "catalog_count": len(self.catalog),
            "partition_counts": {"train": len(self.training), "holdout": len(self.holdout), "outside_window": 0},
        }

    def _expected_report(self, lineage):
        prediction_inputs = {
            "protocol_id": "streamforge-cutoff-popularity-v1", "cutoff_utc": "2027-01-20T00:00:00Z",
            "k": 3, "candidate_ids": ["a", "b", "c", "d"],
            "support_by_media": {"a": 0, "b": 2, "c": 1, "d": 3},
            "recommendation_ids": ["d", "b", "c"], "fallback_status": "popularity_ranked",
        }
        fingerprint = hashlib.sha256(json.dumps(
            prediction_inputs, ensure_ascii=False, allow_nan=False, sort_keys=True,
            separators=(",", ":")).encode("utf-8") + b"\n").hexdigest()

        return {
            "schema_version": 1,
            "protocol": dict(self.EXPECTED_PROTOCOL),
            "prediction": {**prediction_inputs, "fingerprint": fingerprint},
            "baselines": {
                "popularity": {"recommendation_ids": ["d", "b", "c"], "status": "available",
                    "precision_at_3": "4/9", "recall_at_3": "5/6", "counts": {
                        "all_holdout_viewers": 4, "evaluated_viewers": 3, "zero_positive_viewers": 1,
                        "positive_viewer_media_pairs": 5, "candidate_unreachable_positive_pairs": 1,
                        "training_viewer_count": 4, "cold_start_evaluated_viewers": 1,
                        "candidate_count": 4, "returned_count": 3}},
                "id_order_reference": {"recommendation_ids": ["a", "b", "c"], "status": "available",
                    "precision_at_3": "2/9", "recall_at_3": "1/3", "counts": {
                        "all_holdout_viewers": 4, "evaluated_viewers": 3, "zero_positive_viewers": 1,
                        "positive_viewer_media_pairs": 5, "candidate_unreachable_positive_pairs": 1,
                        "training_viewer_count": 4, "cold_start_evaluated_viewers": 1,
                        "candidate_count": 4, "returned_count": 3}},
                "signed_deltas": {"precision_at_3": "2/9", "recall_at_3": "1/2"},
                "status": "available",
            },
            "preparation": dict(lineage),
            "limits": list(self.EXPECTED_LIMITS),
        }

    def test_complete_report_matches_independent_full_oracle_and_separates_lineage(self):
        candidates = seams.cutoff_candidates(self.catalog, self.cutoff)
        state, ranking, comparison = self._comparison(candidates, self.holdout)
        protocol = fixed_protocol("2027-01-20T00:00:00Z")
        lineage = self._lineage()
        report = seams.assemble_evaluation_report(lineage, protocol, candidates, state, ranking, comparison)
        self.assertEqual(self.EXPECTED_PROTOCOL, protocol)
        expected = self._expected_report(lineage)
        self.assertEqual(expected, report, "entire report must include metrics, counts, lineage and limits")
        self.assertEqual({"schema_version", "protocol", "prediction", "baselines", "preparation", "limits"},
                         set(report))

    def test_holdout_composition_changes_exact_metrics_but_not_entire_prediction(self):
        candidates = seams.cutoff_candidates(self.catalog, self.cutoff)
        state, ranking, base_comparison = self._comparison(candidates, self.holdout)
        protocol = fixed_protocol("2027-01-20T00:00:00Z")
        lineage = self._lineage()
        first = seams.assemble_evaluation_report(lineage, protocol, candidates, state, ranking, base_comparison)

        changed_holdout = self.holdout + (event("h-v2-future", "v2", "e", 1,
                                                "2027-01-24T00:00:00Z"),)
        _, _, changed_comparison = self._comparison(candidates, changed_holdout)
        changed_lineage = {**lineage,
                           "source_sha256": {**lineage["source_sha256"], "events.csv": "e" * 64},
                           "holdout_diagnostics": {**lineage["holdout_diagnostics"],
                                                   "viewer_seen_item_unseen": 1,
                                                   "viewer_item_unseen": 1},
                           "partition_counts": {**lineage["partition_counts"],
                                                "holdout": len(changed_holdout)}}
        changed = seams.assemble_evaluation_report(changed_lineage, protocol, candidates, state, ranking,
                                                  changed_comparison)
        self.assertEqual(first["prediction"], changed["prediction"])
        self.assertEqual("4/9", first["baselines"]["popularity"]["precision_at_3"])
        self.assertEqual("5/6", first["baselines"]["popularity"]["recall_at_3"])
        self.assertEqual("4/9", changed["baselines"]["popularity"]["precision_at_3"])
        self.assertEqual("13/18", changed["baselines"]["popularity"]["recall_at_3"])
        self.assertEqual("5/18", changed["baselines"]["id_order_reference"]["recall_at_3"])
        self.assertEqual("4/9", changed["baselines"]["signed_deltas"]["recall_at_3"])
        self.assertEqual(2, changed["baselines"]["popularity"]["counts"]["candidate_unreachable_positive_pairs"])
        self.assertNotEqual(first["preparation"]["source_sha256"], changed["preparation"]["source_sha256"])

    def test_positive_watch_magnitude_does_not_change_targets_metrics_or_prediction(self):
        adjusted = tuple(event(e.event_id, e.viewer_id, e.media_id,
                               2 if e.watch_seconds == 1 else e.watch_seconds,
                               e.occurred_at_utc.strftime("%Y-%m-%dT%H:%M:%SZ")) for e in self.holdout)
        self.assertEqual(seams.holdout_targets(self.holdout), seams.holdout_targets(adjusted))
        candidates = seams.cutoff_candidates(self.catalog, self.cutoff)
        state, ranked, first_comparison = self._comparison(candidates, self.holdout)
        _, _, adjusted_comparison = self._comparison(candidates, adjusted)
        protocol = fixed_protocol("2027-01-20T00:00:00Z")
        first = seams.assemble_evaluation_report(self._lineage(), protocol, candidates, state, ranked,
                                                 first_comparison)
        second = seams.assemble_evaluation_report(self._lineage(), protocol, candidates, state, ranked,
                                                  adjusted_comparison)
        self.assertEqual(first["prediction"], second["prediction"])
        self.assertEqual(first["baselines"], second["baselines"])

    def test_catalog_eligibility_change_changes_candidates_and_prediction_fingerprint(self):
        from ai.streamforge.evaluation_io import prediction_fingerprint
        candidates = seams.cutoff_candidates(self.catalog, self.cutoff)
        state, ranked, _ = self._comparison(candidates, self.holdout)
        protocol = fixed_protocol("2027-01-20T00:00:00Z")
        first = prediction_fingerprint(protocol, candidates, state, ranked)
        changed_catalog = tuple(
            item(value.media_id,
                 value.available_from_utc.strftime("%Y-%m-%dT%H:%M:%SZ"),
                 "2027-01-20T00:00:00Z") if value.media_id == "d" else value
            for value in self.catalog
        )
        changed_candidates = seams.cutoff_candidates(changed_catalog, self.cutoff)
        changed_state = seams.training_popularity(seams.positive_pairs(self.training), changed_candidates)
        changed_ranked = seams.rank_popularity(changed_state)
        second = prediction_fingerprint(protocol, changed_candidates, changed_state, changed_ranked)
        self.assertEqual(("a", "b", "c"), changed_candidates)
        self.assertNotEqual(first, second)
