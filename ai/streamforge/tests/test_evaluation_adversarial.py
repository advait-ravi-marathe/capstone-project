"""Independent assertions for common denominator, cohort and leakage mistakes."""
from __future__ import annotations

import unittest
from fractions import Fraction

from ai.streamforge.tests.test_baseline_contracts import (
    BaselineFixtureMixin, event, seams,
)
from ai.streamforge.evaluation_models import PopularityState


class Day180Test(BaselineFixtureMixin, unittest.TestCase):
    def test_duplicate_activity_zero_watch_expired_item_and_holdout_leak_are_detected(self):
        pairs = seams.positive_pairs(self.training)
        candidates = seams.cutoff_candidates(self.catalog, self.cutoff)
        state = seams.training_popularity(pairs, candidates)
        self.assertEqual({"a": 0, "b": 2, "c": 1, "d": 3}, dict(state.support_by_media))
        # Counting events would give d=4; zero watch must not add a viewer to b.
        self.assertNotEqual(4, state.support_by_media["d"])
        self.assertNotEqual(3, state.support_by_media["b"])
        self.assertNotIn("x", state.support_by_media)
        changed_holdout = self.holdout + (event("holdout-only", "new", "a", 9,
                                                "2027-01-24T00:00:00Z"),)
        self.assertEqual(state, seams.training_popularity(pairs, candidates))
        self.assertEqual(8, changed_holdout[-1].watch_seconds - 1)

    def test_tie_order_unreachable_targets_and_fixed_precision_denominator(self):
        tied = seams.rank_popularity(PopularityState({"z": 1, "a": 1, "m": 1}, False))
        self.assertEqual(("a", "m", "z"), tied.recommendation_ids)
        targets = seams.holdout_targets(self.holdout)
        self.assertIn("e", targets.targets_by_viewer["new"])
        score = seams.score_viewer(("d", "b"), frozenset({"d", "e"}))
        self.assertEqual(Fraction(1, 3), score.precision_at_3)
        self.assertEqual(Fraction(1, 2), score.recall_at_3)

    def test_hand_oracle_rejects_canned_or_event_count_based_output(self):
        candidates = seams.cutoff_candidates(self.catalog, self.cutoff)
        pairs = seams.positive_pairs(self.training)
        state = seams.training_popularity(pairs, candidates)
        ranked = seams.rank_popularity(state)
        self.assertEqual(("d", "b", "c"), ranked.recommendation_ids)
        self.assertNotEqual(("a", "b", "c"), ranked.recommendation_ids)
        self.assertEqual({"a": 0, "b": 2, "c": 1, "d": 3}, dict(state.support_by_media))


if __name__ == "__main__":
    unittest.main()
