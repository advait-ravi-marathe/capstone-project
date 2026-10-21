import importlib.util
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).with_name("dsa-review-queue.py")
spec = importlib.util.spec_from_file_location("dsa_review_queue", MODULE_PATH)
queue_module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(queue_module)


class DsaReviewQueueTest(unittest.TestCase):
    def test_actual_attempt_date_overrides_ticket_date_for_d_plus_reviews(self):
        result = queue_module.build_queue([{
            "anchor_day": 1, "problem_id": 121, "kind": "first_attempt",
            "date": "2026-10-21", "result": "attempted"
        }], "2026-10-24")
        anchor = next(a for a in result["anchors"] if a["anchor_day"] == 1)
        self.assertEqual("2026-10-21", anchor["actual_first_attempt_date"])
        self.assertEqual("2026-10-24", anchor["reviews"][0]["due_date_from_actual_attempt"])
        self.assertEqual("due", anchor["reviews"][0]["status"])
        self.assertEqual("2026-10-28", anchor["reviews"][1]["due_date_from_actual_attempt"])

    def test_unrecorded_cross_batch_anchor_has_no_actual_or_due_date(self):
        result = queue_module.build_queue([], "2026-11-19")
        carryover = [a for a in result["anchors"] if a["anchor_day"] in (9, 23, 27)]
        self.assertEqual({238, 155, 35}, {a["problem_id"] for a in carryover})
        self.assertTrue(all(a["actual_first_attempt_date"] is None for a in carryover))
        self.assertTrue(all(a["status"] == "awaiting_actual_attempt" for a in carryover))
        self.assertTrue(all(a["reviews"] == [] for a in carryover))
        anchor_29 = next(a for a in result["anchors"] if a["anchor_day"] == 29)
        self.assertIsNone(anchor_29["actual_first_attempt_date"])
        self.assertEqual("awaiting_actual_attempt", anchor_29["status"])
        self.assertEqual([], anchor_29["reviews"])

    def test_failed_recall_displaces_new_problem_inside_same_sixty_minutes(self):
        result = queue_module.build_queue([
            {"anchor_day": 37, "problem_id": 143, "kind": "first_attempt", "date": "2026-11-25", "result": "attempted"},
            {"anchor_day": 37, "problem_id": 143, "kind": "review", "interval_days": 3, "date": "2026-11-28", "result": "failed"},
        ], "2026-11-28")
        anchor = next(a for a in result["anchors"] if a["anchor_day"] == 37)
        self.assertEqual("failed_repair_pending", anchor["reviews"][0]["status"])
        self.assertIn("replace new work", anchor["reviews"][0]["failed_review_action"])
        self.assertEqual(60, result["daily_dsa_budget_minutes"])
        self.assertIn("no extra minutes", result["failed_review_rule"])

    def test_day_29_to_31_carryover_is_from_actual_attempt_if_entered(self):
        result = queue_module.build_queue([
            {"anchor_day": 29, "problem_id": 153, "kind": "first_attempt", "date": "2026-11-18", "result": "attempted"}
        ], "2026-11-21")
        anchor = next(a for a in result["anchors"] if a["anchor_day"] == 29)
        self.assertEqual("2026-11-21", anchor["reviews"][0]["due_date_from_actual_attempt"])
        self.assertEqual("due", anchor["reviews"][0]["status"])

    def test_planned_revisits_keep_original_anchor_and_no_clock_without_attempt(self):
        result = queue_module.build_queue([], "2026-12-14")
        for problem_id, canonical_day in ((49, 7), (20, 21), (155, 23)):
            matches = [a for a in result["anchors"] if a["problem_id"] == problem_id]
            self.assertEqual(1, len(matches))
            self.assertEqual(canonical_day, matches[0]["anchor_day"])
            self.assertIsNone(matches[0]["actual_first_attempt_date"])
            self.assertEqual("awaiting_actual_attempt", matches[0]["status"])
            self.assertEqual([], matches[0]["reviews"])

    def test_first_attempt_on_revisit_date_attaches_to_original_anchor(self):
        result = queue_module.build_queue([{
            "anchor_day": 49, "problem_id": 49, "kind": "first_attempt",
            "date": "2026-12-07", "result": "attempted"
        }], "2026-12-10")
        anchor = next(a for a in result["anchors"] if a["problem_id"] == 49)
        self.assertEqual(7, anchor["anchor_day"])
        self.assertEqual("2026-12-07", anchor["actual_first_attempt_date"])
        self.assertEqual("2026-12-10", anchor["reviews"][0]["due_date_from_actual_attempt"])
        self.assertEqual("due", anchor["reviews"][0]["status"])

    def test_later_first_attempt_event_does_not_reset_clock(self):
        result = queue_module.build_queue([
            {"anchor_day": 7, "problem_id": 49, "kind": "first_attempt", "date": "2026-10-26", "result": "attempted"},
            {"anchor_day": 49, "problem_id": 49, "kind": "first_attempt", "date": "2026-12-07", "result": "attempted"},
        ], "2026-12-07")
        anchor = next(a for a in result["anchors"] if a["problem_id"] == 49)
        self.assertEqual(7, anchor["anchor_day"])
        self.assertEqual("2026-10-26", anchor["actual_first_attempt_date"])
        self.assertEqual("2026-10-29", anchor["reviews"][0]["due_date_from_actual_attempt"])

    def test_original_anchor_review_evidence_survives_later_planned_revisit(self):
        result = queue_module.build_queue([
            {"anchor_day": 7, "problem_id": 49, "kind": "first_attempt", "date": "2026-09-20", "result": "attempted"},
            {"anchor_day": 7, "problem_id": 49, "kind": "review", "interval_days": 3, "date": "2026-09-23", "result": "passed"},
        ], "2026-12-07")
        matches = [a for a in result["anchors"] if a["problem_id"] == 49]
        self.assertEqual(1, len(matches))
        self.assertEqual(7, matches[0]["anchor_day"])
        self.assertEqual("historical_passed_unclassified", matches[0]["reviews"][0]["status"])
        self.assertFalse(matches[0]["reviews"][0]["independent_transfer_evidence"])


if __name__ == "__main__":
    unittest.main()
