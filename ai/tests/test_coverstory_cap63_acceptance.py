from __future__ import annotations

import json
import unittest
from pathlib import Path

from ai.coverstory.cap_63 import validate_evidence

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-63/synthetic-v1.json"


class CAP63AcceptanceTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        cls.by_id = {row["id"]: row for row in cls.data["records"]}

    def test_cs01_pass_plus_blocked_holds_first_demo_gate(self):
        report = validate_evidence([self.by_id["CAP-57"], self.by_id["CAP-166"]], ["CAP-57", "CAP-166"])
        self.assertEqual("HOLD", report.gate)

    def test_cs02_missing_cap60_is_listed_and_holds(self):
        report = validate_evidence([self.by_id["CAP-57"]], ["CAP-57", "CAP-60"])
        self.assertEqual(("CAP-60",), report.missing)
        self.assertEqual("HOLD", report.gate)

    def test_cs03_failed_command_or_absent_artifact_invalidates_claimed_pass(self):
        for field, value in (("exit", 1), ("artifact", None)):
            with self.subTest(field=field):
                candidate = dict(self.by_id["CAP-57"], **{field: value})
                report = validate_evidence([candidate], ["CAP-57"])
                self.assertIn("CAP-57", report.invalid)

    def test_cs04_legacy_event_receipt_is_rejected_but_retained(self):
        record = self.by_id["LEGACY-EVENT-01"]
        report = validate_evidence([record], ["CAP-57"])
        self.assertIn("LEGACY-EVENT-01", report.invalid)
        self.assertIn(record, report.records)


if __name__ == "__main__":
    unittest.main()
