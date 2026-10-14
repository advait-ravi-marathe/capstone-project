from __future__ import annotations

import hashlib
import json
import unittest
from datetime import datetime, timedelta, timezone
from pathlib import Path

from ai.coverstory.cap_79 import SourceHit

FIXTURE = Path(__file__).resolve().parents[1] / "../fixtures/coverstory/CAP-79/synthetic-v1.json"


def parse_time(value: str) -> datetime:
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def fixture_hit(row: dict) -> SourceHit:
    return SourceHit(row["source_id"], row["version"], row["content_sha256"], row["evidence_type"],
                     parse_time(row["observed_at"]), row["source_label"])


def instructor_reference(hits: list[SourceHit], now: datetime, max_age: timedelta) -> dict:
    """Independent oracle implementation; intentionally does not call filter_fresh."""
    retained, rejected = [], []
    for hit in hits:
        timestamp = hit.observed_at
        if timestamp.tzinfo is None or timestamp.utcoffset() is None or timestamp > now:
            rejected.append({"source_id": hit.source_id, "version": hit.version, "reason": "INVALID_TIME"})
            continue
        age = now - timestamp
        if age > max_age:
            rejected.append({"source_id": hit.source_id, "version": hit.version, "reason": "STALE"})
            continue
        retained.append({"source_id": hit.source_id, "version": hit.version,
                         "content_sha256": hit.content_sha256, "evidence_type": hit.evidence_type,
                         "source_label": hit.source_label})
    return {"status": "FRESH" if retained else "ABSTAIN", "retained": retained, "rejected": rejected,
            "quote_order_eligible": False}


class CAP79InfrastructureTest(unittest.TestCase):
    def setUp(self) -> None:
        self.fixture = json.loads(FIXTURE.resolve().read_text(encoding="utf-8"))
        self.now = parse_time(self.fixture["fixed_now"])
        self.max_age = timedelta(seconds=self.fixture["max_age_seconds"])
        self.cases = {row["id"]: row for row in self.fixture["cases"]}

    def test_fixture_hashes_clock_and_source_versions_are_self_consistent(self):
        self.assertEqual("SYNTHETIC_OFFLINE_CONTRACT", self.fixture["classification"])
        self.assertFalse(self.fixture["actual_sources_used"])
        self.assertIsNotNone(self.now.tzinfo)
        self.assertEqual(timedelta(hours=24), self.max_age)
        for case in self.fixture["cases"]:
            self.assertTrue(case["id"].startswith("CAP-79-CS-"))
            for hit in case["input"]:
                self.assertEqual(hashlib.sha256(hit["excerpt"].encode("utf-8")).hexdigest(), hit["content_sha256"])
                self.assertIn(hit["scope"], self.fixture["allowed_source_scope"])
        versions = {row["version"] for row in self.cases["CAP-79-CS-04"]["input"]}
        self.assertEqual({"v1", "v2"}, versions)

    def test_independent_oracle_accepts_each_saved_case_and_detects_boundary_mutant(self):
        for case in self.fixture["cases"]:
            hits = [fixture_hit(row) for row in case["input"]]
            reference = instructor_reference(hits, self.now, self.max_age)
            expected = case["expected"]
            self.assertEqual(expected["status"], reference["status"], case["id"])
            self.assertEqual(expected["retained"], reference["retained"], case["id"])
            self.assertEqual(expected.get("rejected", []), reference["rejected"], case["id"])
            self.assertFalse(reference["quote_order_eligible"], case["id"])

        boundary = fixture_hit(self.cases["CAP-79-CS-02"]["input"][0])
        age = self.now - boundary.observed_at
        defective_retained = age < self.max_age  # deliberate strict-boundary mutant
        self.assertFalse(defective_retained)
        self.assertEqual("boundary", boundary.version)
        self.assertIn("boundary", [row["version"] for row in instructor_reference([boundary], self.now, self.max_age)["retained"]])

    def test_reference_rejects_latest_version_relabel_and_payable_illustration_mutants(self):
        version_case = self.cases["CAP-79-CS-04"]
        result = instructor_reference([fixture_hit(row) for row in version_case["input"]], self.now, self.max_age)
        self.assertEqual(["v2"], [row["version"] for row in result["retained"]])
        defective_latest_relabel = [{**result["retained"][0], "version": "v1"}]
        self.assertNotEqual(result["retained"], defective_latest_relabel)

        illustration = instructor_reference(
            [fixture_hit(row) for row in self.cases["CAP-79-CS-05"]["input"]], self.now, self.max_age)
        self.assertEqual("ADVERTISEMENT_ILLUSTRATION", illustration["retained"][0]["evidence_type"])
        self.assertFalse(illustration["quote_order_eligible"])
        defective_payable = {**illustration, "quote_order_eligible": True}
        self.assertNotEqual(illustration, defective_payable)

    def test_setup_smoke_is_independent_of_learner_todo(self):
        self.assertEqual("ABSTAIN", instructor_reference([], self.now, self.max_age)["status"])


if __name__ == "__main__":
    unittest.main()
