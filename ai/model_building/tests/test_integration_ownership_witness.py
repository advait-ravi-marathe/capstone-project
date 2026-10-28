"""Known-good reference and deliberately wrong adapter mutants for OWN-INT."""

import unittest
from pathlib import Path
from unittest.mock import patch

from ai.model_building import integration_learner
from ai.model_building.model_api_scaffold import running_model_api
from ai.model_building.learner_acceptance import test_integration_ownership as learner_acceptance
from ai.model_building.tests import reference_witness as ref

MODEL = learner_acceptance.MODEL
VALID = learner_acceptance.VALID
post = learner_acceptance.post


def assert_public_contract(candidate):
    expected = {"predicted_label": "schedule", "decision_scope": "synthetic_document_type_only"}
    if candidate(MODEL, VALID) != expected:
        raise AssertionError("candidate output differs from independent fixed oracle")
    for bad in (None, {"feature_names": MODEL["feature_names"], "features": [1.0, 2.0]},
                {"feature_names": list(reversed(MODEL["feature_names"])), "features": [1.4, 0.1, 0.0]},
                {"feature_names": MODEL["feature_names"], "features": [True, 0.1, 0.0]}):
        try:
            candidate(MODEL, bad)
        except ValueError:
            continue
        raise AssertionError(f"candidate accepted malformed payload: {bad}")


class IntegrationOwnershipWitnessTests(unittest.TestCase):
    def test_demo_server_refuses_nonloopback_binding(self):
        from ai.model_building.model_api_scaffold import serve_artifact
        with self.assertRaises(ValueError):
            serve_artifact(Path("missing-model.json"), host="0.0.0.0", port=8765)

    def test_actual_learner_acceptance_selectors_pass_reference_and_reject_mutants(self):
        method_names = [
            "test_own_001_builds_narrow_response_from_day30_model",
            "test_own_001_rejects_malformed_and_order_confused_requests",
            "test_own_002_actual_http_path_maps_validation_and_model_failures_safely",
            "test_own_002_distinguishes_input_errors_from_inference_failures",
            "test_own_002_internal_failure_is_generic_and_never_leaks_exception_text",
        ]

        def run(candidate, error_candidate=ref.prediction_error_response):
            suite = unittest.TestSuite(learner_acceptance.IntegrationOwnershipAcceptance(name) for name in method_names)
            with patch.object(integration_learner, "build_prediction_response", candidate):
                with patch.object(integration_learner, "prediction_error_response", error_candidate):
                    result = unittest.TestResult()
                    suite.run(result)
            return result

        reference = run(ref.build_prediction_response)
        self.assertTrue(reference.wasSuccessful(), f"reference failures: {reference.errors + reference.failures}")
        mutants = {
            "wrong-label": lambda model, payload: {"predicted_label": "definition", "decision_scope": "synthetic_document_type_only"},
            "accepts-malformed": lambda model, payload: {"predicted_label": "schedule", "decision_scope": "synthetic_document_type_only"},
        }
        for name, mutant in mutants.items():
            with self.subTest(mutant=name):
                result = run(mutant)
                self.assertFalse(result.wasSuccessful(), f"actual acceptance selectors accepted mutant {name}")
        wrong_error_mapping = lambda error: (200, {"error": "prediction_failed"})
        result = run(ref.build_prediction_response, wrong_error_mapping)
        self.assertFalse(result.wasSuccessful(), "actual acceptance selectors accepted incorrect error mapping")

    def test_same_public_acceptance_contract_passes_reference_and_rejects_mutants(self):
        assert_public_contract(ref.build_prediction_response)
        mutants = {
            "wrong-class": lambda model, payload: {"predicted_label": "exclusion", "decision_scope": "synthetic_document_type_only"},
            "wrong-feature-order-accepted": lambda model, payload: ({"predicted_label": "schedule", "decision_scope": "synthetic_document_type_only"} if payload is VALID else {"predicted_label": "schedule", "decision_scope": "synthetic_document_type_only"}),
            "extra-key-accepted": lambda model, payload: {"predicted_label": "schedule", "decision_scope": "synthetic_document_type_only"},
            "nonfinite-coerced": lambda model, payload: {"predicted_label": "schedule", "decision_scope": "synthetic_document_type_only"},
        }
        for name, mutant in mutants.items():
            with self.subTest(mutant=name), self.assertRaises(AssertionError):
                assert_public_contract(mutant)

    def test_real_loopback_route_uses_same_reference_oracle_and_rejects_wrong_result_mutant(self):
        with patch.object(integration_learner, "build_prediction_response", ref.build_prediction_response):
            with patch.object(integration_learner, "prediction_error_response", ref.prediction_error_response):
                with running_model_api(MODEL) as base_url:
                    status, result = post(base_url, "/api/model/predict", VALID)
        self.assertEqual((200, {"predicted_label": "schedule", "decision_scope": "synthetic_document_type_only"}), (status, result))
        wrong_label = lambda model, payload: {"predicted_label": "definition", "decision_scope": "synthetic_document_type_only"}
        with patch.object(integration_learner, "build_prediction_response", wrong_label):
            with patch.object(integration_learner, "prediction_error_response", ref.prediction_error_response):
                with running_model_api(MODEL) as base_url:
                    status, result = post(base_url, "/api/model/predict", VALID)
        with self.assertRaises(AssertionError):
            self.assertEqual((200, ref.build_prediction_response(MODEL, VALID)), (status, result))

    def test_loopback_acceptance_checks_invalid_request_and_inference_failure_disclosure(self):
        malformed = {"feature_names": list(reversed(MODEL["feature_names"])), "features": [1.4, 0.1, 0.0]}
        with patch.object(integration_learner, "build_prediction_response", ref.build_prediction_response):
            with patch.object(integration_learner, "prediction_error_response", ref.prediction_error_response):
                with running_model_api(MODEL) as base_url:
                    status, result = post(base_url, "/api/model/predict", malformed)
        self.assertEqual((400, {"error": "invalid_request"}), (status, result))
        with patch.object(integration_learner, "build_prediction_response", side_effect=RuntimeError("fixture internal detail")):
            with patch.object(integration_learner, "prediction_error_response", ref.prediction_error_response):
                with running_model_api(MODEL) as base_url:
                    status, result = post(base_url, "/api/model/predict", VALID)
        self.assertEqual((500, {"error": "prediction_failed"}), (status, result))
        self.assertNotIn("fixture internal detail", str(result))


if __name__ == "__main__":
    unittest.main()
