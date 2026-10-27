import unittest

from ai.coverstory_assistant.contract_day29 import assert_day29
from ai.coverstory_assistant.instructor_reference import run_offline_pipeline


class Day29OracleTests(unittest.TestCase):
    def test_independent_reference_passes_fixed_integrated_cases(self):
        assert_day29(run_offline_pipeline)

    def test_actual_day29_acceptance_rejects_pipeline_mutants(self):
        def always_abstain(question, principal, documents, model_fixture):
            return {"answer": "I don't know.", "citations": [], "abstained": True,
                    "executionMode": "offline_model_fixture", "providerRequestMade": False}

        def leaks_foreign_claim(question, principal, documents, model_fixture):
            return {"answer": "Hail is covered.", "citations": [{"document_id": "SYN-POL-2", "section": "Coverage"}],
                    "abstained": False, "executionMode": "offline_model_fixture", "providerRequestMade": False}

        for mutant in (always_abstain, leaks_foreign_claim):
            with self.subTest(mutant=mutant.__name__), self.assertRaises(AssertionError):
                assert_day29(mutant)


if __name__ == "__main__":
    unittest.main()
