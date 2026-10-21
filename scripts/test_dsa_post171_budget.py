import importlib.machinery
import unittest

queue = importlib.machinery.SourceFileLoader(
    "post171_queue", "scripts/dsa-review-queue.py"
).load_module()


class Post171BudgetTest(unittest.TestCase):
    PIDS_AND_TARGETS = ((121, 2016), (1, 2441), (383, 1160), (49, 893))

    @staticmethod
    def _first_attempt(pid):
        return {
            "schema_version": 2,
            "event_id": f"synthetic-first-{pid}",
            "attempt_id": f"synthetic-attempt-{pid}",
            "evidence_reference": f"synthetic/{pid}.json",
            "date": "2026-10-01",
            "timezone": "Asia/Kolkata",
            "anchor_id": f"lc:{pid}",
            "anchor_problem_id": pid,
            "kind": "anchor_attempt",
            "activity": "guided_anchor",
            "result": "attempted",
            "assistance": "none",
        }

    def _due_fixture(self):
        events = [self._first_attempt(pid) for pid, _ in self.PIDS_AND_TARGETS]
        confirmations = [f"lc:{pid}+3={target}" for pid, target in self.PIDS_AND_TARGETS]
        return events, confirmations

    def test_day171_preserves_four_exercise_tier_and_day172_switches_to_shared_block(self):
        events, confirmations = self._due_fixture()
        before = [dict(event) for event in events]

        day171 = queue.build_queue(events, "2026-10-04", 171, confirmations)
        self.assertEqual(4, len(day171["selected_exercises"]))
        self.assertEqual(90, day171["daily_dsa_budget_minutes"])
        self.assertEqual(120, day171["engineering_minutes"])
        self.assertEqual(210, day171["total_daily_minutes"])
        self.assertEqual("legacy_tiered_dsa_and_engineering", day171["budget_mode"])

        day172 = queue.build_queue(events, "2026-10-04", 172, confirmations)
        self.assertEqual(1, len(day172["selected_exercises"]))
        self.assertEqual(45, day172["daily_dsa_budget_minutes"])
        self.assertEqual(45, day172["shared_interview_and_remaining_reviews_minutes"])
        self.assertEqual(165, day172["engineering_minutes"])
        self.assertEqual(210, day172["total_daily_minutes"])
        self.assertEqual({"study": 35, "practice_and_test": 115, "evidence": 15,
                          "status": "proposed planning allocation"}, day172["engineering_allocation_minutes"])
        self.assertEqual("interview_and_remaining_reviews", day172["budget_mode"])
        self.assertEqual(1, day172["substantive_exercise_cap"])
        self.assertEqual("2026-10-04", day172["selected_exercises"][0]["original_due_date"])
        deferred = {item["slot_id"]: item for item in day172["deferred_exercises"]}
        self.assertEqual(3, len(deferred))
        selected_slot = day172["selected_exercises"][0]["slot_id"]
        self.assertEqual("lc:1+3", selected_slot)  # Existing oldest-date/anchor ordering is unchanged.
        for pid, _ in self.PIDS_AND_TARGETS:
            slot = f"lc:{pid}+3"
            if slot == selected_slot:
                continue
            self.assertEqual("2026-10-04", deferred[slot]["original_due_date"])
            self.assertIn("one-exercise cap", deferred[slot]["deferred_reason"])
        self.assertFalse(any(item.get("activity") == "guided_anchor"
                             for item in day172["selected_exercises"]))
        self.assertEqual(before, events)

    def test_day171_one_through_four_review_tiers_remain_unchanged(self):
        events, confirmations = self._due_fixture()
        for count, expected in ((1, 60), (2, 60), (3, 75), (4, 90)):
            with self.subTest(exercise_count=count):
                result = queue.build_queue(events, "2026-10-04", 171, confirmations[:count])
                self.assertEqual(count, len(result["selected_exercises"]))
                self.assertEqual(expected, result["daily_dsa_budget_minutes"])
                self.assertEqual(120, result["engineering_minutes"])
                self.assertEqual(expected + 120, result["total_daily_minutes"])
                self.assertFalse(any(item.get("activity") == "guided_anchor"
                                     for item in result["selected_exercises"]))

    def test_day182_empty_queue_keeps_new_budget_without_fabricating_work(self):
        result = queue.build_queue([], "2027-04-19", 182)
        self.assertEqual([], result["selected_exercises"])
        self.assertEqual([], result["pending_due_tasks"])
        self.assertEqual(165, result["engineering_minutes"])
        self.assertEqual(45, result["shared_interview_and_remaining_reviews_minutes"])
        self.assertEqual(210, result["total_daily_minutes"])
        self.assertFalse(any(anchor["actual_first_attempt_date"] is not None
                             for anchor in result["anchors"]))

    def test_day172_failed_repair_precedes_oldest_overdue_and_keeps_deadlines(self):
        events = [self._first_attempt(pid) for pid, _ in self.PIDS_AND_TARGETS[:3]]
        events.append({
            "schema_version": 2, "event_id": "failed-review-121", "attempt_id": "failed-attempt-121",
            "evidence_reference": "synthetic/failed-121.json", "review_slot_id": "lc:121+3",
            "date": "2026-10-04", "timezone": "Asia/Kolkata", "anchor_id": "lc:121",
            "anchor_problem_id": 121, "target_problem_id": 2016, "kind": "retrieval_attempt",
            "activity": "independent_transfer", "assistance": "none", "review_interval_days": 3,
            "result": "failed", "invariant_explanation": "Synthetic failed attempt for queue testing.",
        })
        transfers = ("lc:1+3=2441", "lc:383+3=1160")
        result = queue.build_queue(events, "2026-10-05", 172, confirmed_transfers=transfers,
                                  confirmed_repairs=("lc:121+3=2016",))
        self.assertEqual(1, len(result["selected_exercises"]))
        self.assertEqual("failed_repair_pending", result["selected_exercises"][0]["status"])
        self.assertEqual("repair_attempt", result["selected_exercises"][0]["activity"])
        deferred = {item["slot_id"]: item for item in result["deferred_exercises"]}
        self.assertIn("lc:1+3", deferred)
        self.assertIn("lc:383+3", deferred)
        self.assertEqual("2026-10-04", deferred["lc:1+3"]["original_due_date"])
        self.assertEqual("2026-10-04", deferred["lc:383+3"]["original_due_date"])
        self.assertTrue(all("one-exercise cap" in item["deferred_reason"]
                            for item in deferred.values()))

    def test_missing_roadmap_day_keeps_legacy_budget_and_marks_phase_unknown(self):
        result = queue.build_queue([], "2027-04-09")
        self.assertEqual(120, result["engineering_minutes"])
        self.assertEqual(180, result["total_daily_minutes"])
        self.assertEqual("phase_unspecified_legacy", result["budget_mode"])
        self.assertTrue(result["roadmap_day_required_for_phase_budget"])
        self.assertIsNone(result["shared_interview_and_remaining_reviews_minutes"])


if __name__ == "__main__":
    unittest.main()
