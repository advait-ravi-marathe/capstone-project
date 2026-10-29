"""Fail-closed M2.3 preparation path; deliberately contains no cloud client."""
from __future__ import annotations
from typing import Any, Mapping
from . import learner

def prepare_live_operation(record: Mapping[str, Any], now: int, *, adapter=learner) -> dict[str, Any]:
    """Feature entry point. Even valid scope cannot invoke AWS in this local scaffold."""
    try:
        allowed=adapter.validate_authorization(record,now)
    except NotImplementedError:
        raise
    if not allowed:
        return {"status":"BLOCKED","reason":"authorization scope invalid or expired","external_call_made":False}
    return {"status":"BLOCKED","reason":"live AWS transport is not configured or authorized","external_call_made":False}
