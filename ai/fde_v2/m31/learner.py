"""Unsolved M3.1 seams. Workflow execution routes through these functions."""
from __future__ import annotations
from typing import Any, Mapping


def resolve_allowed_path(root: str, relative_path: str) -> str:
    """Resolve an existing file strictly inside an explicitly allowed scratch root."""
    raise NotImplementedError("M3.1: implement fail-closed scratch path resolution")


def validate_plan(plan: Mapping[str, Any], allowed_tools: set[str]) -> dict[str, Any]:
    """Validate a bounded plan; model output is untrusted data."""
    raise NotImplementedError("M3.1: validate typed, bounded tool plan")


def classify_failure(outcome: Mapping[str, Any]) -> str:
    """Classify actual workflow outcomes without dropping failures."""
    raise NotImplementedError("M3.1: classify tool denial, timeout, error and task failure")


def execute_tool(root: str, tool: str, arguments: Mapping[str, Any], *, cancel=None) -> dict[str, Any]:
    """Execute one allowed local read/test tool through bounded local adapters."""
    raise NotImplementedError("M3.1: implement typed bounded scratch tool execution")


def render_patch_proposal(root: str, relative_path: str, original: str, proposed: str) -> dict[str, Any]:
    """Create a reviewable patch artifact within scratch only; never apply it."""
    raise NotImplementedError("M3.1: render scoped, bounded proposal")
