from __future__ import annotations

import json
import unittest
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-47/synthetic-v1.json"


def reference(cases: list[dict], answers: dict[str, dict]) -> dict[str, int]:
    indexed = {case["case_id"]: case for case in cases}
    if len(indexed) != len(cases) or set(indexed) != set(answers):
        raise ValueError("each evaluation case needs exactly one answer")
    supported = correct_abstentions = unsupported = 0
    for case_id, case in indexed.items():
        answer = answers[case_id]
        cited = [tuple(value) for value in answer["citations"]]
        if case["answerable"] and not answer["abstained"] and cited and all(value in map(tuple, case["allowed"]) for value in cited):
            supported += 1
        elif not case["answerable"] and answer["abstained"] and not cited:
            correct_abstentions += 1
        else:
            unsupported += 1
    return {"total_cases": len(cases), "supported_answers": supported,
            "correct_abstentions": correct_abstentions, "unsupported": unsupported}


class CAP47InstructorInfrastructureTest(unittest.TestCase):
    def test_fixture_and_independent_reference_match_saved_public_vector(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.assertEqual("CAP-47-synthetic-v1", data["fixture_id"])
        answers = {"q1": data["answers"]["supported"], "q2": data["answers"]["correct_abstention"]}
        self.assertEqual(data["expected"], reference(data["cases"], answers))

    def test_reference_rejects_presence_only_and_missing_answer_mutants(self):
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        q1 = data["cases"][0]
        for bad in ("missing_citation", "foreign_citation", "abstained_with_citation"):
            report = reference([q1], {"q1": data["answers"][bad]})
            self.assertEqual(1, report["unsupported"], bad)
        with self.assertRaises(ValueError):
            reference(data["cases"], {"q1": data["answers"]["supported"]})
        with self.assertRaises(ValueError):
            reference(data["cases"], {"q1": data["answers"]["supported"],
                                       "q2": data["answers"]["correct_abstention"], "extra": data["answers"]["supported"]})


if __name__ == "__main__":
    unittest.main()
