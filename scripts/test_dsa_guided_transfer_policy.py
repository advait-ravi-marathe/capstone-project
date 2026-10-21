import importlib.machinery
import unittest

queue = importlib.machinery.SourceFileLoader("guided_queue", "scripts/dsa-review-queue.py").load_module()


class GuidedTransferPolicyTest(unittest.TestCase):
    @staticmethod
    def anchor_attempt(pid, day, date):
        return {"schema_version": 2, "event_id": f"anchor-{pid}-{date}", "date": date,
                "timezone": "Asia/Kolkata", "anchor_id": f"lc:{pid}", "anchor_problem_id": pid,
                "anchor_day": day, "kind": "anchor_attempt", "activity": "guided_anchor",
                "result": "attempted", "assistance": "none"}

    @staticmethod
    def transfer_attempt(pid, interval, date, target, *, result="passed", activity="independent_transfer",
                         assistance="none", attempt_id=None, evidence=None, slot=None):
        return {"schema_version": 2, "event_id": f"event-{attempt_id or date}-{target}-{interval}",
                "attempt_id": attempt_id or f"attempt-{date}-{target}-{interval}",
                "evidence_reference": evidence or f"evidence/{date}-{target}-{interval}.json",
                "review_slot_id": slot or f"lc:{pid}+{interval}", "date": date, "timezone": "Asia/Kolkata",
                "anchor_id": f"lc:{pid}", "anchor_problem_id": pid, "target_problem_id": target,
                "kind": "retrieval_attempt", "activity": activity, "assistance": assistance,
                "review_interval_days": interval, "result": result,
                "invariant_explanation": "I preserved the invariant on every step."}

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

    def test_day_four_does_not_skip_unattempted_initial_guided_anchor(self):
        result = queue.build_queue([], "2026-10-25", 4)
        self.assertEqual(121, result["selected_exercises"][0]["problem_id"])
        self.assertEqual("awaiting_actual_attempt", result["anchors"][0]["status"])

    def test_guided_actual_attempt_starts_clock_and_transfer_does_not_reset_it(self):
        events = [
            {"schema_version": 2, "event_id": "a1", "date": "2026-10-20", "timezone": "Asia/Kolkata", "anchor_id": "lc:121", "anchor_problem_id": 121, "kind": "anchor_attempt", "activity": "guided_anchor", "assistance": "editorial", "result": "attempted"},
            {"schema_version": 2, "event_id": "r1", "attempt_id": "attempt-r1", "evidence_reference": "evidence/r1.json", "review_slot_id": "lc:121+3", "date": "2026-10-24", "timezone": "Asia/Kolkata", "anchor_id": "lc:121", "anchor_problem_id": 121, "target_problem_id": 2016, "kind": "retrieval_attempt", "activity": "independent_transfer", "assistance": "none", "review_interval_days": 3, "result": "failed"},
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
            {"schema_version": 2, "event_id": "as2", "attempt_id": "attempt-as2", "evidence_reference": "evidence/as2.json", "review_slot_id": "lc:121+3", "timezone": "Asia/Kolkata", "anchor_id": "lc:121", "anchor_problem_id": 121, "target_problem_id": 2016, "kind": "retrieval_attempt", "activity": "independent_transfer", "review_interval_days": 3, "date": "2026-10-04", "result": "passed", "assistance": ["hint"], "invariant_explanation": "some explanation"},
        ]
        anchor = next(a for a in queue.build_queue(events, "2026-10-04")["anchors"] if a["problem_id"] == 121)
        self.assertEqual("due", anchor["reviews"][0]["status"])
        self.assertTrue(anchor["reviews"][0]["assisted_observation_does_not_close_slot"])
        self.assertFalse(anchor["reviews"][0]["independent_transfer_evidence"])

    def test_legacy_early_pass_is_retained_but_does_not_close_a_not_yet_due_slot(self):
        events = [
            {"anchor_day": 1, "problem_id": 121, "kind": "first_attempt", "date": "2026-10-20", "result": "attempted"},
            {"anchor_day": 1, "problem_id": 121, "kind": "review", "interval_days": 3, "date": "2026-10-21", "result": "passed"},
        ]
        anchor = next(a for a in queue.build_queue(events, "2026-10-23")["anchors"] if a["problem_id"] == 121)
        self.assertEqual("due", anchor["reviews"][0]["status"])
        self.assertTrue(anchor["reviews"][0]["historical_result_retained_unclassified"])
        self.assertFalse(anchor["reviews"][0]["independent_transfer_evidence"])

    def test_late_legacy_pass_remains_unclassified_historical_evidence(self):
        events = [
            {"anchor_day": 1, "problem_id": 121, "kind": "first_attempt", "date": "2026-10-20", "result": "attempted"},
            {"anchor_day": 1, "problem_id": 121, "kind": "review", "interval_days": 3, "date": "2026-10-23", "result": "passed"},
        ]
        anchor = next(a for a in queue.build_queue(events, "2026-10-23")["anchors"] if a["problem_id"] == 121)
        self.assertEqual("due", anchor["reviews"][0]["status"])
        self.assertFalse(anchor["reviews"][0]["historical_result_retained_unclassified"])
        self.assertFalse(anchor["reviews"][0]["independent_transfer_evidence"])

    def test_missing_transfer_assistance_is_not_silently_unaided(self):
        anchor_attempt = {"schema_version": 2, "event_id": "ma1", "timezone": "Asia/Kolkata", "anchor_id": "lc:121",
                          "anchor_problem_id": 121, "kind": "anchor_attempt", "activity": "guided_anchor",
                          "date": "2026-10-01", "result": "attempted", "assistance": "editorial"}
        transfer = self.transfer_attempt(121, 3, "2026-10-04", 2016)
        transfer["event_id"] = "ma2"
        with self.assertRaisesRegex(ValueError, "explicit assistance"):
            queue.build_queue([anchor_attempt, {**transfer, "assistance": None}], "2026-10-04")
        row = next(a for a in queue.build_queue([anchor_attempt, {**transfer, "assistance": []}], "2026-10-04")
                   ["anchors"] if a["problem_id"] == 121)
        self.assertEqual("due", row["reviews"][0]["status"])

    def test_legacy_correction_event_without_v2_fields_is_rejected(self):
        event = {"schema_version": 1, "event_id": "legacy-correction", "date": "2026-10-20",
                 "anchor_day": 1, "problem_id": 121, "kind": "correction"}
        with self.assertRaisesRegex(ValueError, "require version 2"):
            queue.build_queue([event], "2026-10-20")

    def test_canonical_anchor_aliases_must_agree_and_be_valid(self):
        base = {"schema_version": 2, "event_id": "alias", "date": "2026-10-20", "timezone": "Asia/Kolkata",
                "anchor_id": "lc:121", "anchor_problem_id": 121, "anchor_day": 1,
                "kind": "anchor_attempt", "activity": "guided_anchor", "result": "attempted", "assistance": "none"}
        queue.build_queue([base], "2026-10-20")
        cases = [
            {**base, "event_id": "wrong-pid", "anchor_problem_id": 1},
            {**base, "event_id": "wrong-id", "anchor_id": "lc:999"},
            {**base, "event_id": "wrong-day", "anchor_day": 3},
        ]
        for event in cases:
            with self.subTest(event=event["event_id"]), self.assertRaises(ValueError):
                queue.build_queue([event], "2026-10-20")

    def test_missing_legacy_first_attempt_result_and_transfer_kind_do_not_start_clock(self):
        missing_result = {"anchor_day": 1, "problem_id": 121, "kind": "first_attempt", "date": "2026-10-20"}
        anchor = next(a for a in queue.build_queue([missing_result], "2026-10-30")["anchors"] if a["problem_id"] == 121)
        self.assertIsNone(anchor["actual_first_attempt_date"])
        self.assertEqual("awaiting_actual_attempt", anchor["status"])
        transfer_as_anchor = {"schema_version": 2, "event_id": "ta1", "date": "2026-10-20", "timezone": "Asia/Kolkata",
                              "anchor_id": "lc:121", "anchor_problem_id": 121, "kind": "anchor_attempt",
                              "activity": "independent_transfer", "result": "passed", "assistance": "none"}
        with self.assertRaisesRegex(ValueError, "transfer activity"):
            queue.build_queue([transfer_as_anchor], "2026-10-20")

    def test_transfer_credit_requires_mapped_full_relation_and_unaided_evidence(self):
        first = self.anchor_attempt(121, 1, "2026-10-22")
        valid = self.transfer_attempt(121, 3, "2026-10-25", 2016, assistance=["none"])
        row = next(a for a in queue.build_queue([first, valid], "2026-10-25")["anchors"] if a["problem_id"] == 121)
        self.assertEqual("passed_independent_transfer", row["reviews"][0]["status"])

        unmapped = self.transfer_attempt(121, 3, "2026-10-25", 122)
        row = next(a for a in queue.build_queue([first, unmapped], "2026-10-25")["anchors"] if a["problem_id"] == 121)
        self.assertEqual("due", row["reviews"][0]["status"])
        self.assertIn("not mapped", row["reviews"][0]["invalid_transfer_observation_reasons"][0])

        partial = [self.anchor_attempt(238, 9, "2026-10-22"), self.transfer_attempt(238, 3, "2026-10-25", 724)]
        row = next(a for a in queue.build_queue(partial, "2026-10-25")["anchors"] if a["problem_id"] == 238)
        self.assertEqual("due", row["reviews"][0]["status"])
        self.assertIn("not eligible", row["reviews"][0]["invalid_transfer_observation_reasons"][0])

    def test_missing_transfer_target_and_wrong_review_slot_are_rejected(self):
        first = self.anchor_attempt(121, 1, "2026-10-22")
        missing = self.transfer_attempt(121, 3, "2026-10-25", 2016)
        del missing["target_problem_id"]
        with self.assertRaisesRegex(ValueError, "target_problem_id"):
            queue.build_queue([first, missing], "2026-10-25")
        wrong_slot = self.transfer_attempt(121, 3, "2026-10-25", 2016, slot="lc:121+21")
        with self.assertRaisesRegex(ValueError, "review_slot_id"):
            queue.build_queue([first, wrong_slot], "2026-10-25")

    def test_exposed_duplicate_family_cannot_receive_fresh_transfer_credit(self):
        first = self.anchor_attempt(238, 9, "2026-10-22")
        exposure = {"schema_version": 2, "event_id": "exposed-1991", "date": "2026-10-24", "timezone": "Asia/Kolkata",
                    "anchor_id": "lc:238", "anchor_problem_id": 238, "kind": "exposure", "target_problem_id": 1991}
        attempt = self.transfer_attempt(238, 3, "2026-10-25", 724)
        row = next(a for a in queue.build_queue([first, exposure, attempt], "2026-10-25")["anchors"] if a["problem_id"] == 238)
        self.assertEqual("due", row["reviews"][0]["status"])
        self.assertIn("previously exposed", row["reviews"][0]["invalid_transfer_observation_reasons"][0])

    def test_one_attempt_or_evidence_cannot_close_multiple_intervals(self):
        first = self.anchor_attempt(121, 1, "2026-10-01")
        one = self.transfer_attempt(121, 3, "2026-10-22", 2016, attempt_id="same-attempt", evidence="same-evidence")
        two = self.transfer_attempt(121, 7, "2026-10-22", 1014, attempt_id="same-attempt", evidence="same-evidence")
        with self.assertRaisesRegex(ValueError, "attempt_id"):
            queue.build_queue([first, one, two], "2026-10-22")
        two = self.transfer_attempt(121, 7, "2026-10-22", 1014, attempt_id="different-attempt", evidence="same-evidence")
        with self.assertRaisesRegex(ValueError, "evidence reference"):
            queue.build_queue([first, one, two], "2026-10-22")

    def test_failed_slot_can_be_repaired_or_passed_by_a_later_distinct_attempt(self):
        first = self.anchor_attempt(35, 27, "2026-10-01")
        failed = self.transfer_attempt(35, 3, "2026-10-04", 744, result="failed", attempt_id="failed", evidence="failed.json")
        repair = self.transfer_attempt(35, 3, "2026-10-05", 744, activity="repair", attempt_id="repair", evidence="repair.json")
        row = next(a for a in queue.build_queue([first, failed, repair], "2026-10-05")["anchors"] if a["problem_id"] == 35)
        self.assertEqual("overdue", row["reviews"][0]["status"])
        self.assertTrue(row["reviews"][0]["failed_attempt_resolved_by_later_repair_or_transfer"])

        fresh = self.transfer_attempt(35, 3, "2026-10-05", 34, attempt_id="fresh", evidence="fresh.json")
        row = next(a for a in queue.build_queue([first, failed, fresh], "2026-10-05")["anchors"] if a["problem_id"] == 35)
        self.assertEqual("passed_independent_transfer", row["reviews"][0]["status"])
        self.assertTrue(row["reviews"][0]["failed_attempt_resolved_by_later_repair_or_transfer"])

    def test_correction_must_reference_an_earlier_event_and_changes_its_projection(self):
        first = self.anchor_attempt(121, 1, "2026-10-22")
        failed = self.transfer_attempt(121, 3, "2026-10-25", 2016, result="failed", attempt_id="corrected", evidence="correction-evidence")
        correction = {"schema_version": 2, "event_id": "correction-1", "date": "2026-10-26", "timezone": "Asia/Kolkata",
                      "anchor_id": "lc:121", "anchor_problem_id": 121, "kind": "correction",
                      "corrects_event_id": failed["event_id"], "corrected_fields": {"result": "passed"}}
        row = next(a for a in queue.build_queue([first, failed, correction], "2026-10-26")["anchors"] if a["problem_id"] == 121)
        self.assertEqual("passed_independent_transfer", row["reviews"][0]["status"])
        dangling = {**correction, "event_id": "dangling", "corrects_event_id": "absent"}
        with self.assertRaisesRegex(ValueError, "unknown event_id"):
            queue.build_queue([first, failed, dangling], "2026-10-26")

    def test_future_conflicting_event_ids_are_still_rejected(self):
        first = self.anchor_attempt(121, 1, "2026-10-20")
        future = {**first, "event_id": first["event_id"], "date": "2026-10-23", "result": "failed"}
        with self.assertRaisesRegex(ValueError, "conflicting duplicate event_id"):
            queue.build_queue([first, future], "2026-10-20")

    def test_due_today_can_share_capacity_with_guidance_and_catalog_gaps_do_not_deadlock(self):
        first = self.anchor_attempt(121, 1, "2026-10-22")
        result = queue.build_queue([first], "2026-10-25", 4, ["lc:121+3=2016"])
        self.assertEqual(["independent_transfer_attempt", "guided_anchor"],
                         [item["activity"] for item in result["selected_exercises"]])
        self.assertEqual(60, result["daily_dsa_budget_minutes"])

        no_candidate = self.anchor_attempt(155, 23, "2026-10-22")
        eligible = self.anchor_attempt(383, 5, "2026-10-22")
        result = queue.build_queue([no_candidate, eligible], "2026-10-25", 26, ["lc:383+3=1160"])
        self.assertTrue(any(item.get("slot_id") == "lc:383+3" for item in result["selected_exercises"]))
        self.assertTrue(any(item.get("slot_id") == "lc:155+3" and "validated_transfer_needed" in item["deferred_reason"]
                            for item in result["deferred_exercises"]))

    def test_same_anchor_due_slot_does_not_block_another_anchor_and_five_confirmations_overflow(self):
        first_a = self.anchor_attempt(121, 1, "2026-10-01")
        first_b = self.anchor_attempt(1, 3, "2026-10-05")
        result = queue.build_queue([first_a, first_b], "2026-10-08", 10,
                                   ["lc:121+3=2016", "lc:1+3=2441"])
        self.assertTrue(any(item.get("slot_id") == "lc:1+3" for item in result["selected_exercises"]))
        self.assertTrue(any(item.get("slot_id") == "lc:121+7" for item in result["deferred_exercises"]))

        pids_targets = [(121, 1, 2016), (1, 3, 2441), (383, 5, 1160), (49, 7, 893), (238, 9, 2906)]
        events = [self.anchor_attempt(pid, day, "2026-10-01") for pid, day, _ in pids_targets]
        confirmations = [f"lc:{pid}+3={target}" for pid, _, target in pids_targets]
        result = queue.build_queue(events, "2026-10-04", 10, confirmations)
        self.assertEqual(4, len(result["selected_exercises"]))
        self.assertTrue(any(item.get("slot_id") == "lc:49+3" and
                            item["original_due_date"] == "2026-10-04" for item in result["deferred_exercises"]))

    def test_conflicting_duplicate_event_ids_and_bad_timezone_fail_closed(self):
        event = {"schema_version": 2, "event_id": "dupe", "date": "2026-10-20", "timezone": "Asia/Kolkata",
                 "anchor_id": "lc:121", "anchor_problem_id": 121, "kind": "anchor_attempt",
                 "activity": "guided_anchor", "result": "attempted", "assistance": "none"}
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
