"""Unsolved M2.3 adapters. These are never replaced silently by references."""
from __future__ import annotations
from typing import Any, Mapping


def validate_authorization(record: Mapping[str, Any], now: int) -> bool:
    """Validate explicit, unexpired scope and residual-cost acknowledgement."""
    raise NotImplementedError("M2.3: validate user authorization before any cloud operation")


def parse_model_response(body: Mapping[str, Any], expected_model: str) -> dict[str, Any]:
    """Parse a provider response; never invent usage or successful output."""
    raise NotImplementedError("M2.3: parse an actual bounded provider response")


def diff_inventory(desired: Mapping[str, Any], observed: Mapping[str, Any]) -> dict[str, list[str]]:
    """Return deterministic create/change/delete sets from a read-only inventory."""
    raise NotImplementedError("M2.3: compare desired and observed resources")


def build_bounded_model_request(model_id: str, prompt: str, max_tokens: int) -> dict[str, Any]:
    """Build the approved provider/model's request without making a network call."""
    raise NotImplementedError("M2.3: implement a model-specific, bounded request adapter")


def evaluate_live_gate(receipts: Mapping[str, Any]) -> dict[str, Any]:
    """Evaluate real evidence; missing cloud/model evidence remains BLOCKED."""
    raise NotImplementedError("M2.3: evaluate cloud and hosted-model gates independently")
