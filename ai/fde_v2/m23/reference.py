"""Instructor mechanics for conditional M2.3; does not call AWS or a model."""
from __future__ import annotations
import math
from typing import Any, Mapping

REQUIRED_SCOPE = ("account_id", "region", "services", "model_id", "spend_ceiling", "currency",
                  "expires_at", "teardown_owner", "residual_cost_acknowledged")


def validate_authorization(record: Mapping[str, Any], now: int) -> bool:
    if not isinstance(record, Mapping) or any(k not in record for k in REQUIRED_SCOPE):
        return False
    account, region, services = record["account_id"], record["region"], record["services"]
    model, currency, owner = record["model_id"], record["currency"], record["teardown_owner"]
    expiry = record["expires_at"]
    spend = record["spend_ceiling"]
    try:
        valid_spend = (isinstance(spend, (int, float)) and not isinstance(spend, bool)
                       and math.isfinite(float(spend)) and spend > 0)
    except (OverflowError, TypeError, ValueError):
        valid_spend = False
    return (isinstance(account, str) and bool(account.strip())
            and isinstance(region, str) and bool(region.strip())
            and isinstance(services, (list, tuple, set, frozenset)) and bool(services)
            and all(isinstance(item, str) and bool(item.strip()) for item in services)
            and isinstance(model, str) and bool(model.strip())
            and valid_spend
            and isinstance(currency, str) and currency in {"USD", "CAD", "EUR", "GBP"}
            and isinstance(expiry, int) and not isinstance(expiry, bool) and expiry > now
            and isinstance(owner, str) and bool(owner.strip())
            and record["residual_cost_acknowledged"] is True)


def parse_model_response(body: Mapping[str, Any], expected_model: str) -> dict[str, Any]:
    if not isinstance(body, Mapping) or body.get("modelId") != expected_model:
        raise ValueError("provider model mismatch")
    output = body.get("outputText")
    if not isinstance(output, str) or not output.strip() or len(output) > 20_000:
        raise ValueError("missing or oversized provider output")
    usage = body.get("usage")
    if usage is not None and (not isinstance(usage, Mapping) or any(
            not isinstance(usage.get(k), int) or isinstance(usage.get(k), bool) or usage[k] < 0
            for k in ("inputTokens", "outputTokens"))):
        raise ValueError("invalid provider usage")
    return {"model_id": expected_model, "output": output,
            "usage": dict(usage) if usage is not None else None,
            "usage_status": "observed" if usage is not None else "provider_field_unavailable"}


def diff_inventory(desired: Mapping[str, Any], observed: Mapping[str, Any]) -> dict[str, list[str]]:
    if not isinstance(desired, Mapping) or not isinstance(observed, Mapping):
        raise ValueError("inventories must be mappings")
    d, o = set(desired), set(observed)
    return {"create": sorted(d-o), "change": sorted(k for k in d & o if desired[k] != observed[k]),
            "delete": sorted(o-d)}


def evaluate_live_gate(receipts: Mapping[str, Any]) -> dict[str, Any]:
    cloud_keys = ("authorized_resource_readback", "live_app_request", "live_trace", "rollback", "teardown")
    model_keys = ("authorized_model_request", "model_id", "request_id", "provider_response")
    cloud = all(receipts.get(k) is True for k in cloud_keys)
    model = all(receipts.get(k) is True for k in model_keys)
    return {"cloud_status": "PASS_EVIDENCE_PRESENT" if cloud else "BLOCKED",
            "model_status": "PASS_EVIDENCE_PRESENT" if model else "BLOCKED",
            "status": "EVIDENCE_PRESENT_REQUIRES_REVIEW" if cloud and model else "BLOCKED",
            "milestone_pass": False}
