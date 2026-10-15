from __future__ import annotations

import json
import unittest
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-49/synthetic-v1.json"


def instructor_reference(row: dict) -> str:
    case, answer = row, row["answer"]
    if not case["answerable"] and not answer["abstained"]:
        return "UNSUPPORTED_ANSWER"
    if not case["answerable"] and answer["abstained"] and not answer["citations"]:
        return "PASS"
    if case["answerable"] and answer["abstained"]:
        return "FALSE_ABSTENTION"
    if not answer["citations"]:
        return "MISSING_CITATION"
    if any(citation[0] != case["expected_source_id"] for citation in answer["citations"]):
        return "WRONG_SOURCE"
    if any(citation[1] != case["expected_version"] for citation in answer["citations"]):
        return "WRONG_VERSION"
    return "PASS"


class CAP49InstructorInfrastructureTest(unittest.TestCase):
    def test_fixture_categories_match_independent_reference(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.assertEqual("CAP-49-synthetic-v1", data["fixture_id"])
        self.assertEqual("SYNTHETIC", data["classification"])
        for row in data["cases"]:
            with self.subTest(case=row["question_id"]):
                self.assertEqual(row["expected_category"], instructor_reference(row))

    def test_precedence_mutants_are_detected(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        unanswerable = next(row for row in data["cases"] if row["question_id"] == "q-unsupported-first")
        self.assertEqual("UNSUPPORTED_ANSWER", instructor_reference(unanswerable))
        wrong_both = next(row for row in data["cases"] if row["question_id"] == "q-source-precedes-version")
        self.assertEqual("WRONG_SOURCE", instructor_reference(wrong_both))


if __name__ == "__main__":
    unittest.main()
