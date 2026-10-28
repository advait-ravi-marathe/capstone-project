"""Intentional learner acceptance for the post-Day-30 integration slice."""

import json
import math
import unittest
from copy import deepcopy
from urllib.error import HTTPError
from urllib.request import Request, urlopen

from ai.model_building import integration_learner
from ai.model_building.model_api_scaffold import running_model_api


MODEL = {
    "classes": ["schedule", "exclusion", "definition"],
    "feature_names": ["schedule_count", "exclusion_count", "definition_count"],
    "training_ids": ["train-1", "train-2", "train-3"],
    "centroids": [[1.5, 0.0, 0.0], [0.0, 1.5, 0.0], [0.0, 0.0, 1.5]],
}
VALID = {"feature_names": list(MODEL["feature_names"]), "features": [1.4, 0.1, 0.0]}


def post(base_url: str, path: str, payload: object):
    body = json.dumps(payload, allow_nan=False).encode("utf-8")
    request = Request(base_url + path, data=body, headers={"Content-Type": "application/json"}, method="POST")
    try:
        with urlopen(request, timeout=2) as response:
            return response.status, json.loads(response.read())
    except HTTPError as error:
        return error.code, json.loads(error.read())


class IntegrationOwnershipAcceptance(unittest.TestCase):
    def test_own_001_builds_narrow_response_from_day30_model(self):
        self.assertEqual(
            {"predicted_label": "schedule", "decision_scope": "synthetic_document_type_only"},
            integration_learner.build_prediction_response(MODEL, VALID),
        )

    def test_own_001_uses_classifier_for_independent_labels_across_models(self):
        # Independent expected classes follow the fixture geometry. Changed
        # centroids force an adapter to respond to the model, not a fixed label.
        cases = [
            (MODEL, {"feature_names": list(MODEL["feature_names"]), "features": [1.4, 0.1, 0.0]}, "schedule"),
            (MODEL, {"feature_names": list(MODEL["feature_names"]), "features": [0.1, 1.4, 0.0]}, "exclusion"),
            (MODEL, {"feature_names": list(MODEL["feature_names"]), "features": [0.0, 0.1, 1.4]}, "definition"),
            ({**MODEL, "centroids": [[8.0, 0.0, 0.0], [0.0, 8.0, 0.0], [0.0, 0.0, 0.2]]},
             {"feature_names": list(MODEL["feature_names"]), "features": [1.4, 0.1, 0.0]}, "definition"),
        ]
        for model, payload, expected_label in cases:
            with self.subTest(expected=expected_label, centroids=model["centroids"]):
                self.assertEqual(
                    {"predicted_label": expected_label, "decision_scope": "synthetic_document_type_only"},
                    integration_learner.build_prediction_response(model, payload),
                )

    def test_own_001_calls_day30_classifier(self):
        from unittest.mock import patch
        from ai.model_building import learner
        with patch.object(learner, "predict_document_type", wraps=learner.predict_document_type) as classifier:
            integration_learner.build_prediction_response(MODEL, VALID)
        classifier.assert_called_once_with(MODEL, [VALID["features"]])

    def test_own_001_loopback_success_path_is_available_before_day332(self):
        with running_model_api(MODEL) as base_url:
            self.assertEqual(
                (200, {"predicted_label": "schedule", "decision_scope": "synthetic_document_type_only"}),
                post(base_url, "/api/model/predict", VALID),
            )

    def test_own_001_rejects_malformed_and_order_confused_requests(self):
        malformed = [
            None,
            {"features": [1.4, 0.1, 0.0]},
            {**VALID, "extra": "ignored?"},
            {"feature_names": list(reversed(MODEL["feature_names"])), "features": [1.4, 0.1, 0.0]},
            {"feature_names": list(MODEL["feature_names"]), "features": [1.4, 0.1]},
            {"feature_names": list(MODEL["feature_names"]), "features": [1.4, math.inf, 0.0]},
            {"feature_names": list(MODEL["feature_names"]), "features": [True, 0.1, 0.0]},
        ]
        for payload in malformed:
            with self.subTest(payload=payload), self.assertRaises(ValueError):
                integration_learner.build_prediction_response(MODEL, payload)

    def test_own_002_actual_http_path_maps_validation_and_model_failures_safely(self):
        with running_model_api(MODEL) as base_url:
            status, result = post(base_url, "/api/model/predict", VALID)
            self.assertEqual(200, status)
            self.assertEqual({"predicted_label": "schedule", "decision_scope": "synthetic_document_type_only"}, result)
            status, result = post(base_url, "/api/model/predict", {"feature_names": [], "features": []})
            self.assertEqual((400, {"error": "invalid_request"}), (status, result))
            status, result = post(base_url, "/wrong", VALID)
            self.assertEqual((404, {"error": "not_found"}), (status, result))

    def test_own_002_distinguishes_input_errors_from_inference_failures(self):
        self.assertEqual((400, {"error": "invalid_request"}), integration_learner.prediction_error_response(integration_learner.InvalidPredictionRequest("bad input")))
        self.assertEqual((500, {"error": "prediction_failed"}), integration_learner.prediction_error_response(ValueError("model inference failed")))
        self.assertEqual((500, {"error": "prediction_failed"}), integration_learner.prediction_error_response(RuntimeError("private details")))

    def test_own_002_internal_failure_is_generic_and_never_leaks_exception_text(self):
        from unittest.mock import patch
        with patch.object(integration_learner, "build_prediction_response", side_effect=RuntimeError("private centroid detail")):
            with running_model_api(MODEL) as base_url:
                status, result = post(base_url, "/api/model/predict", VALID)
        self.assertEqual((500, {"error": "prediction_failed"}), (status, result))
        self.assertNotIn("private centroid detail", json.dumps(result))

    def test_own_002_failure_does_not_mutate_model_and_same_server_recovers(self):
        from unittest.mock import patch
        from ai.model_building import learner
        model = deepcopy(MODEL)
        original = deepcopy(model)
        with patch.object(learner, "predict_document_type", side_effect=[ValueError("private model error"), ["schedule"]]):
            with running_model_api(model) as base_url:
                self.assertEqual((500, {"error": "prediction_failed"}), post(base_url, "/api/model/predict", VALID))
                self.assertEqual((200, {"predicted_label": "schedule", "decision_scope": "synthetic_document_type_only"}), post(base_url, "/api/model/predict", VALID))
        self.assertEqual(original, model)


if __name__ == "__main__":
    unittest.main()
