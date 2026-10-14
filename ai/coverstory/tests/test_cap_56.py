from __future__ import annotations

import json
import unittest
from pathlib import Path

from ai.coverstory.cap_56 import AuthorizationViolation, EvaluationCase, evaluate_retriever

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-56/synthetic-v1.json"


def loaded() -> tuple[dict, list[EvaluationCase]]:
    data = json.loads(FIXTURE.read_text(encoding="utf-8"))
    cases = [EvaluationCase(row["id"], row["query"], row["answerable"], frozenset(row["relevant"]), frozenset(row["authorized"]))
             for row in data["cases"]]
    return data, cases


def fake_retriever(data: dict):
    results = {row["query"]: row["results"] for row in data["cases"]}
    return lambda query, k: results[query][:k]


class CAP56AcceptanceTest(unittest.TestCase):
    def test_macro_hit_at_k_uses_answerable_cases_only(self) -> None:
        data, cases = loaded()
        # q4 is omitted so this run contains the source's q1/q2 pair.
        report = evaluate_retriever(cases[:2], fake_retriever(data), 2, data["corpus_sha256"], data["config_sha256"])
        self.assertEqual([1, 0], [case.hit_at_k for case in report.cases])
        self.assertEqual((2, 0, 0.5), (report.answerable_count, report.unanswerable_count, report.macro_hit_at_k))

    def test_no_answerable_cases_has_null_metric_and_explicit_counts(self) -> None:
        data, cases = loaded(); case = cases[2]
        report = evaluate_retriever([case], fake_retriever(data), 2, data["corpus_sha256"], data["config_sha256"])
        self.assertIsNone(report.macro_hit_at_k)
        self.assertEqual((0, 1), (report.answerable_count, report.unanswerable_count))

    def test_foreign_private_result_fails_whole_run(self) -> None:
        data, cases = loaded()
        with self.assertRaises(AuthorizationViolation):
            evaluate_retriever([cases[3]], fake_retriever(data), 2, data["corpus_sha256"], data["config_sha256"])

    def test_same_candidate_and_fixture_produce_same_measured_output(self) -> None:
        data, cases = loaded()
        first = evaluate_retriever(cases[:2], fake_retriever(data), 2, data["corpus_sha256"], data["config_sha256"])
        second = evaluate_retriever(cases[:2], fake_retriever(data), 2, data["corpus_sha256"], data["config_sha256"])
        self.assertEqual(first, second)
        self.assertEqual(data["corpus_sha256"], first.corpus_sha256)
        self.assertEqual(data["config_sha256"], first.config_sha256)


if __name__ == "__main__": unittest.main()
