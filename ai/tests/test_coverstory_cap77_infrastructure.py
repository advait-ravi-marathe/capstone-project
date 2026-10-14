from __future__ import annotations

import hashlib
import json
import unittest
from pathlib import Path

from ai.coverstory.cap_77 import (
    AuthorizedSource,
    Claim,
    Citation,
    ConsistencyIssue,
    ConsistencyReport,
    StructuredAnswer,
    check_answer,
)

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-77/synthetic-v1.json"


def load_inputs():
    fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))
    all_sources = {(row["source_id"], row["version"]): row for row in fixture["sources"]}
    authorized = {
        key: AuthorizedSource(row["text"], True, row["applicable"])
        for key, row in all_sources.items() if row["authorized_for_fixture"]
    }
    return fixture, all_sources, authorized


def make_claim(row: dict, source_rows: dict) -> Claim:
    source = source_rows[(row["source_id"], row["version"])]
    return Claim(row["claim"], Citation(row["source_id"], row["version"], row["start"], row["end"],
                                         row["excerpt"], source["content_sha256"]))


def instructor_oracle(answer: StructuredAnswer, authorized_sources: dict) -> ConsistencyReport:
    """Independent deterministic exact-slice witness; no semantic inference."""
    if not answer.claims:
        return ConsistencyReport("ABSTAIN_REQUIRED", 0)
    valid = 0
    issues: list[ConsistencyIssue] = []
    for index, claim in enumerate(answer.claims):
        citation = claim.citation
        source = authorized_sources.get((citation.source_id, citation.version))
        code = None
        if source is None:
            code = "UNKNOWN_SOURCE"
        elif not source.authorized:
            code = "SOURCE_NOT_AUTHORIZED"
        elif not source.applicable:
            code = "SOURCE_NOT_APPLICABLE"
        else:
            digest = hashlib.sha256(source.text.encode("utf-8")).hexdigest()
            if citation.content_sha256 != digest or not (0 <= citation.start < citation.end <= len(source.text)):
                code = "CITATION_MISMATCH"
            else:
                extract = source.text[citation.start:citation.end]
                if citation.excerpt != extract:
                    code = "CITATION_MISMATCH"
                elif claim.text != extract:
                    code = "CLAIM_TEXT_MISMATCH"
        if code is None:
            valid += 1
        else:
            issues.append(ConsistencyIssue(index, code))
    status = "SUPPORTED" if valid == len(answer.claims) else "UNSUPPORTED"
    return ConsistencyReport(status, valid, tuple(issues), False)


class CAP77InfrastructureTest(unittest.TestCase):
    def setUp(self) -> None:
        self.fixture, self.all_sources, self.authorized = load_inputs()

    def test_fixture_hashes_and_case_registry_boundary_are_valid(self) -> None:
        self.assertTrue(self.fixture["synthetic"])
        self.assertEqual([f"CAP-77-CS-0{i}" for i in range(1, 6)],
                         [case["id"] for case in self.fixture["cases"]])
        for row in self.fixture["sources"]:
            digest = hashlib.sha256(row["text"].encode("utf-8")).hexdigest()
            self.assertEqual(digest, row["content_sha256"], row["source_id"])
        self.assertNotIn(("payment-help-B", "v1"), self.authorized)
        self.assertTrue(callable(check_answer))
        with self.assertRaisesRegex(NotImplementedError, "CAP-77 learner TODO"):
            check_answer(StructuredAnswer(()), self.authorized)

    def test_exact_extract_is_supported_without_authoritative_state(self) -> None:
        case = self.fixture["cases"][0]
        report = instructor_oracle(StructuredAnswer((make_claim(case, self.all_sources),)), self.authorized)
        self.assertEqual(("SUPPORTED", 1, False),
                         (report.status, report.valid_claims, report.authoritative_state))
        self.assertEqual((), report.issues)

    def test_claim_rewording_and_b_only_source_are_rejected(self) -> None:
        mismatch = instructor_oracle(
            StructuredAnswer((make_claim(self.fixture["cases"][1], self.all_sources),)), self.authorized
        )
        self.assertEqual(("UNSUPPORTED", 0, ["CLAIM_TEXT_MISMATCH"]),
                         (mismatch.status, mismatch.valid_claims, [issue.code for issue in mismatch.issues]))

        unknown = instructor_oracle(
            StructuredAnswer((make_claim(self.fixture["cases"][2], self.all_sources),)), self.authorized
        )
        self.assertEqual(("UNSUPPORTED", 0, ["UNKNOWN_SOURCE"]),
                         (unknown.status, unknown.valid_claims, [issue.code for issue in unknown.issues]))

    def test_empty_answer_and_mixed_valid_invalid_answer_are_not_vacuously_supported(self) -> None:
        empty = instructor_oracle(StructuredAnswer(()), self.authorized)
        self.assertEqual(("ABSTAIN_REQUIRED", 0), (empty.status, empty.valid_claims))

        case = self.fixture["cases"][4]
        mixed = instructor_oracle(
            StructuredAnswer(tuple(make_claim(row, self.all_sources) for row in case["claims"])), self.authorized
        )
        self.assertEqual(("UNSUPPORTED", 1, ["CLAIM_TEXT_MISMATCH"]),
                         (mixed.status, mixed.valid_claims, [issue.code for issue in mixed.issues]))

    def test_source_integrity_failure_cannot_turn_into_supported_claim(self) -> None:
        source = self.authorized[("payment-help-A", "v1")]
        row = self.fixture["cases"][0]
        original = make_claim(row, self.all_sources)
        broken = Claim(original.text, Citation(original.citation.source_id, original.citation.version,
                                                original.citation.start, original.citation.end,
                                                original.citation.excerpt, "0" * 64))
        report = instructor_oracle(StructuredAnswer((broken,)), self.authorized)
        self.assertEqual(("UNSUPPORTED", ["CITATION_MISMATCH"]),
                         (report.status, [issue.code for issue in report.issues]))
        self.assertEqual("Payment is pending.", source.text)


if __name__ == "__main__":
    unittest.main()
