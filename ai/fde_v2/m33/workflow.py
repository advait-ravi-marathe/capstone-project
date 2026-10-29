"""Feature path: run real bounded M3.1 tool calls, then learner-owned case scoring."""
from __future__ import annotations
from typing import Any, Mapping, Sequence
from ai.fde_v2.m31 import learner as m31_learner, workflow as m31_workflow
from . import learner


def run_task_suite(root: str, cases: Sequence[Mapping[str, Any]], *,
                   tool_adapter=m31_learner, evaluator=learner) -> list[dict[str, Any]]:
    rows=[]
    for case in cases:
        observed=m31_workflow.run_workflow(root,case.get("plan",{}),adapter=tool_adapter)
        rows.append(evaluator.score_case(case,observed))
    return rows
