import importlib.util
import json
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).with_name("dsa-review-queue.py")
spec = importlib.util.spec_from_file_location("dsa_review_queue_57_70", MODULE_PATH)
queue_module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(queue_module)


class DsaReviewQueueDays57To70Test(unittest.TestCase):
    def test_tree_anchors_are_unique_across_canonical_history(self):
        anchors = queue_module.anchors_from_manifests()
        ids = [anchor["problem_id"] for anchor in anchors]
        self.assertEqual(len(ids), len(set(ids)))
        added = [a for a in anchors if a["batch"] == "continuation-57-70"]
        self.assertEqual({59, 61, 65, 67}, {a["anchor_day"] for a in added})
        self.assertEqual({104, 100, 102, 98}, {a["problem_id"] for a in added})

    def test_empty_log_never_invents_attempts_or_review_due_dates(self):
        log = json.loads(Path("docs/reboot/continuation-31-44/dsa/actual-attempt-log.json").read_text())
        self.assertEqual([], log["events"])
        result = queue_module.build_queue(log["events"], "2026-12-28")
        added = [a for a in result["anchors"] if a["batch"] == "continuation-57-70"]
        self.assertEqual(4, len(added))
        self.assertTrue(all(a["actual_first_attempt_date"] is None for a in added))
        self.assertTrue(all(a["status"] == "awaiting_actual_attempt" and not a["reviews"] for a in added))

    def test_first_attempt_uses_actual_date_for_d_plus_reviews(self):
        result = queue_module.build_queue([{
            "anchor_day": 59, "problem_id": 104, "kind": "first_attempt",
            "date": "2026-12-18", "result": "attempted"
        }], "2026-12-21")
        anchor = next(a for a in result["anchors"] if a["problem_id"] == 104)
        self.assertEqual("2026-12-18", anchor["actual_first_attempt_date"])
        self.assertEqual("2026-12-21", anchor["reviews"][0]["due_date_from_actual_attempt"])
        self.assertEqual("due", anchor["reviews"][0]["status"])

    def test_unknown_day_problem_pair_is_rejected(self):
        with self.assertRaisesRegex(ValueError, "unknown anchor"):
            queue_module.build_queue([{
                "anchor_day": 59, "problem_id": 49, "kind": "first_attempt",
                "date": "2026-12-17", "result": "attempted"
            }], "2026-12-17")


if __name__ == "__main__":
    unittest.main()
