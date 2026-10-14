from __future__ import annotations

import hashlib
import json
import unittest
from datetime import datetime, timedelta
from pathlib import Path

from ai.coverstory.cap_86 import SourceEvidence, decide_onboarding_answer

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-86/synthetic-v1.json"


def parse_time(value: str) -> datetime:
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def as_hit(row: dict) -> SourceEvidence:
    return SourceEvidence(row["source_id"], row["owner_id"], row["version"], row["excerpt"],
                          row["content_sha256"], parse_time(row["observed_at"]), row["score"],
                          row.get("source_label", "SYNTHETIC"))


def independent_policy(principal: str, hits: tuple[dict, ...], now: datetime, threshold: float) -> dict:
    """Independent reference: directly filters raw fixture records, no seam calls."""
    cutoff = now - timedelta(hours=24)
    accepted = []
    for row in hits:
        observed = parse_time(row["observed_at"])
        if row["owner_id"] != principal:
            continue
        if observed.tzinfo is None or observed.utcoffset() is None or observed > now or observed < cutoff:
            continue
        if hashlib.sha256(row["excerpt"].encode("utf-8")).hexdigest() != row["content_sha256"]:
            continue
        if row["score"] < threshold:
            continue
        accepted.append(row)
    if not accepted:
        return {"status": "ABSTAIN", "explanation": "", "citations": [], "manual_path": True, "writes": 0}
    chosen = max(accepted, key=lambda row: row["score"])
    return {"status": "ANSWER", "explanation": chosen["excerpt"],
            "citations": [{"source_id": chosen["source_id"], "version": chosen["version"],
                           "content_sha256": chosen["content_sha256"]}],
            "manual_path": False, "writes": 0}


def serialized(decision) -> dict:
    return {"status": decision.status, "explanation": decision.explanation,
            "citations": [{"source_id": c.source_id, "version": c.version,
                           "content_sha256": c.content_sha256} for c in decision.citations],
            "manual_path": decision.manual_path, "writes": decision.writes}


class CAP86InfrastructureTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))
        cls.now = parse_time(cls.fixture["fixed_now"])
        cls.cases = {case["id"]: case for case in cls.fixture["cases"]}

    def test_synthetic_fixture_is_offline_hashed_and_has_six_saved_cases(self):
        self.assertTrue(self.fixture["synthetic"])
        self.assertFalse(self.fixture["actual_sources_used"])
        self.assertEqual(86400, self.fixture["max_age_seconds"])
        self.assertEqual(0.7, self.fixture["score_threshold"])
        self.assertEqual([f"CAP-86-CS-0{i}" for i in range(1, 7)], list(self.cases))
        for case in self.fixture["cases"]:
            records = ([case["source"]] if "source" in case else case["hits"])
            for row in records:
                digest_matches = hashlib.sha256(row["excerpt"].encode()).hexdigest() == row["content_sha256"]
                if case["id"] == "CAP-86-CS-04":
                    self.assertFalse(digest_matches, row["source_id"])
                else:
                    self.assertTrue(digest_matches, row["source_id"])
                self.assertEqual("SYNTHETIC", row["source_label"])

    def test_independent_oracle_satisfies_all_saved_cases_and_empty_hits(self):
        for case in self.fixture["cases"]:
            records = (case["source"],) if "source" in case else tuple(case["hits"])
            actual = independent_policy(case["principal"], records, self.now,
                                        self.fixture["score_threshold"])
            self.assertEqual(case["expected"], actual, case["id"])
        empty = independent_policy("applicant-A", (), self.now, 0.7)
        self.assertEqual(("ABSTAIN", [], True, 0),
                         (empty["status"], empty["citations"], empty["manual_path"], empty["writes"]))

    def test_mutants_for_owner_staleness_digest_and_inclusive_threshold_are_distinguished(self):
        foreign = self.cases["CAP-86-CS-02"]["source"]
        self.assertEqual("ABSTAIN", independent_policy("applicant-A", (foreign,), self.now, 0.7)["status"])
        stale = self.cases["CAP-86-CS-03"]["source"]
        self.assertEqual("ABSTAIN", independent_policy("applicant-A", (stale,), self.now, 0.7)["status"])
        tampered = self.cases["CAP-86-CS-04"]["source"]
        self.assertEqual("ABSTAIN", independent_policy("applicant-A", (tampered,), self.now, 0.7)["status"])
        boundary = self.cases["CAP-86-CS-06"]["source"]
        result = independent_policy("applicant-A", (boundary,), self.now, 0.7)
        self.assertEqual("ANSWER", result["status"])
        strict_greater_mutant = boundary["score"] > 0.7
        self.assertFalse(strict_greater_mutant)
        self.assertNotEqual(strict_greater_mutant, result["status"] == "ANSWER")

    def test_setup_smoke_uses_only_fixed_clock_and_local_values(self):
        self.assertEqual("ABSTAIN", independent_policy("applicant-A", (), self.now, 0.7)["status"])
        self.assertEqual(0, self.fixture.get("network_calls", 0))
        self.assertEqual(0, self.fixture.get("application_writes", 0))

    def test_learner_seam_is_present_and_explicitly_unimplemented(self):
        sample = as_hit(self.cases["CAP-86-CS-01"]["source"])
        with self.assertRaisesRegex(NotImplementedError, "CAP-86 learner TODO"):
            decide_onboarding_answer("applicant-A", (sample,), self.now)


if __name__ == "__main__":
    unittest.main()
