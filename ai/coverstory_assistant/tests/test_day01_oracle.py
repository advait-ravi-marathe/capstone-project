import unittest

from ai.coverstory_assistant.contract_day01 import assert_day01
from ai.coverstory_assistant.instructor_reference import build_policy_prompt


class Day01OracleTests(unittest.TestCase):
    def test_reference_matches_independent_fixed_expectations(self):
        assert_day01(build_policy_prompt)

    def test_mutants_that_weaken_grounding_or_input_validation_are_rejected(self):
        def omit_grounding(question, excerpt):
            return {
                "instructions": "Answer the question.",
                "input": f"Policy excerpt:\n{excerpt}\nQuestion:\n{question}",
            }

        def leak_extra_fields(question, excerpt):
            result = build_policy_prompt(question, excerpt)
            return {**result, "debug_excerpt": excerpt}

        def accept_blank(question, excerpt):
            if not question.strip():
                return {"instructions": "", "input": ""}
            return build_policy_prompt(question, excerpt)

        for mutant in (omit_grounding, leak_extra_fields, accept_blank):
            with self.subTest(mutant=mutant.__name__):
                with self.assertRaises(AssertionError):
                    assert_day01(mutant)
