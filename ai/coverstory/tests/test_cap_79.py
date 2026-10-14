from __future__ import annotations

import json
import unittest
from datetime import datetime, timedelta
from pathlib import Path

from ai.coverstory.cap_79 import SourceHit, filter_fresh

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-79/synthetic-v1.json"


def load_fixture():
    return json.loads(FIXTURE.read_text(encoding="utf-8"))


def parse_time(value: str) -> datetime:
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def to_hit(row: dict) -> SourceHit:
    return SourceHit(row["source_id"], row["version"], row["content_sha256"], row["evidence_type"],
                     parse_time(row["observed_at"]), row["source_label"])


class CAP79AcceptanceTest(unittest.TestCase):
    def setUp(self) -> None:
        self.fixture = load_fixture()
        self.now = parse_time(self.fixture["fixed_now"])
        self.max_age = timedelta(seconds=self.fixture["max_age_seconds"])
        self.cases = {row["id"]: row for row in self.fixture["cases"]}

    def run_case(self, case_id: str):
        case = self.cases[case_id]
        result = filter_fresh([to_hit(row) for row in case["input"]], self.now, self.max_age)
        retained = [{"source_id": hit.source_id, "version": hit.version,
                     "content_sha256": hit.content_sha256, "evidence_type": hit.evidence_type,
                     "source_label": hit.source_label} for hit in result.retained]
        rejected = [{"source_id": item.source_id, "version": item.version, "reason": item.reason}
                    for item in result.rejected]
        self.assertEqual(case["expected"]["status"], result.status)
        self.assertEqual(case["expected"]["retained"], retained)
        self.assertEqual(case["expected"]["rejected"], rejected)
        self.assertFalse(result.quote_order_eligible)
        return result

    def test_cs01_twelve_hour_source_preserves_identity_digest_and_synthetic_label(self):
        self.run_case("CAP-79-CS-01")

    def test_cs02_exact_24_hour_boundary_is_inclusive_and_one_second_older_is_stale(self):
        self.run_case("CAP-79-CS-02")

    def test_cs03_future_and_timezone_naive_timestamps_are_invalid_before_age_calculation(self):
        self.run_case("CAP-79-CS-03")

    def test_cs04_old_v1_is_not_relabelled_as_fresh_v2(self):
        result = self.run_case("CAP-79-CS-04")
        self.assertEqual(["v2"], [hit.version for hit in result.retained])

    def test_cs05_fresh_illustration_remains_non_payable_evidence(self):
        result = self.run_case("CAP-79-CS-05")
        self.assertEqual("ADVERTISEMENT_ILLUSTRATION", result.retained[0].evidence_type)
        self.assertIn("SYNTHETIC", result.retained[0].source_label)
        self.assertFalse(result.quote_order_eligible)


if __name__ == "__main__":
    unittest.main()
