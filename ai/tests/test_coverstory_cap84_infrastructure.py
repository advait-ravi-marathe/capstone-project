"""Independent CAP-84 contract witnesses; never invokes learner validation."""
from __future__ import annotations

import copy
import json
import unittest
from datetime import datetime, timezone
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-84/synthetic-v1.json"
FIELDS = {"ownerId", "eventId", "providerRef", "schemaVersion", "occurredAt", "amountMinor", "currency", "state"}
STATES = {"PENDING", "FAILED", "UNKNOWN", "SUCCEEDED"}


def data():
    return json.loads(FIXTURE.read_text(encoding="utf-8"))


def reference_validate(raw: dict, context: dict) -> dict:
    unknown = sorted(set(raw) - FIELDS)
    if unknown:
        return {"code": "UNKNOWN_FIELD", "diagnostics": unknown, "eligible": False, "writes": 0, "unlock": False}
    missing = sorted(FIELDS - set(raw))
    if missing:
        return {"code": "MISSING_FIELD", "diagnostics": missing, "eligible": False, "writes": 0, "unlock": False}
    if type(raw["schemaVersion"]) is not int or type(raw["amountMinor"]) is not int:
        return {"code": "INVALID_TYPE", "diagnostics": [], "eligible": False, "writes": 0, "unlock": False}
    for key in ("ownerId", "eventId", "providerRef", "occurredAt", "currency", "state"):
        if not isinstance(raw[key], str):
            return {"code": "INVALID_TYPE", "diagnostics": [key], "eligible": False, "writes": 0, "unlock": False}
    if raw["schemaVersion"] != 1:
        return {"code": "INVALID_SCHEMA", "diagnostics": [], "eligible": False, "writes": 0, "unlock": False}
    if raw["ownerId"] != context["trusted_owner_id"]:
        return {"code": "OWNER_MISMATCH", "diagnostics": [], "eligible": False, "writes": 0, "unlock": False}
    if raw["amountMinor"] < 0 or raw["amountMinor"] != context["expected_amount_minor"] or raw["currency"] != "INR" or raw["currency"] != context["expected_currency"]:
        return {"code": "PAYMENT_MISMATCH", "diagnostics": [], "eligible": False, "writes": 0, "unlock": False}
    if not context["provider_source_supported"]:
        return {"code": "UNSUPPORTED_SOURCE", "diagnostics": [], "eligible": False, "writes": 0, "unlock": False}
    try:
        when = datetime.fromisoformat(raw["occurredAt"].replace("Z", "+00:00"))
    except ValueError:
        return {"code": "INVALID_TIMEZONE", "diagnostics": [], "eligible": False, "writes": 0, "unlock": False}
    if when.tzinfo is None or when.utcoffset() is None or when.utcoffset().total_seconds() != 0:
        return {"code": "INVALID_TIMEZONE", "diagnostics": [], "eligible": False, "writes": 0, "unlock": False}
    if raw["state"] not in STATES:
        return {"code": "INVALID_VALUE", "diagnostics": ["state"], "eligible": False, "writes": 0, "unlock": False}
    normalized = {
        "ownerId": raw["ownerId"], "eventId": raw["eventId"], "providerRef": raw["providerRef"],
        "providerSource": context["provider_source"], "schemaVersion": 1,
        "occurredAt": when.astimezone(timezone.utc).isoformat(), "amountMinor": raw["amountMinor"],
        "currency": raw["currency"], "state": raw["state"],
    }
    return {"code": "VALID", "diagnostics": [], "eligible": True, "writes": 0, "unlock": False,
            "normalized": normalized}


class CAP84InfrastructureTest(unittest.TestCase):
    def setUp(self):
        self.fixture = data()

    def test_fixture_strict_schema_and_positive_record_are_consistent(self):
        self.assertEqual("SYNTHETIC_MOCK_PROVIDER_ENVELOPE", self.fixture["classification"])
        self.assertFalse(self.fixture["actual_provider_call"])
        self.assertEqual(FIELDS, set(self.fixture["required_fields"]))
        self.assertEqual(FIELDS, set(self.fixture["valid_envelope"]))
        context = self.fixture["base_context"]
        accepted = reference_validate(self.fixture["valid_envelope"], context)
        self.assertEqual("VALID", accepted["code"])
        self.assertTrue(accepted["eligible"])
        self.assertEqual(0, accepted["writes"])
        self.assertFalse(accepted["unlock"])
        self.assertEqual("2026-11-27T12:30:00+00:00", accepted["normalized"]["occurredAt"])

    def test_reference_accepts_all_saved_expected_outcomes(self):
        context = self.fixture["base_context"]
        valid = self.fixture["valid_envelope"]
        for row in self.fixture["cases"]:
            with self.subTest(case=row["id"]):
                if row["kind"] == "valid":
                    result = reference_validate(valid, context)
                    self.assertEqual(row["expected_code"], result["code"])
                    self.assertEqual(row["expected_eligible"], result["eligible"])
                elif row["kind"] == "invalid_types":
                    for variant in row["variants"]:
                        candidate = {**valid, variant["field"]: variant["value"]}
                        result = reference_validate(candidate, context)
                        self.assertEqual(row["expected_code"], result["code"])
                        self.assertEqual(row["expected_writes"], result["writes"])
                elif row["kind"] == "owner_mismatch":
                    result = reference_validate({**valid, "ownerId": row["owner"]}, context)
                    self.assertEqual(row["expected_code"], result["code"])
                elif row["kind"] == "unknown_field":
                    candidate = {**valid, row["field"]: row["value"]}
                    result = reference_validate(candidate, context)
                    self.assertEqual(row["expected_code"], result["code"])
                    self.assertEqual(row["expected_diagnostic_fields"], result["diagnostics"])
                    self.assertNotIn(row["value"], json.dumps(result))
                elif row["kind"] == "domain_mismatch":
                    for variant in row["variants"]:
                        candidate = {**valid, variant["field"]: variant["value"]}
                        if "state" in variant:
                            candidate["state"] = variant["state"]
                        result = reference_validate(candidate, context)
                        self.assertEqual(variant["expected_code"], result["code"])
                        self.assertFalse(result["unlock"])
                elif row["kind"] == "unsupported_source":
                    unsupported = {**context, "provider_source": row["context_source"],
                                   "provider_source_supported": False}
                    self.assertEqual(row["expected_code"], reference_validate(valid, unsupported)["code"])

    def test_defect_witnesses_reject_bool_integer_owner_trust_and_echo(self):
        raw = copy.deepcopy(self.fixture["valid_envelope"])
        context = self.fixture["base_context"]

        # Deliberately defective Python type check that treats bool as int.
        self.assertTrue(isinstance(True, int))
        self.assertNotEqual("VALID", reference_validate({**raw, "schemaVersion": True}, context)["code"])
        self.assertNotEqual("VALID", reference_validate({**raw, "amountMinor": True}, context)["code"])

        # Deliberately defective owner source: replacing the trusted batch owner with body owner.
        body_b = {**raw, "ownerId": "owner-B"}
        self.assertEqual("OWNER_MISMATCH", reference_validate(body_b, context)["code"])
        defective_trust = {**context, "trusted_owner_id": body_b["ownerId"]}
        self.assertEqual("VALID", reference_validate(body_b, defective_trust)["code"])

        extra = {**raw, "apiKey": "private-marker-value"}
        result = reference_validate(extra, context)
        self.assertEqual(["apiKey"], result["diagnostics"])
        self.assertNotIn("private-marker-value", json.dumps(result))

        time = reference_validate({**raw, "occurredAt": "2026-11-27T12:30:00"}, context)
        self.assertEqual("INVALID_TIMEZONE", time["code"])
        wrong_money = reference_validate({**raw, "amountMinor": 99999, "state": "UNKNOWN_STATE"}, context)
        self.assertEqual("PAYMENT_MISMATCH", wrong_money["code"], "exact money match is checked before state classification")


if __name__ == "__main__":
    unittest.main()
