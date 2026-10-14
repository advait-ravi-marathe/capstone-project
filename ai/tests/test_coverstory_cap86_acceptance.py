from __future__ import annotations

import json
import unittest
from datetime import datetime
from pathlib import Path

from ai.coverstory.cap_86 import SourceEvidence, decide_onboarding_answer

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-86/synthetic-v1.json"


def parse_time(value: str) -> datetime:
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def hit(row: dict) -> SourceEvidence:
    return SourceEvidence(row["source_id"], row["owner_id"], row["version"], row["excerpt"],
                          row["content_sha256"], parse_time(row["observed_at"]), row["score"],
                          row.get("source_label", "SYNTHETIC"))


def result(decision) -> dict:
    return {"status": decision.status, "explanation": decision.explanation,
            "citations": [{"source_id": citation.source_id, "version": citation.version,
                           "content_sha256": citation.content_sha256} for citation in decision.citations],
            "manual_path": decision.manual_path, "writes": decision.writes}


class CAP86AcceptanceTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))
        cls.cases = {case["id"]: case for case in cls.fixture["cases"]}
        cls.now = parse_time(cls.fixture["fixed_now"])

    def assert_case(self, case_id: str, hits: tuple[SourceEvidence, ...] | None = None):
        case = self.cases[case_id]
        selected = hits if hits is not None else (hit(case["source"]),)
        decision = decide_onboarding_answer(case["principal"], selected, self.now,
                                            self.fixture["score_threshold"])
        self.assertEqual(case["expected"], result(decision))

    def test_01_authorized_fresh_digest_valid_source_answers_with_exact_citation_and_no_writes(self):
        self.assert_case("CAP-86-CS-01")

    def test_02_high_confidence_other_owner_source_abstains(self):
        self.assert_case("CAP-86-CS-02")

    def test_03_source_older_than_24_hours_abstains(self):
        self.assert_case("CAP-86-CS-03")

    def test_04_wrong_digest_abstains_without_cached_excerpt(self):
        self.assert_case("CAP-86-CS-04")

    def test_05_below_threshold_and_empty_hits_abstain_without_exception(self):
        case = self.cases["CAP-86-CS-05"]
        self.assert_case("CAP-86-CS-05", (hit(case["hits"][0]),))
        decision = decide_onboarding_answer(case["principal"], (), self.now,
                                            self.fixture["score_threshold"])
        self.assertEqual(case["expected"], result(decision))

    def test_06_exact_threshold_is_inclusive(self):
        self.assert_case("CAP-86-CS-06")


if __name__ == "__main__":
    unittest.main()
