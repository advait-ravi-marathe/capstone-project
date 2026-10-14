#!/usr/bin/env python3
"""Check source integrity, curriculum cardinality and generated daily guides."""

from __future__ import annotations

import json
import re
from pathlib import Path

from materialize_coverstory_curriculum import (
    CURRICULUM,
    INDEX,
    MANIFEST,
    OUTPUT,
    ROOT,
    acceptance_excerpt,
    active_task,
    explicit_path_references,
    planned_paths,
    sha256,
    verify_source,
)


def check() -> dict:
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    raw = CURRICULUM.read_bytes()
    if sha256(raw) != manifest["source_final_contract_sha256"]:
        raise AssertionError("saved curriculum hash differs from verified manifest")
    for sums in (MANIFEST.parent / "SHA256SUMS", ROOT / "docs/source-inputs/visual-2026-10-14/SHA256SUMS"):
        base = sums.parent.resolve()
        for line in sums.read_text(encoding="utf-8").splitlines():
            match = re.fullmatch(r"([0-9a-f]{64})  (.+)", line)
            if not match:
                raise AssertionError(f"malformed checksum line in {sums}")
            relative = Path(match.group(2))
            target = (base / relative).resolve()
            if base not in target.parents:
                raise AssertionError(f"checksum path escapes source bundle: {relative}")
            if not target.is_file() or sha256(target.read_bytes()) != match.group(1):
                raise AssertionError(f"source bundle checksum mismatch: {sums.parent / relative}")
    visual_manifest = json.loads((ROOT / "docs/source-inputs/visual-2026-10-14/VISUAL-INPUT-MANIFEST.json").read_text(encoding="utf-8"))
    if visual_manifest["counts"] != {"html_wireframes": 87, "preview_pngs": 24, "source_generators": 4}:
        raise AssertionError("visual input manifest count mismatch")
    curriculum = json.loads(raw)
    primaries = verify_source(curriculum, manifest)
    index = json.loads(INDEX.read_text(encoding="utf-8"))
    if len(index["days"]) != len(primaries):
        raise AssertionError("generated day index count does not match primary count")
    by_id = {c["id"]: c for c in curriculum["cards"]}
    for day, primary in zip(index["days"], primaries):
        guide = Path(day["guide"])
        if not guide.is_absolute():
            guide = ROOT / guide
        if not guide.is_file():
            raise AssertionError(f"missing daily guide {guide}")
        body = guide.read_text(encoding="utf-8")
        if day["primary_id"] != primary["id"] or day["primary_description_sha256"] != primary["description_sha256"]:
            raise AssertionError(f"day index identity/hash mismatch: {primary['id']}")
        # Recompute every derived field from the canonical card independently of
        # the guide and matrix; edits to both outputs cannot hide source drift.
        if day["practical_task"] != active_task(primary["description"]):
            raise AssertionError(f"task summary differs from authoritative source section: {primary['id']}")
        if day["acceptance_contract_excerpt"] != acceptance_excerpt(primary["description"]):
            raise AssertionError(f"acceptance excerpt differs from authoritative source: {primary['id']}")
        if day["explicit_planned_paths"] != planned_paths(primary["description"]):
            raise AssertionError(f"planned paths differ from authoritative source: {primary['id']}")
        if day["explicit_path_references"] != explicit_path_references(primary["description"]):
            raise AssertionError(f"path references differ from authoritative source: {primary['id']}")
        expected_components = sorted(
            c["id"] for c in curriculum["cards"] if c["role"] == "linked_component" and c["primary_id"] == primary["id"]
        )
        if day["component_ids"] != expected_components:
            raise AssertionError(f"linked-component mapping mismatch: {primary['id']}")
        for component_id in expected_components:
            component = next(item for item in day["components"] if item["id"] == component_id)
            if component["description_sha256"] != by_id[component_id]["description_sha256"]:
                raise AssertionError(f"component source hash mismatch: {component_id}")
            if component["practical_task"] != active_task(by_id[component_id]["description"]):
                raise AssertionError(f"component task summary differs from source: {component_id}")
            if component["acceptance_contract_excerpt"] != acceptance_excerpt(by_id[component_id]["description"]):
                raise AssertionError(f"component acceptance differs from source: {component_id}")
            if component["practical_task"] not in body:
                raise AssertionError(f"guide omits component task summary: {component_id}")
        if not day["practical_task"].strip():
            raise AssertionError(f"missing source-specific practical task: {primary['id']}")
        if day["practical_task"] not in body:
            raise AssertionError(f"guide omits source-specific primary task: {primary['id']}")
        if not day["acceptance_contract_excerpt"]:
            raise AssertionError(f"saved card has no surfaced acceptance section: {primary['id']}")
        if day["acceptance_contract_excerpt"] not in body:
            raise AssertionError(f"guide omits primary acceptance criteria: {primary['id']}")
        if primary["description"] in body:
            raise AssertionError(f"guide duplicates complete source description rather than linking canonical JSON: {primary['id']}")
        if day["learner_readiness"] != "NOT_ESTABLISHED_BY_SOURCE_OR_MATERIALIZATION":
            raise AssertionError(f"unsafe readiness claim: {primary['id']}")
    return index["counts"]


if __name__ == "__main__":
    print(json.dumps({"status": "PASS", "counts": check()}, indent=2))
