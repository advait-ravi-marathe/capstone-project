from __future__ import annotations

import json
import unittest
from pathlib import Path

from ai.coverstory.cap_77 import (
    AuthorizedSource,
    Claim,
    Citation,
    StructuredAnswer,
    check_answer,
)

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-77/synthetic-v1.json"


def load_fixture() -> tuple[dict, dict[tuple[str, str], AuthorizedSource]]:
    data = json.loads(FIXTURE.read_text(encoding="utf-8"))
    sources = {
        (row["source_id"], row["version"]): AuthorizedSource(
            row["text"], row["authorized_for_fixture"], row["applicable"]
        )
        for row in data["sources"]
        if row["authorized_for_fixture"]
    }
    return data, sources


def claim_from(row: dict) -> Claim:
    return Claim(
        row["claim"],
        Citation(row["source_id"], row["version"], row["start"], row["end"],
                 row["excerpt"], next(
                     source["content_sha256"] for source in json.loads(FIXTURE.read_text(encoding="utf-8"))["sources"]
                     if source["source_id"] == row["source_id"] and source["version"] == row["version"]
                 )),
    )


class CAP77AcceptanceTest(unittest.TestCase):
    def setUp(self) -> None:
        self.fixture, self.authorized_sources = load_fixture()

    def test_exact_source_and_citation_is_consistent_but_not_authoritative_state(self) -> None:
        case = self.fixture["cases"][0]
        report = check_answer(StructuredAnswer((claim_from(case),)), self.authorized_sources)
        self.assertEqual("SUPPORTED", report.status)
        self.assertEqual(1, report.valid_claims)
        self.assertFalse(report.authoritative_state)

    def test_confirmed_claim_with_pending_citation_is_unsupported(self) -> None:
        case = self.fixture["cases"][1]
        report = check_answer(StructuredAnswer((claim_from(case),)), self.authorized_sources)
        self.assertEqual("UNSUPPORTED", report.status)
        self.assertEqual(0, report.valid_claims)
        self.assertEqual(["CLAIM_TEXT_MISMATCH"], [issue.code for issue in report.issues])

    def test_exact_text_from_b_only_source_is_unknown_to_authorized_registry(self) -> None:
        case = self.fixture["cases"][2]
        report = check_answer(StructuredAnswer((claim_from(case),)), self.authorized_sources)
        self.assertEqual("UNSUPPORTED", report.status)
        self.assertEqual(["UNKNOWN_SOURCE"], [issue.code for issue in report.issues])

    def test_empty_claims_require_abstention(self) -> None:
        report = check_answer(StructuredAnswer(()), self.authorized_sources)
        self.assertEqual("ABSTAIN_REQUIRED", report.status)
        self.assertEqual(0, report.valid_claims)

    def test_one_valid_and_one_invalid_claim_make_whole_answer_unsupported(self) -> None:
        case = self.fixture["cases"][4]
        answer = StructuredAnswer(tuple(claim_from(row) for row in case["claims"]))
        report = check_answer(answer, self.authorized_sources)
        self.assertEqual("UNSUPPORTED", report.status)
        self.assertEqual(1, report.valid_claims)
        self.assertEqual(["CLAIM_TEXT_MISMATCH"], [issue.code for issue in report.issues])

    def test_fixture_integrity_and_saved_case_ids(self) -> None:
        self.assertTrue(self.fixture["synthetic"])
        self.assertEqual([f"CAP-77-CS-0{i}" for i in range(1, 6)],
                         [case["id"] for case in self.fixture["cases"]])
        self.assertTrue(all(case["authoritative_state"] is False for case in self.fixture["cases"]))


if __name__ == "__main__":
    unittest.main()
