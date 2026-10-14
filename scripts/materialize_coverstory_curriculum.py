#!/usr/bin/env python3
"""Materialize source-backed daily contract guides from the verified input JSON.

Complete card descriptions remain byte-exact in the verified source JSON. Guides
surface task-specific sections without claiming scaffold or learner completion.
"""

from __future__ import annotations

import hashlib
import json
import re
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INPUT_ROOT = ROOT / "docs/source-inputs/2026-10-14"
CURRICULUM = INPUT_ROOT / "curriculum/coverstory-final-saved-contracts-594-days.json"
MANIFEST = INPUT_ROOT / "PROJECT-INPUT-MANIFEST.json"
OUTPUT = ROOT / "docs/assignments/coverstory/saved-days"
INDEX = ROOT / "docs/assignments/coverstory/curriculum-index.json"


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def planned_paths(description: str) -> list[str]:
    """Extract explicit repository path bullets without guessing paths."""
    found: list[str] = []
    for line in description.splitlines():
        match = re.match(
            r"^-\s+(?:[A-Za-z][A-Za-z0-9 _-]*:\s*)?`?((?:src|frontend|web|ai|fixtures|docs|scripts)/[^`\s]+)`?\s*$",
            line,
        )
        if match and match.group(1) not in found:
            found.append(match.group(1))
    return found


def explicit_path_references(description: str) -> list[str]:
    """Collect source-written file references; these are not claims they exist."""
    found: list[str] = []
    pattern = re.compile(r"(?<![\w/])((?:frontend|src|ai|fixtures|docs|scripts|web)/[A-Za-z0-9_./-]+\.(?:tsx?|jsx?|py|java|sql|json|md|yaml|yml|sh|toml|xml|lock))")
    for match in pattern.finditer(description):
        path = match.group(1).rstrip(".,;:")
        if path not in found:
            found.append(path)
    return found


def section_after(description: str, heading: str) -> str:
    lines = description.splitlines()
    for i, line in enumerate(lines):
        if line.strip() == heading:
            collected: list[str] = []
            level = len(heading) - len(heading.lstrip("#"))
            for item in lines[i + 1 :]:
                if re.match(r"^#{1,%d} " % level, item):
                    break
                collected.append(item)
            return "\n".join(collected).strip()
    return ""


def acceptance_excerpt(description: str) -> str:
    candidates = (
        "## Acceptance cases",
        "### Acceptance cases · expected, NOT_RUN",
        "## Observable acceptance",
        "## Explicit acceptance cases — expected, not observed",
    )
    for heading in candidates:
        text = section_after(description, heading)
        if text:
            return text
    for line in description.splitlines():
        if line.startswith("#") and "acceptance" in line.lower():
            text = section_after(description, line.strip())
            if text:
                return text
    return ""


def source_sections(description: str) -> list[dict[str, str]]:
    """Expose task-specific source sections, with trailing-space formatting removed."""
    headings = {
        "Instructor scaffold and learner TODO",
        "Instructor scaffold and planned interfaces",
        "Instructor scaffold — planned paths and prerequisites",
        "Planned instructor scaffold",
        "Tangible learner task",
        "New daily outcome",
        "Daily outcome",
        "What deepens prior learning",
        "Exact synthetic inputs",
        "Concrete inputs and outputs",
        "Concrete proposed inputs and output",
        "Inputs and outputs",
        "Output",
        "Observable acceptance",
        "Acceptance cases",
        "Acceptance cases · expected, NOT_RUN",
        "Fixed acceptance cases",
        "Explicit acceptance cases — expected, not observed",
        "One concrete daily outcome",
        "Learner TODO",
        "Instructor starter and learner ownership",
        "Dependencies and readiness",
        "Earlier CAP dependencies",
        "Earlier CAP evidence dependencies",
        "Earlier mapped prerequisites",
        "Daily timebox",
        "Existing calendar and timebox",
        "Architecture/interview evidence delta",
        "Architecture and interview delta",
        "Architecture/interview evidence",
        "Default hands-on fixture and stopping rule",
        "Evidence to record",
        "Recovered source rules and explicit teaching choices",
        "Active budget, bounds and evidence",
    }
    lines = description.splitlines()
    result: list[dict[str, str]] = []
    for i, line in enumerate(lines):
        if not line.startswith("#") or line.lstrip("# ").strip() not in headings:
            continue
        level = len(line) - len(line.lstrip("#"))
        body: list[str] = []
        for item in lines[i + 1 :]:
            if item.startswith("#") and len(item) - len(item.lstrip("#")) <= level:
                break
            body.append(item.rstrip())
        text = "\n".join(body).strip()
        if text:
            result.append({"heading": line.lstrip("# ").strip(), "body": text})
    return result


def active_task(description: str) -> str:
    """Return the exact opening paragraph(s) of the card's task-specific section."""
    lines = description.splitlines()
    candidates = []
    for i, line in enumerate(lines):
        normalized = line.lstrip("# ").strip().lower()
        if line.startswith("#") and (
            "new daily outcome" in normalized
            or "tangible learner task" in normalized
            or normalized == "daily outcome"
            or "active coverstory semantic migration" in normalized
            or ("active coverstory" in normalized and any(word in normalized for word in ("contract", "learning")))
        ):
            if "new daily outcome" in normalized:
                priority = 0
            elif "tangible learner task" in normalized:
                priority = 1
            elif "daily outcome" in normalized:
                priority = 2
            elif "active coverstory semantic migration" in normalized:
                priority = 3
            else:
                priority = 4
            candidates.append((priority, i))
    for _, i in sorted(candidates):
        paragraph: list[str] = []
        for item in lines[i + 1 :]:
            if item.startswith("#"):
                break
            if not paragraph and not item.strip():
                continue
            if paragraph and not item.strip():
                break
            paragraph.append(item)
        text = "\n".join(paragraph).strip()
        if text:
            return text
    raise ValueError("saved card has no recognizable practical task heading")


def primary_sections(cards: list[dict]) -> list[dict]:
    return sorted((c for c in cards if c["role"] == "primary"), key=lambda c: c["day_number"])


def verify_source(doc: dict, manifest: dict) -> list[dict]:
    cards = doc["cards"]
    errors: list[str] = []
    expected = manifest["learning_card_count"]
    if len(cards) != expected:
        errors.append(f"expected {expected} cards, found {len(cards)}")
    ids = [c["id"] for c in cards]
    if len(ids) != len(set(ids)):
        errors.append("card IDs are not unique")
    for c in cards:
        actual = sha256(c["description"].encode("utf-8"))
        if actual != c["description_sha256"]:
            errors.append(f"description hash mismatch: {c['id']}")
    primaries = primary_sections(cards)
    if len(primaries) != manifest["daily_primary_count"]:
        errors.append(f"expected {manifest['daily_primary_count']} primaries, found {len(primaries)}")
    linked = [c for c in cards if c["role"] == "linked_component"]
    if len(linked) != manifest["retained_component_count"]:
        errors.append(f"expected {manifest['retained_component_count']} components, found {len(linked)}")
    primary_ids = {c["id"] for c in primaries}
    if any(c["primary_id"] not in primary_ids for c in linked):
        errors.append("a linked component has no matching primary")
    dates = [date.fromisoformat(c["date"]) for c in primaries]
    if any(b - a != timedelta(days=1) for a, b in zip(dates, dates[1:])):
        errors.append("primary dates are not consecutive")
    for phase, count in ((1, manifest["phase1_primary_count"]), (2, manifest["phase2_primary_count"])):
        if sum(c["phase"] == phase for c in primaries) != count:
            errors.append(f"phase {phase} primary count mismatch")
    if errors:
        raise ValueError("; ".join(errors))
    return primaries


def materialize() -> dict:
    archive_manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    raw = CURRICULUM.read_bytes()
    if sha256(raw) != archive_manifest["source_final_contract_sha256"]:
        raise ValueError("curriculum file does not match the verified source hash")
    curriculum = json.loads(raw)
    primaries = verify_source(curriculum, archive_manifest)
    all_cards = {c["id"]: c for c in curriculum["cards"]}
    OUTPUT.mkdir(parents=True, exist_ok=True)

    entries: list[dict] = []
    for primary in primaries:
        components = sorted(
            (c for c in curriculum["cards"] if c["role"] == "linked_component" and c["primary_id"] == primary["id"]),
            key=lambda c: c["id"],
        )
        cards = [primary, *components]
        day_path = OUTPUT / f"day-{primary['day_number']:03d}-{primary['id'].lower()}.md"
        sections = [
            "# " + primary["title"],
            "",
            f"**Date:** {primary['date']} · **Phase:** {primary['phase']} · **Primary ID:** `{primary['id']}` · **Daily minutes:** 150 maximum",
            f"**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `{primary['description_sha256']}`",
            "",
            "This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.",
            "",
            "## Practical task from the saved contract",
            "",
            active_task(primary["description"]),
            "",
            "## Explicit repository paths in the saved contract",
            "",
        ]
        paths = planned_paths(primary["description"])
        if paths:
            sections.extend(f"- `{p}` (planned in source; existence/readiness is not implied)" for p in paths)
        else:
            sections.append("The card does not enumerate repository file paths. Reconcile its instructor starter section against the current checkout before assigning; do not invent paths.")
        sections += [
            "",
            "## Task-specific source excerpts",
            "",
            "The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.",
            "",
        ]
        for card in cards:
            sections += [
                f"### `{card['id']}` · {card['role']} · {card['title']}",
                "",
                f"Description SHA-256: `{card['description_sha256']}`",
                "",
                "**Practical task:** " + active_task(card["description"]),
                "",
                f"Exact source description: [saved curriculum record `{card['id']}`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)",
                "",
            ]
            sections.extend(f"#### {source_section['heading']}\n\n{source_section['body']}\n" for source_section in source_sections(card["description"]))
            sections.append("")
        day_path.write_text("\n".join(sections).rstrip() + "\n", encoding="utf-8")
        entries.append(
            {
                "day_number": primary["day_number"],
                "date": primary["date"],
                "phase": primary["phase"],
                "primary_id": primary["id"],
                "primary_title": primary["title"],
                "primary_description_sha256": primary["description_sha256"],
                "component_ids": [c["id"] for c in components],
                "components": [
                    {
                        "id": c["id"],
                        "title": c["title"],
                        "description_sha256": c["description_sha256"],
                        "practical_task": active_task(c["description"]),
                        "acceptance_contract_excerpt": acceptance_excerpt(c["description"]),
                    }
                    for c in components
                ],
                "guide": day_path.relative_to(ROOT).as_posix(),
                "practical_task": active_task(primary["description"]),
                "explicit_planned_paths": paths,
                "explicit_path_references": explicit_path_references(primary["description"]),
                "acceptance_contract_excerpt": acceptance_excerpt(primary["description"]),
                "learner_readiness": "NOT_ESTABLISHED_BY_SOURCE_OR_MATERIALIZATION",
                "path_mapping_status": (
                    "SOURCE_PLANNED_PATHS_ENUMERATED_NOT_CHECKED"
                    if paths
                    else "SOURCE_REFERENCES_ONLY_NOT_RECONCILED"
                    if explicit_path_references(primary["description"])
                    else "NO_EXPLICIT_REPOSITORY_PATH_IN_CARD"
                ),
            }
        )

    INDEX.parent.mkdir(parents=True, exist_ok=True)
    index = {
        "source_curriculum_sha256": archive_manifest["source_final_contract_sha256"],
        "source_archive_sha256": "ce89674349261792888284ced50a33f3aeab20d95a6ac5a3675c179454ed27e5",
        "source_delivery_commit": "0e70938d94804c23029b39de106b8e7f61dccd8c",
        "source_input_manifest": "docs/source-inputs/2026-10-14/PROJECT-INPUT-MANIFEST.json",
        "counts": {
            "learning_cards": len(curriculum["cards"]),
            "daily_primaries": len(primaries),
            "linked_components": sum(len(e["component_ids"]) for e in entries),
            "phase1_primaries": sum(e["phase"] == 1 for e in entries),
            "phase2_primaries": sum(e["phase"] == 2 for e in entries),
            "primary_guides_with_explicit_planned_paths": sum(bool(e["explicit_planned_paths"]) for e in entries),
            "primary_guides_with_source_path_references_only": sum(e["path_mapping_status"] == "SOURCE_REFERENCES_ONLY_NOT_RECONCILED" for e in entries),
            "primary_guides_with_no_explicit_repository_path": sum(e["path_mapping_status"] == "NO_EXPLICIT_REPOSITORY_PATH_IN_CARD" for e in entries),
        },
        "days": entries,
    }
    INDEX.write_text(json.dumps(index, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return index


if __name__ == "__main__":
    result = materialize()
    print(json.dumps({"status": "MATERIALIZED_SOURCE_BACKED_GUIDES", "counts": result["counts"], "index": INDEX.relative_to(ROOT).as_posix()}, indent=2))
