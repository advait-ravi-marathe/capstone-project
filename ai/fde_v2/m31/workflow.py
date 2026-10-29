"""Feature path for bounded M3.1 scratch tools; defaults to learner TODOs."""
from __future__ import annotations
import threading
from typing import Any, Mapping
from . import learner

def run_workflow(root: str, plan: Mapping[str, Any] | None = None, *, adapter=learner,
                 cancel=None, planner=None, planning_inputs=None) -> dict[str, Any]:
    if (plan is None) == (planner is None):
        raise ValueError("supply exactly one bounded plan or explicit planner adapter")
    trace=[]
    if planner is not None:
        if not callable(planner) or not isinstance(planning_inputs, list) or not planning_inputs or len(planning_inputs)>2:
            raise ValueError("planner mode needs a callable and one or two explicit scratch input paths")
        observations=[]
        for relative_path in planning_inputs:
            result=adapter.execute_tool(root,"read_file",{"path":relative_path},cancel=cancel)
            if result.get("status")!="ok":
                trace.append({"tool":"read_file","result":result,"failure_class":adapter.classify_failure(result)})
                return {"trace":trace,"cancelled":bool(cancel and cancel.is_set()),"patch":None,"write_applied":False}
            observations.append({"path":relative_path,"text":result["text"],"sha256":result["sha256"]})
            trace.append({"tool":"read_file","result":result,"failure_class":adapter.classify_failure(result)})
        # Planner output remains untrusted and is revalidated below. No planner
        # receives shell, network, write, or external repository capabilities.
        plan=planner({"observations":observations})
    checked=adapter.validate_plan(plan, {"read_file","read_test_log","propose_patch"})
    patch=None
    for index, step in enumerate(checked["steps"]):
        if index >= 4:
            raise ValueError("workflow step limit exceeded")
        if cancel is not None and cancel.is_set():
            trace.append({"tool":step["tool"],"result":{"status":"cancelled"},
                          "failure_class":"cancelled"})
            break
        try:
            result=adapter.execute_tool(root,step["tool"],step["arguments"],cancel=cancel)
        except (PermissionError,ValueError,FileNotFoundError) as exc:
            result={"status":"tool_denied" if isinstance(exc,PermissionError) else "tool_error",
                    "error_type":type(exc).__name__}
        trace.append({"tool":step["tool"],"result":result,
                      "failure_class":adapter.classify_failure(result)})
        if result.get("status")=="proposal_ready":
            # At most one review artifact is returned; the tool never applies it.
            patch=result["proposal"]
    return {"trace":trace,"cancelled":bool(cancel and cancel.is_set()),"patch":patch,
            "write_applied":False}
