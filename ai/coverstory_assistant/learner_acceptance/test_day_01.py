import unittest

from ai.coverstory_assistant.contract_day01 import assert_day01
from ai.coverstory_assistant.learner import build_policy_prompt


class Day01LearnerAcceptanceTests(unittest.TestCase):
    def test_bounded_grounded_policy_prompt(self):
        assert_day01(build_policy_prompt)


if __name__ == "__main__":
    unittest.main()
