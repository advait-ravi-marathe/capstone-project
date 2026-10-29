"""M2.3 operation boundary with an inert, explicitly injected transport seam.

No cloud client is constructed by default. Local fixtures can exercise adapter
wiring, but they do not represent AWS or hosted-model evidence.
"""
from __future__ import annotations
from typing import Any, Mapping
from . import reference as application_adapter
from .providers import BedrockConverseAdapter

def prepare_live_operation(record: Mapping[str, Any], now: int, *, adapter=application_adapter,
                           transport=None) -> dict[str, Any]:
    """Validate exact authorization before optionally invoking an injected transport.

    The repository ships no live transport and never creates one implicitly.
    A transport object is intentionally dependency-injected so local fixture
    tests can prove the orchestration contract without network access.
    """
    try:
        allowed=adapter.validate_authorization(record,now)
    except NotImplementedError:
        raise
    # This is a side-effect boundary. Do not coerce malformed truthy policy
    # results such as "false" into authorization.
    if allowed is not True:
        return {"status":"BLOCKED","reason":"authorization scope invalid or expired","external_call_made":False}
    if transport is None:
        return {"status":"BLOCKED","reason":"no explicitly authorized provider transport is configured","external_call_made":False}
    if not isinstance(transport,BedrockConverseAdapter):
        return {"status":"BLOCKED","reason":"only the approval-bound Bedrock adapter is supported",
                "external_call_made":False}
    result=transport.execute(dict(record))
    if not isinstance(result,Mapping):
        return {"status":"INVALID_PROVIDER_RESPONSE","external_call_made":False}
    if result.get("external_call_made") is not True:
        return {"status":"BLOCKED","reason":result.get("reason","provider adapter did not confirm a bounded request"),
                "external_call_made":False}
    return {"status":"PROVIDER_RESPONSE_REQUIRES_REVIEW","external_call_made":True,
            "provider_result":dict(result),"milestone_pass":False}
