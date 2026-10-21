import importlib.util
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).with_name("dsa-review-queue.py")
spec = importlib.util.spec_from_file_location("dsa_review_queue_45_56", MODULE_PATH)
queue_module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(queue_module)


class DsaReviewQueueDays45To56Test(unittest.TestCase):
    def test_new_manifest_anchors_join_actual_attempt_carryover(self):
        result = queue_module.build_queue([
            {"anchor_day": 45, "problem_id": 128, "kind": "first_attempt",
             "date": "2026-12-04", "result": "attempted"}
        ], "2026-12-07")
        anchor = next(a for a in result["anchors"] if a["anchor_day"] == 45)
        self.assertEqual("2026-12-04", anchor["actual_first_attempt_date"])
        self.assertEqual("2026-12-07", anchor["reviews"][0]["due_date_from_actual_attempt"])
        self.assertEqual("due", anchor["reviews"][0]["status"])

    def test_new_anchors_without_attempts_have_no_invented_due_dates(self):
        result = queue_module.build_queue([], "2026-12-14")
        new_anchors = [a for a in result["anchors"] if a["batch"] == "continuation-45-56"]
        self.assertEqual({45, 47, 53}, {a["anchor_day"] for a in new_anchors})
        self.assertTrue(all(a["actual_first_attempt_date"] is None for a in new_anchors))
        self.assertTrue(all(a["status"] == "awaiting_actual_attempt" and not a["reviews"] for a in new_anchors))

    def test_revisited_problems_are_not_duplicate_new_anchors(self):
        result = queue_module.build_queue([], "2026-12-14")
        for problem_id, original_day in ((49, 7), (20, 21), (155, 23)):
            matches = [a for a in result["anchors"] if a["problem_id"] == problem_id]
            self.assertEqual(1, len(matches))
            self.assertEqual(original_day, matches[0]["anchor_day"])


if __name__ == "__main__":
    unittest.main()
