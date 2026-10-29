"""Feature path routes change/recovery assessments through learner-owned seams."""
from __future__ import annotations
from typing import Any, Mapping, Sequence
from . import learner

def review_change(change: Mapping[str, Any], observations: Sequence[Mapping[str, Any]], incident: Mapping[str, Any],
                  test_receipt: Mapping[str, Any], *, adapter=learner) -> dict[str, Any]:
    c=adapter.validate_change_contract(change)
    o=adapter.summarize_observations(observations)
    i=adapter.validate_incident(incident)
    gate=adapter.evaluate_change_gate(change,{**test_receipt,"status":test_receipt.get("status")},incident)
    return {"change":c,"observations":o,"incident":i,"gate":gate}
