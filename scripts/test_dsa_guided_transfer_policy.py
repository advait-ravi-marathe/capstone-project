import importlib.machinery
import unittest

queue = importlib.machinery.SourceFileLoader("guided_queue", "scripts/dsa-review-queue.py").load_module()


class GuidedTransferPolicyTest(unittest.TestCase):
    def test_registry_has_31_immutable_canonical_ids_and_initial_sequence(self):
        anchors = queue.load_registry()
        self.assertEqual(31, len(anchors))
        self.assertEqual(31, len({a["anchor_id"] for a in anchors}))
        self.assertEqual([121, 1, 383], [queue.build_queue([], "2026-10-22", d)["selected_exercises"][0]["problem_id"] for d in (1, 2, 3)])
        self.assertEqual([1, 3, 5], [a["original_anchor_day"] for a in anchors[:3]])

    def test_empty_log_and_editorial_exposure_do_not_invent_attempts_or_due_dates(self):
        result = queue.build_queue([], "2026-10-25", 4)
        self.assertTrue(all(a["actual_first_attempt_date"] is None and not a["reviews"] for a in result["anchors"]))
        self.assertEqual("awaiting_actual_attempt", result["anchors"][0]["status"])
        self.assertFalse(result["pending_due_tasks"])

    def test_guided_actual_attempt_starts_clock_and_transfer_does_not_reset_it(self):
        events = [
            {"schema_version": 2, "event_id": "a1", "date": "2026-10-20", "timezone": "Asia/Kolkata", "anchor_id": "lc:121", "anchor_problem_id": 121, "kind": "anchor_attempt", "activity": "guided_anchor", "assistance": "editorial", "result": "attempted"},
            {"schema_version": 2, "event_id": "r1", "date": "2026-10-24", "timezone": "Asia/Kolkata", "anchor_id": "lc:121", "anchor_problem_id": 121, "target_problem_id": 2016, "kind": "retrieval_attempt", "activity": "independent_transfer", "assistance": "none", "review_interval_days": 3, "result": "failed"},
        ]
        result = queue.build_queue(events, "2026-10-24", 5)
        anchor = next(a for a in result["anchors"] if a["problem_id"] == 121)
        self.assertEqual("2026-10-20", anchor["actual_first_attempt_date"])
        self.assertEqual("2026-10-23", anchor["reviews"][0]["due_date_from_actual_attempt"])
        self.assertEqual("failed_repair_pending", anchor["reviews"][0]["status"])
        self.assertEqual("unknown_requires_history_check", anchor["transfer_candidates"][1]["freshness"])

    def test_future_events_excluded_and_later_failure_remains_visible(self):
        events = [
            {"anchor_day": 1, "problem_id": 121, "kind": "first_attempt", "date": "2026-10-20", "result": "attempted"},
            {"anchor_day": 1, "problem_id": 121, "kind": "review", "interval_days": 3, "date": "2026-10-23", "result": "passed"},
            {"anchor_day": 1, "problem_id": 121, "kind": "review", "interval_days": 3, "date": "2026-10-24", "result": "failed"},
            {"anchor_day": 1, "problem_id": 121, "kind": "review", "interval_days": 7, "date": "2026-10-29", "result": "passed"},
        ]
        anchor = next(a for a in queue.build_queue(events, "2026-10-24")["anchors"] if a["problem_id"] == 121)
        self.assertEqual("failed_repair_pending", anchor["reviews"][0]["status"])
        self.assertEqual("scheduled", anchor["reviews"][1]["status"])

    def test_overflow_keeps_original_due_date_and_displaces_guidance(self):
        first_date = "2026-09-01"
        events = [{"anchor_id": f"lc:{pid}", "anchor_problem_id": pid, "kind": "anchor_attempt",
                   "activity": "guided_anchor", "date": first_date, "result": "attempted"}
                  for pid in (121, 1, 383, 49, 238)]
        # Record failed D+3 slots for four anchors; the fifth remains due and
        # must be shown with the original deadline rather than discarded.
        events += [{"anchor_id": f"lc:{pid}", "anchor_problem_id": pid, "kind": "review",
                    "review_interval_days": 3, "date": "2026-09-04", "result": "failed"}
                   for pid in (121, 1, 383, 49)]
        result = queue.build_queue(events, "2026-09-22", 6, [
            "lc:1+3=2441", "lc:121+3=2016", "lc:383+3=1160", "lc:49+3=893"
        ])
        self.assertEqual(90, result["daily_dsa_budget_minutes"])
        self.assertEqual(210, result["total_daily_minutes"])
        self.assertEqual(4, len(result["selected_exercises"]))
        self.assertTrue(any(t["slot_id"] == "lc:238+3" and t["original_due_date"] == "2026-09-04" for t in result["deferred_exercises"]))
        self.assertTrue(any(t["slot_id"] == "lc:121+21" and t["original_due_date"] == "2026-09-22" for t in result["deferred_exercises"]))
        self.assertFalse(any(t.get("status") == "planned_guided_encounter" for t in result["selected_exercises"]))

    def test_assisted_acceptance_does_not_close_transfer_due(self):
        events = [
            {"schema_version": 2, "event_id": "as1", "timezone": "Asia/Kolkata", "anchor_id": "lc:121", "anchor_problem_id": 121, "kind": "anchor_attempt", "activity": "guided_anchor", "date": "2026-10-01", "result": "attempted", "assistance": ["editorial"]},
            {"schema_version": 2, "event_id": "as2", "timezone": "Asia/Kolkata", "anchor_id": "lc:121", "anchor_problem_id": 121, "kind": "retrieval_attempt", "activity": "independent_transfer", "review_interval_days": 3, "date": "2026-10-04", "result": "passed", "assistance": ["hint"], "invariant_explanation": "some explanation"},
        ]
        anchor = next(a for a in queue.build_queue(events, "2026-10-04")["anchors"] if a["problem_id"] == 121)
        self.assertEqual("due", anchor["reviews"][0]["status"])
        self.assertTrue(anchor["reviews"][0]["assisted_observation_does_not_close_slot"])
        self.assertFalse(anchor["reviews"][0]["independent_transfer_evidence"])

    def test_legacy_early_pass_is_retained_without_changing_its_historical_credit(self):
        events = [
            {"anchor_day": 1, "problem_id": 121, "kind": "first_attempt", "date": "2026-10-20", "result": "attempted"},
            {"anchor_day": 1, "problem_id": 121, "kind": "review", "interval_days": 3, "date": "2026-10-21", "result": "passed"},
        ]
        anchor = next(a for a in queue.build_queue(events, "2026-10-23")["anchors"] if a["problem_id"] == 121)
        self.assertEqual("historical_passed_unclassified", anchor["reviews"][0]["status"])
        self.assertFalse(anchor["reviews"][0]["independent_transfer_evidence"])

    def test_conflicting_duplicate_event_ids_and_bad_timezone_fail_closed(self):
        event = {"schema_version": 2, "event_id": "dupe", "date": "2026-10-20", "timezone": "Asia/Kolkata",
                 "anchor_id": "lc:121", "anchor_problem_id": 121, "kind": "anchor_attempt",
                 "activity": "guided_anchor", "result": "attempted"}
        with self.assertRaisesRegex(ValueError, "conflicting duplicate event_id"):
            queue.build_queue([event, {**event, "date": "2026-10-21"}], "2026-10-22")
        with self.assertRaisesRegex(ValueError, "unknown IANA timezone"):
            queue.build_queue([{**event, "event_id": "tz", "timezone": "Not/AZone"}], "2026-10-22")

    def test_workload_tiers_and_day_150_cutoff(self):
        self.assertEqual([60, 60, 75, 90], [queue._tiers(n) for n in range(1, 5)])
        self.assertFalse(queue.build_queue([], "2027-03-22", 151)["selected_exercises"])
        self.assertEqual(120, queue.build_queue([], "2027-03-22", 151)["engineering_minutes"])

    def test_seed_catalog_keeps_explicit_gaps_and_does_not_claim_freshness(self):
        catalog = queue.load_catalog()
        self.assertEqual(31, len(catalog))
        self.assertTrue(catalog[155]["gaps"])
        self.assertTrue(all(c["unseen_status"] == "unknown_requires_learner_history_check"
                            for e in catalog.values() for c in e["candidates"]))

    def test_capacity_simulation_is_abstract_and_matches_tier_totals(self):
        import json
        from pathlib import Path
        simulation = json.loads(Path("docs/reboot/dsa/tiered-capacity-simulation-2026-10-21.json").read_text())
        self.assertEqual("SYNTHETIC_CAPACITY_ILLUSTRATION_NOT_LEARNER_EVENTS", simulation["status"])
        self.assertEqual(154, len(simulation["rows"]))
        self.assertEqual({"60": 25, "75": 17, "90": 10}, simulation["summary"]["active_days_by_tier"])
        self.assertTrue(all(t["target_problem_id"] is None for row in simulation["rows"] for t in row["selected_transfer_attempts"]))
        self.assertEqual(210, simulation["rows"][21]["total_budget_minutes"])
        self.assertEqual("D+3 remains overdue with original due day30; no clock reset or auto-pass.",
                         simulation["adversarial_capacity_examples"][0]["next_day"])


if __name__ == "__main__":
    unittest.main()
