import unittest
from ai.grounded.evidence import Clause, explain
from ai.grounded.fixtures import CLAUSES


class ExplainTests(unittest.TestCase):
    def test_unique_literal_match_returns_exact_text_and_versioned_citation(self):
        result = explain("What renewal account requirement applies?", owner_id="owner-a", clauses=CLAUSES)
        self.assertEqual("cited", result.status)
        self.assertEqual("Renewal requires a current account.", result.explanation)
        self.assertEqual(("synthetic-renewal-01@v1",), result.citations)

    def test_foreign_owner_is_filtered_before_matching(self):
        result = explain("What private account condition applies?", owner_id="owner-a", clauses=CLAUSES)
        self.assertEqual("abstain", result.status)
        self.assertEqual((), result.citations)

    def test_ambiguous_matches_abstain(self):
        clauses = (Clause("a", "v1", "o", "Renewal requires a current account."),
                   Clause("b", "v1", "o", "Renewal requires a current payment."))
        self.assertEqual("abstain", explain("renewal requires", owner_id="o", clauses=clauses).status)

    def test_no_match_and_empty_query_abstain_or_reject(self):
        self.assertEqual("abstain", explain("coverage", owner_id="owner-a", clauses=CLAUSES).status)
        with self.assertRaises(ValueError):
            explain(" ", owner_id="owner-a", clauses=CLAUSES)

    def test_input_fixture_is_not_mutated(self):
        before = tuple(CLAUSES)
        explain("renewal", owner_id="owner-a", clauses=CLAUSES)
        self.assertEqual(before, CLAUSES)


if __name__ == "__main__":
    unittest.main()
