"""Chronological acceptance for the frozen toy popularity protocol.

The expected values below are literal hand calculations, independent of the
learner functions. This suite is intentionally red at the named learner TODOs
until each seam is completed.
"""
from __future__ import annotations

import unittest
from datetime import datetime, timezone
from fractions import Fraction

from ai.streamforge import baseline_contracts as seams
from ai.streamforge.evaluation_models import (PROTOCOL_ID, HoldoutTargets,
                                             PopularityState, RankedList)
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


class Day171Test(BaselineFixtureMixin, unittest.TestCase):
    def test_support_counts_distinct_positive_train_viewers_not_events(self):
        pairs = seams.positive_pairs(self.training)
        state = seams.training_popularity(pairs, self.candidate_ids)
        self.assertEqual({"a": 0, "b": 2, "c": 1, "d": 3}, dict(state.support_by_media))
        self.assertFalse(state.no_positive_training_signal)
        changed_holdout = self.holdout + (event("h-extra", "new", "d", 90, "2027-01-24T00:00:00Z"),)
        self.assertEqual(state, seams.training_popularity(pairs, self.candidate_ids))
        self.assertNotEqual(state, seams.training_popularity(pairs | {("v5", "d")}, self.candidate_ids))
        self.assertEqual(changed_holdout[-1].watch_seconds, 90)


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


class Day173Test(BaselineFixtureMixin, unittest.TestCase):
    def test_targets_keep_unreachable_items_and_count_zero_positive_viewer(self):
        targets = seams.holdout_targets(self.holdout)
        self.assertEqual({"v1": frozenset({"d"}), "v2": frozenset({"b", "d"}),
                          "new": frozenset({"c", "e"})}, dict(targets.targets_by_viewer))
        self.assertEqual(frozenset({"v1", "v2", "new", "zero"}), targets.all_holdout_viewer_ids)
        self.assertEqual(1, targets.zero_positive_viewer_count)


class Day174Test(unittest.TestCase):
    def test_fixed_precision_denominator_and_full_recall_truth(self):
        score = seams.score_viewer(("b", "a"), frozenset({"a", "c"}))
        self.assertEqual(1, score.hit_count)
        self.assertEqual(2, score.positive_target_count)
        self.assertEqual(Fraction(1, 3), score.precision_at_3)
        self.assertEqual(Fraction(1, 2), score.recall_at_3)
        with self.assertRaises((TypeError, ValueError)):
            seams.score_viewer(("a", "a"), frozenset({"a"}))
        with self.assertRaises((TypeError, ValueError)):
            seams.score_viewer(("a", "b", "c", "d"), frozenset({"a"}))
        self.assertIsNone(seams.score_viewer(("a",), frozenset()).recall_at_3)


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
    def test_report_separates_prediction_fingerprint_from_holdout_metrics_and_source_hashes(self):
        candidates = seams.cutoff_candidates(self.catalog, self.cutoff)
        state = seams.training_popularity(seams.positive_pairs(self.training), candidates)
        ranking = seams.rank_popularity(state)
        comparison = seams.compare_baselines(ranking, candidates, seams.holdout_targets(self.holdout),
                                            frozenset(e.viewer_id for e in self.training))
        protocol = {"protocol_id": PROTOCOL_ID, "k": 3, "cutoff_utc": "2027-01-20T00:00:00Z"}
        lineage = {"source_sha256": {"events.csv": "a" * 64},
                   "holdout_targets": {"must_not_enter_prediction": ["e"]},
                   "holdout_metrics": {"must_not_enter_prediction": "diagnostic"}}
        report = seams.assemble_evaluation_report(lineage, protocol, candidates, state, ranking, comparison)
        prediction = report["prediction"]
        self.assertEqual(PROTOCOL_ID, prediction["protocol_id"])
        self.assertEqual(("a", "b", "c", "d"), tuple(prediction["candidate_ids"]))
        self.assertEqual(("d", "b", "c"), tuple(prediction["recommendation_ids"]))
        fingerprint = prediction["fingerprint"]
        changed_lineage = {**lineage, "source_sha256": {"events.csv": "b" * 64},
                           "holdout_targets": {"changed": True}, "holdout_metrics": {"changed": True}}
        again = seams.assemble_evaluation_report(changed_lineage, protocol, candidates, state, ranking, comparison)
        self.assertEqual(fingerprint, again["prediction"]["fingerprint"])
