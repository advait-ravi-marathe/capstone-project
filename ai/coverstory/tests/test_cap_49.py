from __future__ import annotations

import json
import unittest
from pathlib import Path

from ai.coverstory.cap_49 import Citation, EvidenceAnswer, EvidenceCase, FailureCategory, classify_failure

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-49/synthetic-v1.json"


def typed(row):
    case = EvidenceCase(row["question_id"], row["answerable"], row["expected_source_id"], row["expected_version"])
    raw = row["answer"]
    answer = EvidenceAnswer(raw["abstained"], tuple(Citation(*citation) for citation in raw["citations"]))
    return case, answer


class CAP49AcceptanceTest(unittest.TestCase):
    def test_all_saved_precedence_cases(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        for row in data["cases"]:
            with self.subTest(case=row["question_id"]):
                case, answer = typed(row)
                self.assertEqual(FailureCategory(row["expected_category"]), classify_failure(case, answer))

    def test_unanswerable_nonabstention_precedes_missing_citation(self):
        case = EvidenceCase("q", False, None, None)
        answer = EvidenceAnswer(False, ())
        self.assertEqual(FailureCategory.UNSUPPORTED_ANSWER, classify_failure(case, answer))

    def test_wrong_source_precedes_wrong_version(self):
        case = EvidenceCase("q", True, "health-A", "v2")
        answer = EvidenceAnswer(False, (Citation("health-B", "v1"),))
        self.assertEqual(FailureCategory.WRONG_SOURCE, classify_failure(case, answer))


if __name__ == "__main__":
    unittest.main()
