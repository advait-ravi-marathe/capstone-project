"""Learner-owned M4.3 assessment seams; no peer feedback is synthesized."""
from typing import Any, Mapping

def validate_attempt(record: Mapping[str, Any]) -> dict[str, Any]:
    raise NotImplementedError("M4.3: validate original exercise and attempt provenance")

def score_attempt(record: Mapping[str, Any]) -> dict[str, Any]:
    raise NotImplementedError("M4.3: score actual attempt dimensions and feedback type")

def evaluate_handoff(receipts: Mapping[str, Any]) -> dict[str, Any]:
    raise NotImplementedError("M4.3: assess clean-run and handoff evidence honestly")
