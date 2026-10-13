"""CAP-44 learner acceptance tests; expected to fail until rank_runbooks is implemented."""

import json
from pathlib import Path
import unittest
from unittest.mock import patch

from ai.retrieval import ranking
from ai.retrieval.ranking import RankedHit, Runbook, rank_runbooks


ROOT = Path(__file__).resolve().parents[2]
FIXTURE = json.loads((ROOT / "ai/fixtures/CAP-44.json").read_text())["normal"]


def rows(items):
    return [Runbook(**item) for item in items]


def rank_without_network(*args):
    with patch("socket.socket", side_effect=AssertionError("CAP-44 tests must stay offline")), \
            patch("socket.create_connection", side_effect=AssertionError("CAP-44 tests must stay offline")), \
            patch("urllib.request.urlopen", side_effect=AssertionError("CAP-44 tests must stay offline")):
        return rank_runbooks(*args)


class RankingAcceptanceTest(unittest.TestCase):
    def test_hand_labeled_scores_order_and_unchanged_provenance(self):
        result = rank_without_network(FIXTURE["query"], FIXTURE["owner_id"], rows(FIXTURE["runbooks"]), 3)
        self.assertEqual([RankedHit(**hit) for hit in FIXTURE["expected"]], result)

    def test_reverse_input_order_keeps_stable_tie_break(self):
        corpus = rows(FIXTURE["runbooks"][:3])
        forward = rank_without_network("retry timeout", "A", corpus, 3)
        reverse = rank_without_network("retry timeout", "A", list(reversed(corpus)), 3)
        self.assertEqual(forward, reverse)
        self.assertEqual(["r1", "r2", "r3"], [hit.source_id for hit in reverse])

    def test_k_limits_results_and_versions_break_same_source_ties(self):
        same_source = [Runbook("same", "v2", ["A"], "retry"),
                       Runbook("same", "v1", ["A"], "retry")]
        hits = rank_without_network("retry", "A", same_source, 1)
        self.assertEqual([RankedHit("same", "v1", 1, "retry")], hits)
        self.assertEqual(1, len(hits))

    def test_repeated_query_token_counts_once(self):
        corpus = [Runbook("repeat", "v1", ["A"], "retry retry retry")]
        self.assertEqual([RankedHit("repeat", "v1", 1, "retry retry retry")],
                         rank_without_network("retry retry", "A", corpus, 3))

    def test_denied_sources_are_never_scored(self):
        denied = Runbook("r0", "v1", ["B"], "retry timeout")
        allowed = Runbook("r1", "v1", ["A"], "retry")
        original_score = ranking.score_runbook
        with patch.object(ranking, "score_runbook", wraps=original_score) as score_spy:
            result = rank_without_network("retry timeout", "A", [denied, allowed], 3)
        self.assertEqual(["r1"], [hit.source_id for hit in result])
        self.assertEqual(1, score_spy.call_count)
        self.assertEqual("retry", score_spy.call_args.args[1])

    def test_no_match_and_blank_query_return_empty(self):
        corpus = rows(FIXTURE["runbooks"])
        self.assertEqual([], rank_without_network("billing", "A", corpus, 3))
        self.assertEqual([], rank_without_network(" \t", "A", corpus, 3))

    def test_unknown_owner_sees_no_sources(self):
        corpus = rows(FIXTURE["runbooks"])
        with patch.object(ranking, "score_runbook", wraps=ranking.score_runbook) as score_spy:
            self.assertEqual([], rank_without_network("retry", "unknown", corpus, 3))
        score_spy.assert_not_called()

    def test_invalid_owner_k_and_provenance_raise_before_partial_results(self):
        corpus = [Runbook("ok", "v1", ["A"], "retry"), Runbook("bad", "", ["A"], "timeout")]
        for args in [("retry", " ", corpus[:1], 1), ("retry", "A", corpus[:1], 0),
                     ("retry", "A", corpus[:1], 4), ("retry", "A", corpus, 2)]:
            with self.subTest(args=args), self.assertRaises(ValueError):
                rank_without_network(*args)
        with patch.object(ranking, "score_runbook", wraps=ranking.score_runbook) as score_spy:
            with self.assertRaises(ValueError):
                rank_without_network("retry", "A", corpus, 2)
        score_spy.assert_not_called()

    def test_duplicate_source_version_raises(self):
        duplicate = [Runbook("r1", "v1", ["A"], "retry"), Runbook("r1", "v1", ["A"], "timeout")]
        with self.assertRaises(ValueError):
            rank_without_network("retry", "A", duplicate, 2)

    def test_missing_source_id_or_version_raises(self):
        for invalid in [Runbook("", "v1", ["A"], "retry"),
                        Runbook("r1", " ", ["A"], "retry")]:
            with self.subTest(invalid=invalid), self.assertRaises(ValueError):
                rank_without_network("retry", "A", [invalid], 1)


if __name__ == "__main__":
    unittest.main()
