from __future__ import annotations

import json
import unittest
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-63/synthetic-v1.json"


def reference_validate(records, required_ids):
    """Instructor-only oracle, deliberately separate from learner module."""
    retained = tuple(dict(row) for row in records)
    by_id = {str(row.get("id")): row for row in retained}
    missing = tuple(item for item in required_ids if item not in by_id)
    invalid = []
    blocked = False
    passed = True
    required_set = set(required_ids)
    for row in retained:
        if row.get("id") not in required_set and row.get("domain") != "coverstory":
            invalid.append(str(row.get("id")))
    for evidence_id in required_ids:
        row = by_id.get(evidence_id)
        if row is None:
            passed = False
            continue
        if row.get("domain") != "coverstory" or row.get("fixture") != f"{evidence_id}-synthetic-v1":
            invalid.append(evidence_id)
            passed = False
            continue
        status = row.get("status")
        if status == "PASS":
            required = ("command", "commit", "environment", "fixture", "artifact")
            if any(not row.get(field) for field in required) or row.get("exit") != 0:
                invalid.append(evidence_id)
                passed = False
        elif status == "BLOCKED":
            blocked = True
            passed = False
        else:
            passed = False
    if invalid:
        gate = "HOLD"
    elif passed and not blocked and not missing:
        gate = "PASS"
    else:
        gate = "HOLD"
    return {"records": retained, "missing": missing, "invalid": tuple(invalid), "gate": gate}


class CAP63InfrastructureTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        cls.by_id = {row["id"]: row for row in cls.data["records"]}

    def test_fixture_provenance_is_complete_and_first_case_holds(self):
        cap57 = self.by_id["CAP-57"]
        self.assertEqual("coverstory", cap57["domain"])
        self.assertEqual("CAP-57-synthetic-v1", cap57["fixture"])
        self.assertEqual(0, cap57["exit"])
        self.assertTrue(cap57["artifact"])
        out = reference_validate([cap57, self.by_id["CAP-166"]], ["CAP-57", "CAP-166"])
        self.assertEqual("HOLD", out["gate"])

    def test_all_required_valid_receipts_pass_and_nonzero_mutant_does_not(self):
        cap57 = self.by_id["CAP-57"]
        cap60 = dict(cap57, id="CAP-60", fixture="CAP-60-synthetic-v1", artifact="docs/evidence/coverstory/CAP-60/result.json")
        self.assertEqual("PASS", reference_validate([cap57, cap60], ["CAP-57", "CAP-60"])["gate"])
        broken = dict(cap60, exit=1)
        out = reference_validate([cap57, broken], ["CAP-57", "CAP-60"])
        self.assertEqual(("CAP-60",), out["invalid"])
        self.assertEqual("HOLD", out["gate"])

    def test_oracle_detects_exit_artifact_and_domain_mutations(self):
        valid = self.by_id["CAP-57"]
        for field, value in (("exit", 1), ("artifact", None), ("domain", "event"), ("fixture", "event-domain-v1")):
            with self.subTest(field=field):
                out = reference_validate([dict(valid, **{field: value})], ["CAP-57"])
                self.assertEqual(("CAP-57",), out["invalid"])

    def test_missing_is_not_pass_and_historical_record_is_preserved(self):
        historical = self.by_id["LEGACY-EVENT-01"]
        out = reference_validate([historical], ["CAP-60", "CAP-57"])
        self.assertEqual(("CAP-60", "CAP-57"), out["missing"])
        self.assertIn(historical, out["records"])
        self.assertIn("LEGACY-EVENT-01", out["invalid"])
        self.assertEqual("HOLD", out["gate"])

    def test_oracle_rejects_sort_as_missing_behavior_mutant(self):
        # The contract preserves required-ID order, including multiple absences.
        required = ["CAP-60", "CAP-57", "CAP-43"]
        out = reference_validate([], required)
        sorted_mutant_output = tuple(sorted(required))
        self.assertEqual(tuple(required), out["missing"])
        self.assertNotEqual(out["missing"], sorted_mutant_output)


if __name__ == "__main__":
    unittest.main()
