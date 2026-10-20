import importlib.util
import json
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).with_name("dsa-review-queue.py")
spec = importlib.util.spec_from_file_location("dsa_review_queue_71_84", MODULE_PATH)
queue_module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(queue_module)


class DsaReviewQueueDays71To84Test(unittest.TestCase):
    def test_two_new_anchors_are_unique_and_planned(self):
        anchors = queue_module.anchors_from_manifests()
        ids = [a["problem_id"] for a in anchors]
        self.assertEqual(len(ids), len(set(ids)))
        added = [a for a in anchors if a["batch"] == "continuation-71-84"]
        self.assertEqual({73, 79}, {a["anchor_day"] for a in added})
        self.assertEqual({112, 230}, {a["problem_id"] for a in added})
        self.assertEqual({"2026-12-31", "2027-01-06"},
                         {a["planned_first_attempt_date"] for a in added})

    def test_empty_log_never_invents_attempts_or_reviews(self):
        log = json.loads(Path("docs/reboot/continuation-31-44/dsa/actual-attempt-log.json").read_text())
        self.assertEqual([], log["events"])
        added = [a for a in queue_module.build_queue(log["events"], "2027-01-11")["anchors"]
                 if a["batch"] == "continuation-71-84"]
        self.assertEqual(2, len(added))
        self.assertTrue(all(a["actual_first_attempt_date"] is None for a in added))
        self.assertTrue(all(a["status"] == "awaiting_actual_attempt" and not a["reviews"] for a in added))

    def test_reviews_derive_only_from_actual_first_attempt_date(self):
        result = queue_module.build_queue([{
            "anchor_day": 73, "problem_id": 112, "kind": "first_attempt",
            "date": "2027-01-02", "result": "attempted"
        }], "2027-01-05")
        anchor = next(a for a in result["anchors"] if a["problem_id"] == 112)
        self.assertEqual("2027-01-02", anchor["actual_first_attempt_date"])
        self.assertEqual("2027-01-05", anchor["reviews"][0]["due_date_from_actual_attempt"])
        self.assertEqual("due", anchor["reviews"][0]["status"])

    def test_unknown_day_problem_pair_is_rejected(self):
        with self.assertRaisesRegex(ValueError, "unknown anchor"):
            queue_module.build_queue([{
                "anchor_day": 73, "problem_id": 230, "kind": "first_attempt",
                "date": "2026-12-31", "result": "attempted"
            }], "2026-12-31")


if __name__ == "__main__":
    unittest.main()
