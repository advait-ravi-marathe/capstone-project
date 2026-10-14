"""Independent CAP-69 instructor oracle with positive and defective witnesses."""
from __future__ import annotations
import json
import math
import unittest
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-69/synthetic-v1.json"


def oracle(run: dict) -> dict:
    offered, capacity, rate = run["offered"], run["capacity"], run["rate_per_second"]
    if (not isinstance(offered, int) or offered < 0 or not isinstance(capacity, int)
            or capacity <= 0 or not isinstance(rate, int) or rate <= 0):
        raise ValueError("INVALID_CONFIG")
    strategy = run.get("strategy", "bounded")
    if strategy not in ("bounded", "direct"):
        raise ValueError("INVALID_CONFIG")
    accepted = (min(offered, capacity) if strategy == "bounded"
                else min(offered, 1))
    rejected = offered - accepted
    # Every admitted task completes in FIFO order at a fixed interval.
    service_ms = 1000 // rate
    completion = [service_ms * i for i in range(1, accepted + 1)]
    p95 = None if not completion else completion[math.ceil(.95 * len(completion)) - 1]
    return {"accepted": accepted, "rejected": rejected, "completed": accepted,
            "queue_max": accepted, "p95_ms": p95,
            "drain_ms": 0 if not completion else completion[-1]}


def unbounded_capacity_defect(run: dict) -> dict:
    """Known bad oracle: lets every offer queue, silently dropping the cap."""
    return oracle({**run, "capacity": run["offered"] or 1})


def hides_overload_defect(evidence: dict) -> bool:
    """Known bad validator: equates queueing with eliminated overload."""
    return evidence["completed"] == evidence["accepted"]


def correct_claim_validator(evidence: dict, claim: str) -> bool:
    normalized = claim.casefold()
    if "eliminated overload" in normalized and evidence["rejected"] > 0:
        return False
    return True


class CAP69InstructorInfrastructureTest(unittest.TestCase):
    def test_fixture_and_exact_positive_negative_cases(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.assertEqual("CAP-69-synthetic-v1", data["fixture_id"])
        self.assertTrue(data["synthetic"])
        burst = data["cases"][0]
        actual = oracle(burst["run"])
        for key, value in burst["expected"].items():
            mapped = "queue_max" if key == "queue_max_at_admission" else key
            self.assertEqual(value, actual[mapped])
        empty_case = next(case for case in data["cases"] if case["id"] == "CAP-69-CS-02")
        empty = oracle(empty_case["run"])
        self.assertEqual((0, 0, 0, None, 0), tuple(empty[k] for k in
                         ("accepted", "rejected", "completed", "p95_ms", "drain_ms")))
        for case in data["cases"]:
            if case.get("error") != "INVALID_CONFIG":
                continue
            with self.assertRaisesRegex(ValueError, "INVALID_CONFIG"):
                oracle(case["run"])

    def test_known_defects_are_detected_by_the_correct_witness(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        run = data["cases"][0]["run"]
        correct = oracle(run)
        defective = unbounded_capacity_defect(run)
        self.assertEqual(10, correct["accepted"])
        self.assertEqual(20, defective["accepted"])
        self.assertNotEqual(correct, defective)
        direct = oracle({**run, "strategy": "direct"})
        self.assertEqual((1, 19, 1, 500), tuple(direct[k] for k in
                         ("accepted", "rejected", "completed", "drain_ms")))
        self.assertLess(direct["completed"], correct["completed"])
        claim_case = next(case for case in data["cases"] if "evidence" in case)
        claim_evidence = claim_case["evidence"]
        self.assertEqual(10, claim_evidence["rejected"])
        self.assertTrue(hides_overload_defect(claim_evidence))
        # Correct evidence rule rejects the false claim because rejection remains.
        self.assertFalse(correct_claim_validator(claim_evidence,
                                                 "The queue eliminated overload."))
        self.assertTrue(correct_claim_validator(claim_evidence,
                                                "The queue bounded in-flight work."))


if __name__ == "__main__":
    unittest.main()
