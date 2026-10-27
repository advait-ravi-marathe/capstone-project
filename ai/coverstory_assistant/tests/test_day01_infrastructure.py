import json
import unittest

from ai.coverstory_assistant.day01_example import example_model_output, example_request


class Day01InfrastructureTests(unittest.TestCase):
    def test_python_dicts_round_trip_as_json_without_claiming_a_live_model(self):
        request = example_request()
        output = example_model_output()
        self.assertEqual(
            {"instructions", "input"}, set(request)
        )
        self.assertEqual(
            {"answer", "citations", "abstained"}, set(output)
        )
        self.assertEqual(request, json.loads(json.dumps(request)))
        self.assertEqual(output, json.loads(json.dumps(output)))
        self.assertIn("SYN-POL-1", output["citations"][0]["document_id"])
