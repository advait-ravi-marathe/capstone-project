"""Instructor reference mechanics for M3.1; tools still read real scratch files."""
from __future__ import annotations
import difflib, hashlib, json, os, pathlib, re
from typing import Any, Mapping

MAX_FILE_BYTES = 256_000
MAX_PLAN_STEPS = 4
ALLOWED_TOOLS = {"read_file", "read_test_log", "propose_patch"}


def resolve_allowed_path(root: str, relative_path: str) -> str:
    base = pathlib.Path(root).resolve(strict=True)
    if not relative_path or pathlib.Path(relative_path).is_absolute():
        raise PermissionError("path must be relative")
    if ".." in pathlib.PurePath(relative_path).parts:
        raise PermissionError("parent traversal is not allowed")
    candidate = (base / relative_path).resolve(strict=True)
    if not candidate.is_relative_to(base) or not candidate.is_file():
        raise PermissionError("path escapes allowed root or is not a file")
    if candidate.stat().st_size > MAX_FILE_BYTES:
        raise ValueError("file exceeds read limit")
    return str(candidate)


def validate_plan(plan: Mapping[str, Any], allowed_tools: set[str] = ALLOWED_TOOLS) -> dict[str, Any]:
    if not isinstance(plan, Mapping) or set(plan) != {"steps"} or not isinstance(plan["steps"], list):
        raise ValueError("plan must contain only a steps list")
    if len(plan["steps"]) > MAX_PLAN_STEPS:
        raise ValueError("plan exceeds step limit")
    checked=[]
    for step in plan["steps"]:
        if not isinstance(step, Mapping) or set(step) != {"tool", "arguments"}:
            raise ValueError("malformed tool step")
        tool, args = step["tool"], step["arguments"]
        if not isinstance(tool, str) or tool not in allowed_tools or not isinstance(args, Mapping):
            raise PermissionError("unapproved tool or malformed arguments")
        if tool in {"read_file", "read_test_log"}:
            if set(args) != {"path"} or not isinstance(args["path"], str): raise ValueError("invalid read args")
        elif tool == "propose_patch":
            if (set(args) != {"path", "original", "proposed"}
                    or any(not isinstance(args[k], str) for k in args)):
                raise ValueError("invalid patch proposal args")
        checked.append({"tool":tool,"arguments":dict(args)})
    return {"steps":checked}


def execute_tool(root: str, tool: str, arguments: Mapping[str, Any], *, cancel=None) -> dict[str, Any]:
    """Execute one allowlisted local tool against actual files/stdlib unittest."""
    import time
    if cancel is not None and cancel.is_set(): return {"status":"cancelled","trace_id":os.urandom(8).hex()}
    trace=os.urandom(8).hex(); started=time.monotonic()
    if not isinstance(tool, str) or tool not in ALLOWED_TOOLS:
        raise PermissionError("tool is not allowlisted")
    if not isinstance(arguments, Mapping):
        raise ValueError("tool arguments must be a mapping")
    if tool in {"read_file", "read_test_log"}:
        path=resolve_allowed_path(root, arguments.get("path", ""))
        text=pathlib.Path(path).read_text(encoding="utf-8")
        return {"status":"ok","tool":tool,"text":text,"sha256":hashlib.sha256(text.encode()).hexdigest(),
                "trace_id":trace,"elapsed_seconds":time.monotonic()-started}
    if tool == "propose_patch":
        proposal = render_patch_proposal(root, arguments.get("path"),
                                         arguments.get("original"), arguments.get("proposed"))
        return {"status":"proposal_ready", "tool":tool, "proposal":proposal,
                "trace_id":trace,"elapsed_seconds":time.monotonic()-started}
    raise PermissionError("tool is not allowlisted")


def classify_failure(outcome: Mapping[str, Any]) -> str:
    if outcome.get("status")=="cancelled": return "cancelled"
    if outcome.get("status")=="tool_denied": return "authorization_denial"
    if outcome.get("status")=="test_failed" or outcome.get("returncode",0)!=0: return "test_failure"
    if outcome.get("status")=="timeout": return "timeout"
    if outcome.get("status")!="ok": return "tool_error"
    return "success"


def render_patch_proposal(root: str, relative_path: str, original: str, proposed: str) -> dict[str, Any]:
    path=resolve_allowed_path(root,relative_path)
    if not isinstance(original,str) or not isinstance(proposed,str) or len(proposed.encode())>32_000:
        raise ValueError("invalid or oversized proposal")
    actual=pathlib.Path(path).read_text(encoding="utf-8")
    if actual != original: raise ValueError("source changed since review")
    diff="".join(difflib.unified_diff(original.splitlines(True),proposed.splitlines(True),
                                     fromfile=relative_path,tofile=relative_path))
    if len(diff.encode())>32_000: raise ValueError("diff exceeds limit")
    return {"path":relative_path,"source_sha256":hashlib.sha256(original.encode()).hexdigest(),
            "proposal_sha256":hashlib.sha256(proposed.encode()).hexdigest(),"diff":diff,
            "applied":False,"scratch_root":str(pathlib.Path(root).resolve())}
