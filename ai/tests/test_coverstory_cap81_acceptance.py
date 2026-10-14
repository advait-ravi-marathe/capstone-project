"""Saved CAP-81 learner contracts. All data is synthetic and content-free."""
from __future__ import annotations

import copy
import json
import unittest
from dataclasses import fields
from pathlib import Path

from ai.coverstory.cap_81 import (
    AuditRecord, AuthoritativeJourneySnapshot, BusinessTransition, JourneyProjection,
    JourneyStepEvent, PaymentEffect, PaymentObservation, ProtectedPayloadReference,
    RollbackProbe, RunMetadata, check_invariants,
)

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-81/synthetic-v1.json"


def load_case(case_id: str):
    fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))
    case = next(row for row in fixture["cases"] if row["id"] == case_id)
    raw = copy.deepcopy(fixture["base_snapshot"])
    mutation = case["mutations"]
    remove_audits = set(mutation.get("remove_audit_transition_ids", ()))
    raw["audit_records"] = [row for row in raw["audit_records"] if row["transition_id"] not in remove_audits]
    raw["audit_records"].extend(mutation.get("foreign_audits", ()))
    remove_steps = set(mutation.get("remove_step_transition_ids", ()))
    raw["journey_step_events"] = [row for row in raw["journey_step_events"] if row["transition_id"] not in remove_steps]
    raw["analytics_transition_ids"] = mutation.get("analytics_transition_ids", raw["analytics_transition_ids"])
    raw["protected_payload_references"].extend(mutation.get("extra_payload_references", ()))
    raw["history_sequences"] = mutation.get("history_sequences", raw["history_sequences"])
    raw["run_metadata"]["commit_sha"] = mutation.get("commit_sha", raw["run_metadata"]["commit_sha"])
    raw["run_metadata"]["command"] = mutation.get("command", raw["run_metadata"]["command"])
    return case, make_snapshot(raw)


def make_snapshot(raw: dict) -> AuthoritativeJourneySnapshot:
    return AuthoritativeJourneySnapshot(
        owner_id=raw["owner_id"],
        run_metadata=RunMetadata(**raw["run_metadata"]),
        transitions=tuple(BusinessTransition(**row) for row in raw["transitions"]),
        audit_records=tuple(AuditRecord(**row) for row in raw["audit_records"]),
        journey=JourneyProjection(**raw["journey"]) if raw["journey"] else None,
        journey_step_events=tuple(JourneyStepEvent(**row) for row in raw["journey_step_events"]),
        history_sequences=tuple(raw["history_sequences"]),
        analytics_transition_ids=tuple(raw["analytics_transition_ids"]),
        payment_observations=tuple(PaymentObservation(**row) for row in raw["payment_observations"]),
        payment_effects=tuple(PaymentEffect(**row) for row in raw["payment_effects"]),
        protected_payload_references=tuple(ProtectedPayloadReference(**row)
                                           for row in raw["protected_payload_references"]),
        rollback_probes=tuple(RollbackProbe(
            boundary=row["boundary"],
            before={key: tuple(value) for key, value in row["before"].items()},
            after={key: tuple(value) for key, value in row["after"].items()},
        ) for row in raw["rollback_probes"]),
    )


class CAP81AcceptanceTest(unittest.TestCase):
    def assert_expected(self, case_id: str) -> None:
        case, snapshot = load_case(case_id)
        report = check_invariants(snapshot)
        self.assertEqual(case["expected_status"], report.status)
        self.assertEqual(set(case["expected_codes"]), {row.code for row in report.violations})

    def test_complete_owner_a_payment_application_and_version_five_journey_pass_with_no_analytics(self):
        case, snapshot = load_case("CAP-81-CS-01")
        self.assertEqual(3, len(snapshot.transitions))
        self.assertEqual(5, snapshot.journey.version)
        self.assertEqual([], list(snapshot.analytics_transition_ids))
        report = check_invariants(snapshot)
        self.assertEqual(case["expected_status"], report.status)
        self.assertEqual(3, report.counts["audits_matched"])
        self.assertEqual(3, report.counts["step_events_matched"])

    def test_missing_authoritative_rows_cannot_be_masked_by_balanced_counts_or_analytics(self):
        self.assert_expected("CAP-81-CS-02")

    def test_every_injected_failure_snapshot_is_unchanged_across_four_record_families(self):
        case, snapshot = load_case("CAP-81-CS-03")
        self.assertEqual(4, len(snapshot.rollback_probes))
        report = check_invariants(snapshot)
        self.assertEqual(case["expected_status"], report.status)
        self.assertEqual(4, report.counts["rollback_probes_checked"])

    def test_append_only_failed_unknown_success_history_counts_one_linked_effect(self):
        case, snapshot = load_case("CAP-81-CS-04")
        self.assertEqual(case["expected_payment_states"], [row.state for row in snapshot.payment_observations])
        self.assertTrue(snapshot.payment_observations[-1].reconciled)
        report = check_invariants(snapshot)
        self.assertEqual(case["expected_status"], report.status)
        self.assertEqual(case["expected_effect_count"], report.counts["money_effect_count"])
        self.assertEqual(case["expected_posted_total_minor"], report.counts["posted_total_minor"])

    def test_foreign_owner_rows_do_not_mask_audit_gap_and_plaintext_duplicate_is_redacted(self):
        self.assert_expected("CAP-81-CS-05")
        case, snapshot = load_case("CAP-81-CS-05")
        report = check_invariants(snapshot)
        rendered = repr(report)
        self.assertNotIn("sha256:1111111111111111111111111111111111111111111111111111111111111111", rendered)
        self.assertNotIn("consumer", rendered.lower())

    def test_sequence_gap_or_missing_commit_command_can_never_pass(self):
        self.assert_expected("CAP-81-CS-06")


if __name__ == "__main__":
    unittest.main()
