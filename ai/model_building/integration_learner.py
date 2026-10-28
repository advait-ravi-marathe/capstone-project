"""Learner-owned model-serving boundary; exercises are assigned after Day 30."""

from __future__ import annotations

from ai.model_building import learner


class InvalidPredictionRequest(ValueError):
    """A request-shape or feature-validation error at the API boundary."""


def build_prediction_response(model: dict, payload: object) -> dict:
    """Validate one JSON request and return a deliberately narrow model result.

    OWN-INT-001: implement exact-key, feature-order, finite-number and width
    checks. Raise InvalidPredictionRequest for caller-controlled invalid input,
    call learner.predict_document_type for one row, and return only the contract
    fields documented in integration-ownership-amendment.json. Do not convert
    model/inference exceptions into InvalidPredictionRequest.
    """
    raise NotImplementedError("OWN-INT-001: implement the model-serving adapter")


def prediction_error_response(error: Exception) -> tuple[int, dict]:
    """OWN-INT-332: only InvalidPredictionRequest is HTTP 400; other failures are 500."""
    raise NotImplementedError("OWN-INT-332: implement safe model API error mapping")
