import unittest
from ai.grounded.evaluation import CASES, evaluate
from ai.grounded.fixtures import CLAUSES

class EvaluationTests(unittest.TestCase):
    def test_counts_match_independent_literal_expectations(self):
        self.assertEqual({"total":3,"cited":2,"abstained":1,"exact_match":3,"citation_mismatch":0}, evaluate(CASES, CLAUSES))

    def test_incorrect_expected_source_is_counted_as_failure(self):
        from ai.grounded.evaluation import Case
        cases = (Case("What renewal account requirement applies?", "owner-a", "cited", ("wrong@v9",)),)
        self.assertEqual({"total":1,"cited":1,"abstained":0,"exact_match":0,"citation_mismatch":1}, evaluate(cases, CLAUSES))
