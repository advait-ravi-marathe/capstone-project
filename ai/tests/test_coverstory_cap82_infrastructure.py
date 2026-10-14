"""Independent CAP-82 ranker comparison oracle with safety defect witnesses."""
from __future__ import annotations
import json
import unittest
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-82/synthetic-v1.json"


def mrr_recall(case: dict, k: int) -> tuple[float, float]:
    if not case["answerable"]:
        raise ValueError("NON_ANSWERABLE")
    ranked = case["retrieved_source_ids"][:k]
    relevant = set(case["relevant_source_ids"])
    rank = next((index for index, source in enumerate(ranked, 1) if source in relevant), None)
    return (0.0 if rank is None else 1.0 / rank,
            0.0 if not relevant else len(set(ranked) & relevant) / len(relevant))


def owner_violations(run: dict) -> list[str]:
    invalid: list[str] = []
    for case in run["cases"]:
        owners = case["source_owner_by_id"]
        if any(owners.get(source) not in ("PUBLIC", case["query_owner"])
               for source in case["retrieved_source_ids"]):
            invalid.append(case["case_id"])
    return invalid


def mean(values: list[float]) -> float | None:
    return None if not values else sum(values) / len(values)


def oracle(scenario: dict) -> dict:
    base, cand = scenario["baseline"], scenario["candidate"]
    identity_ok = (scenario["dataset_sha256"] == scenario.get("candidate_dataset_sha256", scenario["dataset_sha256"])
                   and scenario["corpus_sha256"] == scenario.get("candidate_corpus_sha256", scenario["corpus_sha256"])
                   and scenario.get("k", 2) == scenario.get("candidate_k", scenario.get("k", 2)))
    if not identity_ok:
        return {"status": "INCOMPARABLE", "reason": "EVALUATION_IDENTITY_MISMATCH",
                "delta_mean_reciprocal_rank": None, "delta_mean_recall_at_k": None,
                "changed_case_ids": []}
    disqualified = sorted(set(owner_violations(base)) | set(owner_violations(cand)))
    if disqualified:
        return {"status": "DISQUALIFIED", "reason": "CROSS_OWNER_HIT",
                "delta_mean_reciprocal_rank": None, "delta_mean_recall_at_k": None,
                "changed_case_ids": [], "disqualified_case_ids": disqualified}
    baseline_cases = {case["case_id"]: case for case in base["cases"]}
    candidate_cases = {case["case_id"]: case for case in cand["cases"]}
    if baseline_cases.keys() != candidate_cases.keys() or any(
            (baseline_cases[case_id]["answerable"],
             baseline_cases[case_id]["relevant_source_ids"],
             baseline_cases[case_id]["query_owner"],
             baseline_cases[case_id]["source_owner_by_id"])
            !=
            (candidate_cases[case_id]["answerable"],
             candidate_cases[case_id]["relevant_source_ids"],
             candidate_cases[case_id]["query_owner"],
             candidate_cases[case_id]["source_owner_by_id"])
            for case_id in baseline_cases.keys() & candidate_cases.keys()):
        return {"status": "INCOMPARABLE", "reason": "CASE_SET_MISMATCH",
                "delta_mean_reciprocal_rank": None, "delta_mean_recall_at_k": None,
                "changed_case_ids": []}
    answerable = [case_id for case_id, case in baseline_cases.items() if case["answerable"]]
    latency_base = mean(base["latency_samples_ms"])
    latency_cand = mean(cand["latency_samples_ms"])
    if not answerable:
        return {"status": "INSUFFICIENT_EVIDENCE", "reason": "NO_ANSWERABLE_CASES",
                "baseline_mean_reciprocal_rank": None, "candidate_mean_reciprocal_rank": None,
                "delta_mean_reciprocal_rank": None, "baseline_mean_recall_at_k": None,
                "candidate_mean_recall_at_k": None, "delta_mean_recall_at_k": None,
                "baseline_latency_ms": latency_base, "candidate_latency_ms": latency_cand,
                "changed_case_ids": []}
    k = scenario.get("k", 2)
    bm, br, cm, cr = [], [], [], []
    changed = []
    for case_id in answerable:
        before = mrr_recall(baseline_cases[case_id], k)
        after = mrr_recall(candidate_cases[case_id], k)
        bm.append(before[0]); br.append(before[1]); cm.append(after[0]); cr.append(after[1])
        if before != after:
            changed.append(case_id)
    base_mrr, candidate_mrr = mean(bm), mean(cm)
    base_recall, candidate_recall = mean(br), mean(cr)
    return {"status": "COMPARABLE", "reason": None,
            "baseline_mean_reciprocal_rank": base_mrr,
            "candidate_mean_reciprocal_rank": candidate_mrr,
            "delta_mean_reciprocal_rank": candidate_mrr - base_mrr,
            "baseline_mean_recall_at_k": base_recall,
            "candidate_mean_recall_at_k": candidate_recall,
            "delta_mean_recall_at_k": candidate_recall - base_recall,
            "baseline_latency_ms": latency_base, "candidate_latency_ms": latency_cand,
            "changed_case_ids": changed, "disqualified_case_ids": []}


def unsafe_gain_only_defect(scenario: dict) -> dict:
    """Known bad comparator: computes gain while ignoring identity and ownership."""
    base, cand = scenario["baseline"], scenario["candidate"]
    k = scenario.get("k", 2)
    b = [mrr_recall(case, k)[0] for case in base["cases"] if case["answerable"]]
    c = [mrr_recall(case, k)[0] for case in cand["cases"] if case["answerable"]]
    return {"status": "COMPARABLE", "delta_mean_reciprocal_rank": mean(c) - mean(b)}


class CAP82InstructorInfrastructureTest(unittest.TestCase):
    def test_fixture_positive_regression_and_empty_case_results(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.assertEqual("CAP-82-synthetic-v1", data["fixture_id"])
        self.assertTrue(data["synthetic"])
        for scenario in data["scenarios"]:
            with self.subTest(scenario=scenario["id"]):
                actual = oracle(scenario)
                for key, expected in scenario["expected"].items():
                    self.assertEqual(expected, actual.get(key))

    def test_correct_oracle_blocks_identity_and_owner_blind_gain_witnesses(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        by_id = {row["id"]: row for row in data["scenarios"]}
        identity = by_id["CAP-82-CS-03"]
        cross_owner = by_id["CAP-82-CS-04"]
        self.assertGreater(unsafe_gain_only_defect(identity)["delta_mean_reciprocal_rank"], 0)
        self.assertEqual("INCOMPARABLE", oracle(identity)["status"])
        self.assertGreater(unsafe_gain_only_defect(cross_owner)["delta_mean_reciprocal_rank"], 0)
        self.assertEqual("DISQUALIFIED", oracle(cross_owner)["status"])
        self.assertEqual(["q2"], oracle(cross_owner)["disqualified_case_ids"])


if __name__ == "__main__":
    unittest.main()
