"""CAP-84 saved acceptance contract. Synthetic inputs only; no provider calls."""
from __future__ import annotations

import copy
import json
import unittest
from datetime import timezone
from pathlib import Path

from ai.coverstory.cap_84 import BatchContext, validate_provider_event

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-84/synthetic-v1.json"


class WriteSpy:
    def __init__(self):
        self.calls = 0

    def attempted_write(self):
        self.calls += 1


class CAP84AcceptanceTest(unittest.TestCase):
    def setUp(self):
        self.fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.event = copy.deepcopy(self.fixture["valid_envelope"])
        self.spy = WriteSpy()
        self.context = self.make_context()

    def make_context(self, **changes):
        return BatchContext(**{**self.fixture["base_context"], "write_probe": self.spy, **changes})

    def case(self, case_id: str):
        return next(row for row in self.fixture["cases"] if row["id"] == case_id)

    def test_owner_a_envelope_normalizes_to_one_eligible_input_without_mutation(self):
        result = validate_provider_event(self.event, self.context)
        self.assertEqual("VALID", result.code)
        self.assertTrue(result.eligible_command_input)
        self.assertEqual(0, result.writes)
        self.assertEqual(0, self.spy.calls)
        self.assertFalse(result.onboarding_unlocked)
        self.assertEqual("owner-A", result.normalized.owner_id)
        self.assertEqual("event-synth-A-001", result.normalized.event_id)
        self.assertEqual("provider-ref-synth-A-001", result.normalized.provider_ref)
        self.assertEqual(1, result.normalized.schema_version)
        self.assertEqual(timezone.utc, result.normalized.occurred_at.tzinfo)
        self.assertEqual(100000, result.normalized.amount_minor)
        self.assertEqual("INR", result.normalized.currency)
        self.assertEqual("SUCCEEDED", result.normalized.state)

    def test_boolean_and_string_integer_fields_are_rejected_as_invalid_type(self):
        row = self.case("CAP-84-CS-02")
        for variant in row["variants"]:
            with self.subTest(variant=variant):
                event = copy.deepcopy(self.event)
                event[variant["field"]] = variant["value"]
                result = validate_provider_event(event, self.context)
                self.assertEqual("INVALID_TYPE", result.code)
                self.assertIsNone(result.normalized)
                self.assertEqual(0, result.writes)
        self.assertEqual(0, self.spy.calls)

    def test_owner_b_under_trusted_batch_a_is_rejected_before_any_write(self):
        self.event["ownerId"] = self.case("CAP-84-CS-03")["owner"]
        result = validate_provider_event(self.event, self.context)
        self.assertEqual("OWNER_MISMATCH", result.code)
        self.assertEqual(0, result.writes)
        self.assertEqual(0, self.spy.calls)
        self.assertFalse(result.eligible_command_input)

    def test_unknown_field_name_is_safe_diagnostic_without_value_echo(self):
        row = self.case("CAP-84-CS-04")
        self.event[row["field"]] = row["value"]
        result = validate_provider_event(self.event, self.context)
        self.assertEqual("UNKNOWN_FIELD", result.code)
        self.assertEqual(tuple(row["expected_diagnostic_fields"]), result.diagnostics)
        self.assertNotIn(row["value"], repr(result))
        self.assertEqual(0, self.spy.calls)

    def test_naive_time_and_money_mismatches_are_rejected_without_unlock(self):
        row = self.case("CAP-84-CS-05")
        for variant in row["variants"]:
            with self.subTest(variant=variant):
                event = copy.deepcopy(self.event)
                event[variant["field"]] = variant["value"]
                if "state" in variant:
                    event["state"] = variant["state"]
                result = validate_provider_event(event, self.context)
                self.assertEqual(variant["expected_code"], result.code)
                self.assertFalse(result.onboarding_unlocked)
                self.assertEqual(0, result.writes)
        self.assertEqual(0, self.spy.calls)

    def test_unsupported_source_is_not_accepted_as_live_callback(self):
        row = self.case("CAP-84-CS-06")
        context = self.make_context(provider_source=row["context_source"], provider_source_supported=False)
        result = validate_provider_event(self.event, context)
        self.assertEqual(row["expected_code"], result.code)
        self.assertFalse(result.eligible_command_input)
        self.assertEqual(0, result.writes)
        self.assertEqual(0, self.spy.calls)


if __name__ == "__main__":
    unittest.main()
