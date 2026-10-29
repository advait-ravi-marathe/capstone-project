"""Feature path for bounded M3.1 scratch tools; defaults to learner TODOs."""
from __future__ import annotations
import threading
from typing import Any, Mapping
from . import learner

def run_workflow(root: str, plan: Mapping[str, Any], *, adapter=learner, cancel=None) -> dict[str, Any]:
    checked=adapter.validate_plan(plan, {"read_file","read_test_log"})
    trace=[]
    for step in checked["steps"]:
        if cancel is not None and cancel.is_set(): break
        try:
            result=adapter.execute_tool(root,step["tool"],step["arguments"],cancel=cancel)
        except (PermissionError,ValueError,FileNotFoundError) as exc:
            result={"status":"tool_denied" if isinstance(exc,PermissionError) else "tool_error",
                    "error_type":type(exc).__name__}
        trace.append({"tool":step["tool"],"result":result,
                      "failure_class":adapter.classify_failure(result)})
    return {"trace":trace,"cancelled":bool(cancel and cancel.is_set()),"patch":None}
