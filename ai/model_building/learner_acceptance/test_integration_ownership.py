"""Intentional learner acceptance for the post-Day-30 integration slice."""

import json
import math
import unittest
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
        self.assertEqual((400, {"error": "invalid_request"}), integration_learner.prediction_error_response(ValueError("bad input")))
        self.assertEqual((500, {"error": "prediction_failed"}), integration_learner.prediction_error_response(RuntimeError("private details")))

    def test_own_002_internal_failure_is_generic_and_never_leaks_exception_text(self):
        from unittest.mock import patch
        with patch.object(integration_learner, "build_prediction_response", side_effect=RuntimeError("private centroid detail")):
            with running_model_api(MODEL) as base_url:
                status, result = post(base_url, "/api/model/predict", VALID)
        self.assertEqual((500, {"error": "prediction_failed"}), (status, result))
        self.assertNotIn("private centroid detail", json.dumps(result))


if __name__ == "__main__":
    unittest.main()
