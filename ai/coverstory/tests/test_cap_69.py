"""CAP-69 learner acceptance contract; TODO failures are expected initially."""
from __future__ import annotations
import json
import unittest
from pathlib import Path

from ai.coverstory.cap_69 import LoadRun, summarize, validate_claim

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-69/synthetic-v1.json"


class CAP69AcceptanceTest(unittest.TestCase):
    def test_bounded_burst_counts_capacity_rejections_and_drain(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        case = data["cases"][0]
        result = summarize(LoadRun(**case["run"]))
        for key in ("accepted", "rejected", "completed", "queue_max", "p95_ms", "drain_ms"):
            expected_key = "queue_max_at_admission" if key == "queue_max" else key
            self.assertEqual(case["expected"][expected_key], getattr(result, key))
        self.assertLessEqual(result.queue_max, case["run"]["capacity"])

    def test_empty_population_has_null_p95(self):
        result = summarize(LoadRun(offered=0, capacity=10, rate_per_second=2))
        self.assertEqual((0, 0, 0, 0, None, 0),
                         (result.accepted, result.rejected, result.completed,
                          result.queue_max, result.p95_ms, result.drain_ms))

    def test_direct_rejection_baseline_completes_only_the_offer_seen_while_idle(self):
        result = summarize(LoadRun(offered=20, capacity=10, rate_per_second=2,
                                   strategy="direct"))
        self.assertEqual((1, 19, 1, 1, 500, 500),
                         (result.accepted, result.rejected, result.completed,
                          result.queue_max, result.p95_ms, result.drain_ms))

    def test_zero_or_unbounded_capacity_is_invalid(self):
        for capacity in (0, None):
            with self.subTest(capacity=capacity), self.assertRaisesRegex(ValueError, "INVALID_CONFIG"):
                summarize(LoadRun(offered=1, capacity=capacity, rate_per_second=2))

    def test_claim_cannot_hide_rejected_work(self):
        result = summarize(LoadRun(offered=20, capacity=10, rate_per_second=2))
        self.assertFalse(validate_claim(result, "The queue eliminated overload."))


if __name__ == "__main__":
    unittest.main()
