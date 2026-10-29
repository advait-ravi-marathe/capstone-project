"""Discovery feature path: learner-owned validation and evaluator-attributed rubric."""
from __future__ import annotations
from typing import Any, Mapping
from . import reference as application_adapter

def review_discovery(record: Mapping[str, Any], evaluation: Mapping[str, Any] | None, *, adapter=application_adapter) -> dict[str, Any]:
    discovery=adapter.validate_discovery(record)
    rubric=adapter.assess_rubric(evaluation or {})
    contract=adapter.build_pilot_contract(record,rubric)
    real_user_claim=(record.get("evidence_class")=="real_user" and record.get("authorized_contact") is True)
    return {"discovery":discovery,"rubric":rubric,"pilot_contract":contract,
            "real_user_gate":"REVIEW_REQUIRED" if real_user_claim else "BLOCKED"}
