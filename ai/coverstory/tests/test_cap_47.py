from __future__ import annotations

import json
import unittest
from pathlib import Path

from ai.coverstory.cap_47 import Answer, Citation, EvalCase, EvalReport, evaluate

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-47/synthetic-v1.json"


def fixture():
    data = json.loads(FIXTURE.read_text(encoding="utf-8"))
    cases = [EvalCase(row["case_id"], row["answerable"], frozenset(map(tuple, row["allowed"])))
             for row in data["cases"]]
    def answer(name):
        item = data["answers"][name]
        return Answer(item["abstained"], tuple(Citation(*pair) for pair in item["citations"]))
    return data, cases, answer


class CAP47AcceptanceTest(unittest.TestCase):
    def test_answerable_with_allowed_source_version_and_unanswerable_abstention(self):
        data, cases, answer = fixture()
        report = evaluate(cases, {"q1": answer("supported"), "q2": answer("correct_abstention")})
        self.assertEqual(EvalReport(**data["expected"]), report)

    def test_missing_or_foreign_citation_is_unsupported(self):
        _, cases, answer = fixture()
        for name in ("missing_citation", "foreign_citation"):
            report = evaluate(cases[:1], {"q1": answer(name)})
            self.assertEqual(1, report.unsupported)

    def test_missing_case_answer_is_not_counted_as_abstention(self):
        _, cases, _ = fixture()
        with self.assertRaises(ValueError):
            evaluate(cases, {"q1": Answer(False, (Citation("w1", "v1"),))})

    def test_abstention_with_citation_is_invalid_and_not_correct(self):
        _, cases, answer = fixture()
        report = evaluate(cases[:1], {"q1": answer("abstained_with_citation")})
        self.assertEqual(1, report.unsupported)


if __name__ == "__main__":
    unittest.main()
