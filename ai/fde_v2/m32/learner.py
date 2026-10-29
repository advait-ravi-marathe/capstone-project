"""Unsolved M3.2 approval/authorization seams; never a permissive fallback."""
from __future__ import annotations
from typing import Any, Mapping

def canonical_action_digest(action: str, target: str, content: bytes, source_hash: str,
                            scope_hash: str, source_path: str, session_binding: str) -> str:
    raise NotImplementedError("M3.2: bind approval to exact action, bytes, target, source and scope")

def consume_approval(ledger, approval_id: str, action_digest: str, now: float) -> bool:
    raise NotImplementedError("M3.2: atomically consume one unexpired, exact-action approval")

def authorize_resource(root: str, relative_path: str) -> str:
    raise NotImplementedError("M3.2: confine MCP resources to owned scratch root")

def write_patch_proposal(root: str, ledger, approval_id: str, target_name: str, content: bytes,
                         source_hash: str, scope_hash: str, source_path: str, session_binding: str,
                         now: float | None = None):
    raise NotImplementedError("M3.2: verify/consume exact approval before controlled scratch write")
