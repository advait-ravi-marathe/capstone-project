"""Green smoke checks that do not depend on the learner's ranking method."""

import json
from pathlib import Path
import unittest

from ai.retrieval.ranking import RankedHit, Runbook, rank_runbooks, tokenize


ROOT = Path(__file__).resolve().parents[2]


class RankingInfrastructureTest(unittest.TestCase):
    def test_assignment_api_and_ascii_tokenizer_are_available(self):
        self.assertEqual({"retry", "timeout", "7"}, tokenize("Retry TIMEOUT-7 é"))
        self.assertIsInstance(Runbook("r", "v1", ["A"], "text"), Runbook)
        self.assertIsInstance(RankedHit("r", "v1", 1, "text"), RankedHit)
        self.assertTrue(callable(rank_runbooks))

    def test_fixed_fixture_has_required_shape(self):
        fixture = json.loads((ROOT / "ai/fixtures/CAP-44.json").read_text())
        case = fixture["normal"]
        self.assertEqual({"owner_id", "query", "k", "runbooks", "expected"}, set(case))
        self.assertEqual(4, len(case["runbooks"]))
        self.assertTrue(all({"source_id", "version", "allowed_owners", "text"} <= set(row)
                            for row in case["runbooks"]))
        self.assertEqual(["r1", "r2", "r3"], [row["source_id"] for row in case["expected"]])


if __name__ == "__main__":
    unittest.main()
