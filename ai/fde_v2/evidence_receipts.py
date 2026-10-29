"""Hash-verified, review-required evidence artifact manifests for later gates.

This verifies local artifact identity and coverage only. It cannot authenticate
people, providers, cloud state, learner authorship, or the truth of a claim.
"""
from __future__ import annotations

import hashlib
import json
import sys
from pathlib import Path
from typing import Iterable


SCHEMA = "fde-v2-evidence-artifacts/v1"


def report_receipt(receipt_path: str | Path | None, milestone: str,
                   required_keys: Iterable[str]) -> int:
    """CLI helper: verified bytes earn only independent-review eligibility."""
    if receipt_path is None:
        result={"status":"BLOCKED","reason":"no evidence artifact manifest supplied",
                "missing_keys":sorted(set(required_keys)),"milestone_pass":False}
    else:
        result=audit_artifact_receipt(receipt_path,milestone,required_keys)
    print(json.dumps(result,indent=2))
    return 0 if result["status"]=="ARTIFACTS_PRESENT_REQUIRES_INDEPENDENT_REVIEW" else 2


def audit_artifact_receipt(receipt_path: str | Path, milestone: str,
                           required_keys: Iterable[str] = ()) -> dict:
    path = Path(receipt_path).resolve(strict=True)
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        return {"status": "INVALID", "reason": f"receipt cannot be read as JSON: {type(exc).__name__}",
                "verified_artifacts": [], "missing_keys": list(required_keys)}
    if not isinstance(data, dict) or data.get("schema") != SCHEMA or data.get("milestone") != milestone:
        return {"status": "INVALID", "reason": "schema or milestone does not match",
                "verified_artifacts": [], "missing_keys": list(required_keys)}
    artifacts = data.get("artifacts")
    if not isinstance(artifacts, list):
        return {"status": "INVALID", "reason": "artifacts must be a list",
                "verified_artifacts": [], "missing_keys": list(required_keys)}
    root = path.parent.resolve()
    keys: set[str] = set(); verified = []
    for item in artifacts:
        if not isinstance(item, dict) or set(item) != {"key", "path", "sha256"}:
            return {"status": "INVALID", "reason": "artifact rows require exactly key/path/sha256",
                    "verified_artifacts": verified, "missing_keys": sorted(set(required_keys)-keys)}
        key, rel, digest = item["key"], item["path"], item["sha256"]
        if (not isinstance(key, str) or not key.strip() or key in keys or not isinstance(rel, str)
                or not isinstance(digest, str) or len(digest) != 64
                or any(ch not in "0123456789abcdef" for ch in digest)):
            return {"status": "INVALID", "reason": "artifact key/path/hash is malformed or duplicated",
                    "verified_artifacts": verified, "missing_keys": sorted(set(required_keys)-keys)}
        relative = Path(rel)
        if relative.is_absolute() or ".." in relative.parts:
            return {"status": "INVALID", "reason": f"artifact path is missing or escapes receipt directory: {rel}",
                    "verified_artifacts": verified, "missing_keys": sorted(set(required_keys)-keys)}
        candidate = root / relative
        probe = root
        symlink = False
        for part in relative.parts:
            probe = probe / part
            if probe.is_symlink():
                symlink = True; break
        artifact = candidate.resolve(strict=False)
        if symlink or not artifact.is_relative_to(root) or not artifact.is_file():
            return {"status": "INVALID", "reason": f"artifact path is missing or escapes receipt directory: {rel}",
                    "verified_artifacts": verified, "missing_keys": sorted(set(required_keys)-keys)}
        actual = hashlib.sha256(artifact.read_bytes()).hexdigest()
        if actual != digest:
            return {"status": "INVALID", "reason": f"artifact digest mismatch: {rel}",
                    "verified_artifacts": verified, "missing_keys": sorted(set(required_keys)-keys)}
        keys.add(key)
        verified.append({"key": key, "path": rel, "sha256": actual})
    missing = sorted(set(required_keys)-keys)
    return {"status": "ARTIFACTS_PRESENT_REQUIRES_INDEPENDENT_REVIEW" if not missing else "BLOCKED",
            "reason": "artifact hashes and declared gate-key coverage verified; claim truth/provenance requires independent review",
            "verified_artifacts": verified, "missing_keys": missing,
            "milestone_pass": False}
