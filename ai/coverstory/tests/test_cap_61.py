from __future__ import annotations

import json
import math
import unittest
from pathlib import Path

from ai.coverstory.cap_61 import AnswerDecision, EvidenceHit, decide_answer

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-61/synthetic-v1.json"


def load_fixture() -> dict: return json.loads(FIXTURE.read_text(encoding="utf-8"))


class CAP61AcceptanceTest(unittest.TestCase):
    def test_above_threshold_returns_only_exact_extract_and_citation(self) -> None:
        case = load_fixture()["cases"][0]; row = case["hits"][0]
        hit = EvidenceHit(row["score"], row["authorized"], row["applicable"], row["text"],
                          (row["citation"]["source_id"], row["citation"]["version"]))
        self.assertEqual(AnswerDecision("ANSWER", row["text"], ("health-A", "v1")), decide_answer([hit], 0.7))

    def test_threshold_is_inclusive_but_lower_score_abstains(self) -> None:
        at = EvidenceHit(0.7, True, True, "at", ("source", "v1"))
        below = EvidenceHit(0.699, True, True, "below", ("source", "v1"))
        self.assertEqual("ANSWER", decide_answer([at], 0.7).decision)
        self.assertEqual("ABSTAIN", decide_answer([below], 0.7).decision)

    def test_empty_or_denied_only_evidence_abstains(self) -> None:
        denied = EvidenceHit(0.99, False, True, "PRIVATE", ("private", "v1"))
        inapplicable = EvidenceHit(0.99, True, False, "wrong product", ("other", "v1"))
        self.assertEqual("ABSTAIN", decide_answer([], 0.7).decision)
        self.assertEqual("ABSTAIN", decide_answer([denied], 0.7).decision)
        self.assertEqual("ABSTAIN", decide_answer([inapplicable], 0.7).decision)

    def test_nonfinite_and_out_of_range_scores_or_thresholds_are_rejected(self) -> None:
        for value in (math.nan, -0.01, 1.01):
            with self.subTest(value=value), self.assertRaises(ValueError): decide_answer([], value)
            with self.subTest(score=value), self.assertRaises(ValueError): decide_answer(
                [EvidenceHit(value, True, True, "text", ("source", "v1"))], 0.7)


if __name__ == "__main__": unittest.main()
