#!/usr/bin/env python3
"""Mechanical structure checks for the single FDE v2 exercise; not a quality score."""
import json
import sys
from pathlib import Path


def validate(path: Path) -> list[str]:
    data = json.loads(path.read_text())
    errors = []
    if data.get("schema_version") != 2 or data.get("criteria_version") != "fde-coursework-v2":
        errors.append("unsupported schema/criteria version")
    cases = data.get("acceptance_cases", [])
    ids = [case.get("id") for case in cases]
    if any(not item for item in ids) or len(ids) != len(set(ids)):
        errors.append("acceptance case IDs must be present and unique")
    selectors = {case.get("selector") for case in cases if case.get("selector")}
    if len(selectors) != len([case for case in cases if case.get("selector")]):
        errors.append("acceptance selectors must be unique")
    if any(not case.get("selector") for case in cases):
        errors.append("each acceptance case needs an existing selector reference")
    seen = set()
    for observation in data.get("observations", []):
        case_id = observation.get("case_id")
        if case_id not in ids:
            errors.append(f"observation references unknown case/selector: {case_id}")
        if case_id in seen:
            errors.append(f"duplicate observation for case: {case_id}")
        seen.add(case_id)
        verdict = observation.get("verdict")
        if verdict not in {"not_run", "blocked", "failed", "passed"}:
            errors.append(f"invalid verdict for {case_id}")
        if verdict == "passed" and (not observation.get("actual") or not observation.get("evidence_ref")):
            errors.append(f"passed observation lacks actual result/evidence: {case_id}")
    observed = {row.get("case_id"): row for row in data.get("observations", [])}
    for case in cases:
        if case.get("result") == "passed" and (case.get("id") not in observed or observed[case["id"]].get("verdict") != "passed"):
            errors.append(f"case marked passed without a passed observation: {case.get('id')}")
    recommendation = data.get("recommendation", {})
    if recommendation.get("state") == "ready" and not any(row.get("verdict") == "passed" for row in data.get("observations", [])):
        errors.append("ready recommendation lacks observed passing evidence")
    return errors


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit("usage: python3 scripts/validate-lifecycle-delivery-v2.py PATH.json")
    found = validate(Path(sys.argv[1]))
    if found:
        for message in found:
            print(f"ERROR: {message}")
        raise SystemExit(1)
    print("structure valid; human review still required")
