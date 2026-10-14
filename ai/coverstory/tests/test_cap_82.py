"""CAP-82 learner acceptance contract; TODO failures expected initially."""
from __future__ import annotations
import json
import unittest
from pathlib import Path

from ai.coverstory.cap_82 import EvalRun, EvaluationCase, compare_runs

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-82/synthetic-v1.json"


def run(scenario: dict, side: str) -> EvalRun:
    data = scenario[side]
    cases = tuple(EvaluationCase(
        row["case_id"], row["answerable"], frozenset(row["relevant_source_ids"]),
        tuple(row["retrieved_source_ids"]), row["query_owner"],
        tuple(sorted(row["source_owner_by_id"].items()))) for row in data["cases"])
    return EvalRun(scenario.get(f"{side}_dataset_sha256", scenario["dataset_sha256"]),
                   scenario["corpus_sha256"], scenario.get("k", 2), cases,
                   tuple(data["latency_samples_ms"]))


class CAP82AcceptanceTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.scenarios = {row["id"]: row for row in json.loads(FIXTURE.read_text(encoding="utf-8"))["scenarios"]}

    def test_same_fixture_reports_metric_gain_changed_case_and_latency(self):
        scenario = self.scenarios["CAP-82-CS-01"]
        report = compare_runs(run(scenario, "baseline"), run(scenario, "candidate"))
        expected = scenario["expected"]
        for field in ("status", "baseline_mean_reciprocal_rank", "candidate_mean_reciprocal_rank",
                      "delta_mean_reciprocal_rank", "baseline_mean_recall_at_k",
                      "candidate_mean_recall_at_k", "delta_mean_recall_at_k",
                      "baseline_latency_ms", "candidate_latency_ms"):
            self.assertEqual(expected[field], getattr(report, field))
        self.assertEqual(("q2",), report.changed_case_ids)

    def test_regression_is_reported_without_clamping(self):
        scenario = self.scenarios["CAP-82-CS-02"]
        report = compare_runs(run(scenario, "baseline"), run(scenario, "candidate"))
        self.assertEqual(-0.25, report.delta_mean_reciprocal_rank)
        self.assertEqual(("q1",), report.changed_case_ids)

    def test_different_dataset_identity_has_no_gain_claim(self):
        scenario = self.scenarios["CAP-82-CS-03"]
        report = compare_runs(run(scenario, "baseline"), run(scenario, "candidate"))
        self.assertEqual("INCOMPARABLE", report.status)
        self.assertIsNone(report.delta_mean_reciprocal_rank)
        self.assertIsNone(report.delta_mean_recall_at_k)

    def test_cross_owner_result_disqualifies_even_if_metrics_would_improve(self):
        scenario = self.scenarios["CAP-82-CS-04"]
        report = compare_runs(run(scenario, "baseline"), run(scenario, "candidate"))
        self.assertEqual("DISQUALIFIED", report.status)
        self.assertIsNone(report.delta_mean_reciprocal_rank)
        self.assertEqual(("q2",), report.disqualified_case_ids)

    def test_no_answerable_cases_reports_insufficient_evidence_and_null_metrics(self):
        scenario = self.scenarios["CAP-82-CS-05"]
        report = compare_runs(run(scenario, "baseline"), run(scenario, "candidate"))
        self.assertEqual("INSUFFICIENT_EVIDENCE", report.status)
        self.assertIsNone(report.delta_mean_reciprocal_rank)
        self.assertIsNone(report.delta_mean_recall_at_k)


if __name__ == "__main__":
    unittest.main()
