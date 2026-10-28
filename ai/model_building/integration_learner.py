"""Learner-owned model-serving boundary; exercises are assigned after Day 30."""

from __future__ import annotations

from ai.model_building import learner


def build_prediction_response(model: dict, payload: object) -> dict:
    """Validate one JSON request and return a deliberately narrow model result.

    OWN-INT-001: implement exact-key, feature-order, finite-number and width
    checks, call learner.predict_document_type for one row, and return only the
    contract fields documented in integration-ownership-amendment.json.
    """
    raise NotImplementedError("OWN-INT-001: implement the model-serving adapter")


def prediction_error_response(error: Exception) -> tuple[int, dict]:
    """OWN-INT-332: map bad requests and model failures to safe HTTP contracts."""
    raise NotImplementedError("OWN-INT-332: implement safe model API error mapping")
