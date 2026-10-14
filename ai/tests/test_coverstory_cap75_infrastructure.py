"""Independent CAP-75 instructor oracle and adverse witnesses."""
from __future__ import annotations
import json
import unittest
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-75/synthetic-v1.json"


def oracle(run: dict) -> dict:
    kind = run.get("kind")
    batch_id = run.get("batch_id")
    if not isinstance(batch_id, str) or not batch_id:
        raise ValueError("INVALID_INPUT")
    if kind == "malformed_json" and isinstance(run.get("line_number"), int):
        return {"stage": "PARSE", "code": "MALFORMED_JSON", "retryable": False,
                "evidence_ids": [f"{batch_id}:{run['line_number']}"]}
    if kind == "sql_timeout" and run.get("commit_state") == "not_committed":
        event_id = run.get("event_id")
        if not isinstance(event_id, str) or not event_id:
            raise ValueError("INVALID_INPUT")
        return {"stage": "LOAD", "code": "DB_TIMEOUT", "retryable": True,
                "evidence_ids": [f"batch:{batch_id}", f"event:{event_id}"]}
    if kind == "event_hash_conflict":
        if run.get("event_id") and run.get("existing_hash") != run.get("incoming_hash"):
            return {"stage": "IDEMPOTENCY", "code": "EVENT_HASH_CONFLICT",
                    "retryable": False, "evidence_ids": [f"event:{run['event_id']}"]}
        raise ValueError("INVALID_INPUT")
    # Unknown details are intentionally discarded; identifiers are the only evidence.
    return {"stage": "UNKNOWN", "code": "UNKNOWN", "retryable": False,
            "evidence_ids": [f"batch:{batch_id}"]}


def broad_retry_defect(run: dict) -> dict:
    """Known bad policy: blindly retries every failure and forwards raw details."""
    return {"stage": "UNKNOWN", "code": "RETRY", "retryable": True,
            "evidence_ids": [run.get("batch_id", ""), run.get("exception_text", "")]}


class CAP75InstructorInfrastructureTest(unittest.TestCase):
    def test_fixture_cases_match_independent_safe_oracle(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.assertEqual("CAP-75-synthetic-v1", data["fixture_id"])
        self.assertTrue(data["synthetic"])
        for case in data["cases"]:
            with self.subTest(case=case["id"]):
                actual = oracle(case["run"])
                self.assertEqual(case["expected"], actual)
                for forbidden in case.get("forbidden_output_substrings", []):
                    self.assertNotIn(forbidden, repr(actual))

    def test_correct_policy_catches_blind_retry_and_secret_leak_witness(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        for case in data["cases"]:
            correct = oracle(case["run"])
            defective = broad_retry_defect(case["run"])
            self.assertNotEqual(correct, defective)
        unknown = data["cases"][3]
        defective = broad_retry_defect(unknown["run"])
        rendered = repr(defective)
        self.assertIn("SYNTHETIC_SECRET_75_DO_NOT_EMIT", rendered)
        safe = oracle(unknown["run"])
        self.assertNotIn("SYNTHETIC_SECRET_75_DO_NOT_EMIT", repr(safe))
        self.assertFalse(safe["retryable"])
        self.assertEqual(["batch:b4"], safe["evidence_ids"])


if __name__ == "__main__":
    unittest.main()
